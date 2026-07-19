package com.web_tutorial.javabackend.service.devops;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web_tutorial.javabackend.domain.devops.DevopsPhase;
import com.web_tutorial.javabackend.domain.devops.dto.DevopsDTOs;
import com.web_tutorial.javabackend.exception.DevopsContentSerializationException;
import com.web_tutorial.javabackend.repository.devops.DevopsPhaseRepository;

@ExtendWith(MockitoExtension.class)
public class DevopsServiceSerializationTest {

    @Mock
    private DevopsPhaseRepository phaseRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DevopsService devopsService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", "password"));
    }

    @Test
    void testCreatePhase_SerializationSuccess() throws Exception {
        DevopsDTOs.PhaseRequest request = new DevopsDTOs.PhaseRequest(
                "plan", "Plan", "Plan Phase", "Tagline", "Summary",
                "Hero Title", "Hero Snippet", "icon", "gradient", 1, true,
                new Object(), new Object(), new Object(), new Object(), new Object(), new Object());

        when(phaseRepository.existsByPhaseKey("plan")).thenReturn(false);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"valid\":\"json\"}");
        
        DevopsPhase savedPhase = new DevopsPhase();
        savedPhase.setId(1L);
        savedPhase.setPhaseKey("plan");
        when(phaseRepository.save(any(DevopsPhase.class))).thenReturn(savedPhase);

        DevopsDTOs.PhaseDetailResponse response = devopsService.createPhase(request);
        
        assertNotNull(response);
        verify(phaseRepository).save(any(DevopsPhase.class));
    }

    @Test
    void testCreatePhase_SerializationFailure() throws Exception {
        DevopsDTOs.PhaseRequest request = new DevopsDTOs.PhaseRequest(
                "plan", "Plan", "Plan Phase", "Tagline", "Summary",
                "Hero Title", "Hero Snippet", "icon", "gradient", 1, true,
                new Object(), new Object(), new Object(), new Object(), new Object(), new Object());

        when(phaseRepository.existsByPhaseKey("plan")).thenReturn(false);
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Mocked error") {});

        DevopsContentSerializationException exception = assertThrows(DevopsContentSerializationException.class, () -> {
            devopsService.createPhase(request);
        });

        assertEquals("SERIALIZE", exception.getOperation());
        assertEquals("theme", exception.getFieldName()); // Theme is the first one processed
        verify(phaseRepository, never()).save(any(DevopsPhase.class));
    }

    @Test
    void testUpdatePhase_SerializationFailure() throws Exception {
        Long id = 1L;
        DevopsDTOs.PhaseRequest request = new DevopsDTOs.PhaseRequest(
                "plan", "Plan", "Plan Phase", "Tagline", "Summary",
                "Hero Title", "Hero Snippet", "icon", "gradient", 1, true,
                new Object(), new Object(), new Object(), new Object(), new Object(), new Object());

        DevopsPhase existingPhase = new DevopsPhase();
        existingPhase.setId(id);
        existingPhase.setPhaseKey("plan");
        
        when(phaseRepository.findById(id)).thenReturn(Optional.of(existingPhase));
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Mocked error") {});

        DevopsContentSerializationException exception = assertThrows(DevopsContentSerializationException.class, () -> {
            devopsService.updatePhase(id, request);
        });

        assertEquals("SERIALIZE", exception.getOperation());
        verify(phaseRepository, never()).save(any(DevopsPhase.class));
    }

    @Test
    void testGetPhaseDetail_DeserializationFailure() throws Exception {
        DevopsPhase existingPhase = new DevopsPhase();
        existingPhase.setId(1L);
        existingPhase.setPhaseKey("plan");
        existingPhase.setThemeJson("{\"invalid\": json}"); // will trigger error if mocked
        
        when(phaseRepository.findByPhaseKey("plan")).thenReturn(Optional.of(existingPhase));
        when(objectMapper.readTree(anyString())).thenThrow(new JsonProcessingException("Mocked error") {});

        DevopsContentSerializationException exception = assertThrows(DevopsContentSerializationException.class, () -> {
            devopsService.getPhaseDetailByKey("plan");
        });

        assertEquals("DESERIALIZE", exception.getOperation());
    }

    @Test
    void testGetPhaseDetail_ValidNullBehavior() throws Exception {
        DevopsPhase existingPhase = new DevopsPhase();
        existingPhase.setId(1L);
        existingPhase.setPhaseKey("plan");
        existingPhase.setThemeJson(null); // Optional field
        
        when(phaseRepository.findByPhaseKey("plan")).thenReturn(Optional.of(existingPhase));

        // It shouldn't throw exception, but return null for that field safely
        Optional<DevopsDTOs.PhaseDetailResponse> responseOpt = devopsService.getPhaseDetailByKey("plan");
        
        assertTrue(responseOpt.isPresent());
        assertNull(responseOpt.get().theme());
        // Since other fields are also null, they won't trigger readTree either, no exceptions thrown
        verify(objectMapper, never()).readTree(anyString());
    }
}
