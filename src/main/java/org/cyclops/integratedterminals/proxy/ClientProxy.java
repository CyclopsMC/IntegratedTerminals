package org.cyclops.integratedterminals.proxy;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.proxy.ClientProxyComponent;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.lwjgl.input.Keyboard;

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
			KeyConflictContext.GUI, Keyboard.KEY_TAB,
			KEYBINDING_CATEGORY_NAME);
	public static final KeyBinding TERMINAL_TAB_PREVIOUS = new KeyBinding(
			"key." + Reference.MOD_ID + ".terminal.tab.previous",
			KeyConflictContext.GUI, KeyModifier.SHIFT, Keyboard.KEY_TAB,
			KEYBINDING_CATEGORY_NAME);

	public ClientProxy() {
		super(new CommonProxy());
	}

	@Override
	public ModBase getMod() {
		return IntegratedTerminals._instance;
	}

}
