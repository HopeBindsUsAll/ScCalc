package com.example.app;

import javafx.scene.image.Image;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class ItemList {

    public static ArrayList<Item> listOfItems = new ArrayList<Item>();
    private static final String LOCAL_DATA_FILE = "items.txt";
    private static final String LOCAL_ICONS_FOLDER = "icons/";
    private static final String FILE_PREFIX = "file:";

    private static final String SEPARATOR = " : ";
    public void readItems(){
        try {
            Scanner itemReader = new Scanner(new File(LOCAL_DATA_FILE));
            while (itemReader.hasNext()){
                String line = itemReader.nextLine();
                String[] parts = line.split(SEPARATOR);
                String iconPath = FILE_PREFIX + System.getProperty("user.dir") + File.separator + LOCAL_ICONS_FOLDER + parts[0] + ".png";
                //System.out.println(iconPath);
                File iconFile = new File(iconPath.substring(FILE_PREFIX.length()));
                if (!iconFile.exists()) {
                    System.err.println("Icon file not found: " + iconPath);
                    continue;
                }
                Image currentItemIcon = new Image(iconPath);
                listOfItems.add(new Item(parts[0], parts[1], Integer.parseInt(parts[2]), currentItemIcon));
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeItems (){
        try {
            FileWriter fileWriter = new FileWriter(new File(LOCAL_DATA_FILE), false);
            for (Item item : listOfItems) {
                String line = (item.getItemID() + SEPARATOR + item.getItemName() + SEPARATOR + item.getPrice().toString() + "\n");
                fileWriter.write(line);
            }
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}