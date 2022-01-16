package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integratedterminals.Reference;

import javax.annotation.Nullable;

/**
 * @author rubensworks
 */
public class TerminalStorageState {

    public static final String SETTING_TAB = "tab";
    public static final String SETTING_SEARCH = "search";
    public static final String SETTING_BUTTON = "button";

    public static final String PLAYER_TAG_DEFAULT_KEY = Reference.MOD_ID + ":terminalStorageStateDefault";

    private CompoundTag tag;
    private IDirtyMarkListener dirtyMarkListener;

    public TerminalStorageState(IDirtyMarkListener dirtyMarkListener) {
        this(new CompoundTag(), dirtyMarkListener);
    }

    public TerminalStorageState(CompoundTag tag, IDirtyMarkListener dirtyMarkListener) {
        this.tag = tag;
        this.dirtyMarkListener = dirtyMarkListener;
    }

    public void setDirtyMarkListener(IDirtyMarkListener dirtyMarkListener) {
        this.dirtyMarkListener = dirtyMarkListener;
    }

    protected void markDirty() {
        this.dirtyMarkListener.onDirty();
    }

    public CompoundTag getTag() {
        return tag;
    }

    public void setTag(CompoundTag tag) {
        this.tag = tag;
        this.markDirty();
    }

    public String getTab() {
        return tag.getString(SETTING_TAB);
    }

    public boolean hasTab() {
        return tag.contains(SETTING_TAB, Tag.TAG_STRING);
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
        return tag.contains(SETTING_SEARCH + "_" + tab  + "_" + channel, Tag.TAG_STRING);
    }

    public void setSearch(String tab, int channel, @Nullable String search) {
        if (tab != null) {
            tag.putString(SETTING_SEARCH + "_" + tab  + "_" + channel, search);
        } else {
            tag.remove(SETTING_SEARCH + "_" + tab  + "_" + channel);
        }
        this.markDirty();
    }

    public Tag getButton(String tab, String buttonName) {
        return tag.get(SETTING_BUTTON + "_" + tab + "_" + buttonName);
    }

    public boolean hasButton(String tab, String buttonName) {
        return tag.contains(SETTING_BUTTON + "_" + tab + "_" + buttonName);
    }

    public void setButton(String tab, String buttonName, @Nullable Tag button) {
        if (button != null) {
            tag.put(SETTING_BUTTON + "_" + tab + "_" + buttonName, button);
        } else {
            tag.remove(SETTING_TAB);
        }
        this.markDirty();
    }

    public void writeToPacketBuffer(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeNbt(tag);
    }

    public static TerminalStorageState readFromPacketBuffer(FriendlyByteBuf packetBuffer) {
        return new TerminalStorageState(packetBuffer.readNbt(), () -> {});
    }

    public static void setPlayerDefault(Player playerEntity, TerminalStorageState state) {
        playerEntity.getPersistentData().put(TerminalStorageState.PLAYER_TAG_DEFAULT_KEY, state.getTag().copy());
    }

    public static TerminalStorageState getPlayerDefault(Player playerEntity, IDirtyMarkListener dirtyMarkListener) {
        if (playerEntity.getPersistentData().contains(TerminalStorageState.PLAYER_TAG_DEFAULT_KEY)) {
            return new TerminalStorageState(playerEntity.getPersistentData().getCompound(TerminalStorageState.PLAYER_TAG_DEFAULT_KEY), dirtyMarkListener);
        }
        return new TerminalStorageState(dirtyMarkListener);
    }
}
