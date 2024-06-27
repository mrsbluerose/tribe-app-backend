package com.savvato.tribeapp.services;

import com.savvato.tribeapp.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PictureServiceImpl implements PictureService {

    @Autowired
    ResourceTypeService resourceTypeService;

    @Override
    public void writeThumbnailFromOriginal(String resourceType, String filename) throws IOException {
        String dir = resourceTypeService.getDirectoryForResourceType(resourceType);

        boolean done = false;
        int retryCount = 0;
        long delay = 2;

        while (!done && retryCount++ < 3) {
            log.debug("Sleeping.... " + delay);
            try {
                TimeUnit.SECONDS.sleep(delay);
            } catch (InterruptedException ie) {

            }
            log.debug("Woke up!");

            delay *= 2;

            try {
                log.debug("Just about to get the file " + dir + File.separator + filename);
                FileSystemResource fileSystemResource = getFileSystemResource(dir + File.separator + filename);
                InputStream is = getInputStreamFromFileSystemResource(fileSystemResource);

                BufferedImage originalImage = readImage(is);

                if (originalImage != null) {

                    log.debug("YES! Found the file.. doing the resize... " + dir + File.separator + filename);
                    BufferedImage resizedImage = resizeImage(originalImage, 250, 250);

                    File out = createFile(dir + File.separator + filename + "_thumbnail");
                    ImageIO.write(resizedImage, "jpg", out);

                    done = true;
                } else {
                    log.debug("nooo... the file wasn't there yet."+ dir + File.separator + filename);
                }
            } catch (IOException ioe) {
                throw new IOException("Expected the file " + dir + File.separator + filename + " to be in place, but we got this exception instead!.", ioe);
            }
        }

        if (!done) {
            throw new IOException("Tried waiting for the file to appear, but there was nothing there to thumbnail-ize.");
        }
    }

    @Override
    public InputStream getInputStreamFromFileSystemResource(FileSystemResource fileSystemResource) throws IOException {
        return fileSystemResource.getInputStream();
    }

    @Override
    public FileSystemResource getFileSystemResource(String path) {
        return new FileSystemResource(path);
    }

    @Override
    public BufferedImage readImage(InputStream inputStream) throws IOException {
        return ImageIO.read(inputStream);
    }

    @Override
    public File createFile(String path) {
        return new File(path);
    }

    @Override
    public String transformFilenameUsingSizeInfo(String photoSize, String filename) {
        if (photoSize.equals(Constants.PHOTO_SIZE_THUMBNAIL))
            return filename + "_thumbnail";
        else
            return filename; // original
    }

    @Override
    public BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        return outputImage;
    }
}
