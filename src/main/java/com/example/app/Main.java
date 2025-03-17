package com.example.app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import java.util.Scanner;

public class Main extends Application {
    private final TextField[] quantityFields = new TextField[5];
    private final TextField[] priceFields = new TextField[5];
    private final Label[] totalLabels = new Label[5];
    private final CheckBox[] itemCheckBoxes = new CheckBox[5];
    private final Button[] buttons = new Button[4];
    private Label totalSumLabel;
    private int totalSum = 0;
    private Scene scene;
    private String[] lastPrices;
    private String[] lastColors;
    private Color bgColor;
    private Color miscColor;

    public int getTotalSum() {
        return totalSum;
    }

    public void setTotalSum(int totalSum) {
        this.totalSum = totalSum;
    }

    @Override
    public void start(Stage primaryStage) {
        loadSettings();
        primaryStage.setTitle("Калькулятор Мякоти");
        primaryStage.getIcons().add(new Image("/blue.png"));
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);
        String[] itemNames = {"Мякоть солевика", "Мякоть сластены", "Мякоть куборбуза", "Мякоть лимонника", "Аномальная Пыль"};
        String[] headerNames = {"Товар", "Цена", "Количество", "Сумма"};
        String[] itemImagePaths = {"/white.png", "/blue.png", "/red.png", "/yellow.png", "/dust.png"};
        for (int i = 0; i < headerNames.length; i++) {
            Label headerLabel = new Label(headerNames[i]);
            grid.add(headerLabel, i+1, 0);
        }
        for (int i = 0; i < itemNames.length; i++) {
            int k = -1;
            itemCheckBoxes[i] = new CheckBox();
            ImageView icon = new ImageView(new Image(itemImagePaths[i]));
            Tooltip.install(icon, new Tooltip(itemNames[i]));
            icon.setFitWidth(36);
            icon.setFitHeight(36);
            if (lastPrices != null ){
                if (lastPrices[i] != null){
                    priceFields[i] = new TextField(lastPrices[i]);
                }

            } else {
                priceFields[i] = new TextField("");
            }
            priceFields[i].setPrefWidth(75);
            quantityFields[i] = new TextField("");
            quantityFields[i].setPrefWidth(75);
            totalLabels[i] = new Label("0");
            grid.add(itemCheckBoxes[i], ++k, i + 1);
            grid.add(icon, ++k, i + 1);
            grid.add(priceFields[i], ++k, i +  1);
            grid.add(quantityFields[i], ++k, i + 1);
            grid.add(totalLabels[i], ++k, i + 1);
        }

        Button calculateButton = new Button("Рассчитать");
        Button clearButton = new Button("Очистить");
        Button copyButton = new Button("Скопировать");
        Button msgButton = new Button("Сообщение");
        buttons[0] = calculateButton;
        buttons[1] = clearButton;
        buttons[2] = copyButton;
        buttons[3] = msgButton;

        totalSumLabel = new Label("Общая сумма: 0");

        calculateButton.setOnAction(e -> calculateTotal());
        clearButton.setOnAction(e -> clearFields());
        copyButton.setOnAction(e -> copySum(String.valueOf(getTotalSum())));
        msgButton.setOnAction(e -> generateMessage());
        ImageView colorBtnIcon = new ImageView(new Image("/colorPicker.png"));
        colorBtnIcon.setFitWidth(24);
        colorBtnIcon.setFitHeight(24);
        Button colorBtn = new Button("", colorBtnIcon);
        colorBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        colorBtn.setOnAction(e -> openColorSelectionWindow());
        GridPane btnGrid = new GridPane();
        btnGrid.setHgap(11);
        btnGrid.add(colorBtn, 0, totalLabels.length + 1);
        btnGrid.add(msgButton, 1, totalLabels.length + 1);
        btnGrid.add(clearButton, 4, totalLabels.length + 1);
        btnGrid.add(calculateButton, 5, totalLabels.length + 1);



        VBox layout = new VBox(10, grid, btnGrid, totalSumLabel, copyButton);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);


        scene = new Scene(layout, 370, 400);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
            saveSettings();
        });
        // Applying saved color for the background
        if (bgColor != null){
            scene.getRoot().setStyle("-fx-background-color: " + toRgb(bgColor));
        }
        // Applying saved color for all the buttons and text fields
        if (miscColor != null){
            String miscColorStyle = "-fx-background-color: " + toRgb(miscColor);
            for (Button btn : buttons) {
                btn.setStyle(miscColorStyle);
            }
            for (TextField field : priceFields) {
                field.setStyle(miscColorStyle);
            }
            for (TextField field : quantityFields) {
                field.setStyle(miscColorStyle);
            }
            for (CheckBox checkBox : itemCheckBoxes){
                checkBox.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                checkBox.lookup(".box").setStyle(miscColorStyle);
            }
        }
    }

    private void calculateTotal() {
        setTotalSum(0);
        for (int i = 0; i < quantityFields.length; i++) {
            try {
                int price = Integer.parseInt(priceFields[i].getText());
                int quantity = Integer.parseInt(quantityFields[i].getText());
                int total = quantity * price;
                totalLabels[i].setText(String.valueOf(total));
                totalSum += total;
            } catch (NumberFormatException e) {
                totalLabels[i].setText("0");
            }
        }
        totalSumLabel.setText("Общая сумма: " + getTotalSum());
    }

    private void clearFields() {
        for (TextField field : quantityFields) {
            field.setText("");
        }
        for (Label label : totalLabels) {
            label.setText("0");
        }
        setTotalSum(0);
        totalSumLabel.setText("Общая сумма: " + getTotalSum());
    }

    private void copySum(String text) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    private void generateMessage() {
        String[] listOfItems = {"Мякоть солевика", "Мякоть сластены", "Мякоть куборбуза", "Мякоть лимонника", "Аномальная пыль"};
        StringBuilder msg = new StringBuilder("СКУПАЮ");

        for (int i = 0; i < listOfItems.length; i++) {
            if (itemCheckBoxes[i].isSelected()) {  // Only include selected items
                msg.append(" [").append(listOfItems[i]).append("]").append(formatNumber(Integer.parseInt(priceFields[i].getText())));
            }
        }
        msg.append(" [Вступайте в мой отряд]!!!!!");
        copySum(msg.toString());
    }
    private String formatNumber(int num) {
        if (num >= 1000) {
            double shortNum = num / 1000.0;
            return (shortNum % 1 == 0) ? ((int) shortNum + "k") : (String.format("%.1fk", shortNum));
        }
        return String.valueOf(num);
    }

    private void openColorSelectionWindow() {
        Stage colorStage = new Stage();
        colorStage.setTitle("Выбор цвета");
        colorStage.getIcons().add(new Image("/colorPicker.png"));
        RadioButton bgColorOption = new RadioButton("Фон");
        RadioButton elementsColorOption = new RadioButton("Элементы (кнопки и поля)");
        ToggleGroup toggleGroup = new ToggleGroup();
        bgColorOption.setToggleGroup(toggleGroup);
        elementsColorOption.setToggleGroup(toggleGroup);
        bgColorOption.setSelected(true);
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(bgColor);
        bgColorOption.setOnAction(e -> colorPicker.setValue(bgColor));
        elementsColorOption.setOnAction(e -> colorPicker.setValue(miscColor));
        Button applyButton = new Button("Применить");
        Button resetButton = new Button("Сбросить");
        applyButton.setOnAction(e -> {
            Color selectedColor = colorPicker.getValue();
            String colorStyle = "-fx-background-color: " + toRgb(selectedColor);
            if (bgColorOption.isSelected()) {
                bgColor = selectedColor;
                scene.getRoot().setStyle(colorStyle);
            } else {
                miscColor = selectedColor;
                for (Button btn : buttons) {
                    btn.setStyle(colorStyle);
                }
                for (TextField field : priceFields) {
                    field.setStyle(colorStyle);
                }
                for (TextField field : quantityFields) {
                    field.setStyle(colorStyle);
                }
                for (CheckBox checkBox : itemCheckBoxes){
                    checkBox.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                    checkBox.lookup(".box").setStyle(colorStyle);
                }
            }
            colorStage.close();
        });

        resetButton.setOnAction(e -> {
            String colorStyle = "-fx-background-color: " + "white";
            scene.getRoot().setStyle(colorStyle);
            for (Button btn : buttons) {
                btn.setStyle("");
            }
            for (TextField field : priceFields) {
                field.setStyle("");
            }
            for (TextField field : quantityFields) {
                field.setStyle("");
            }
            for (CheckBox checkBox : itemCheckBoxes){
                checkBox.lookup(".box").setStyle("");
            }
            bgColor = null;
            miscColor = null;
            colorStage.close();
        });

        VBox popupLayout = new VBox(10, bgColorOption, elementsColorOption, colorPicker, applyButton, resetButton);
        popupLayout.setPadding(new Insets(10));
        popupLayout.setAlignment(Pos.CENTER_LEFT);

        Scene popupScene = new Scene(popupLayout, 250, 200);
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
        try(FileWriter writer = new FileWriter("save.txt", false)) {
            for (TextField price : priceFields){
                if (price.getText().equals("")){
                    writer.write(0 );
                }
                writer.write(price.getText() + " ");
            }
            if(miscColor == null && bgColor == null){
                writer.write("\ndefault default");
            }
            else if (bgColor == null){
                writer.write("\ndefault " + miscColor);
            } else if (miscColor == null){
                writer.write("\n"+ bgColor + " default");
            } else{
                writer.write("\n" + bgColor + " " + miscColor);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void loadSettings(){
        File savedSettings = new File("save.txt");
        if (savedSettings.exists()){
            String pricesLine = null;
            String colorsLine = null;
            try {
                Scanner lastSaveScanner = new Scanner(new File("save.txt"));
                while(lastSaveScanner.hasNext()) {
                    pricesLine = lastSaveScanner.nextLine();
                    colorsLine = lastSaveScanner.nextLine();
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            if (pricesLine != null && colorsLine != null){
                lastPrices = pricesLine.split(" ");
                lastColors = colorsLine.split(" ");
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

    public static void main(String[] args) {
        launch(args);
    }
}
