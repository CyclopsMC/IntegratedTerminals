package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

/**
 * @author rubensworks
 */
public class TerminalStorageState {

    public static final String SETTING_TAB = "tab";
    public static final String SETTING_SEARCH = "search";
    public static final String SETTING_BUTTON = "button";

    private final NBTTagCompound tag;

    public TerminalStorageState() {
        this.tag = new NBTTagCompound();
    }

    public String getTab() {
        return tag.getString(SETTING_TAB);
    }

    public boolean hasTab() {
        return tag.hasKey(SETTING_TAB, Constants.NBT.TAG_STRING);
    }

    public void setTab(@Nullable String tab) {
        if (tab != null) {
            tag.setString(SETTING_TAB, tab);
        } else {
            tag.removeTag(SETTING_TAB);
        }
    }

    public String getSearch(String tab, int channel) {
        return tag.getString(SETTING_SEARCH + "_" + tab  + "_" + channel);
    }

    public boolean hasSearch(String tab, int channel) {
        return tag.hasKey(SETTING_SEARCH + "_" + tab  + "_" + channel, Constants.NBT.TAG_STRING);
    }

    public void setSearch(String tab, int channel, @Nullable String search) {
        if (tab != null) {
            tag.setString(SETTING_SEARCH + "_" + tab  + "_" + channel, search);
        } else {
            tag.removeTag(SETTING_SEARCH + "_" + tab  + "_" + channel);
        }
    }

    public NBTBase getButton(String tab, String buttonName) {
        return tag.getTag(SETTING_BUTTON + "_" + tab + "_" + buttonName);
    }

    public boolean hasButton(String tab, String buttonName) {
        return tag.hasKey(SETTING_BUTTON + "_" + tab + "_" + buttonName);
    }

    public void setButton(String tab, String buttonName, @Nullable NBTBase button) {
        if (button != null) {
            tag.setTag(SETTING_BUTTON + "_" + tab + "_" + buttonName, button);
        } else {
            tag.removeTag(SETTING_TAB);
        }
    }
}
