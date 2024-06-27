package com.savvato.tribeapp.services;

import org.springframework.core.io.FileSystemResource;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface PictureService {

    InputStream getInputStreamFromFileSystemResource(FileSystemResource fileSystemResource) throws IOException;

    FileSystemResource getFileSystemResource(String path);

    BufferedImage readImage(InputStream inputStream) throws IOException;

    File createFile(String path);

    String transformFilenameUsingSizeInfo(String photoSize, String filename);
    void writeThumbnailFromOriginal(String resourceType, String filename) throws IOException;

    BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException;
}
