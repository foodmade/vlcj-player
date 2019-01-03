package com.vlcjPlayer;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import javax.swing.*;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        URL location = getClass().getClassLoader().getResource("fxml/sample.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = fxmlLoader.load();

        primaryStage.setTitle("My First Media Player");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        eventHandler(primaryStage);
    }


    private void eventHandler(Stage primaryStage){
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                try {
                    if(Tutorial.getInstall() !=null ){
                        Tutorial.getInstall().closeWindow();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        });
    }

    public static void main(String[] args) {
        //检查LibVLC本机库是否存在
        if(!new NativeDiscovery().discover()){
            System.out.println("address:[5743857483] LibVLC库不存在");
            return;
        }
        System.out.println("LibVLC 检测成功");
        launch(args);
    }
}
