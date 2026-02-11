package com.example.test.controller;

import com.example.test.model.Response;
import com.example.test.entity.User;
import com.example.test.service.MyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyControllerTest {

    @Mock
    private MyService myService;

    @InjectMocks
    private MyController myController;

    @Test
    void shouldReturnSuccessStatusForValidId() {
        //Arrange
        long id = 1L;
        User user = new User(id, "Bhati");
        when(myService.getUser(id)).thenReturn(user);

        //Act
        ResponseEntity<Response> resonseEntity = myController.getUser(id);

        //Assert
        assertEquals(user, resonseEntity.getBody().getPayload());
        assertEquals(HttpStatus.OK, resonseEntity.getStatusCode());
        verify(myService).getUser(id);
    }

    @Test
    void shouldThrowExceptionForInvalidId() {
        //Arrange
        long id = 2L;
        when(myService.getUser(id)).thenThrow(new RuntimeException("Invalid user ID"));

        //Act
        ResponseEntity<Response> resonseEntity = myController.getUser(id);

        //Assert
        assertNull(resonseEntity.getBody().getPayload());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resonseEntity.getStatusCode());
        verify(myService).getUser(id);
    }
}
