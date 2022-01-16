package org.cyclops.integratedterminals.proxy;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.client.key.IKeyRegistry;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.inventory.PlayerInventoryIterator;
import org.cyclops.cyclopscore.proxy.ClientProxyComponent;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.item.ItemTerminalStoragePortable;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientItemOpenGenericPacket;
import org.lwjgl.glfw.GLFW;

/**
 * Proxy for the client side.
 * 
 * @author rubensworks
 * 
 */
public class ClientProxy extends ClientProxyComponent {

	private static final String KEYBINDING_CATEGORY_NAME = "key.categories." + Reference.MOD_ID;

	public static final KeyMapping TERMINAL_TAB_NEXT = new KeyMapping(
			"key." + Reference.MOD_ID + ".terminal.tab.next",
			KeyConflictContext.GUI, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_TAB,
			KEYBINDING_CATEGORY_NAME);
	public static final KeyMapping TERMINAL_TAB_PREVIOUS = new KeyMapping(
			"key." + Reference.MOD_ID + ".terminal.tab.previous",
			KeyConflictContext.GUI, KeyModifier.SHIFT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_TAB,
			KEYBINDING_CATEGORY_NAME);
	public static final KeyMapping TERMINAL_CRAFTINGGRID_CLEARPLAYER = new KeyMapping(
			"key." + Reference.MOD_ID + ".terminal.craftinggrid.clearplayer",
			KeyConflictContext.GUI, KeyModifier.SHIFT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C,
			KEYBINDING_CATEGORY_NAME);
	public static final KeyMapping TERMINAL_CRAFTINGGRID_CLEARSTORAGE = new KeyMapping(
			"key." + Reference.MOD_ID + ".terminal.craftinggrid.clearstorage",
			KeyConflictContext.GUI, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C,
			KEYBINDING_CATEGORY_NAME);
	public static final KeyMapping TERMINAL_CRAFTINGGRID_BALANCE = new KeyMapping(
			"key." + Reference.MOD_ID + ".terminal.craftinggrid.balance",
			KeyConflictContext.GUI, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B,
			KEYBINDING_CATEGORY_NAME);
	public static final KeyMapping TERMINAL_STORAGE_PORTABLE_OPEN = new KeyMapping(
			"key." + Reference.MOD_ID + ".terminal.portable.open",
			KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C,
			KEYBINDING_CATEGORY_NAME);

	public ClientProxy() {
		super(new CommonProxy());
	}

	@Override
	public ModBase getMod() {
		return IntegratedTerminals._instance;
	}

	@Override
	public void registerKeyBindings(IKeyRegistry keyRegistry) {
		ClientRegistry.registerKeyBinding(TERMINAL_TAB_NEXT);
		ClientRegistry.registerKeyBinding(TERMINAL_TAB_PREVIOUS);
		ClientRegistry.registerKeyBinding(TERMINAL_CRAFTINGGRID_CLEARPLAYER);
		ClientRegistry.registerKeyBinding(TERMINAL_CRAFTINGGRID_CLEARSTORAGE);
		ClientRegistry.registerKeyBinding(TERMINAL_CRAFTINGGRID_BALANCE);
		ClientRegistry.registerKeyBinding(TERMINAL_STORAGE_PORTABLE_OPEN);

		keyRegistry.addKeyHandler(TERMINAL_STORAGE_PORTABLE_OPEN, (kb) -> {
			LocalPlayer player = Minecraft.getInstance().player;
			Pair<InteractionHand, Integer> found = null;
			PlayerInventoryIterator it = new PlayerInventoryIterator(player);
			while (it.hasNext() && found == null) {
				Pair<Integer, ItemStack> pair = it.nextIndexed();
				if (pair.getRight() != null && pair.getRight().getItem() instanceof ItemTerminalStoragePortable) {
					found = Pair.of(InteractionHand.MAIN_HAND, pair.getLeft());
				}
			}
			if(found == null) {
				if (player.getOffhandItem().getItem() instanceof ItemTerminalStoragePortable) {
					found = Pair.of(InteractionHand.OFF_HAND, 0);
				}
			}
			if(found != null) {
				TerminalStorageIngredientItemOpenGenericPacket.send(found);
			}
		});
	}

}
