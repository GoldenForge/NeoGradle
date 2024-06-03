package net.neoforged.gradle.dsl.vanilla.runtime.spec;

import groovy.transform.CompileStatic;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import net.neoforged.gradle.dsl.common.runtime.spec.LegacySpecification;

/**
 * Defines a specification for a vanilla runtime.
 */
@CompileStatic
public interface VanillaSpecification extends LegacySpecification {

    /**
     * Gets the version of FART to use.
     *
     * @return The version of FART to use.
     */
    @NotNull
    String getFartVersion();

    /**
     * Gets the version of ForgeFlower to use.
     *
     * @return The version of ForgeFlower to use.
     */
    @NotNull
    String getForgeFlowerVersion();

    /**
     * Gets the version of AccessTransformerApplier to use.
     *
     * @return The version of AccessTransformerApplier to use.
     */
    @NotNull
    String getAccessTransformerApplierVersion();
}
