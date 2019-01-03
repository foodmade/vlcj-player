package com.vlcjPlayer.controller;

import com.vlcjPlayer.Tutorial;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

public class MainController {


    @FXML
    public Pane mainPane;
    @FXML
    public Button show;
    @FXML
    public Button hidden;


    public void showWindow() throws Exception {
        Tutorial tutorial = Tutorial.getInstall();
        if(tutorial == null){
            tutorial = Tutorial.initTemplate();
        }
        tutorial.getFrame().setVisible(true);
    }

    public void closeWindow() throws Exception {

    }

}
