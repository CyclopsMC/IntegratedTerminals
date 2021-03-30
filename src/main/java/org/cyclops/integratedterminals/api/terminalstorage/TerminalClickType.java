package org.cyclops.integratedterminals.api.terminalstorage;

/**
 * @author rubensworks
 */
public enum TerminalClickType {
    /**
     * Max movement from hovered storage ingredient to player.
     */
    STORAGE_QUICK_MOVE,
    /**
     * Incremental movement from hovered storage ingredient to player.
     */
    STORAGE_QUICK_MOVE_INCREMENTAL,
    /**
     * Movement from active storage ingredient into the world.
     */
    STORAGE_PLACE_WORLD,
    /**
     * Movement from active storage ingredient into a player slot.
     */
    STORAGE_PLACE_PLAYER,
    /**
     * Movement from active player stack into storage.
     */
    PLAYER_PLACE_STORAGE,
    /**
     * Max movement from hovered player stack into storage.
     */
    PLAYER_QUICK_MOVE,
    /**
     * Incremental movement from hovered player stack into storage.
     */
    PLAYER_QUICK_MOVE_INCREMENTAL
}
