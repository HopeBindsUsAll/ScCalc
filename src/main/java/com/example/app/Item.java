package com.example.app;

import javafx.scene.image.Image;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Item {
    private String itemID;
    private String itemName;
    private Integer price;

    private Image icon;




    public Item(String itemID, String itemName, Integer price, Image icon) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.price = price;
        this.icon = icon;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }


    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Image getIcon() {
        return icon;
    }

    public void setIcon(String iconPath){
        this.icon = new Image(iconPath);
    }
}
