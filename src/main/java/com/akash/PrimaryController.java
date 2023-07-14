package com.akash;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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
    @FXML private CheckBox GP0, GP1, GP2, GP3;

    @FXML private CheckMenuItem MCLRE;

    @FXML private Slider freqSlider;

    @FXML private Menu clkText;
    private final Core pic = new Core();
    @FXML private TableView<Data> memTable, dataTable;
    @FXML private TextArea editor;
    @FXML private TableColumn<Data, String> progCol, progValCol;
    @FXML private TableColumn<Data, String> dataCol, dataValCol;

    private Path tempFolderPath = null;

    private Thread thread;


    public short readPort() {
        short port = 0b1111;
        if(!GP0.isSelected()) port = (short) (port & 0b1110);
        if(!GP1.isSelected()) port = (short) (port & 0b1101);
        if(!GP2.isSelected()) port = (short) (port & 0b1011);
        if(!GP3.isSelected()) port = (short) (port & 0b0111);
        return port;
    }

    public void writePorts() {
        if((pic.mem.fetchData((short) Memory.SFR.TRISGPIO.val) & 0b1) == 0b1) GP0.setDisable(false);
        else {
            GP0.setSelected((pic.mem.fetchGpio() & 0b1) == 0b1);
            GP0.setDisable(true);
        }
        if((pic.mem.fetchData((short) Memory.SFR.TRISGPIO.val) & 0b10) == 0b10) GP1.setDisable(false);
        else {
            GP1.setSelected((pic.mem.fetchGpio() & 0b10) == 0b10);
            GP1.setDisable(true);
        }
        if((pic.mem.fetchData((short) Memory.SFR.OPTION.val) & 0b100000) == 0 && (pic.mem.fetchData((short) Memory.SFR.OSCCAL.val) & 0b1) == 0) {
            if((pic.mem.fetchData((short) Memory.SFR.TRISGPIO.val) & 0b100) == 0b100) GP2.setDisable(false);
            else {
                GP2.setSelected((pic.mem.fetchGpio() & 0b100) == 0b100);
                GP2.setDisable(true);
            }
        }
        GP3.setDisable(false);
    }

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
        pic.isRunnable = false;
        if(thread != null) {
            try {
                thread.join();
                thread = null;
            }
            catch(InterruptedException e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }
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
        if(pic.isRunning) {
            raiseError("Already running.");
            return;
        }
        pic.load(tempFolderPath.toString()+"/temp.hex");
        updateTables();
        pic.isLoaded = true;
    }

    private ObservableList<Data> fileData() {
        ObservableList<Data> data = FXCollections.observableArrayList();
        data.add(new Data("WReg", Integer.toString(pic.WReg)));
        data.add(new Data("PC", Integer.toString(pic.pc)));
        for(int i = 0; i<6; i++) {
            data.add(new Data(Memory.SFR.getSFR(i).toString(), Integer.toHexString(pic.mem.fetchData((short) i))));
        }
        data.add(new Data(Memory.SFR.GPIO.toString(), Integer.toHexString(pic.mem.fetchGpio())));
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
            raiseError("Already Running.");
            return;
        }
        if(thread != null) {
            try {
                thread.join();
                thread = null;
            }
            catch(InterruptedException e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }
        if(!pic.isRunnable) {
            pic.pc = pic.spc;
            pic.isRunnable = true;
            MCLRE.setDisable(true);
        }
        pic.step();
        clkText.setText("Clk: " + pic.clk);
        updateTables();
    }

    @FXML
    public void run() {

        if(!pic.isLoaded) {
            raiseError("Compile and load before running");
            return;
        }
        if(pic.isRunning) {
            raiseError("Already running.");
            return;
        }
        if(thread != null) {
            try {
                thread.join();
                thread = null;
            }
            catch(InterruptedException e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }
        if(!pic.isRunnable) {
            pic.isRunnable = true;
            pic.pc = pic.spc;
        }
        pic.isRunning = true;
        long inTime = System.nanoTime();
        thread = new Thread(()->{
            MCLRE.setDisable(true);
            while(pic.isRunnable) {
                pic.step();
                Platform.runLater(() -> {
                    clkText.setText("Clk: " + pic.clk);
                });
                updateTables();
            }
            pic.isRunning = false;
            long outTime = System.nanoTime();
            System.out.println(outTime - inTime);
        }, "running-thread");
        thread.start();
    }

    @FXML
    public void reset() {
        if(pic.isRunning) {
            pic.isRunnable = false;
            try {
                thread.join();
                thread = null;
            }
            catch(InterruptedException e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }
        MCLRE.setDisable(false);
        load();
    }

    @FXML
    public void delete() {
        if(pic.isRunning) {
            raiseError("Already running.");
            return;
        }
        editor.clear();
        pic.reset();
        MCLRE.setDisable(false);
        updateTables();
    }

    @FXML void GP2Update() {
        if((pic.mem.fetchData((short) Memory.SFR.OPTION.val) & 0b100000) == 0b100000 && (pic.mem.fetchData((short) Memory.SFR.OSCCAL.val) & 0b1) == 0) {
            if((pic.mem.fetchData((short) Memory.SFR.OPTION.val) & 0b10000) == 0 && GP2.isSelected()) pic.clk += 1;
            else if((pic.mem.fetchData((short) Memory.SFR.OPTION.val) & 0b10000) == 0b10000 && !GP2.isSelected()) pic.clk += 1;
            clkText.setText("Clk: "+ pic.clk);
        }
    }

    @FXML void MCLREUpdate() {
        pic.mclre = MCLRE.isSelected();
    }

    @FXML void GP3Update() {
        if(MCLRE.isSelected() && GP3.isSelected()) {
            pic.mclrStart = pic.clk;
        }
    }

    @FXML void changeFreq() {
        pic.freq = freqSlider.getValue();
    }
}
