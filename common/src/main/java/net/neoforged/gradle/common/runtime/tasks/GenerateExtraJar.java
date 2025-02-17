package net.neoforged.gradle.common.runtime.tasks;

import net.neoforged.gradle.common.services.caching.CachedExecutionService;
import net.neoforged.gradle.common.services.caching.jobs.ICacheableJob;
import net.neoforged.gradle.dsl.common.tasks.NeoGradleBase;
import net.neoforged.gradle.dsl.common.tasks.WithOutput;
import net.neoforged.gradle.dsl.common.tasks.WithWorkspace;
import net.neoforged.gradle.util.ZipBuildingFileTreeVisitor;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.services.ServiceReference;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipOutputStream;

@CacheableTask
public abstract class GenerateExtraJar extends NeoGradleBase implements WithOutput, WithWorkspace {

    public GenerateExtraJar() {
        super();
        getOutputFileName().set("client-extra.jar");
    }

    @ServiceReference(CachedExecutionService.NAME)
    public abstract Property<CachedExecutionService> getCacheService();

    @TaskAction
    public void run() throws Throwable {
        getCacheService().get()
                        .cached(
                                this,
                                ICacheableJob.Default.file(getOutput(), this::doRun)
                        ).execute();
    }

    private void doRun() throws Exception {
        final File originalJar = getOriginalJar().get().getAsFile();
        final File outputJar = ensureFileWorkspaceReady(getOutput());


        final FileTree inputTree = getArchiveOperations().zipTree(originalJar);
        final FileTree filteredInput = inputTree.matching(filter -> {
            filter.exclude("**/*.class");
        });

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputJar))) {
            filteredInput.visit(new ZipBuildingFileTreeVisitor(zos));
        }
    }

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getOriginalJar();
}
