package com.eyxpoliba.emotion_recognition.service;

import static org.junit.jupiter.api.Assertions.*;

import com.eyxpoliba.emotion_recognition.model.ReactionsEntity;
import com.eyxpoliba.emotion_recognition.model.UserEntity;
import com.eyxpoliba.emotion_recognition.payload.ImageDescriptionAndReactionPayload;
import com.eyxpoliba.emotion_recognition.payload.ResultPayload;
import com.eyxpoliba.emotion_recognition.repository.ReactionsRepository;
import com.eyxpoliba.emotion_recognition.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReactionsServiceTest {
    @Mock
    private ReactionsRepository reactionsRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private ReactionsService reactionsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testRegisterResult_Success() {
        // Arrange
        Long userId = 1L;
        UserEntity userEntity = mock(UserEntity.class);
        ImageDescriptionAndReactionPayload imagePayload = mock(ImageDescriptionAndReactionPayload.class);
        when(imagePayload.image()).thenReturn("img.png");
        when(imagePayload.description()).thenReturn("desc");
        when(imagePayload.reaction()).thenReturn("happy");
        when(imagePayload.aiComment()).thenReturn("ai comment");
        List<ImageDescriptionAndReactionPayload> payloadList = List.of(imagePayload);
        ResultPayload resultPayload = mock(ResultPayload.class);
        when(resultPayload.imagesDescriptionsAndReactions()).thenReturn(payloadList);
        when(authentication.getCredentials()).thenReturn(userId);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        // Act
        ResponseEntity<Object> response = reactionsService.registerResult(resultPayload);

        // Assert
        assertEquals(201, response.getStatusCodeValue());
        assertEquals(URI.create("/api/register-result"), response.getHeaders().getLocation());
        assertTrue(response.getBody().toString().contains("Result registered successfully"));
        verify(reactionsRepository, times(1)).save(any(ReactionsEntity.class));
    }

    @Test
    void testRegisterResult_UserNotFound() {
        Long userId = 2L;
        ResultPayload resultPayload = mock(ResultPayload.class);
        when(resultPayload.imagesDescriptionsAndReactions()).thenReturn(List.of(mock(ImageDescriptionAndReactionPayload.class)));
        when(authentication.getCredentials()).thenReturn(userId);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> reactionsService.registerResult(resultPayload));
        assertEquals("User not found", ex.getMessage());
        verify(reactionsRepository, never()).save(any());
    }
}

