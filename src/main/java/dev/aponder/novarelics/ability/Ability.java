package dev.aponder.novarelics.ability;

import dev.aponder.novarelics.NovaRelics;

/**
 * Base class for all NovaRelics abilities.
 * Implementations must be stateless — all state comes from AbilityConfig and AbilityContext.
 */
public abstract class Ability {

    protected final NovaRelics plugin;

    protected Ability(NovaRelics plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when the ability's trigger fires with the relic active.
     *
     * @param config  Configuration section for this specific ability invocation.
     * @param context Runtime context (player, relic, event, extra data).
     * @return true if the ability executed successfully; false to cancel the trigger chain.
     */
    public abstract boolean execute(AbilityConfig config, AbilityContext context);

    /**
     * Optional prepare phase — called before execute for two-phase abilities (e.g., ANVIL).
     * Override to modify the game state before the player confirms the action.
     *
     * @return true if preparation succeeded and execution should be allowed.
     */
    public boolean prepare(AbilityConfig config, AbilityContext context) {
        return true;
    }

    /**
     * Human-readable name shown in the editor.
     */
    public abstract String getName();

    /**
     * Brief description shown in the editor.
     */
    public abstract String getDescription();
}
