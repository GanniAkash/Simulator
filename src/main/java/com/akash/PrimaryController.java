package com.akash;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

import com.akash.core.Core;
import com.akash.core.Memory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;

public class PrimaryController {
    @FXML private BorderPane root;
    private final Core pic = new Core();
    @FXML private Tab memTab, dataTab;
    @FXML private TableView<Data> memTable, dataTable;
    @FXML private TextArea editor;
    @FXML private TableColumn<Data, String> progCol, progValCol;
    @FXML private TableColumn<Data, String> dataCol, dataValCol;

    private Path tempFolderPath = null;

    private Path pic_as_path = Paths.get("/Applications/microchip/xc8/v2.40/pic-as/bin/pic-as");

    private void raiseError(String err) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(err);
        alert.show();
    }

    public static class Data {
        private String addr, val;


        public void setAddr(String addr) {
            this.addr = addr;
        }

        public void setVal(String val) {
            this.val = val;
        }

        public String getAddr() {
            return addr;
        }

        public String getVal() {
            return val;
        }

        Data (String addr, String val) {
            setAddr(addr);
            setVal(val);
        }
    }

    @FXML
    public void open() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open .asm or .s file");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Assembly files", ".*.asm", ".*.s")
        );
        File file = fileChooser.showOpenDialog(memTable.getScene().getWindow());
        StringBuilder data = new StringBuilder();
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                data.append("\n");
                data.append(scanner.nextLine());
            }
            editor.setText(data.toString());
        } catch (FileNotFoundException e) {
            System.out.println("File not found. Try again.");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    @FXML
    public void compile() {
        try {
            File tempFile = File.createTempFile("temp", ".asm");
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile, true));
            tempFile.deleteOnExit();
            writer.write(editor.getText());
            writer.close();
            this.tempFolderPath = Files.createTempDirectory("temp");
            Runtime.getRuntime().exec(pic_as_path+" -mcpu=PIC10F200 -o"+this.tempFolderPath.toAbsolutePath()+"/temp "+tempFile.getAbsolutePath()+" -xassembler-with-cpp");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private ObservableList<Data> memData(Core pic) {
        ObservableList<Data> data = FXCollections.observableArrayList();
        for(int i = 0; i <= 255; i++) {
            data.add(new Data(Integer.toHexString(i), Integer.toHexString(pic.mem.fetchInstruction((short) i))));
        }
        return data;
    }

    @FXML
    public void close() {
        Platform.exit();
    }

    @FXML
    public void setPicPath() {
        TextInputDialog dialog = new TextInputDialog(pic_as_path.toString());
        dialog.setTitle("pic-as path");
        dialog.setHeaderText("Give the path for the pic-as assembler.");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().strip().equals("")) {
            this.pic_as_path = Paths.get(result.get());
        }
    }

    @FXML
    public void load() {
        pic.load(tempFolderPath.toString()+"/temp.hex");
        updateTables();
        pic.isLoaded = true;
    }

    private ObservableList<Data> fileData() {
        ObservableList<Data> data = FXCollections.observableArrayList();
        data.add(new Data("WReg", Integer.toString(pic.WReg)));
        data.add(new Data("PC", Integer.toString(pic.pc)));
        for(int i = 0; i<7; i++) {
            data.add(new Data(Memory.SFR.getSFR(i).toString(), Integer.toHexString(pic.mem.fetchData((short) i))));
        }
        data.add(new Data(Memory.SFR.TRISGPIO.toString(), Integer.toHexString(pic.mem.fetchData((short) Memory.SFR.TRISGPIO.val))));
        data.add(new Data(Memory.SFR.OPTION.toString(), Integer.toHexString(pic.mem.fetchData((short) Memory.SFR.OPTION.val))));
        for (int i = 0x10; i<32; i++) {
            data.add(new Data(Integer.toHexString(i), Integer.toHexString(pic.mem.fetchData((short) i))));
        }
        return data;
    }

    private void updateTables() {
        progCol.setCellValueFactory(new PropertyValueFactory<>("addr"));
        progValCol.setCellValueFactory(new PropertyValueFactory<>("val"));
        memTable.setItems(memData(pic));
        dataCol.setCellValueFactory(new PropertyValueFactory<>("addr"));
        dataValCol.setCellValueFactory(new PropertyValueFactory<>("val"));
        dataTable.setItems(fileData());
    }

    @FXML
    public void step() {
        if(!pic.isLoaded) {
            raiseError("Not loaded");
            return;
        }
        else if(pic.isRunning) {
            raiseError("Already Running");
            return;
        }
        if(!pic.isRunnable) {
            pic.pc = pic.spc;
            pic.isRunnable = true;
        }
        pic.step();
        updateTables();
    }

    @FXML
    public void run() {
        if(!pic.isLoaded) {
            raiseError("Compile and load before running");
            return;
        }
        if(pic.isRunning) {
            raiseError("Already running");
            return;
        }
        if(!pic.isRunnable) {
            pic.isRunnable = true;
            pic.pc = pic.spc;
        }
        pic.isRunning = true;
        long inTime = System.nanoTime();
        new Thread(()->{
            while(pic.isRunnable) {
                pic.step();
                updateTables();
            }
            pic.isRunning = false;
            long outTime = System.nanoTime();
            System.out.println(outTime - inTime);
        }, "running-thread").start();
    }

    @FXML
    public void reset() {
        load();
    }

    @FXML
    public void delete() {
        editor.clear();
        pic.reset();
    }
}
