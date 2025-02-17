package net.neoforged.gradle.dsl.common.extensions.dependency.replacement

import groovy.transform.CompileStatic
import net.minecraftforge.gdi.annotations.DefaultMethods
import net.neoforged.gradle.dsl.common.tasks.WithOutput
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.tasks.TaskProvider

/**
 * Defines an object that is aware of dynamic dependency replacement.
 */
@CompileStatic
@DefaultMethods
interface ReplacementAware {

    /**
     * Called when tasks are created for dependency replacement.
     *
     * @param copiesRawJar The task that copies the raw jar.
     * @param copiesMappedJar The task that copies the mapped jar.
     */
    void onTasksCreated(
            TaskProvider<? extends WithOutput> copiesRawJar,
            TaskProvider<? extends WithOutput> copiesMappedJar
    );

    /**
     * Gets the replacement dependency for the given dependency.
     *
     * @param externalModuleDependency The dependency to get the replacement for.
     * @return The replacement dependency.
     */
    ExternalModuleDependency getReplacementDependency(ExternalModuleDependency externalModuleDependency)

    /**
     * Invoked when a dependency that is targeted by this replacement has been added to a configuration.
     * Note: This might be invoked lazily when a provider based dependency is added to a configuration, and the
     * configuration is about to be resolved.
     */
    default void onTargetDependencyAdded() {}
}
