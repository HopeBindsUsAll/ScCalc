package com.example.app;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main extends Application {
    private final Label[] totalLabels = new Label[5];

    private final ArrayList<Item> selectedItems = new ArrayList<>();

    private Label totalSumLabel;
    private int totalSum = 0;
    private Scene scene;
    private Color bgColor;
    private Color miscColor;

    private static final double MAIN_STAGE_WIDTH = 415;
    private static final double MAIN_STAGE_HEIGHT = 530;
    private GridPane chosenItemsGrid;
    private ScrollPane chosenItemsScrollPane;
    private final ArrayList<TextField> chosenItemsPriceFields = new ArrayList<>();
    private final ArrayList<TextField> chosenItemsQuantityFields = new ArrayList<>();
    private final ArrayList<Label> chosenItemsSumLabels = new ArrayList<>();

    private final ItemList itemList = new ItemList();

    public int getTotalSum() {
        return totalSum;
    }

    public void setTotalSum(int totalSum) {
        this.totalSum = totalSum;
    }

    
    @Override
    public void start(Stage primaryStage) {
        loadSettings();
        itemList.readItems();
        loadSelectedItems();
        primaryStage.setTitle("Калькулятор Мамонта");
        primaryStage.getIcons().add(new Image("/blue.png"));
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);
        Button clearButton = new Button("Очистить");
        Button copyButton = new Button("Скопировать");
        Button msgButton = new Button("Сообщение");
        clearButton.setPrefWidth(120);
        msgButton.setPrefWidth(120);


        totalSumLabel = new Label("Общая сумма: 0");

        clearButton.setOnAction(e -> clearFields());
        copyButton.setOnAction(e -> copyText(String.valueOf(getTotalSum())));
        msgButton.setOnAction(e -> openMessageGenerator(primaryStage.getX(), primaryStage.getY()));
        ImageView colorBtnIcon = new ImageView(new Image("/colorPicker.png"));
        colorBtnIcon.setFitWidth(24);
        colorBtnIcon.setFitHeight(24);
        Button colorBtn = new Button("", colorBtnIcon);
        colorBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        colorBtn.setOnAction(e -> openColorSelectionWindow(primaryStage.getX(), primaryStage.getY()));
        GridPane btnGrid = new GridPane();
        btnGrid.setHgap(15);
        btnGrid.add(msgButton, 0, totalLabels.length + 1);
        btnGrid.add(colorBtn, 1, totalLabels.length + 1);
        btnGrid.add(clearButton, 2, totalLabels.length + 1);
        btnGrid.setAlignment(Pos.CENTER);
        
        // Initialize the scrollGrid and create the table with fixed header
        Button addItemsButton = new Button("Добавить предмет");
        addItemsButton.setOnAction(e -> openItemPicker(primaryStage.getX(), primaryStage.getY()));
        addItemsButton.setAlignment(Pos.CENTER);
        addItemsButton.setPrefWidth(380);
        chosenItemsGrid = createScrollGrid();
        BorderPane tableWithFixedHeader = createTableWithFixedHeader(chosenItemsGrid);

        VBox layout = new VBox(10, tableWithFixedHeader, addItemsButton, btnGrid, totalSumLabel, copyButton);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);


        scene = new Scene(layout, MAIN_STAGE_WIDTH, MAIN_STAGE_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> saveSettings());
        // Applying saved color for the background
        if (bgColor != null){
            scene.getRoot().setStyle("-fx-background-color: " + toRgb(bgColor));
            chosenItemsScrollPane.setStyle("-fx-background-color: " + "black");
        }
        // Applying saved color for all the buttons and text fields
        if (miscColor != null){
            String tableColorStyle = "-fx-background-color: " + toRgb(miscColor);
            chosenItemsScrollPane.setStyle("-fx-background-color: " + "black;" + "-fx-background: " + toRgb(miscColor));
            chosenItemsGrid.setStyle(tableColorStyle);
            // Apply the same style to the header grid
        }
        rebuildItemsGrid();
    }

    GridPane createScrollGrid() {
        GridPane scrollGrid = new GridPane();
        scrollGrid.setPadding(new Insets(10));
        scrollGrid.setHgap(10);
        scrollGrid.setVgap(10);
        // No header in this grid anymore, it will only contain items
        return scrollGrid;
    }
    
    GridPane createHeaderGrid() {
        GridPane headerGrid = new GridPane();
        headerGrid.setPadding(new Insets(10));
        headerGrid.setHgap(10);
        String[] headerNames = {"Товар", "Цена", "Количество", "Сумма"};
        Label item = new Label(headerNames[0]);
        Label price = new Label(headerNames[1]);
        Label quantity = new Label(headerNames[2]);
        Label sum = new Label(headerNames[3]);
        item.setPrefWidth(40);
        price.setPrefWidth(80);
        quantity.setPrefWidth(80);
        sum.setPrefWidth(60);
        headerGrid.add(item, 0, 0);
        headerGrid.add(price, 1, 0);
        headerGrid.add(quantity, 2, 0);
        headerGrid.add(sum, 3, 0);
        
        return headerGrid;
    }


    BorderPane createTableWithFixedHeader(GridPane contentGrid){
        // Create a BorderPane to hold both the header and scrollable content
        BorderPane tablePane = new BorderPane();
        
        // Create the header grid and add it to the top of the BorderPane
        GridPane headerGrid = createHeaderGrid();
        tablePane.setTop(headerGrid);
        
        // Create the ScrollPane for the content and add it to the center of the BorderPane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(contentGrid);
        scrollPane.setFitToWidth(false);
        scrollPane.setPrefHeight(375);
        tablePane.setCenter(scrollPane);
        // Store the ScrollPane reference for later use
        chosenItemsScrollPane = scrollPane;
        
        return tablePane;
    }
    private void addItem(Item item){
        if (chosenItemsGrid == null) {

            chosenItemsGrid = createScrollGrid();
            createTableWithFixedHeader(chosenItemsGrid);
        }
        
        int existingIndex = selectedItems.indexOf(item);
        if (existingIndex == -1) { // Item not in list yet
            selectedItems.add(item);
            int row = selectedItems.size() - 1; // Row index is now 0-based (no header row in content)

            // Create UI elements for the item
            ImageView itemIcon = new ImageView(item.getIcon());
            itemIcon.setFitWidth(32);
            itemIcon.setFitHeight(32);
            Tooltip.install(itemIcon, new Tooltip(item.getItemName()));

            TextField priceField = new TextField(item.getPrice().toString());
            priceField.setPrefWidth(80);
            chosenItemsPriceFields.add(priceField);

            TextField quantityField = new TextField("");
            quantityField.setPrefWidth(80);
            chosenItemsQuantityFields.add(quantityField);

            Label sumLabel = new Label("0");
            chosenItemsSumLabels.add(sumLabel);

            // Add change listeners to update sum
            priceField.textProperty().addListener((observable, oldValue, newValue) -> {
                int itemIndex = selectedItems.indexOf(item);
                updateItemSum(itemIndex);
                calculateChosenItemsTotal();

                // Update the original item's price in the ItemList
                try {
                    int newPrice = Integer.parseInt(newValue);
                    // Update the item in the selectedItems list
                    selectedItems.get(itemIndex).setPrice(newPrice);

                    // Find and update the same item in the global ItemList
                    for (Item listItem : ItemList.listOfItems) {
                        if (listItem.getItemID().equals(item.getItemID())) {
                            listItem.setPrice(newPrice);
                            break;
                        }
                    }
                } catch (NumberFormatException e) {
                    // Invalid price format, do nothing
                }
            });

            quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
                updateItemSum(selectedItems.indexOf(item));
                calculateChosenItemsTotal();
            });

            // Add checkbox instead of remove button
            CheckBox itemCheckBox = new CheckBox();
            itemCheckBox.setSelected(false); // Default to unselected

            chosenItemsGrid.add(itemCheckBox, 0, row);
            chosenItemsGrid.add(itemIcon, 1, row);
            chosenItemsGrid.add(priceField, 2, row);
            chosenItemsGrid.add(quantityField, 3, row);
            chosenItemsGrid.add(sumLabel, 4, row);

        }
        rebuildItemsGrid();
    }
    
    private void updateItemSum(int index) {
        if (index >= 0 && index < selectedItems.size()) {
            try {
                int price = Integer.parseInt(chosenItemsPriceFields.get(index).getText());
                int quantity = Integer.parseInt(chosenItemsQuantityFields.get(index).getText());
                int sum = price * quantity;
                chosenItemsSumLabels.get(index).setText(String.valueOf(sum));
            } catch (NumberFormatException e) {
                chosenItemsSumLabels.get(index).setText("0");
            }
        }
    }

    private void removeItem(int index) {
        // Remove the item at the specified index
        if (index >= 0 && index < selectedItems.size()) {
            selectedItems.remove(index);
            chosenItemsPriceFields.remove(index);
            chosenItemsQuantityFields.remove(index);
            chosenItemsSumLabels.remove(index);
            
            // Rebuild the grid and recalculate total
            rebuildItemsGrid();
            calculateChosenItemsTotal();
        }
    }

    
    private void rebuildItemsGrid() {
        try {
            chosenItemsGrid.getChildren().clear();
        } catch (Exception e){
            //
        }
        int row = 0;
        // Re-add all items (no headers in the content grid anymore)
        for (int i = 0; i < selectedItems.size(); i++) {
            Item currentItem = selectedItems.get(i);
            row = i; // Start from row 0 since there's no header row in the content grid
            ImageView deleteIcon = new ImageView(new Image("/deleteIcon.png"));
            deleteIcon.setFitWidth(20);
            deleteIcon.setFitHeight(20);
            Button removeButton = new Button("", deleteIcon);
            removeButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
            final int index = i; // Need final variable for lambda
            removeButton.setOnAction(e -> removeItem(index));
            removeButton.setTooltip(new Tooltip("Удалить предмет"));
            
            ImageView itemIcon = new ImageView(currentItem.getIcon());
            itemIcon.setFitWidth(40);
            itemIcon.setFitHeight(40);
            Tooltip.install(itemIcon, new Tooltip(currentItem.getItemName()));

            chosenItemsGrid.add(itemIcon, 0, row);
            chosenItemsPriceFields.get(i).setPrefWidth(80);
            chosenItemsGrid.add(chosenItemsPriceFields.get(i), 1, row);
            chosenItemsQuantityFields.get(i).setPrefWidth(80);
            chosenItemsGrid.add(chosenItemsQuantityFields.get(i), 2, row);
            chosenItemsSumLabels.get(i).setPrefWidth(60);
            chosenItemsGrid.add(chosenItemsSumLabels.get(i), 3, row);
            chosenItemsGrid.add(removeButton, 4, row);
        }
    }
    
    private void calculateChosenItemsTotal() {
        int total = 0;
        for (Label sumLabel : chosenItemsSumLabels) {
            try {
                total += Integer.parseInt(sumLabel.getText());
            } catch (NumberFormatException e) {
                // Skip invalid values
            }
        }
        totalSumLabel.setText("Общая сумма: " + total);
        setTotalSum(total);
    }

    private void openItemPicker(double mainStageX, double mainStageY) {
        Stage pickerStage = new Stage();
        pickerStage.setTitle("Выбор предмета");
        pickerStage.getIcons().add(new Image("/itemSearch.png"));
        pickerStage.initModality(Modality.APPLICATION_MODAL);
        pickerStage.setX(mainStageX + MAIN_STAGE_WIDTH);
        pickerStage.setY(mainStageY);
        TextField searchField = new TextField();
        searchField.setPromptText("Поиск по названию...");

        ListView<HBox> itemListview = new ListView<>();
        ObservableList<HBox> itemObservableList = FXCollections.observableArrayList();
        
        // Keep track of which items are displayed in the ListView
        List<Item> displayedItems = new ArrayList<>();
        
        // Populate initial list
        for (Item item : ItemList.listOfItems) {
            HBox itemBox = new HBox(10);
            itemBox.setAlignment(Pos.CENTER_LEFT);

            ImageView itemIcon = new ImageView(item.getIcon());
            itemIcon.setFitWidth(48);
            itemIcon.setFitHeight(48);

            Label itemName = new Label(item.getItemName());
            itemBox.getChildren().addAll(itemIcon, itemName);
            itemObservableList.add(itemBox);
            displayedItems.add(item);
        }

        itemListview.setItems(itemObservableList);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Clear the current displayed items list
            displayedItems.clear();
            
            // Filter items based on search text
            List<Item> filteredItemsList = ItemList.listOfItems.stream()
                    .filter(item -> item.getItemName().toLowerCase().contains(newValue.toLowerCase()))
                    .toList();
            
            // Create HBox representations for filtered items
            List<HBox> filteredHBoxes = filteredItemsList.stream()
                    .map(item -> {
                        HBox itemBox = new HBox(10);
                        itemBox.setAlignment(Pos.CENTER_LEFT);
                        ImageView itemIcon = new ImageView(item.getIcon());
                        itemIcon.setFitWidth(48);
                        itemIcon.setFitHeight(48);

                        Label itemName = new Label(item.getItemName());
                        itemBox.getChildren().addAll(itemIcon, itemName);
                        return itemBox;
                    })
                    .collect(Collectors.toList());
            
            // Update the displayed items list with filtered items
            displayedItems.addAll(filteredItemsList);
            
            // Update the ListView
            itemObservableList.setAll(filteredHBoxes);
        });

        Button selectButton = new Button("Добавить");
        selectButton.setOnAction(e -> {
            int selectedIndex = itemListview.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < displayedItems.size()) {
                // Get the item from our tracked list of displayed items
                Item selectedItem = displayedItems.get(selectedIndex);
                addItem(selectedItem);
                pickerStage.close();
            }
        });

        VBox layout = new VBox(10, searchField, itemListview, selectButton);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout, 400, 500);
        pickerStage.setScene(scene);
        pickerStage.showAndWait();
    }


    private void clearFields() {
        // Only clear quantity fields in chosen items
        if (chosenItemsGrid != null) {
            // Clear only the quantity fields
            for (TextField quantityField : chosenItemsQuantityFields) {
                quantityField.setText("");
            }
            
            // Reset sum labels to 0
            for (Label sumLabel : chosenItemsSumLabels) {
                sumLabel.setText("0");
            }
        }
        
        setTotalSum(0);
        totalSumLabel.setText("Общая сумма: " + getTotalSum());
    }

    private void copyText(String text) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    private void generateGameMessage(GridPane msgGrid) {
        StringBuilder msg = new StringBuilder("СКУПАЮ");
        ObservableList<Node> gridChildren = msgGrid.getChildren();
        for (int i = 0; i < gridChildren.size(); i++) {
            String itemName = "";
            Integer itemPrice= 0;
            Integer row = GridPane.getRowIndex(gridChildren.get(i));
            Integer column = GridPane.getColumnIndex(gridChildren.get(i));
            if(column == 0 && ((CheckBox)gridChildren.get(i)).isSelected()) {
                itemName = selectedItems.get(row).getItemName();
                itemPrice = selectedItems.get(row).getPrice();
                try {
                    msg.append(" [").append(itemName).append("]").append(formatNumber(itemPrice));
                } catch (NumberFormatException e) {
                    // Skip items with invalid price
                }
            }
        }
        msg.append("[Вступайте в мой отряд]!!!!!");
        copyText(msg.toString());
    }

    private void openMessageGenerator(double mainStageX, double mainStageY){
        double msgStageWidth = 300;
        double msgStageHeight = 380;
        Stage msgGeneratorStage = new Stage();
        msgGeneratorStage.setTitle("Генератор Сообщений");
        msgGeneratorStage.getIcons().add(new Image("/msgIcon.png"));
        msgGeneratorStage.initModality(Modality.APPLICATION_MODAL);
        msgGeneratorStage.setX(mainStageX - msgStageWidth);
        msgGeneratorStage.setY(mainStageY + MAIN_STAGE_HEIGHT - msgStageHeight);
        GridPane msgGrid = new GridPane();
        msgGrid.setPadding(new Insets(10));
        msgGrid.setHgap(20);
        for (int i = 0; i < selectedItems.size(); i++) {
            ImageView itemIcon = new ImageView(selectedItems.get(i).getIcon());
            itemIcon.setFitWidth(48);
            itemIcon.setFitHeight(48);
            CheckBox itemSelect = new CheckBox();
            itemSelect.setSelected(false);
            msgGrid.add(itemSelect, 0, i);
            msgGrid.add(itemIcon, 1, i);
            msgGrid.add(new Label(selectedItems.get(i).getItemName()), 2, i);
        }
        ScrollPane msgScrollPane = new ScrollPane();
        msgScrollPane.setFitToWidth(false);
        msgScrollPane.setContent(msgGrid);
        msgScrollPane.setPadding(new Insets(10));


        ImageView scIcon = new ImageView(new Image("/scIcon.png"));
        scIcon.setFitHeight(20);
        scIcon.setFitWidth(20);
        Button copyGameMessage = new Button("", scIcon); // button to copy message for the game chat
        copyGameMessage.setPrefWidth(120);
        copyGameMessage.setOnAction(e -> generateGameMessage(msgGrid));

        ImageView dsIcon = new ImageView(new Image("/discordIcon.png"));
        dsIcon.setFitHeight(20);
        dsIcon.setFitWidth(20);
        Button copyDiscordMessage = new Button("", dsIcon); //button to copy message for discord
        copyDiscordMessage.setPrefWidth(120);
        copyDiscordMessage.setOnAction(e -> generateGameMessage(msgGrid));

        GridPane btnGrid = new GridPane();
        btnGrid.setPadding(new Insets(10));
        btnGrid.setHgap(20);
        btnGrid.setAlignment(Pos.CENTER);
        btnGrid.add(copyGameMessage, 0, 0);
        btnGrid.add(copyDiscordMessage, 1, 0);

        VBox msgWindow = new VBox(10, msgScrollPane, btnGrid);
        Scene scene = new Scene(msgWindow, msgStageWidth, msgStageHeight);
        msgGeneratorStage.setScene(scene);
        msgGeneratorStage.showAndWait();
    }

    private String formatNumber(int num) {
        if (num >= 1000) {
            double shortNum = num / 1000.0;
            return (shortNum % 1 == 0) ? ((int) shortNum + "k") : (String.format("%.1fk", shortNum));
        }
        return String.valueOf(num);
    }

    private void openColorSelectionWindow(double mainStageX, double mainStageY) {
        double colorStageWidth = 250;
        double colorStageHeight = 200;
        Stage colorStage = new Stage();
        colorStage.setTitle("Выбор цвета");
        colorStage.setX(mainStageX - colorStageWidth);
        colorStage.setY(mainStageY);
        colorStage.getIcons().add(new Image("/colorPicker.png"));
        RadioButton bgColorOption = new RadioButton("Фон");
        RadioButton tableColorOption = new RadioButton("Фон таблицы");
        ToggleGroup toggleGroup = new ToggleGroup();
        bgColorOption.setToggleGroup(toggleGroup);
        tableColorOption.setToggleGroup(toggleGroup);
        bgColorOption.setSelected(true);
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(bgColor);
        bgColorOption.setOnAction(e -> colorPicker.setValue(bgColor));
        tableColorOption.setOnAction(e -> colorPicker.setValue(miscColor));
        Button applyButton = new Button("Применить");
        Button resetButton = new Button("Сбросить");
        applyButton.setOnAction(e -> {
            Color selectedColor = colorPicker.getValue();
            String colorStyle = "-fx-background-color: " + toRgb(selectedColor);
            if (bgColorOption.isSelected()) {
                bgColor = selectedColor;
                if (miscColor == null){
                    chosenItemsScrollPane.setStyle("-fx-background-color: " + "black");
                } else {
                    chosenItemsScrollPane.setStyle("-fx-background-color: " + "black;" + "-fx-background: " + toRgb(miscColor));
                }

                scene.getRoot().setStyle(colorStyle);
            } else {
                miscColor = selectedColor;
                chosenItemsScrollPane.setStyle("-fx-background-color: " + "black;" + "-fx-background: " + toRgb(miscColor));
                chosenItemsGrid.setStyle(colorStyle);

            }
            colorStage.close();
        });

        resetButton.setOnAction(e -> {
            String colorStyle = "-fx-background-color: " + "white";
            scene.getRoot().setStyle(colorStyle);
            chosenItemsScrollPane.setStyle("");
            chosenItemsGrid.setStyle("");
            bgColor = null;
            miscColor = null;
            colorStage.close();
        });

        VBox popupLayout = new VBox(10, bgColorOption, tableColorOption, colorPicker, applyButton, resetButton);
        popupLayout.setPadding(new Insets(10));
        popupLayout.setAlignment(Pos.CENTER_LEFT);

        Scene popupScene = new Scene(popupLayout, colorStageWidth, colorStageHeight);
        colorStage.setScene(popupScene);
        colorStage.initModality(Modality.APPLICATION_MODAL);
        colorStage.showAndWait();
    }

    private String toRgb(Color color) {
        return String.format("rgb(%d, %d, %d)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private void saveSettings(){
        // Update prices in the ItemList before saving
        for (int i = 0; i < selectedItems.size(); i++) {
            Item selectedItem = selectedItems.get(i);
            try {
                int newPrice = Integer.parseInt(chosenItemsPriceFields.get(i).getText());
                // Find and update the same item in the global ItemList
                for (Item listItem : ItemList.listOfItems) {
                    if (listItem.getItemID().equals(selectedItem.getItemID())) {
                        listItem.setPrice(newPrice);
                        break;
                    }
                }
            } catch (NumberFormatException e) {
                // Invalid price format, do nothing
            }
        }
        
        // Save all items to file
        itemList.writeItems();
        
        // Save selected items
        saveSelectedItems();
        
        // Save color settings
        try(FileWriter writer = new FileWriter("save.txt", false)) {
            if(miscColor == null && bgColor == null){
                writer.write("default default");
            }
            else if (bgColor == null){
                writer.write("default " + miscColor);
            } else if (miscColor == null){
                writer.write(bgColor + " default");
            } else{
                writer.write(bgColor + " " + miscColor);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void saveSelectedItems() {
        try (FileWriter writer = new FileWriter("pickeditems.txt", false)) {
            for (Item item : selectedItems) {
                writer.write(item.getItemID() + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error saving selected items: " + e.getMessage());
        }
    }

    private void loadSettings(){
        File savedSettings = new File("save.txt");
        if (savedSettings.exists()){
            String colorsLine = null;
            try {
                Scanner lastSaveScanner = new Scanner(new File("save.txt"));
                while(lastSaveScanner.hasNext()) {
                    colorsLine = lastSaveScanner.nextLine();
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            if (colorsLine != null){
                String[] lastColors = colorsLine.split(" ");
                if(lastColors[0].equals("default") && lastColors[1].equals("default")){
                    bgColor = null;
                    miscColor = null;
                }else if(lastColors[0].equals("default")){
                    bgColor = null;
                    miscColor = Color.valueOf(lastColors[1]);
                } else if(lastColors[1].equals("default")) {
                    bgColor = Color.valueOf(lastColors[0]);
                    miscColor = null;
                } else {
                    bgColor = Color.valueOf(lastColors[0]);
                    miscColor = Color.valueOf(lastColors[1]);
                }
            }
        }
    }
    
    private void loadSelectedItems() {
        File savedItems = new File("pickeditems.txt");
        if (savedItems.exists()) {
            try (Scanner scanner = new Scanner(savedItems)) {
                while (scanner.hasNextLine()) {
                    String itemId = scanner.nextLine().trim();
                    // Find the item in the global item list
                    for (Item item : ItemList.listOfItems) {
                        if (item.getItemID().equals(itemId)) {
                            addItem(item);
                            break;
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                System.err.println("Error loading selected items: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
