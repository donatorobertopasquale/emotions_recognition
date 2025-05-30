package com.eyxpoliba.emotion_recognition.service;

import com.eyxpoliba.emotion_recognition.model.BlacklistTokenEntity;
import com.eyxpoliba.emotion_recognition.model.UserEntity;
import com.eyxpoliba.emotion_recognition.repository.BlacklistTokenRepository;
import com.eyxpoliba.emotion_recognition.repository.UserRepository;
import com.eyxpoliba.emotion_recognition.responses.LoginResponse;
import com.eyxpoliba.emotion_recognition.security.JwtProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private BlacklistTokenRepository blacklistTokenRepository;
    @Mock
    private AzureStorageService azureStorageService;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogin_Success() {
        // Arrange
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setNickname("testuser");
        when(userRepository.save(user)).thenReturn(user);
        when(jwtProvider.generateToken("testuser", 1L, true)).thenReturn("access-token");
        when(jwtProvider.generateToken("testuser", 1L, false)).thenReturn("refresh-token");
        when(azureStorageService.getRandomBlobNames(10)).thenReturn(Arrays.asList("img1.png", "img2.png"));

        // Act
        ResponseEntity<LoginResponse> result = userService.login(user, response);

        // Assert
        assertEquals(201, result.getStatusCodeValue());
        assertEquals(URI.create("/api/login"), result.getHeaders().getLocation());
        LoginResponse body = result.getBody();
        assertNotNull(body);
        assertEquals(1L, body.userId());
        assertEquals(Arrays.asList("img1.png", "img2.png"), body.imagesName());

        // Verify cookies
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response, times(2)).addCookie(cookieCaptor.capture());
        List<Cookie> cookies = cookieCaptor.getAllValues();
        assertEquals("accessToken", cookies.get(0).getName());
        assertEquals("access-token", cookies.get(0).getValue());
        assertEquals("refreshToken", cookies.get(1).getName());
        assertEquals("refresh-token", cookies.get(1).getValue());
    }



    @Test
    void testLogout_NoToken() {
        // Arrange
        when(request.getCookies()).thenReturn(null);

        // Act
        ResponseEntity<Object> result = userService.logout(request, response);

        // Assert
        assertEquals(400, result.getStatusCodeValue());
        assertEquals("JWT token is required", result.getBody());
        verify(blacklistTokenRepository, never()).save(any());
    }

    @Test
    void testLogout_EmptyCookies() {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{});

        // Act
        ResponseEntity<Object> result = userService.logout(request, response);

        // Assert
        assertEquals(400, result.getStatusCodeValue());
        assertEquals("JWT token is required", result.getBody());
        verify(blacklistTokenRepository, never()).save(any());
    }

    @Test
    void testLogout_NoAccessTokenCookie() {
        // Arrange
        Cookie cookie = new Cookie("OTHER_COOKIE", "value");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        // Act
        ResponseEntity<Object> result = userService.logout(request, response);

        // Assert
        assertEquals(400, result.getStatusCodeValue());
        assertEquals("JWT token is required", result.getBody());
        verify(blacklistTokenRepository, never()).save(any());
    }

    @Test
    void testLogout_AlreadyLoggedOut() {
        // Arrange
        Cookie cookie = new Cookie("ACCESS_TOKEN", "token123");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(blacklistTokenRepository.existsById("token123")).thenReturn(true);

        // Act
        ResponseEntity<Object> result = userService.logout(request, response);

        // Assert
        assertEquals(400, result.getStatusCodeValue());
        assertEquals("JWT token is required", result.getBody());
        verify(blacklistTokenRepository, never()).save(any());
    }
}

