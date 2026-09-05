package org.cyclops.integratedterminals.api.terminalstorage;

import net.minecraft.resources.Identifier;
import org.cyclops.cyclopscore.init.IRegistry;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * A registry for {@link ITerminalStorageTab}.
 * @author rubensworks
 */
public interface ITerminalStorageTabRegistry extends IRegistry {

    /**
     * Register a new tab.
     * @param tab The tab to register.
     * @param <T> The tab type.
     * @return The registered tab.
     */
    public <T extends ITerminalStorageTab> T register(T tab);

    /**
     * Register a new tab, and make sure it is placed after all currently registered tabs.
     *
     * This is useful for tabs that are registered from an event that is fired multiple times,
     * as those would otherwise end up in-between the tabs that are registered from later firings.
     *
     * @param tab The tab to register.
     * @param <T> The tab type.
     * @return The registered tab.
     */
    public default <T extends ITerminalStorageTab> T registerLast(T tab) {
        return register(tab);
    }

    /**
     * Get a tab by unique name.
     * @param name The tab name.
     * @return The registered tab or null.
     */
    @Nullable
    public ITerminalStorageTab getTab(Identifier name);

    /**
     * @return All registered tabs.
     */
    public Collection<ITerminalStorageTab> getTabs();

}
