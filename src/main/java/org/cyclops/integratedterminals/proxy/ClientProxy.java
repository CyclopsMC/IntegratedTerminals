package org.cyclops.integratedterminals.proxy;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.cyclops.cyclopscore.client.key.IKeyRegistry;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.proxy.ClientProxyComponent;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.lwjgl.glfw.GLFW;

/**
 * Proxy for the client side.
 * 
 * @author rubensworks
 * 
 */
public class ClientProxy extends ClientProxyComponent {

	private static final String KEYBINDING_CATEGORY_NAME = "key.categories." + Reference.MOD_ID;

	public static final KeyBinding TERMINAL_TAB_NEXT = new KeyBinding(
			"key." + Reference.MOD_ID + ".terminal.tab.next",
			KeyConflictContext.GUI, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_TAB,
			KEYBINDING_CATEGORY_NAME);
	public static final KeyBinding TERMINAL_TAB_PREVIOUS = new KeyBinding(
			"key." + Reference.MOD_ID + ".terminal.tab.previous",
			KeyConflictContext.GUI, KeyModifier.SHIFT, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_TAB,
			KEYBINDING_CATEGORY_NAME);
	public static final KeyBinding TERMINAL_CRAFTINGGRID_CLEARPLAYER = new KeyBinding(
			"key." + Reference.MOD_ID + ".terminal.craftinggrid.clearplayer",
			KeyConflictContext.GUI, KeyModifier.SHIFT, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_C,
			KEYBINDING_CATEGORY_NAME);
	public static final KeyBinding TERMINAL_CRAFTINGGRID_CLEARSTORAGE = new KeyBinding(
			"key." + Reference.MOD_ID + ".terminal.craftinggrid.clearstorage",
			KeyConflictContext.GUI, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_C,
			KEYBINDING_CATEGORY_NAME);
	public static final KeyBinding TERMINAL_CRAFTINGGRID_BALANCE = new KeyBinding(
			"key." + Reference.MOD_ID + ".terminal.craftinggrid.balance",
			KeyConflictContext.GUI, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_B,
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
	}

}
