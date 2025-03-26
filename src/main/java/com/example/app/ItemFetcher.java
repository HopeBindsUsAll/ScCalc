package com.example.app;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.Duration;
import java.util.*;

public class ItemFetcher implements Runnable {
    private static final String RECIPES_URL = "https://raw.githubusercontent.com/EXBO-Studio/stalcraft-database/main/ru/hideout_recipes.json";

    private static final String[] ITEM_FOLDERS = {
            "misc", "bullet", "other", "medicine", "drink", "food",
             "grenade", "armor", "weapon", "artefact", "containers", "attachment",
    };

    private static final Set<String> IGNORED_CATEGORIES = Set.of(
            "decorative", "furniture", "other"
    );
    private static final String LOCAL_DATA_FILE = "items.txt";
    private static final String LOCAL_ICONS_FOLDER = "icons/";

    private static final String[] ADDITIONAL_ITEMS = {
            "y3nmw", "4q7pl", "l0og1", "4q7pl"
    };

    public void run() {
        new File(LOCAL_ICONS_FOLDER).mkdir(); // Ensure icons folder exists
        System.out.println("🔄 Fetching items from recipes...");
        fetchAndStoreItems();
    }

    private void fetchAndStoreItems() {
        Instant startTime = Instant.now(); // Start timing
        try {

            JSONObject jsonObject = fetchJSON(RECIPES_URL);
            JSONArray recipes = jsonObject.getJSONArray("recipes");

            Set<String> uniqueItems = new HashSet<>();
            List<String> itemData = new ArrayList<>();
            int totalRecipes = recipes.length();
            int processedRecipes = 0;

            for (int i = 0; i < totalRecipes; i++) {
                Instant startTimeRecipe = Instant.now();
                JSONObject recipe = recipes.getJSONObject(i);
                // Check if category exists and is in the ignored list
                if (recipe.has("category")) {
                    JSONObject categoryObj = recipe.getJSONObject("category");
                    String categoryKey = categoryObj.getString("key").toLowerCase();
                    if (IGNORED_CATEGORIES.stream().anyMatch(categoryKey::contains)) {
                        System.out.println("🚫 Skipping category: " + categoryKey);
                        processedRecipes++;
                        continue;
                    }
                }

                // Fetch crafted items (result)
                JSONArray results = recipe.getJSONArray("result");
                for (int j = 0; j < results.length(); j++) {
                    String itemId = results.getJSONObject(j).getString("item");
                    if (uniqueItems.add(itemId)) {
                        String[] fetchItemNameResult = fetchItemName(itemId);
                        itemData.add(itemId + " : " + fetchItemNameResult[0] + " : 0");
                        System.out.println("✅ Fetched: " + itemId + " → " + fetchItemNameResult[0]);
                        fetchItemIcon(itemId, fetchItemNameResult[1]);
                    }
                }

                // Fetch ingredient items
                if (recipe.has("ingredients")) {
                    JSONArray ingredients = recipe.getJSONArray("ingredients");
                    for (int j = 0; j < ingredients.length(); j++) {
                        String itemId = ingredients.getJSONObject(j).getString("item");
                        if (uniqueItems.add(itemId)) {
                            String[] fetchItemNameResult = fetchItemName(itemId);
                            itemData.add(itemId + " : " + fetchItemNameResult[0] + " : 0");
                            System.out.println("🛠️ Fetched Ingredient: " + itemId + " → " + fetchItemNameResult[0]);
                            fetchItemIcon(itemId, fetchItemNameResult[1]);
                        }
                    }
                }
                Instant endTimeRecipe = Instant.now();
                long timeElapsed = Duration.between(startTimeRecipe, endTimeRecipe).toSeconds();
                System.out.println("⏳ Recipe Fetching completed in " + timeElapsed + " seconds.");
                processedRecipes++;
                System.out.println("📦 Progress: " + processedRecipes + "/" + totalRecipes + " recipes processed.");
            }

            for (String itemId : ADDITIONAL_ITEMS){
                if (uniqueItems.add(itemId)) {
                    String[] fetchItemNameResult = fetchItemName(itemId);
                    itemData.add(itemId + " : " + fetchItemNameResult[0] + " : 0");
                    System.out.println("✅ Fetched: " + itemId + " → " + fetchItemNameResult[0]);
                    fetchItemIcon(itemId, fetchItemNameResult[1]);
                }

            }
            saveToFile(LOCAL_DATA_FILE, itemData);

        } catch (Exception e) {
            System.err.println("❌ Error fetching items: " + e.getMessage());
        }

        Instant endTime = Instant.now(); // Stop timing
        long timeElapsed = Duration.between(startTime, endTime).toSeconds();
        System.out.println("⏳ Fetching completed in " + timeElapsed + " seconds.");
    }


    private String[] fetchItemName(String itemId) {

        for (String folder : ITEM_FOLDERS) {
            String url = String.format(
                    "https://raw.githubusercontent.com/EXBO-Studio/stalcraft-database/main/ru/items/%s/%s.json",
                    folder, itemId
            );
            try {
                JSONObject itemJson = fetchJSON(url);
                // Extract the name
                if (itemJson.has("name")) {
                    JSONObject nameObject = itemJson.getJSONObject("name");
                    if (nameObject.has("lines") && nameObject.getJSONObject("lines").has("ru")) {
                        return new String[]{nameObject.getJSONObject("lines").getString("ru"), folder};
                    }
                }
            } catch (Exception ignored) {}
        }
        return new String[]{"Unknown item", "No folder"};
    }

    private void fetchItemIcon(String itemId, String folder) {
        boolean iconFound = false;
        File iconFile = new File(LOCAL_ICONS_FOLDER + itemId + ".png");
        if (!iconFile.exists()) {
            String iconUrl = String.format(
                    "https://raw.githubusercontent.com/EXBO-Studio/stalcraft-database/main/ru/icons/%s/%s.png",
                    folder, itemId
            );
            try {
                saveImage(iconUrl, iconFile);
                System.out.println("🖼️ Downloaded icon: " + itemId + ".png");
                return;
            } catch (Exception e) {

            }
        } else {
            iconFound = true;
            System.out.println("⚠️ Icon already exists in the folder: " + itemId);
        }
        if (!iconFound){
            System.out.println("⚠️ Failed to download icon: " + itemId);
        }
    }

    private JSONObject fetchJSON(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return new JSONObject(response.toString());
        }
    }

    private void saveImage(String imageUrl, File outputFile) throws IOException {
        URL url = new URL(imageUrl);
        try (InputStream in = url.openStream();
             OutputStream out = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    private void saveToFile(String fileName, List<String> data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (String line : data) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("❌ Error saving to file: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        ItemFetcher fetcher = new ItemFetcher();
        fetcher.run();
    }
}
