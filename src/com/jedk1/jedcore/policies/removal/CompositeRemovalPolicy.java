package com.jedk1.jedcore.policies.removal;

import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CompositeRemovalPolicy implements RemovalPolicy {
    private List<RemovalPolicy> policies = new ArrayList<>();
    private CoreAbility ability;

    public CompositeRemovalPolicy(CoreAbility ability, List<RemovalPolicy> policies) {
        this.policies = policies;
        this.ability = ability;
    }

    public CompositeRemovalPolicy(CoreAbility ability, RemovalPolicy... policies) {
        // Create as an ArrayList instead of fixed-sized so policies can be added/removed.
        this.policies = new ArrayList<>(Arrays.asList(policies));
        this.ability = ability;
    }

    @Override
    public boolean shouldRemove() {
        if (policies.isEmpty()) return false;

        for (RemovalPolicy policy : policies) {
            if (policy.shouldRemove()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void load(ConfigurationSection config) {
        if (this.policies.isEmpty()) return;

        String element = ability.getElement().getName();
        String abilityName = ability.getName();

        String pathPrefix = "Abilities." + element + "." + abilityName + ".RemovalPolicy.";

        // Load the configuration section for each policy and pass it to the load method.
        for (Iterator<RemovalPolicy> iterator = policies.iterator(); iterator.hasNext(); ) {
            RemovalPolicy policy = iterator.next();
            ConfigurationSection section = config.getConfigurationSection(pathPrefix + policy.getName());

            if (section != null) {
                boolean enabled = section.getBoolean("Enabled", true);

                if (!enabled) {
                    iterator.remove();
                    continue;
                }

                policy.load(section);
            }
        }
    }

    public void addPolicy(RemovalPolicy policy) {
        this.policies.add(policy);
    }

    public void removePolicyType(Class<? extends RemovalPolicy> type) {
        policies.removeIf((policy) -> type.isAssignableFrom(policy.getClass()));
    }

    @Override
    public String getName() {
        return "Composite";
    }
}
