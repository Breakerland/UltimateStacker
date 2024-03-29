package com.songoda.ultimatestacker.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.ultimatestacker.convert.Convert;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class GUIConvertWhat extends Gui {

    private Convert convertFrom = null;

    private boolean entities = true;
    private boolean spawners = true;

    public GUIConvertWhat(Convert convertFrom, Gui returnTo) {
        super(returnTo);
        this.setRows(1);
        this.setTitle("What Do You Want To Convert?");
        this.convertFrom = convertFrom;

        if (convertFrom.canEntities()) {
            this.setButton(0, GuiUtils.createButtonItem(CompatibleMaterial.STONE,
                    ChatColor.GRAY + "Stacked Entities",
                    entities ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"),
                    (event) -> toggleEntities());
        }

        if (convertFrom.canSpawners()) {
            this.setButton(1, GuiUtils.createButtonItem(CompatibleMaterial.STONE,
                    ChatColor.GRAY + "Stacked Spawners",
                    spawners ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"),
                    (event) -> toggleSpawners());
        }

        this.setButton(8, GuiUtils.createButtonItem(CompatibleMaterial.GREEN_WOOL, ChatColor.GREEN + "Run"),
                (event) -> run(event.player));

    }

    void toggleEntities() {
        entities = !entities;
        this.updateItem(0, ChatColor.GRAY + "Stacked Entities", entities ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No");
    }

    void toggleSpawners() {
        spawners = !spawners;
        this.updateItem(1, ChatColor.GRAY + "Stacked Spawners", spawners ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No");
    }

    void run(Player player) {
        if (entities) {
            convertFrom.convertEntities();
        }
        if (spawners) {
            convertFrom.convertSpawners();
        }

        convertFrom.disablePlugin();
        exit();
        player.sendMessage(Methods.formatText("&7Data converted successfully. Remove &6" + convertFrom.getName() + " &7and restart your server to continue."));
    }
}
