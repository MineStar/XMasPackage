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

import org.bukkit.Material;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import de.minestar.minestarlibrary.data.nbt_1_6_2.NBTBase;
import de.minestar.minestarlibrary.data.nbt_1_6_2.NBTConverter;
import de.minestar.minestarlibrary.data.nbt_1_6_2.NBTTagList;
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
        this.itemList = this.loadItemsFromNBT();
        System.out.println("NOTE: Items loaded for day " + this.day + " : " + this.itemList.size());
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

    public boolean addButtonPos(BlockVector buttonPos) {
        if (!this.hasButtonPos(buttonPos)) {
            if (XMASCore.database.addButton(this.day, buttonPos)) {
                this.buttons.add(buttonPos);
                return true;
            }
            return false;
        }
        return true;
    }

    public boolean hasButtonPos(BlockVector buttonPos) {
        for (BlockVector vector : this.buttons) {
            if (vector.equals(buttonPos)) {
                return true;
            }
        }
        return false;
    }

    public boolean removeButtonPos(BlockVector buttonPos) {
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
            if (XMASCore.database.removeButton(this.day, this.buttons.get(found))) {
                this.buttons.remove(found);
                return true;
            } else {
                return false;
            }
        }
        return true;
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

    // public void saveItemsToNBTFile() {
    // File file = new File(XMASCore.INSTANCE.getDataFolder(), "items_" + this.day + ".nbt");
    //
    // if (file.exists()) {
    // file.delete();
    // }
    //
    // try {
    // DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(file));
    //
    // int itemAmount = 0;
    // for (ItemStack stack : this.itemList) {
    // if (stack == null) {
    // continue;
    // }
    // itemAmount++;
    // }
    // outputStream.writeInt(itemAmount);
    //
    // // create itemlist
    // NBTTagList tagList = new NBTTagList();
    // for (int i = 0; i < this.itemList.size(); i++) {
    // if (this.itemList.get(i) != null) {
    // tagList.add(XMasDay.ItemStackToNBT(this.itemList.get(i)));
    // }
    // }
    //
    // // write to stream
    // // thanks to @Bukkit, we need reflections here...
    // try {
    // Method method = NBTTagList.class.getDeclaredMethod("write", DataOutput.class);
    // method.setAccessible(true);
    // method.invoke(tagList, outputStream);
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    //
    // // close stream
    // outputStream.close();
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

    // public void loadItemsFromNBT() {
    // this.itemList = new ArrayList<ItemStack>();
    // File file = new File(XMASCore.INSTANCE.getDataFolder(), "items_" + this.day + ".nbt");
    // if (!file.exists()) {
    // ConsoleUtils.printInfo(XMASCore.NAME, "ITEMS FOR DAY " + this.day + " NOT FOUND!");
    // return;
    // }
    //
    // try {
    // DataInputStream inputStream = new DataInputStream(new FileInputStream(file));
    // int itemAmount = inputStream.readInt();
    //
    // // load items from file
    // // thanks to @Bukkit, we need reflections here...
    // NBTTagList tagList = new NBTTagList();
    // try {
    // Method method = NBTTagList.class.getDeclaredMethod("load", DataInput.class, int.class);
    // method.setAccessible(true);
    // method.invoke(tagList, inputStream, 0);
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    //
    // this.itemList.clear();
    // for (int i = 0; i < itemAmount; i++) {
    // net.minecraft.server.v1_7_R1.ItemStack nativeStack = net.minecraft.server.v1_7_R1.ItemStack.createStack((NBTTagCompound) tagList.get(i));
    // ItemStack bukkitStack = CraftItemStack.asBukkitCopy(nativeStack);
    // this.itemList.add(bukkitStack);
    // }
    //
    // inputStream.close();
    // } catch (Exception e) {
    // this.itemList.clear();
    // e.printStackTrace();
    // }
    // }

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

    // public static NBTTagCompound ItemStackToNBT(ItemStack stack) {
    // if (stack == null) {
    // System.out.println("STACK IS NULL");
    // return new NBTTagCompound();
    // }
    // CraftItemStack cStack = (CraftItemStack) stack;
    // NBTTagCompound tagCompound = new NBTTagCompound();
    // try {
    // Field field = cStack.getClass().getField("handle");
    // field.setAccessible(true);
    //
    // net.minecraft.server.v1_7_R1.ItemStack nStack = new net.minecraft.server.v1_7_R1.ItemStack((Block) Block.REGISTRY.a(stack.getTypeId()));
    // nStack = (net.minecraft.server.v1_7_R1.ItemStack) (field.get(nStack));
    // if (nStack == null) {
    // System.out.println("NATIVE STACK IS NULL");
    // return new NBTTagCompound();
    // }
    // nStack.save(tagCompound);
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // return tagCompound;
    // }

    public void updateItems(Dispenser dispenser) {
        this.itemList.clear();
        ItemStack[] contents = dispenser.getInventory().getContents();
        for (ItemStack stack : contents) {
            if (stack == null || stack.getType().equals(Material.AIR)) {
                continue;
            }
            this.itemList.add(stack.clone());
        }
        this.saveItemsToNBTFile(this.itemList);
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
        this.saveItemsToNBTFile(this.itemList);
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

    public void loadButtonsFromDB() {
        this.buttons.addAll(XMASCore.database.getButtonsForDay(this.day));
        for (BlockVector vector : this.buttons) {
            XMASCore.registerBlock(vector, this);
        }
    }

    public void saveItemsToNBTFile(ArrayList<ItemStack> itemList) {
        File file = new File(XMASCore.INSTANCE.getDataFolder(), "items_" + this.day + ".nbt");

        if (file.exists()) {
            file.delete();
        }

        try {
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(file));

            int itemAmount = 0;
            for (ItemStack stack : itemList) {
                if (stack == null) {
                    continue;
                }
                itemAmount++;
            }
            outputStream.writeInt(itemAmount);

            // create itemlist
            net.minecraft.server.v1_8_R3.NBTTagList nativeTagList = new net.minecraft.server.v1_8_R3.NBTTagList();
            for (int i = 0; i < itemList.size(); i++) {
                if (itemList.get(i) != null) {
                    net.minecraft.server.v1_8_R3.NBTTagCompound compound = new net.minecraft.server.v1_8_R3.NBTTagCompound();
                    compound = CraftItemStack.asNMSCopy(itemList.get(i)).save(compound);
                    nativeTagList.add(compound);
                }
            }
            NBTTagList tagList = new NBTTagList();
            tagList = (NBTTagList) NBTConverter.fromNative(nativeTagList);

            // write to stream
            NBTBase.write(tagList, outputStream);

            // close stream
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<ItemStack> loadItemsFromNBT() {
        ArrayList<ItemStack> itemList = new ArrayList<ItemStack>();
        File file = new File(XMASCore.INSTANCE.getDataFolder(), "items_" + this.day + ".nbt");
        if (!file.exists()) {
            ConsoleUtils.printInfo(XMASCore.NAME, "ITEMS NOT FOUND!");
            return itemList;
        }
        try {
            DataInputStream reader = new DataInputStream(new FileInputStream(file));
            int itemAmount = reader.readInt();

            NBTTagList tagList = (NBTTagList) NBTBase.read(reader);
            net.minecraft.server.v1_8_R3.NBTTagList nativeList = (net.minecraft.server.v1_8_R3.NBTTagList) NBTConverter.toNative(tagList);
            itemList.clear();
            for (int i = 0; i < itemAmount; i++) {
                net.minecraft.server.v1_8_R3.ItemStack nativeStack = net.minecraft.server.v1_8_R3.ItemStack.createStack((net.minecraft.server.v1_8_R3.NBTTagCompound) nativeList.get(i));
                if (nativeStack != null) {
                    itemList.add(CraftItemStack.asBukkitCopy(nativeStack));
                }
            }

            reader.close();
        } catch (Exception e) {
            itemList.clear();
            e.printStackTrace();
        }
        return itemList;
    }
}
