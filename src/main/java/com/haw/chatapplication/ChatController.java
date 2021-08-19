package com.haw.chatapplication;


import com.haw.chatapplication.model.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.haw.chatapplication.utils.HelpFunctions.send_private_message;
import static com.haw.chatapplication.utils.HelpFunctions.send_public_message;
import static javafx.scene.paint.Color.RED;

public class ChatController {
    public ScrollPane messageScroller;
    public ComboBox<String> box;
    public VBox chatVBox;
    public Label peer_port_label;
    public Label peer_host_label;
    public TabPane chatTabPane;
    public TextField send;
    public Label port_server_label;
    public Label name_server_label;
    public ListView<User> participants;
    public AnchorPane links;
    public ButtonBar server_button_bar;
    public AnchorPane rechts;
    public SplitPane splitPane;
    public Label logginginfo;
    public Button peer_connect;
    public TextField peer_host;
    public TextField peer_port;
    public TextField server_name;
    public TextField server_port;
    public Label participantsLabel;
    private ConnectView connectView;
    private Server server;
    private ConversationView conversationView;
    private final List<Color> COLORS = new ArrayList<>(Arrays.asList(Color.AQUA,
            Color.DARKMAGENTA, Color.DARKTURQUOISE, Color.DARKVIOLET, Color.DEEPPINK,
            Color.DEEPPINK, Color.DIMGRAY, Color.DIMGREY, Color.FORESTGREEN, Color.GOLDENROD, Color.GREENYELLOW));
    private final Button server_connect = new Button("connect");
    private final Button server_disconnect = new Button("disconnect");
    private boolean connected = false;
    private MessageHandler msh;

    @FXML
    protected void initialize() {
        peer_connect.setTextFill(Color.GREEN);
        server_connect.setTextFill(Color.GREEN);
        server_connect.setStyle("-fx-font-weight: bold");
        server_disconnect.setTextFill(RED);
        new LoggingView();
        connectView = new ConnectView();
        this.conversationView = new ConversationView();
    }

    class LoggingView {
        LoggingView() {
            server_disconnect.setTextFill(RED);
            prepareLogging();
            initialize();
        }

        private void initialize() {
            server_connect.setOnAction(ActionEvent -> connect());
            server_port.setOnKeyPressed(actionEvent -> {
                if (actionEvent.getCode().equals(KeyCode.ENTER)) {
                    connect();
                }
            });
            server_name.setOnKeyPressed(actionEvent -> {
                if (actionEvent.getCode().equals(KeyCode.ENTER)) {
                    connect();
                }
            });
        }

        private void handleServerDisconnect() {
            server_disconnect.setOnAction(actionEvent -> {
                server.disconnect();
                server.interrupt();
                msh.interrupt();
                rechts.setVisible(false);
                participants.setItems(FXCollections.observableArrayList());
                set(false);
                server_name.setDisable(false);
                server_port.setDisable(false);
                server_button_bar.getButtons().removeAll(server_button_bar.getButtons());
                server_button_bar.getButtons().add(server_connect);
                box.getItems().removeAll(box.getItems());
                send.setText("");
                logginginfo.setText("create a server");
                connected = false;
                participantsLabel.setVisible(false);
            });
        }

        private void connect() {
            if (!server_name.getText().isEmpty() && !server_port.getText().isEmpty()) {
                login();
                afterLogin();
                conversationView.setConversation();
                connected = true;
            }
        }

        private void login() {
            int port = Integer.parseInt(server_port.getText().trim());
            String name = server_name.getText().trim();
            try {
                server = new Server(port, name);
            } catch (IOException e) {
                e.printStackTrace();
            }
            server.start();
            msh = new MessageHandler(server);
            msh.start();
        }

        void prepareLogging() {
            set(false);
            server_button_bar.getButtons().add(server_connect);
            participants.getItems().clear();
            rechts.setVisible(false);
        }

        void afterLogin() {
            logginginfo.setText(server.getServerInfo());
            server_name.setDisable(true);
            server_port.setDisable(true);
            server_button_bar.getButtons().remove(server_connect);
            server_button_bar.getButtons().add(server_disconnect);
            server_disconnect.setTextFill(RED);
            handleServerDisconnect();
            set(true);
            connectView.updateParticipants();
            rechts.setVisible(true);
            if (conversationView != null && conversationView.getConversation() != null) {
                conversationView.getConversation().clearChat();
            }
            participantsLabel.setVisible(true);
        }

        void set(boolean visible) {
            peer_host.setVisible(visible);
            peer_connect.setVisible(visible);
            peer_port.setVisible(visible);
            peer_host_label.setVisible(visible);
            peer_port_label.setVisible(visible);
        }

    }

    public class ConnectView {
        ContextMenu routingInformationContextMenu = new ContextMenu();
        MenuItem menuItem = new MenuItem("routing information");

        ConnectView() {
            routingInformationContextMenu.getItems().addAll(menuItem);
            initialize();
        }

        private void initialize() {
            splitPane.setContextMenu(routingInformationContextMenu);
            splitPane.setOnMouseClicked(a -> {
                if (a.getButton() == MouseButton.PRIMARY) {

                }
            });
            menuItem.setOnAction(a -> {
                if (server != null && !server.getRoutingTable().getRoutingEntries().isEmpty() && connected) {
                    showRoutingInformation();
                }
            });
            peer_host.setOnKeyPressed(keyCode ->

            {
                if (keyCode.getCode().equals(KeyCode.ENTER)) {
                    connect();
                }
            });
            peer_port.setOnKeyPressed(keyCode ->
            {
                if (keyCode.getCode().equals(KeyCode.ENTER)) {
                    connect();
                }
            });
            peer_connect.setOnAction(ActionEvent ->

                    connect());

        }

        private void showRoutingInformation() {
            TableView<RoutingEntry> tableView = new TableView<>();
            tableView.getItems().removeAll(tableView.getItems());
            List<RoutingEntry> routingEntries = new ArrayList<>(server.getRoutingTable().getRoutingEntries());
            TableColumn<RoutingEntry, String> ip = new TableColumn<>("Ip");
            ip.setCellValueFactory(new PropertyValueFactory<>("ip"));
            TableColumn<RoutingEntry, String> name = new TableColumn<>("Name");
            name.setCellValueFactory(new PropertyValueFactory<>("name"));
            TableColumn<RoutingEntry, String> port = new TableColumn<>("Port");
            port.setCellValueFactory(new PropertyValueFactory<>("port"));
            TableColumn<RoutingEntry, String> hopCount = new TableColumn<>("HopCount");
            hopCount.setCellValueFactory(new PropertyValueFactory<>("hopCount"));
            TableColumn<RoutingEntry, String> socket = new TableColumn<>("Socket");
            socket.setCellValueFactory(new PropertyValueFactory<>("socket"));
            List<TableColumn<RoutingEntry, String>> elements = new ArrayList<>();
            elements.add(ip);
            elements.add(name);
            elements.add(port);
            elements.add(hopCount);
            elements.add(socket);
            tableView.getColumns().addAll(elements);
            routingEntries.forEach(routingEntry -> tableView.getItems().add(routingEntry));
            VBox vBox = new VBox(tableView);
            Scene scene = new Scene(vBox);
            Stage stage = new Stage();
            stage.setWidth(600);
            stage.setScene(scene);
            stage.show();
        }

        private void connect() {
            if (!peer_host.getText().isEmpty() && !peer_port.getText().isEmpty() && connected) {
                connect_to_peer();
                init_box();

            }
        }

        private void init_box() {
            box.setItems(FXCollections.observableArrayList(server.getParticipants().stream().map(User::getName).collect(Collectors.toList())));
            box.getItems().add(1, "all");

        }

        private void connect_to_peer() {
            server.connectToPeer(peer_host.getText().trim(), Integer.parseInt(peer_port.getText().trim()));
            updateParticipants();
        }

        public void updateParticipants() {
            participants.setItems(FXCollections.observableArrayList(server.getParticipants()));
        }

    }

    public class ConversationView {
        private Conversation conversation;
        private final Tab defaultTab;
        private final Map<User, Color> userColorList = new HashMap<>();

        public ConversationView() {
            defaultTab = chatTabPane.getTabs().get(0);
            defaultTab.setText("group chat");
            initialize();

        }

        public void setConversation() {
            this.conversation = new Conversation("group chat", this.defaultTab);
        }

        private void initialize() {
            send.setOnKeyPressed(keyCode -> {
                if (keyCode.getCode().equals(KeyCode.ENTER) && box.getItems().size() != 0 && !box.getSelectionModel().getSelectedItem().equals("<--->")) {
                    if (box.getSelectionModel().getSelectedItem().equals("all")) {
                        sendPublic();
                    } else {
                        User user = getUserByName(box.getSelectionModel().getSelectedItem());
                        send(user);
                    }
                }
            });
            Timeline fiveSecondsWonder = new Timeline(
                    new KeyFrame(Duration.millis(1),
                            event -> {
                                if (server != null && server.getMessage() != null && connected) {
                                    Message message = server.getMessage();
                                    server.setMessage(null);
                                    receive(message);
                                }
                            }));
            fiveSecondsWonder.setCycleCount(Timeline.INDEFINITE);
            fiveSecondsWonder.play();
            Timeline oneSecondsWonder = new Timeline(
                    new KeyFrame(Duration.seconds(1),
                            event -> {
                                if (server != null && connected) {
                                    connectView.updateParticipants();
                                }
                            }));
            oneSecondsWonder.setCycleCount(Timeline.INDEFINITE);
            oneSecondsWonder.play();
            box.setOnMouseClicked(a -> connectView.init_box());
        }

        private void sendPublic() {
            send_public_message(send.getText().trim(), server);
            this.conversation.sendMessage(send.getText().trim());
            send.setText("");
        }

        private User getUserByName(String selectedItem) {
            return server.getParticipants().stream().filter(user1 -> user1.getName().equals(selectedItem)).findAny().orElse(null);
        }

        private void send(User user) {

            try {
                send_private_message(user, send.getText().trim(), server);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.conversation.sendMessage(send.getText().trim());
            send.setText("");
        }

        public void receive(Message message) {
            if (message != null) {
                Color color;
                User user = new User(message.getFromName(), message.getFromIP(), message.getFromPort());
                if (userColorList.containsKey(user)) {
                    color = userColorList.get(user);
                } else {
                    Color c = COLORS.get(0);
                    userColorList.put(user, c);
                    COLORS.remove(0);
                    color = c;
                }
                this.conversation.receiveMessage(message.getFromName() + ":   " + message.getMessage(), color);
                //if (box.getItems().stream().anyMatch(s -> s.equals(message.getFromName()))) {
                //    box.getSelectionModel().select(message.getFromName());
                //}
            }

        }

        public Conversation getConversation() {
            return conversation;
        }
    }

    public class Conversation {
        private final String conversationPartner;
        private final ObservableList<Node> speechBubbles = FXCollections.observableArrayList();
        //private Label contactHeader;
        private final Tab tab;

        public Conversation(String conversationPartner, Tab tab) {
            this.tab = tab;
            this.conversationPartner = conversationPartner;
            setupElements();
        }

        private void setupElements() {
            setupMessageDisplay();
            chatVBox.setPadding(new Insets(5));
        }


        private void setupMessageDisplay() {
            Bindings.bindContentBidirectional(speechBubbles, chatVBox.getChildren());
            messageScroller.prefWidthProperty().bind(chatVBox.prefWidthProperty().subtract(5));
            messageScroller.setFitToWidth(true);
            //Make the scroller scroll to the bottom when a new message is added
            speechBubbles.addListener((ListChangeListener<Node>) change -> {
                while (change.next()) {
                    if (change.wasAdded()) {
                        messageScroller.setVvalue(messageScroller.getVmax());
                    }
                }
            });
            messageScroller.setFitToHeight(true);
            messageScroller.setFitToWidth(true);
        }


        private void sendMessage(String message) {
            SpeechBox speechBox = new SpeechBox(message, SpeechDirection.RIGHT, Color.GOLD);
            speechBubbles.add(speechBox);
        }

        private void receiveMessage(String message, Color color) {
            SpeechBox speechBox = new SpeechBox(message, SpeechDirection.LEFT, color);
            speechBubbles.add(speechBox);
            //playSound();

        }

        private void playSound() {
            // hard coding //TODO
            String complete_file_sound_path = Paths.get("ressource", "com", "haw", "chatapplication", "assets", "sound.mp3").toAbsolutePath().toString().replaceAll("\\\\", "/");
            //Media pick = new Media(Objects.requireNonNull(getClass().getClassLoader().getResource("sound.mp3")).toString());
            Media pick = new Media(complete_file_sound_path);
            MediaPlayer player = new MediaPlayer(pick);
            player.play();
        }

        public String getConversationPartner() {
            return conversationPartner;
        }

        public void clearChat() {
            speechBubbles.clear();
        }
    }

    enum SpeechDirection {
        LEFT, RIGHT
    }

    private static class SpeechBox extends HBox {
        private final Color DEFAULT_SENDER_COLOR = Color.GOLD;
        private final Color receiver_color;
        private Background DEFAULT_SENDER_BACKGROUND, DEFAULT_RECEIVER_BACKGROUND;

        private final String message;
        private final SpeechDirection direction;

        private Label displayedText;
        private SVGPath directionIndicator;

        public SpeechBox(String message, SpeechDirection direction, Color color) {
            this.receiver_color = color;
            this.message = message;
            this.direction = direction;
            initialiseDefaults();
            setupElements();
        }

        private void initialiseDefaults() {
            DEFAULT_SENDER_BACKGROUND = new Background(
                    new BackgroundFill(DEFAULT_SENDER_COLOR, new CornerRadii(5, 0, 5, 5, false), Insets.EMPTY));
            DEFAULT_RECEIVER_BACKGROUND = new Background(
                    new BackgroundFill(receiver_color, new CornerRadii(0, 5, 5, 5, false), Insets.EMPTY));
        }

        private void setupElements() {
            displayedText = new Label(message);
            displayedText.setPadding(new Insets(5));
            displayedText.setWrapText(true);
            directionIndicator = new SVGPath();
            if (direction == SpeechDirection.LEFT) {
                configureForReceiver();
            } else {
                configureForSender();
            }
        }


        private void configureForSender() {
            displayedText.setBackground(DEFAULT_SENDER_BACKGROUND);
            displayedText.setAlignment(Pos.CENTER_RIGHT);
            directionIndicator.setContent("M10 0 L0 10 L0 0 Z");
            directionIndicator.setFill(DEFAULT_SENDER_COLOR);
            HBox container = new HBox(displayedText, directionIndicator);
            //Use at most 75% of the width provided to the SpeechBox for displaying the message
            container.maxWidthProperty().bind(widthProperty().multiply(0.75));
            getChildren().setAll(container);
            setAlignment(Pos.CENTER_RIGHT);
        }

        private void configureForReceiver() {
            displayedText.setBackground(DEFAULT_RECEIVER_BACKGROUND);
            displayedText.setAlignment(Pos.CENTER_LEFT);
            directionIndicator.setContent("M0 0 L10 0 L10 10 Z");
            directionIndicator.setFill(receiver_color);
            HBox container = new HBox(directionIndicator, displayedText);
            //Use at most 75% of the width provided to the SpeechBox for displaying the message
            getChildren().setAll(container);
            setAlignment(Pos.CENTER_LEFT);
        }


    }

    public static void main(String[] args) {
        String complete_file_sound_path = Paths.get("ressource", "com", "haw", "chatapplication", "assets", "sound.mp3").toAbsolutePath().toString().replaceAll("\\\\", "/");
        System.out.println(complete_file_sound_path);
    }
}