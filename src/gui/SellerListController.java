package gui;

import application.Main;
import db.DbIntegrityException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Utils;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.entities.Seller;
import model.services.DepartmentService;
import model.services.SellerService;

public class SellerListController implements Initializable, DataChangeListener {

    private SellerService service;	// serviços de bd

    @FXML
    private TableView<Seller> tableViewSeller;

    @FXML
    private TableColumn<Seller, Integer> tableColumnId;

    @FXML
    private TableColumn<Seller, String> tableColumnName;
    
    @FXML
    private TableColumn<Seller, String> tableColumnEmail;
    
    @FXML
    private TableColumn<Seller, Date> tableColumnBirthDate;
    
    @FXML
    private TableColumn<Seller, Double> tableColumnBaseSalary;

    @FXML
    private TableColumn<Seller, Seller> tableColumnEDIT;

    @FXML
    private TableColumn<Seller, Seller> tableColumnREMOVE;

    @FXML
    private Button btNew;

    private ObservableList<Seller> obsList;  //Tipo de lista que pode ser visualizável em elementos do JavaFX 

    @FXML /*Método-ação do botão Novo*/
    private void onBtNewAction(ActionEvent event) {
	Stage parentStage = Utils.currentStage(event);	// referência para o palco pai (SellerList.fxml) do próximo evento (SellerForm.fxml)
	Seller obj = new Seller();		// objeto necessário no cadastro
	createDialogForm(obj, "/gui/SellerForm.fxml", parentStage);	// criando caixa de diálogo para cadastro de um novo departamento
    }

    public void setSellerService(SellerService service) { //injenção de dependêcia
	this.service = service;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
	initializeNodes();
    }

    private void initializeNodes() {
	/* Para que as colunas funcionem é preciso associá-las aos respectivos atributos da classe de entidade*/
	tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
	tableColumnName.setCellValueFactory(new PropertyValueFactory<>("name"));
	tableColumnEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
	tableColumnBirthDate.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
	Utils.formatTableColumnDate(tableColumnBirthDate, "dd/MM/yyyy");
	tableColumnBaseSalary.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
	Utils.formatTableColumnDouble(tableColumnBaseSalary, 2);
	
	/* Macete para redimensionar a altura da tabela conforme a janela */
	Stage stage = (Stage) Main.getMainScene().getWindow();
	tableViewSeller.prefHeightProperty().bind(stage.heightProperty());

    }

    /* Método que atualiza a tabela de departamentos */
    public void updateTableView() {
	if (service == null) { //Precaução para caso o programador tenha esquecido de injetar a dependência
	    throw new IllegalStateException("Service was null");
	}
	List<Seller> list = service.findAll();	    // recebendo a lista de objetos do bd
	obsList = FXCollections.observableArrayList(list);  // transformando em um tipo visualizável em interface gráfica
	tableViewSeller.setItems(obsList);		    // carregando na tabela (interface gráfica)
	initEditButtons();  // inserindo o botão "editar" para cada linha da tabela
	initRemoveButtons();// inserindo o botão "remover" para cada linha da tabela
    }

    /* Método que cria a caixa de diálogo, um formulário usado para inserir ou editar departamentos */
    private void createDialogForm(Seller obj, String absoluteName, Stage parentStage) {
	try {
	    FXMLLoader loader = new FXMLLoader(getClass().getResource(absoluteName)); // carrega o arquivo "xxxxx.fxml"
	    Pane pane = loader.load();	//carrega um novo painel com o desenho do "xxxxx.fxml"

	    SellerFormController controller = loader.getController();   //obtendo o controller do "xxxxx.fxml"
	    controller.setEntity(obj);	// associando um objeto de departamento ao controller
	    controller.setServices(new SellerService(), new DepartmentService()); // associando um objeto de serviço ao controller
	    controller.loadAssociatedObjects();
	    controller.subscribeDataChangeListener(this);   // ???
	    controller.updateFormData(); // atualiza os dados do formulário com os dados do obj 

	    /* Como se trata de janelas de diálogo, uma vez que são modais, elas devem ocorrer por cima de outras*/
	    Stage dialogStage = new Stage();	// instanciamento de um novo palco
	    dialogStage.setTitle("Insira os dados de um vendedor");
	    dialogStage.setScene(new Scene(pane));  // configurando a cena com o painel desejado
	    dialogStage.setResizable(false);	    // palco não redimensionável
	    dialogStage.initOwner(parentStage);	    // pai da janela, janela anterior
	    dialogStage.initModality(Modality.WINDOW_MODAL); //janela travada até ser fechada salvando ou cancelando
	    dialogStage.showAndWait();	//mostra o palco e aguarda a inserção ou edição nos campos
	} catch (IOException e) {
	    e.printStackTrace();
	    Alerts.showAlert("IO Exception", "Erro ao carregar view", e.getMessage(), Alert.AlertType.ERROR);
	}
    }

    @Override /* ??? */
    public void onDataChanged() {
	updateTableView();
    }

    /* Método que insere um botão de editar a cada linha da tabela */
    private void initEditButtons() {
	/* Configurando a coluna para receber o botão */
	tableColumnEDIT.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
	tableColumnEDIT.setCellFactory(param -> new TableCell<Seller, Seller>() {
	    private final Button button = new Button("editar");	// instanciando um botão com o texto "editar"

	    @Override
	    protected void updateItem(Seller obj, boolean empty) {
		super.updateItem(obj, empty); // obtêm os valores de cada linha da tabela
		if (obj == null) {  // se linha vazia
		    setGraphic(null);	// não coloca botão
		    return; // encerra, pois não há mais o que fazer
		}
		setGraphic(button); // colocando graficamente o botão
		button.setOnAction( // configurando qual será a ação do botão
			event -> createDialogForm( // abrindo a caixa de diálogo (SellerForm.fxml) para edição
				obj, "/gui/SellerForm.fxml", Utils.currentStage(event)));
	    }
	});
    }

    /* Método que insere um botão de remover a cada linha da tabela */
    private void initRemoveButtons() {
	/* Configurando a coluna para receber o botão */
	tableColumnREMOVE.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
	tableColumnREMOVE.setCellFactory(param -> new TableCell<Seller, Seller>() {
	    private final Button button = new Button("remover"); // instanciando um botão com o texto "editar"

	    @Override
	    protected void updateItem(Seller obj, boolean empty) {
		super.updateItem(obj, empty); // obtêm os valores de cada linha da tabela
		if (obj == null) {  // se linha vazia
		    setGraphic(null);	// não coloca botão
		    return; // encerra, pois não há mais o que fazer
		}
		setGraphic(button); // colocando graficamente o botão
		button.setOnAction(event -> removeEntity(obj)); // configurando qual será a ação do botão
	    }
	});
    }
    
    /* Método que intermedia a ação de remoção de um departamento, pois é uma ação que demanda confirmação*/
    private void removeEntity(Seller obj) {
	/*result guarda a opção escolhida pelo usuário: OK ou Cancel*/
	Optional<ButtonType> result = Alerts.showConfirmation("Confirmação", "Tem certeza de que deseja remover o departamento? "); // Mostrando o alerta de confirmação
	
	if (result.get() == ButtonType.OK) {	// caso afirmativo para deleção
	    if (service == null) {  // caso programador esqueça de injetar a dependência
		throw new IllegalStateException("Service was null");
	    }
	    try {
		service.remove(obj);	// remove do bd
		updateTableView();	// atualiza a tabela
	    } catch (DbIntegrityException e) { // exceção de bd
		Alerts.showAlert("Erro ao remover departamento", null, e.getMessage(), Alert.AlertType.ERROR);
	    }
	}
    }
}
