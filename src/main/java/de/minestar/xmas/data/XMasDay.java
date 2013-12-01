package de.minestar.xmas.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.server.v1_6_R2.NBTBase;
import net.minecraft.server.v1_6_R2.NBTTagCompound;
import net.minecraft.server.v1_6_R2.NBTTagList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.minestar.minestarlibrary.utils.ConsoleUtils;
import de.minestar.xmas.XMASCore;

public class XMasDay {

    private BlockVector dispenserPos;
    private ArrayList<BlockVector> buttons;

    private ArrayList<ItemStack> itemList;

    private final int day;

    public XMasDay(int day) {
        this.day = day;
        this.buttons = new ArrayList<BlockVector>();
        this.loadDispenserFromFile();
        this.loadButtonsFromFile();
        this.loadItemsFromFile();
    }

    public boolean addPlayer(String playerName) {
        return XMASCore.database.insertPlayer(day, playerName);
    }

    public boolean hasPlayer(String playerName) {
        return XMASCore.database.hasPlayer(day, playerName);
    }

    public void setDispenserPos(BlockVector dispenserPos) {
        this.dispenserPos = dispenserPos;
        this.saveDispenserToFile();
    }

    public void addButtonPos(BlockVector buttonPos) {
        if (!this.hasButtonPos(buttonPos)) {
            this.buttons.add(buttonPos);
            this.saveButtonsToFile();
        }
    }

    public boolean hasButtonPos(BlockVector buttonPos) {
        for (BlockVector vector : this.buttons) {
            if (vector.equals(buttonPos)) {
                return true;
            }
        }
        return false;
    }

    public void removeButtonPos(BlockVector buttonPos) {
        int index = -1;
        int found = -1;
        for (BlockVector vector : this.buttons) {
            index++;
            if (vector.equals(buttonPos)) {
                found = index;
                break;
            }
        }

        if (found != -1) {
            this.buttons.remove(found);
            this.saveButtonsToFile();
        }
    }

    private void saveButtonsToFile() {
        File file = new File(XMASCore.INSTANCE.getDataFolder(), "buttons_" + this.day + ".txt");

        if (file.exists()) {
            file.delete();
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

            for (BlockVector buttonPos : this.buttons) {
                writer.write(BlockVector.BlockVectorToString(buttonPos));
            }

            writer.flush();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveDispenserToFile() {
        File file = new File(XMASCore.INSTANCE.getDataFolder(), "blocks_" + this.day + ".txt");

        if (file.exists()) {
            file.delete();
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

            if (this.dispenserPos == null) {
                writer.write("NULL");
            } else {
                writer.write(BlockVector.BlockVectorToString(this.dispenserPos));
            }
            writer.newLine();
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveItemsToFile() {
        this.saveItemsToNBTFile();
    }

    public void saveItemsToNBTFile() {
        File file = new File(XMASCore.INSTANCE.getDataFolder(), "items_" + this.day + ".nbt");

        if (file.exists()) {
            file.delete();
        }

        try {
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(file));

            int itemAmount = 0;
            for (ItemStack stack : this.itemList) {
                if (stack == null) {
                    continue;
                }
                itemAmount++;
            }
            outputStream.writeInt(itemAmount);

            // create itemlist
            NBTTagList tagList = new NBTTagList();
            for (int i = 0; i < this.itemList.size(); i++) {
                if (this.itemList.get(i) != null) {
                    NBTTagCompound singleItemCompound = new NBTTagCompound();
                    CraftItemStack.asNMSCopy(this.itemList.get(i)).save(singleItemCompound);
                    tagList.add(singleItemCompound);
                }
            }

            // write to stream
            NBTBase.a(tagList, outputStream);

            // close stream
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadItemsFromNBT() {
        this.itemList = new ArrayList<ItemStack>();
        File file = new File(XMASCore.INSTANCE.getDataFolder(), "items_" + this.day + ".nbt");
        if (!file.exists()) {
            ConsoleUtils.printInfo(XMASCore.NAME, "ITEMS FOR DAY " + this.day + " NOT FOUND!");
            return;
        }

        try {
            DataInputStream reader = new DataInputStream(new FileInputStream(file));
            int itemAmount = reader.readInt();

            NBTTagList tagList = (NBTTagList) NBTBase.a(reader);
            this.itemList.clear();
            for (int i = 0; i < itemAmount; i++) {
                net.minecraft.server.v1_6_R2.ItemStack nativeStack = net.minecraft.server.v1_6_R2.ItemStack.createStack((NBTTagCompound) tagList.get(i));
                ItemStack bukkitStack = CraftItemStack.asBukkitCopy(nativeStack);
                this.itemList.add(bukkitStack);
            }

            reader.close();
        } catch (Exception e) {
            this.itemList.clear();
            e.printStackTrace();
        }
    }

    public void loadDispenserFromFile() {
        File file = new File(XMASCore.INSTANCE.getDataFolder(), "blocks_" + this.day + ".txt");
        if (!file.exists()) {
            ConsoleUtils.printInfo(XMASCore.NAME, "DISPENSERS FOR DAY " + this.day + " NOT FOUND!");
            return;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String zeile = "";

            zeile = reader.readLine();
            if (!zeile.equalsIgnoreCase("NULL")) {
                this.dispenserPos = BlockVector.BlockVectorFromString(zeile);
            }
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadButtonsFromFile() {
        File file = new File(XMASCore.INSTANCE.getDataFolder(), "buttons_" + this.day + ".txt");
        if (!file.exists()) {
            ConsoleUtils.printInfo(XMASCore.NAME, "BUTTONS FOR DAY " + this.day + " NOT FOUND!");
            return;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String zeile = "";

            zeile = reader.readLine();
            while (zeile != null) {
                if (!zeile.equalsIgnoreCase("NULL")) {
                    BlockVector vector = BlockVector.BlockVectorFromString(zeile);
                    if (vector != null) {
                        this.buttons.add(vector);
                    } else {
                        System.out.println("Button for day " + this.day + " is null: " + zeile);
                    }
                } else {
                    System.out.println("Button for day " + this.day + " is null: " + zeile);
                }
                zeile = reader.readLine();
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void loadItemsFromFile() {
        this.loadItemsFromNBT();
    }

    public static NBTTagCompound ItemStackToNBT(ItemStack stack) {
        net.minecraft.server.v1_6_R2.ItemStack nStack = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound tagCompound = new NBTTagCompound();
        nStack.save(tagCompound);
        return tagCompound;
    }

    public static String ItemStackToString(ItemStack stack) {
        net.minecraft.server.v1_6_R2.ItemStack nStack = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound tagCompount = new NBTTagCompound();
        nStack.save(tagCompount);

        String itemName = "";
        String lore = "";
        int index = 0;
        String enchantments = "";
        if (stack.getItemMeta() != null) {
            itemName = stack.getItemMeta().getDisplayName();

            if (itemName == null) {
                itemName = "NULL";
            }
            if (stack.getItemMeta().getLore() != null) {
                for (String singleLore : stack.getItemMeta().getLore()) {
                    lore += singleLore;
                    index++;
                    if (index < stack.getItemMeta().getLore().size()) {
                        lore += "-+-";
                    }
                }
            } else {
                lore = "NULL";
            }

            for (Map.Entry<Enchantment, Integer> entry : stack.getItemMeta().getEnchants().entrySet()) {
                enchantments += ";" + entry.getKey().getName() + ";" + entry.getValue();
            }
        } else {
            for (Map.Entry<Enchantment, Integer> entry : stack.getEnchantments().entrySet()) {
                enchantments += ";" + entry.getKey().getName() + ";" + entry.getValue();
            }
        }

        String text = stack.getAmount() + ";" + stack.getTypeId() + ";" + stack.getDurability() + ";" + itemName + ";" + lore + enchantments;
        return text;
    }

    public static ItemStack ItemStackFromString(String string) {
        String[] split = string.split(";");
        try {
            int amount = Integer.valueOf(split[0]);
            int ID = Integer.valueOf(split[1]);
            short subID = Short.valueOf(split[2]);
            String itemName = split[3];

            // create stack
            ItemStack stack = new ItemStack(ID);
            stack.setDurability(subID);

            // set amount
            stack.setAmount(amount);

            // add enchantments
            for (int i = 5; i < split.length; i = i + 2) {
                int level = Integer.valueOf(split[i + 1]);
                Enchantment enchantment = Enchantment.getByName(split[i]);
                stack.addEnchantment(enchantment, level);
            }

            // create itemMeta
            ItemMeta meta = stack.getItemMeta();
            if (meta == null) {
                meta = Bukkit.getItemFactory().getItemMeta(Material.getMaterial(ID));
            }

            // set displayname
            if (!itemName.equalsIgnoreCase("NULL")) {
                meta.setDisplayName(itemName);
            }

            // set lore
            if (!split[4].equalsIgnoreCase("NULL")) {
                List<String> lore = new ArrayList<String>();
                String[] loreSplit = split[4].split("-+-");
                for (String loreText : loreSplit) {
                    lore.add(loreText);
                }
                meta.setLore(lore);
            }

            stack.setItemMeta(meta);
            return stack;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateItems(Dispenser dispenser) {
        this.itemList.clear();
        ItemStack[] contents = dispenser.getInventory().getContents();
        for (ItemStack stack : contents) {
            if (stack == null || stack.getType().equals(Material.AIR)) {
                continue;
            }
            this.itemList.add(stack.clone());
        }
        this.saveItemsToFile();
    }

    public void updateItems(Dropper dropper) {
        this.itemList.clear();
        ItemStack[] contents = dropper.getInventory().getContents();
        for (ItemStack stack : contents) {
            if (stack == null || stack.getType().equals(Material.AIR)) {
                continue;
            }
            this.itemList.add(stack.clone());
        }
        this.saveItemsToFile();
    }

    public boolean dispenseItemsOP() {
        if (this.dispenserPos == null) {
            return false;
        }

        if (this.dispenserPos.getLocation().getBlock().getType().equals(Material.DISPENSER)) {
            // we have a dispenser
            Dispenser dispenser = (Dispenser) this.dispenserPos.getLocation().getBlock().getState();
            dispenser.getInventory().clear();

            for (ItemStack stack : this.itemList) {
                ItemStack copy = stack.clone();
                copy.setAmount(1);
                for (int count = 0; count < stack.getAmount(); count++) {
                    dispenser.getInventory().addItem(copy.clone());
                    dispenser.dispense();
                }
            }
            return true;
        } else if (this.dispenserPos.getLocation().getBlock().getType().equals(Material.DROPPER)) {
            // we have a dropper
            Dropper dropper = (Dropper) this.dispenserPos.getLocation().getBlock().getState();
            dropper.getInventory().clear();

            for (ItemStack stack : this.itemList) {
                ItemStack copy = stack.clone();
                copy.setAmount(1);
                for (int count = 0; count < stack.getAmount(); count++) {
                    dropper.getInventory().addItem(copy.clone());
                    dropper.drop();
                }
            }
            return true;
        }

        return false;
    }

    public boolean dispenseItems() {
        if (this.dispenserPos == null) {
            return false;
        }

        if (this.dispenserPos.getLocation().getBlock().getType().equals(Material.DISPENSER)) {
            // we have a dispenser
            Dispenser dispenser = (Dispenser) this.dispenserPos.getLocation().getBlock().getState();
            dispenser.getInventory().clear();

            for (ItemStack stack : this.itemList) {
                ItemStack copy = stack.clone();
                copy.setAmount(1);
                for (int count = 0; count < stack.getAmount(); count++) {
                    dispenser.getInventory().addItem(copy.clone());
                    dispenser.dispense();
                }
            }
            return true;
        } else if (this.dispenserPos.getLocation().getBlock().getType().equals(Material.DROPPER)) {
            // we have a dropper
            Dropper dropper = (Dropper) this.dispenserPos.getLocation().getBlock().getState();
            dropper.getInventory().clear();

            for (ItemStack stack : this.itemList) {
                ItemStack copy = stack.clone();
                copy.setAmount(1);
                for (int count = 0; count < stack.getAmount(); count++) {
                    dropper.getInventory().addItem(copy.clone());
                    dropper.drop();
                }
            }
            return true;
        }

        return false;
    }

    public BlockVector getDispenserPos() {
        return dispenserPos;
    }

    public int getDay() {
        return day;
    }

    public ArrayList<BlockVector> getButtons() {
        return buttons;
    }
}
