package com.example.Homework1.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.Homework1.controller.BookController;
import com.example.Homework1.controller.UserController;
import com.example.Homework1.dto.UserDto;
import com.example.Homework1.entity.Book;
import com.example.Homework1.service.BookService;
import com.example.Homework1.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    // 使用 @MockBean 而不是 @Mock 讓 Spring Boot 上下文注入這些 bean
    @SuppressWarnings("removal")
    @MockBean
    private UserService userService;
    
    @SuppressWarnings("removal")
    @MockBean
    private BookService bookService;
    
    // 從 Spring 上下文中注入控制器
    @Autowired
    private UserController userController;
    
    @Autowired
    private BookController bookController;
    
    @Test
    @WithMockUser(username = "admin1", authorities = {"ADMIN"})
    void testAdminCanGetUsers() {
        // 模擬 userService 返回一個空清單（或你期望的內容）
        when(userService.getAllUsers()).thenReturn(List.of(new UserDto()));
        
        ResponseEntity<List<UserDto>> response = userController.getAllUsers();
        
        assertEquals(200, response.getStatusCode().value());
    }
    
    @Test
    @WithMockUser(username = "admin1", authorities = {"ADMIN"})
    void testAdminCanCreateUsers() throws Exception {
        String userJson = """
        {
            "username": "user1",
            "password": "123456",
            "fullname": "User Test1",
            "phone": "0911333666",
            "email": "UserTest1@gmail.com",
            "role": "EMPLOYEE"
        }
        """;
        
        // 模擬必要的 Service 呼叫（依照你 Controller 實作）
        // 若 userController.createUser 用到 userService.createUser()，這裡要做模擬處理
        
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "employee1", authorities = {"EMPLOYEE"})
    void testEmployeeCanGetBooks() {
        when(bookService.getAllBooks()).thenReturn(List.of());
        
        ResponseEntity<List<Book>> response = bookController.getAllBooks();
        
        assertEquals(200, response.getStatusCode().value());
    }
}