package com.deployai;

import atlantafx.base.theme.PrimerDark;
import com.deployai.config.AppConfig;
import com.deployai.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        primaryStage.setTitle(AppConfig.APP_NAME + " v" + AppConfig.APP_VERSION);
        primaryStage.setWidth(AppConfig.WINDOW_WIDTH);
        primaryStage.setHeight(AppConfig.WINDOW_HEIGHT);
        primaryStage.setMinWidth(AppConfig.MIN_WIDTH);
        primaryStage.setMinHeight(AppConfig.MIN_HEIGHT);

        SceneManager.getInstance().init(primaryStage);
        SceneManager.getInstance().showLogin();

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}