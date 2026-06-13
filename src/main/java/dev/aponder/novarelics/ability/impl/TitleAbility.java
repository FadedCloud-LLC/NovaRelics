package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import dev.aponder.novarelics.util.MiniMsg;

public class TitleAbility extends Ability {

    public TitleAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        String titleStr = config.getString("title", "");
        String subtitleStr = config.getString("subtitle", "");
        int fadeIn = config.getInt("fade-in", 10);
        int stay = config.getInt("stay", 70);
        int fadeOut = config.getInt("fade-out", 20);

        context.getPlayer().sendTitle(
                titleStr.isEmpty() ? "" : MiniMsg.toLegacy(titleStr),
                subtitleStr.isEmpty() ? "" : MiniMsg.toLegacy(subtitleStr),
                fadeIn, stay, fadeOut
        );
        return true;
    }

    @Override
    public String getName() { return "Title"; }

    @Override
    public String getDescription() { return "Displays a title and subtitle to the player."; }
}
