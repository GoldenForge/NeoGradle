package net.minecraftforge.gradle.mcp.util;

import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZipBuildingFileTreeVisitor implements FileVisitor {

    private final ZipOutputStream outputZipStream;

    public ZipBuildingFileTreeVisitor(ZipOutputStream outputZipStream) {
        this.outputZipStream = outputZipStream;
    }

    @Override
    public void visitDir(FileVisitDetails fileVisitDetails) {
        try {
            final ZipEntry directoryEntry = new ZipEntry(fileVisitDetails.getRelativePath().getPathString() + "/");
            outputZipStream.putNextEntry(directoryEntry);
            outputZipStream.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException("Could not create zip directory: " + fileVisitDetails.getRelativePath().getPathString(), e);
        }
    }

    @Override
    public void visitFile(FileVisitDetails fileVisitDetails) {
        try {
            final ZipEntry fileEntry = new ZipEntry(fileVisitDetails.getRelativePath().getPathString());
            outputZipStream.putNextEntry(fileEntry);
            fileVisitDetails.copyTo(outputZipStream);
            outputZipStream.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException("Could not create zip file: " + fileVisitDetails.getRelativePath().getPathString(), e);
        }
    }
}
