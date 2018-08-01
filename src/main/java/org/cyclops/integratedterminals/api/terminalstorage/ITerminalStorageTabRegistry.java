package org.cyclops.integratedterminals.api.terminalstorage;

import net.minecraft.util.ResourceLocation;
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
     * Get a tab by unique name.
     * @param name The tab name.
     * @return The registered tab or null.
     */
    @Nullable
    public ITerminalStorageTab getTab(ResourceLocation name);

    /**
     * @return All registered tabs.
     */
    public Collection<ITerminalStorageTab> getTabs();

}
