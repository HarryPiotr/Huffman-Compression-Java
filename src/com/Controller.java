package com;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Controller {

    @FXML
    private Button fileButton;
    @FXML
    private Button startButton;
    @FXML
    private Label fileLabel;
    @FXML
    private RadioButton rb1;
    @FXML
    private RadioButton rb2;
    @FXML
    private ToggleGroup radioButtonGroup = new ToggleGroup();
    @FXML
    private TextArea textArea;

    Stage stage;
    File inputFile;

    public void setStage(Stage st) {
        stage = st;
    }

    @FXML
    private void chooseFile() {
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(stage);
        if(file != null) {
            inputFile = file;
            fileLabel.setText("Plik: " + file.getName());
        }
    }
    @FXML
    private void startProcess() {
        if(inputFile != null) {
            print("--------------------------------------------------");
            print((rb1.isSelected() ? "Zakodowanie" : "Odkodowanie") + " pliku " + inputFile.getName());
        }
        else {
            print("Najpierw wybierz plik.");
        }

        try {
            if(rb1.isSelected()) {
                print("--------------------------------------------------");
                print(inputFile.getName() + " - sprawdzam czy plik istnieje");
                if (!Main.checkIfFileExists(inputFile)) throw new Exception();

                print(inputFile.getName() + " - zliczam symbole wejsciowe");
                ArrayList<Node> nodes = Main.countSymbols(inputFile);

                print(inputFile.getName() + " - sortuje model zrodla danych");
                Main.sortNodeList(nodes);

                print(inputFile.getName() + " - buduje drzewo");
                ReplacementNode treeRoot = Main.buildTree(nodes);

                print(inputFile.getName() + " - tworze slowa kodowe");
                Main.createCodingSequences(treeRoot, "");

                print(inputFile.getName() + " - kompresuje plik");
                Main.codeFile(nodes, inputFile);

                print(inputFile.getName() + " - zapisuje tabele kodowa");
                Main.saveCodingTable(nodes, inputFile);
            }
            else if(rb2.isSelected()) {
                print("--------------------------------------------------");

                if(!inputFile.getName().contains(".huff")) throw new Exception();
                File codedFile = new File(inputFile.getAbsolutePath());
                File codingFile = new File(inputFile.getAbsolutePath().substring(0, inputFile.getAbsolutePath().length() - 5) + ".coding");
                File decodedFile = new File(inputFile.getAbsolutePath().substring(0, inputFile.getAbsolutePath().length() - 5));

                print(inputFile.getName() + " - sprawdzam czy istnieje skompresowany plik");
                if (!Main.checkIfFileExists(codedFile)) throw new Exception();

                print(inputFile.getName() + " - sprawdzam czy istnieje tabela kodowa");
                if (!Main.checkIfFileExists(codingFile)) throw new Exception();

                print(inputFile.getName() + " - odbudowuje drzewo kodowania");
                ReplacementNode codingRoot = Main.rebuildTree(codingFile);

                print(inputFile.getName() + " - dekoduje plik");
                Main.decodeFile(codedFile, decodedFile, codingRoot);
            }
            else throw new Exception();
        }
        catch(Exception e) {
            textArea.appendText("### POWAŻNY BŁĄD! ###");
            textArea.appendText(System.getProperty("line.separator"));
        }
    }

    private void print(String s) {
        textArea.appendText(s);
        textArea.appendText(System.getProperty("line.separator"));
    }
}
