import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.junit.Test;

import de.minestar.minestarlibrary.data.nbt_1_6_2.NBTBase;
import de.minestar.minestarlibrary.data.nbt_1_6_2.NBTTagCompound;
import de.minestar.minestarlibrary.data.nbt_1_6_2.NBTTagList;
import de.minestar.minestarlibrary.utils.ConsoleUtils;
import de.minestar.xmas.XMASCore;
import de.minestar.xmas.data.XMasDay;

public class NBTTest {

//    @Test
    public void test() {
        System.out.println("+++++++++++++++++++++++++++++++++++++++++");
        System.out.println("RUNNING TEST");
        System.out.println("+++++++++++++++++++++++++++++++++++++++++");

        ArrayList<net.minecraft.server.v1_7_R1.ItemStack> itemList = this.loadItemsFromNBT();
        System.out.println("Items loaded: " + itemList.size());
        this.saveItemsToNBTFile(itemList);

        System.out.println("+++++++++++++++++++++++++++++++++++++++++");
    }

    public void saveItemsToNBTFile(ArrayList<net.minecraft.server.v1_7_R1.ItemStack> itemList) {
        File file = new File("src\\test\\resources\\items_new.nbt");

        if (file.exists()) {
            file.delete();
        }

        try {
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(file));

            int itemAmount = 0;
            for (net.minecraft.server.v1_7_R1.ItemStack stack : itemList) {
                if (stack == null) {
                    continue;
                }
                itemAmount++;
            }
            outputStream.writeInt(itemAmount);

            // create itemlist
            NBTTagList tagList = new NBTTagList();
            for (int i = 0; i < itemList.size(); i++) {
                if (itemList.get(i) != null) {
                    System.out.println("Item: " + itemList.get(i).getItem().toString());
                    NBTTagCompound singleItemCompound = new NBTTagCompound();
                    net.minecraft.server.v1_7_R1.NBTTagCompound compound = new net.minecraft.server.v1_7_R1.NBTTagCompound();
                    // CraftItemStack.asNMSCopy(itemList.get(i)).save(compound);
                    itemList.get(i).save(compound);
                    singleItemCompound = (NBTTagCompound) NBTBase.convertFromNative(compound);
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

    public ArrayList<net.minecraft.server.v1_7_R1.ItemStack> loadItemsFromNBT() {
        ArrayList<net.minecraft.server.v1_7_R1.ItemStack> itemList = new ArrayList<net.minecraft.server.v1_7_R1.ItemStack>();
        File file = new File("src\\test\\resources\\items.nbt");
        if (!file.exists()) {
            ConsoleUtils.printInfo(XMASCore.NAME, "ITEMS NOT FOUND!");
            return itemList;
        }
        try {
            DataInputStream reader = new DataInputStream(new FileInputStream(file));
            int itemAmount = reader.readInt();

            NBTTagList tagList = (NBTTagList) NBTBase.a(reader);
            itemList.clear();
            for (int i = 0; i < itemAmount; i++) {
                net.minecraft.server.v1_7_R1.ItemStack nativeStack = net.minecraft.server.v1_7_R1.ItemStack.createStack((net.minecraft.server.v1_7_R1.NBTTagCompound) ((NBTTagCompound) tagList.get(i)).toNative());
                if (nativeStack != null) {
                    itemList.add(nativeStack);
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
