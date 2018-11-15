package org.cyclops.integratedterminals.core.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.client.gui.GuiHandler;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.cyclops.integrateddynamics.core.client.gui.ExtendedGuiHandler.getPartConstructionData;

/**
 * An extension of the default cyclops gui handler with support for some more gui types.
 * @author rubensworks
 */
public class ExtendedGuiHandler extends GuiHandler {
    /**
     * Gui type for part aspects
     */
    public static final GuiType<Pair<EnumFacing, CraftingOptionGuiData<?, ?>>> CRAFTING_OPTION = GuiType.create(true);
    static {
        CRAFTING_OPTION.setContainerConstructor((id, player, world, x, y, z, containerClass, dataIn) -> {
            try {
                Triple<IPartContainer, PartTypeBase, PartTarget> data = getPartConstructionData(world,
                        new BlockPos(x, y, z), dataIn.getLeft());
                if(data == null) return null;
                Constructor<? extends Container> containerConstructor = containerClass.getConstructor(
                            EntityPlayer.class, PartTarget.class, IPartContainer.class,
                            IPartType.class, CraftingOptionGuiData.class);
                return containerConstructor.newInstance(player, data.getRight(), data.getLeft(), data.getMiddle(), dataIn.getRight());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException e) {
                e.printStackTrace();
            }
            return null;
        });
        if(MinecraftHelpers.isClientSide()) {
            CRAFTING_OPTION.setGuiConstructor((id, player, world, x, y, z, guiClass, dataIn) -> {
                try {
                    Triple<IPartContainer, PartTypeBase, PartTarget> data = getPartConstructionData(world,
                            new BlockPos(x, y, z), dataIn.getLeft());
                    if(data == null) return null;
                    Constructor<? extends GuiScreen> guiConstructor = guiClass.getConstructor(
                                EntityPlayer.class, PartTarget.class, IPartContainer.class,
                                IPartType.class, CraftingOptionGuiData.class);
                    return guiConstructor.newInstance(player, data.getRight(), data.getLeft(), data.getMiddle(), dataIn.getRight());
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
                return null;
            });
        }
    }

    public ExtendedGuiHandler(ModBase mod) {
        super(mod);
    }
}
