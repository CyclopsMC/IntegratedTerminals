package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;

import javax.annotation.Nullable;

/**
 * @author rubensworks
 */
public class TerminalStorageState {

    public static final String SETTING_TAB = "tab";
    public static final String SETTING_SEARCH = "search";
    public static final String SETTING_BUTTON = "button";

    private CompoundNBT tag;
    private IDirtyMarkListener dirtyMarkListener;

    public TerminalStorageState(IDirtyMarkListener dirtyMarkListener) {
        this(new CompoundNBT(), dirtyMarkListener);
    }

    public TerminalStorageState(CompoundNBT tag, IDirtyMarkListener dirtyMarkListener) {
        this.tag = tag;
        this.dirtyMarkListener = dirtyMarkListener;
    }

    public void setDirtyMarkListener(IDirtyMarkListener dirtyMarkListener) {
        this.dirtyMarkListener = dirtyMarkListener;
    }

    protected void markDirty() {
        this.dirtyMarkListener.onDirty();
    }

    public CompoundNBT getTag() {
        return tag;
    }

    public void setTag(CompoundNBT tag) {
        this.tag = tag;
        this.markDirty();
    }

    public String getTab() {
        return tag.getString(SETTING_TAB);
    }

    public boolean hasTab() {
        return tag.contains(SETTING_TAB, Constants.NBT.TAG_STRING);
    }

    public void setTab(@Nullable String tab) {
        if (tab != null) {
            tag.putString(SETTING_TAB, tab);
        } else {
            tag.remove(SETTING_TAB);
        }
        this.markDirty();
    }

    public String getSearch(String tab, int channel) {
        return tag.getString(SETTING_SEARCH + "_" + tab  + "_" + channel);
    }

    public boolean hasSearch(String tab, int channel) {
        return tag.contains(SETTING_SEARCH + "_" + tab  + "_" + channel, Constants.NBT.TAG_STRING);
    }

    public void setSearch(String tab, int channel, @Nullable String search) {
        if (tab != null) {
            tag.putString(SETTING_SEARCH + "_" + tab  + "_" + channel, search);
        } else {
            tag.remove(SETTING_SEARCH + "_" + tab  + "_" + channel);
        }
        this.markDirty();
    }

    public INBT getButton(String tab, String buttonName) {
        return tag.get(SETTING_BUTTON + "_" + tab + "_" + buttonName);
    }

    public boolean hasButton(String tab, String buttonName) {
        return tag.contains(SETTING_BUTTON + "_" + tab + "_" + buttonName);
    }

    public void setButton(String tab, String buttonName, @Nullable INBT button) {
        if (button != null) {
            tag.put(SETTING_BUTTON + "_" + tab + "_" + buttonName, button);
        } else {
            tag.remove(SETTING_TAB);
        }
        this.markDirty();
    }

    public void writeToPacketBuffer(PacketBuffer packetBuffer) {
        packetBuffer.writeCompoundTag(tag);
    }

    public static TerminalStorageState readFromPacketBuffer(PacketBuffer packetBuffer) {
        return new TerminalStorageState(packetBuffer.readCompoundTag(), () -> {});
    }
}
