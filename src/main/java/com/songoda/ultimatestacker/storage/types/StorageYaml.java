package com.songoda.ultimatestacker.storage.types;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.storage.Storage;
import com.songoda.ultimatestacker.storage.StorageItem;
import com.songoda.ultimatestacker.storage.StorageRow;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StorageYaml extends Storage {

    private static final Map<String, Object> toSave = new HashMap<>();
    private static Map<String, Object> lastSave = null;

    public StorageYaml(UltimateStacker instance) {
        super(instance);
    }

    @Override
    public boolean containsGroup(String group) {
        return dataFile.contains("data." + group);
    }

    @Override
    public List<StorageRow> getRowsByGroup(String group) {
        List<StorageRow> rows = new ArrayList<>();
        ConfigurationSection currentSection = dataFile.getConfigurationSection("data." + group);
        for (String key : currentSection.getKeys(false)) {

            Map<String, StorageItem> items = new HashMap<>();
            ConfigurationSection currentSection2 = dataFile.getConfigurationSection("data." + group + "." + key);
            for (String key2 : currentSection2.getKeys(false)) {
                String path = "data." + group + "." + key + "." + key2;
                items.put(key2, new StorageItem(dataFile.get(path) instanceof MemorySection
                        ? convertToInLineList(path) : dataFile.get(path)));
            }
            if (items.isEmpty()) continue;
            StorageRow row = new StorageRow(key, items);
            rows.add(row);
        }
        return rows;
    }

    private String convertToInLineList(String path) {
        StringBuilder converted = new StringBuilder();
        for (String key : dataFile.getConfigurationSection(path).getKeys(false)) {
            converted.append(key).append(":").append(dataFile.getInt(path + "." + key)).append(";");
        }
        return converted.toString();
    }

    @Override
    public void prepareSaveItem(String group, StorageItem... items) {
        for (StorageItem item : items) {
            if (item == null || item.asObject() == null) continue;
            toSave.put("data." + group + "." + items[0].asString() + "." + item.getKey(), item.asObject());
        }
    }

    @Override
    public void doSave() {
        this.updateData(instance);

        if (lastSave == null)
            lastSave = new HashMap<>(toSave);

        if (toSave.isEmpty()) return;
        Map<String, Object> nextSave = new HashMap<>(toSave);

        this.makeBackup();
        this.save();

        toSave.clear();
        lastSave.clear();
        lastSave.putAll(nextSave);
    }

    @Override
    public void save() {
        try {
            for (Map.Entry<String, Object> entry : lastSave.entrySet()) {
                if (toSave.containsKey(entry.getKey())) {
                    Object newValue = toSave.get(entry.getKey());
                    if (!entry.getValue().equals(newValue)) {
                        dataFile.set(entry.getKey(), newValue);
                    }
                    toSave.remove(entry.getKey());
                } else {
                    dataFile.set(entry.getKey(), null);
                }
            }

            for (Map.Entry<String, Object> entry : toSave.entrySet()) {
                dataFile.set(entry.getKey(), entry.getValue());
            }

            dataFile.save();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void makeBackup() {
        File data = new File(instance.getDataFolder(), "data.yml");
        File dataClone = new File(instance.getDataFolder(), "data-backup-" + System.currentTimeMillis() + ".yml");
        try {
            copyFile(data, dataClone);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Deque<File> backups = new ArrayDeque<>();
        for (File file : Objects.requireNonNull(instance.getDataFolder().listFiles())) {
            if (file.getName().toLowerCase().contains("data-backup")) {
                backups.add(file);
            }
        }
        if (backups.size() > 3) {
            backups.getFirst().delete();
        }
    }

    @Override
    public void closeConnection() {
        dataFile.saveChanges();
    }

    private static void copyFile(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }
}
