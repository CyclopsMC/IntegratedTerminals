package org.cyclops.integratedterminals.core.part;

import net.minecraft.block.Block;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.part.panel.PartTypePanel;
import org.cyclops.integratedterminals.IntegratedTerminals;

/**
 * Base part for a terminal.
 * @author rubensworks
 */
public abstract class PartTypeTerminal<P extends PartTypeTerminal<P, S>, S extends IPartState<P>> extends PartTypePanel<P, S> {

    public PartTypeTerminal(String name) {
        super(name);
    }

    @Override
    protected Block createBlock(BlockConfig blockConfig) {
        return new IgnoredBlock(blockConfig);
    }

    @Override
    public ModBase getMod() {
        return IntegratedTerminals._instance;
    }

    @Override
    public ModBase getModGui() {
        return IntegratedDynamics._instance;
    }

    @Override
    public Class<? super P> getPartTypeClass() {
        return IPartType.class;
    }

    @Override
    protected boolean hasGui() {
        return true;
    }

}
