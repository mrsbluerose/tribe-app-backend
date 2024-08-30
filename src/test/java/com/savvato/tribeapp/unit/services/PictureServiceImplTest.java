package com.savvato.tribeapp.unit.services;

import com.savvato.tribeapp.services.PictureService;
import com.savvato.tribeapp.services.PictureServiceImpl;
import com.savvato.tribeapp.services.ResourceTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class})
public class PictureServiceImplTest {

    @TestConfiguration
    static class PictureServiceImplTestContextConfiguration {

        @Bean
        public PictureService pictureService() {
            return new PictureServiceImpl() ;
        }
    }

    @Autowired
    PictureService pictureService;

    @MockBean
    private ResourceTypeService resourceTypeService;

    @Test
    public void testWriteThumbnailFromOriginal_Success() throws IOException {
        String resourceType = "testType";
        String filename = "test.jpg";
        String directory = "testDir";

        when(resourceTypeService.getDirectoryForResourceType(resourceType)).thenReturn(directory);

        PictureService pictureServiceSpy = spy(pictureService);

        FileSystemResource mockFileSystemResource = mock(FileSystemResource.class);
        doReturn(mockFileSystemResource).when(pictureServiceSpy).getFileSystemResource(anyString());

        InputStream mockIs = new ByteArrayInputStream(new byte[0]);
        doReturn(mockIs).when(pictureServiceSpy).getInputStreamFromFileSystemResource(any(FileSystemResource.class));

        BufferedImage mockOriginalImage = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
        BufferedImage mockThumbnailImage = new BufferedImage(250, 250, BufferedImage.TYPE_INT_RGB);

        try (var mockedStatic = mockStatic(ImageIO.class)) {
            mockedStatic.when(() -> ImageIO.read(any(InputStream.class))).thenReturn(mockOriginalImage);

            doReturn(mockThumbnailImage).when(pictureServiceSpy).resizeImage(any(BufferedImage.class), anyInt(), anyInt());

            doReturn(File.createTempFile("test", ".jpg")).when(pictureServiceSpy).createFile(anyString());

            pictureServiceSpy.writeThumbnailFromOriginal(resourceType, filename);
        }

        verify(resourceTypeService, times(1)).getDirectoryForResourceType(resourceType);
        verify(pictureServiceSpy, times(1)).getFileSystemResource(anyString());
        verify(pictureServiceSpy, times(1)).getInputStreamFromFileSystemResource(any(FileSystemResource.class));
        verify(pictureServiceSpy, times(1)).readImage(any(InputStream.class));
        verify(pictureServiceSpy, times(1)).resizeImage(any(BufferedImage.class), anyInt(), anyInt());
        verify(pictureServiceSpy, times(1)).createFile(anyString());
    }

    @Test
    public void testWriteThumbnailFromOriginal_FileNotFound() throws IOException {
        String resourceType = "testType";
        String filename = "test.jpg";
        String directory = "testDir";

        when(resourceTypeService.getDirectoryForResourceType(resourceType)).thenReturn(directory);

        PictureService pictureServiceSpy = spy(pictureService);

        // Mock the FileSystemResource to throw an IOException
        FileSystemResource fileSystemResource = mock(FileSystemResource.class);
        when(fileSystemResource.getInputStream()).thenThrow(new IOException("File not found"));

        // Mock the getFileSystemResource method to return the mocked FileSystemResource
        doReturn(fileSystemResource).when(pictureServiceSpy).getFileSystemResource(anyString());

        assertThrows(IOException.class, () -> {
            pictureServiceSpy.writeThumbnailFromOriginal(resourceType, filename);
        });

        verify(resourceTypeService, times(1)).getDirectoryForResourceType(resourceType);
    }

    @Test
    public void testWriteThumbnailFromOriginal_Timeout() throws IOException {
        String resourceType = "testType";
        String filename = "test.jpg";
        String directory = "testDir";

        when(resourceTypeService.getDirectoryForResourceType(resourceType)).thenReturn(directory);

        PictureService pictureServiceSpy = spy(pictureService);

        // Mock the FileSystemResource to throw an IOException
        FileSystemResource fileSystemResource = mock(FileSystemResource.class);
        when(fileSystemResource.getInputStream()).thenThrow(new IOException("File not found"));

        // Mock the getFileSystemResource method to return the mocked FileSystemResource
        doReturn(fileSystemResource).when(pictureServiceSpy).getFileSystemResource(anyString());

        assertThrows(IOException.class, () -> {
            pictureServiceSpy.writeThumbnailFromOriginal(resourceType, filename);
        });

        verify(resourceTypeService, times(1)).getDirectoryForResourceType(resourceType);
    }

    @Test
    public void testGetFileSystemResource() {
        FileSystemResource fileSystemResource = pictureService.getFileSystemResource("testPath");
        assertNotNull(fileSystemResource);
    }

    @Test
    public void testReadImage() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[0]);

        // Mock ImageIO to return a BufferedImage
        BufferedImage mockImage = new BufferedImage(250, 250, BufferedImage.TYPE_INT_RGB);
        try (var mockedStatic = mockStatic(ImageIO.class)) {
            mockedStatic.when(() -> ImageIO.read(inputStream)).thenReturn(mockImage);

            BufferedImage bufferedImage = pictureService.readImage(inputStream);
            assertNotNull(bufferedImage);
        }
    }

    @Test
    public void testCreateFile() throws IOException {
        File file = pictureService.createFile("testPath");
        assertNotNull(file);
    }

    @Test
    public void testGetInputStreamFromFileSystemResource() throws IOException {
        FileSystemResource fileSystemResource = mock(FileSystemResource.class);
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);

        when(fileSystemResource.getInputStream()).thenReturn(inputStream);

        InputStream result = pictureService.getInputStreamFromFileSystemResource(fileSystemResource);
        assertNotNull(result);
    }

}
