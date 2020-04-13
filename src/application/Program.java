/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

/**
 *
 * @author Jeferson Willian
 */
public class Program extends Application {

    @Override
    public void start(Stage primaryStage) {
	try {
	    FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/MainView.fxml"));	// Obtendo configurações da cena de um arquivo de extensão .fxml
	    ScrollPane scrollPane = loader.load();	// Objeto principal (subclasse de Parent) da view
	    
	    scrollPane.setFitToHeight(true);		// Configurando ajuste do objeto ao redimensionamento da tela
	    scrollPane.setFitToWidth(true);
	    
	    Scene mainScene = new Scene(scrollPane);	// "Configurando" a cena
	    primaryStage.setScene(mainScene);		// "Montando" a cena no palco
	    primaryStage.setTitle("Projeto - Modelo de aplicação desktop genérica");	// Título do palco
	    primaryStage.show();			// Apresentando o palco
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
	launch(args);
    }

}
