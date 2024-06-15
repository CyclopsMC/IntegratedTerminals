package org.cyclops.integratedterminals.api.terminalstorage.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;
import org.apache.commons.lang3.tuple.Pair;

/**
 * An event that is emitted on the Forge event bus to determine the width and height of the terminal storage screen.
 * @author rubensworks
 */
public class TerminalStorageScreenSizeEvent extends Event {

    private int width;
    private int height;

    public TerminalStorageScreenSizeEvent(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public static Pair<Integer, Integer> getWidthHeight() {
        Screen screen = Minecraft.getInstance().screen;
        TerminalStorageScreenSizeEvent event = new TerminalStorageScreenSizeEvent(screen.width, screen.height);
        NeoForge.EVENT_BUS.post(event);
        return Pair.of(event.getWidth(), event.getHeight());
    }
}
