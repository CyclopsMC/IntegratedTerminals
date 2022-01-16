package org.cyclops.integratedterminals.core.terminalstorage;

import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTab;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabRegistry;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

/**
 * Implementation of {@link ITerminalStorageTabRegistry}.
 * @author rubensworks
 */
public class TerminalStorageTabRegistry implements ITerminalStorageTabRegistry {

    private static TerminalStorageTabRegistry INSTANCE = new TerminalStorageTabRegistry();

    private final Map<String, ITerminalStorageTab> tabs = Maps.newLinkedHashMap();

    /**
     * @return The unique instance.
     */
    public static TerminalStorageTabRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public <T extends ITerminalStorageTab> T register(T tab) {
        tabs.put(tab.getName().toString(), tab);
        return tab;
    }

    @Nullable
    @Override
    public ITerminalStorageTab getTab(ResourceLocation name) {
        return tabs.get(name.toString());
    }

    @Override
    public Collection<ITerminalStorageTab> getTabs() {
        return tabs.values();
    }
}
