package org.cyclops.integratedterminals.advancement.criterion;

import org.cyclops.cyclopscore.config.extendedconfig.CriterionTriggerConfigCommon;
import org.cyclops.integratedterminals.IntegratedTerminals;

/**
 * Config for {@link TerminalStorageEnderUpgradedTrigger}.
 * @author rubensworks
 */
public class TerminalStorageEnderUpgradedTriggerConfig extends CriterionTriggerConfigCommon<TerminalStorageEnderUpgradedTrigger.Instance, IntegratedTerminals> {

    public TerminalStorageEnderUpgradedTriggerConfig() {
        super(
                IntegratedTerminals._instance,
                "terminal_storage_ender_upgraded",
                new TerminalStorageEnderUpgradedTrigger()
        );
    }

}
