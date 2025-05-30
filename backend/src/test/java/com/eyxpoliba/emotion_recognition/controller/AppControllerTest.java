package com.eyxpoliba.emotion_recognition.controller;

import static org.junit.jupiter.api.Assertions.*;
import com.eyxpoliba.emotion_recognition.model.UserEntity;
import com.eyxpoliba.emotion_recognition.payload.ResultPayload;
import com.eyxpoliba.emotion_recognition.responses.LoginResponse;
import com.eyxpoliba.emotion_recognition.service.AzureStorageService;
import com.eyxpoliba.emotion_recognition.service.ReactionsService;
import com.eyxpoliba.emotion_recognition.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import java.io.IOException;
import java.util.ArrayList;

import static org.mockito.Mockito.*;

class AppControllerTest {
    @Mock
    private UserService userService;
    @Mock
    private AzureStorageService azureStorageService;
    @Mock
    private ReactionsService reactionsService;

    @InjectMocks
    private AppController appController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogin() {
        UserEntity user = new UserEntity();
        LoginResponse loginResponse = new LoginResponse(1L, new ArrayList<>());
        when(userService.login(any(UserEntity.class), any())).thenReturn(ResponseEntity.ok(loginResponse));
        ResponseEntity<LoginResponse> response = appController.login(user, null);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(loginResponse, response.getBody());
    }

    @Test
    void testLogout() {
        when(userService.logout(any(), any())).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<Object> response = appController.logout(null, null);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testDownloadImage() throws IOException {
        String imageName = "test.png";
        byte[] imageBytes = new byte[]{1, 2, 3};
        when(azureStorageService.downloadImage(imageName)).thenReturn(imageBytes);
        ResponseEntity<byte[]> response = appController.downloadImage(imageName);
        assertEquals(200, response.getStatusCodeValue());
        assertArrayEquals(imageBytes, response.getBody());
    }

    @Test
    void testRegisterResult() {
        ResultPayload payload = mock(ResultPayload.class);
        when(reactionsService.registerResult(any(ResultPayload.class))).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<Object> response = appController.registerResult(payload);
        assertEquals(200, response.getStatusCodeValue());
    }
}

