package com.web_tutorial.javabackend.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web_tutorial.javabackend.domain.devops.DevopsPhase;
import com.web_tutorial.javabackend.exception.DevopsDataSeedingException;
import com.web_tutorial.javabackend.repository.devops.DevopsPhaseRepository;

@ExtendWith(MockitoExtension.class)
public class DevopsDataSeederTest {

    @Mock
    private DevopsPhaseRepository phaseRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private DevopsDataSeeder devopsDataSeeder = new DevopsDataSeeder(null, objectMapper);

    @Test
    void testParseAndSaveResource_SkipExisting() throws Exception {
        // Setup
        devopsDataSeeder = new DevopsDataSeeder(phaseRepository, objectMapper);
        Resource mockResource = mock(Resource.class);
        String jsonContent = "{\"slug\": \"plan\", \"title\": \"Plan Phase\", \"name\": \"Plan\"}";
        InputStream is = new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
        
        when(mockResource.getInputStream()).thenReturn(is);
        lenient().when(mockResource.getFilename()).thenReturn("plan.json");
        when(phaseRepository.existsByPhaseKey("plan")).thenReturn(true);

        // Execute
        devopsDataSeeder.parseAndSaveResource(mockResource);

        // Verify
        verify(phaseRepository).existsByPhaseKey("plan");
        verify(phaseRepository, never()).save(any(DevopsPhase.class)); // Existing data is protected
        verify(phaseRepository, never()).deleteAll(); // Destructive calls removed
    }

    @Test
    void testParseAndSaveResource_InsertNew() throws Exception {
        // Setup
        devopsDataSeeder = new DevopsDataSeeder(phaseRepository, objectMapper);
        Resource mockResource = mock(Resource.class);
        String jsonContent = "{\"slug\": \"code\", \"title\": \"Code Phase\", \"name\": \"Code\"}";
        InputStream is = new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
        
        when(mockResource.getInputStream()).thenReturn(is);
        lenient().when(mockResource.getFilename()).thenReturn("code.json");
        when(phaseRepository.existsByPhaseKey("code")).thenReturn(false);

        // Execute
        devopsDataSeeder.parseAndSaveResource(mockResource);

        // Verify
        verify(phaseRepository).existsByPhaseKey("code");
        verify(phaseRepository, times(1)).save(any(DevopsPhase.class));
        verify(phaseRepository, never()).deleteAll();
    }

    @Test
    void testParseAndSaveResource_MalformedJsonThrowsException() throws Exception {
        // Setup
        devopsDataSeeder = new DevopsDataSeeder(phaseRepository, objectMapper);
        Resource mockResource = mock(Resource.class);
        String jsonContent = "{\"slug\": \"code\", \"title\": \"Code Phase\" INVALID JSON";
        InputStream is = new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
        
        when(mockResource.getInputStream()).thenReturn(is);
        when(mockResource.getFilename()).thenReturn("invalid.json");

        // Execute & Verify
        DevopsDataSeedingException exception = assertThrows(DevopsDataSeedingException.class, () -> {
            devopsDataSeeder.parseAndSaveResource(mockResource);
        });

        assertTrue(exception.getMessage().contains("invalid.json"));
        verify(phaseRepository, never()).existsByPhaseKey(anyString());
        verify(phaseRepository, never()).save(any(DevopsPhase.class));
    }
}
