package net.minecraftforge.gradle.tasks.user.reobf;

import groovy.lang.Closure;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Date;

import net.md_5.specialsource.Jar;
import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.JarRemapper;
import net.md_5.specialsource.provider.ClassLoaderProvider;
import net.md_5.specialsource.provider.JarProvider;
import net.md_5.specialsource.provider.JointProvider;
import net.minecraftforge.gradle.delayed.DelayedFile;
import net.minecraftforge.gradle.tasks.dev.ObfuscateTask;
import net.minecraftforge.gradle.user.UserConstants;

import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Task;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.artifacts.publish.AbstractPublishArtifact;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import com.google.common.io.Files;

public class ObfArtifact extends AbstractPublishArtifact
{
    Object                toObfArtifact;

    private String                name;
    private String                extension;
    private String                classifier;
    private Date                  date;
    private File                  file;
    private FileCollection        classpath;

    @SuppressWarnings("unused")
    private String                type;

    private final Closure<Object> toObfGenerator;
    private final Task            caller;

    final ArtifactSpec            outputSpec;

    /**
     * Creates an obfuscated artifact for the given public artifact.
     * <p>
     * The file to obfuscate will be the file of the given artifact and the name of this obfuscated artifact will default to the name of the given artifact to obfuscate.
     * </p>
     * <p>
     * The artifact to obfuscate may change after being used as the source.
     * </p>
     * @param toObf The artifact that is to be obfuscated
     * @param artifactSpec The specification of how the obfuscated artifact is to be named
     * @param task The task(s) that will invoke {@link #generate()} on this jar (optional)
     */
    public ObfArtifact(AbstractArchiveTask toObf, ArtifactSpec artifactSpec, ReobfTask task)
    {
        this(new DelayedThingy(toObf), artifactSpec, task);
        this.toObfArtifact = (PublishArtifact) toObf;
    }

    /**
     * Creates an obfuscated artifact for the given public artifact.
     * <p>
     * The file to obfuscate will be the file of the given artifact and the name of this obfuscated artifact will default to the name of the given artifact to obfuscate.
     * </p>
     * <p>
     * The artifact to obfuscate may change after being used as the source.
     * </p>
     * @param toObf The artifact that is to be obfuscated
     * @param artifactSpec The specification of how the obfuscated artifact is to be named
     * @param task The task(s) that will invoke {@link #generate()} on this jar (optional)
     */
    public ObfArtifact(PublishArtifact toObf, ArtifactSpec artifactSpec, ReobfTask task)
    {
        this(new DelayedThingy(toObf), artifactSpec, task);
        this.toObfArtifact = toObf;
    }

    /**
     * Creates an obfuscated artifact for the given file.
     * @param toObf The file that is to be obfuscated
     * @param artifactSpec The specification of how the obfuscated artifact is to be named
     * @param task The task(s) that will invoke {@link #generate()} on this jar (optional)
     */
    public ObfArtifact(File toObf, ArtifactSpec artifactSpec, ReobfTask task)
    {
        this(new DelayedThingy(toObf), artifactSpec, task);
        this.toObfArtifact = toObf;
    }

    /**
     * Creates an obfuscated artifact for the file returned by the {@code toObf} closure.
     * <p>
     * The closures will be evaluated on demand whenever the value is needed (e.g. at generation time)
     * </p>
     * @param toObf A closure that produces a File for the object to obfuscate (non File return values will be used as the path to the file)
     * @param outputSpec The specification of artifact to outputted
     * @param task The task(s) that will invoke {@link #generate()} on this jar (optional)
     */
    public ObfArtifact(Closure<Object> toObf, ArtifactSpec outputSpec, ReobfTask task)
    {
        super(task);
        this.caller = task;
        toObfGenerator = toObf;
        this.outputSpec = outputSpec;
    }

    /**
     * The file that is to be obfuscated.
     * @return The file. May be {@code null} if unknown at this time.
     */
    public File getToObf()
    {
        Object toObf = null;
        if (toObfGenerator != null)
            toObf = toObfGenerator.call();

        if (toObf == null)
            return null;
        else if (toObf instanceof File)
            return (File) toObf;
        else
            return new File(toObf.toString());
    }

    /**
     * The name of the obfuscated artifact.
     * <p>
     * Defaults to the name of the obfuscated artifact {@link #getFile() file}.
     * @return The name. May be {@code null} if unknown at this time.
     */
    public String getName()
    {
        if (name != null)
            return name;
        else if (toObfArtifact != null)
            return ((File) toObfArtifact).getName();
        else if (outputSpec.getBaseName() != null)
            return outputSpec.getBaseName().toString();
        else
            return getFile() == null ? null : getFile().getName();
    }

    /**
     * The name of the obfuscated artifact.
     * <p>
     * Defaults to the name of the obfuscated artifact {@link #getFile() file}.
     * @return The name. May be {@code null} if unknown at this time.
     */
    public FileCollection getClasspath()
    {
        if (classpath != null)
            return classpath;
        else if (outputSpec.getClasspath() != null)
            return (FileCollection) outputSpec.getClasspath();
        else
            return null;
    }

    /**
     * The extension of the obfuscated artifact.
     * <p>
     * Defaults to '.jar'.
     * </p>
     * @return The extension. May be {@code null} if unknown at this time.
     */
    public String getExtension()
    {
        if (extension != null)
            return extension;
        else if (toObfArtifact != null)
            return ((PublishArtifact) toObfArtifact).getExtension();
        else if (outputSpec.getExtension() != null)
            return outputSpec.getExtension().toString();
        else
            return Files.getFileExtension(getFile() == null ? null : getFile().getName());
    }

    public String getType()
    {
        return getExtension();
    }

    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * The classifier of the obfuscated artifact.
     * <p>
     * Defaults to the classifier of the source artifact (if obfuscating an artifact) or the given classifier at construction (if given).
     * </p>
     * @return The classifier. May be {@code null} if unknown at this time.
     */
    public String getClassifier()
    {
        if (classifier != null)
            return classifier;
        else if (toObfArtifact != null)
            return ((PublishArtifact) toObfArtifact).getClassifier();
        else if (outputSpec.getClassifier() != null)
            return outputSpec.getClassifier().toString();
        else
            return null;
    }

    /**
     * The date of the obfuscated artifact.
     * <p>
     * Defaults to the last modified time of the {@link #getFile() obfuscated file} (if exists)
     * </p>
     * @return The date of the obfuscation. May be {@code null} if unknown at this time.
     */
    public Date getDate()
    {
        if (date == null)
        {
            File file = getFile();
            if (file == null)
                return null;
            else
            {
                long modified = file.lastModified();
                if (modified == 0)
                    return null;
                else
                    new Date(modified);
            }
        }

        return date;
    }

    /**
     * The file for the obfuscated artifact, which may not yet exist.
     * <p>
     * Defaults to a the {@link #getToObf()} () file to obfuscate}
     * </p>
     * @return The obfuscated file. May be {@code null} if unknown at this time.
     */
    public File getFile()
    {
        if (file == null)
        {
            File input = getToObf();

            outputSpec.resolve();
            this.name = outputSpec.getArchiveName().toString();
            this.classifier = outputSpec.getClassifier().toString();
            this.extension = outputSpec.getExtension().toString();
            this.classpath = (FileCollection) outputSpec.getClasspath();

            file = new File(input.getParentFile(), outputSpec.getArchiveName().toString());
            return file;
        }
        else
        {
            return file;
        }
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setExtension(String extension)
    {
        this.extension = extension;
    }

    public void setClassifier(String classifier)
    {
        this.classifier = classifier;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public void setFile(File file)
    {
        this.file = file;
    }

    public void setClasspath(FileCollection classpath)
    {
        this.classpath = classpath;
    }

    /**
     * Obfuscates the file
     * @throws IOException
     * @throws org.gradle.api.InvalidUserDataException if the there is insufficient information available to generate the signature.
     */
    void generate() throws IOException
    {
        File toObf = getToObf();
        if (toObf == null)
        {
            throw new InvalidUserDataException("Unable to obfuscate as the file to obfuscate has not been specified");
        }

        File output = getFile();

        // obfuscate here
        File inTemp = new File(caller.getTemporaryDir(), "jarIn.jar");
        Files.copy(toObf, inTemp);

        // load mapping
        JarMapping mapping = new JarMapping();
        mapping.loadMappings(new DelayedFile(caller.getProject(), UserConstants.REOBF_SRG).call());

        // make remapper
        JarRemapper remapper = new JarRemapper(null, mapping);

        // load jar
        Jar input = Jar.init(inTemp);

        // ensure that inheritance provider is used
        JointProvider inheritanceProviders = new JointProvider();
        inheritanceProviders.add(new JarProvider(input));
        if (classpath != null)
            inheritanceProviders.add(new ClassLoaderProvider(new URLClassLoader(ObfuscateTask.toUrls(classpath))));
        mapping.setFallbackInheritanceProvider(inheritanceProviders);

        // remap jar
        remapper.remapJar(input, output);
    }
}
