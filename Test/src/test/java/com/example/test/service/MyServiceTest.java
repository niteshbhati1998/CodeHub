package com.example.test.service;

import com.example.test.entity.User;
import com.example.test.repository.MyRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyServiceTest {

    @Mock
    private MyRepository myRepository;

    @InjectMocks
    private MyService myService;

    @BeforeAll
    static void init() {
        System.out.println("BeforeAll");
    }

    @BeforeEach
    void initEachTest() {
        System.out.println("BeforeEach");
    }

    @AfterEach
    void cleanup() {
        System.out.println("AfterEach");
    }

    @AfterAll
    static void destroy() {
        System.out.println("AfterAll");
    }

    @Test
    void shouldReturnUserForValidId() {
        //Arrange
        long id = 1L;
        User user = new User(id, "Bhati");
        when(myRepository.findById(id)).thenReturn(Optional.of(user));

        //Act
        User result = myService.getUserDetails(id);

        //Assert
        assertNotNull(result);
        assertEquals(user, result);
        verify(myRepository).findById(id);
    }

    @Test
    void shouldThrowExceptionForInvalidId() {
        //Arrange
        long id = 2L;
        when(myRepository.findById(id)).thenReturn(Optional.empty());

        //Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> myService.getUserDetails(id));
        assertEquals("Invalid user ID", ex.getMessage());
        verify(myRepository).findById(id);
    }

    @Test
    void shouldDeleteUserForValidId() {
        //Arrange
        long id = 1L;
        doNothing().when(myRepository).deleteById(id);

        //Act & Assert
        assertDoesNotThrow(() -> myService.deleteUserDetails(id));
        verify(myRepository, times(1)).deleteById(id);
    }

    @Test
    void shouldNotDeleteUserForInvalidId() {
        //Arrange
        long id = 11L;

        //Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> myService.deleteUserDetails(id));
        assertEquals("Invalid user ID", ex.getMessage());
        verifyNoMoreInteractions(myRepository);
        //verify(myRepository, never()).deleteById(id);
    }

    @Test
    void shouldReturnTrueForValidId() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //Arrange
        long id = 5L;

        //Act
        Method validateId = MyService.class.getDeclaredMethod("validateId", long.class);
        validateId.setAccessible(true);
        boolean isValid = (boolean) validateId.invoke(myService, id);

        //Assert
        assertTrue(isValid);
    }

    @Test
    void shouldReturnFalseForValidId() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //Arrange
        long id = 15L;

        //Act
        Method validateId = MyService.class.getDeclaredMethod("validateId", long.class);
        validateId.setAccessible(true);
        boolean isValid = (boolean) validateId.invoke(myService, id);

        //Assert
        assertFalse(isValid);
    }
}
