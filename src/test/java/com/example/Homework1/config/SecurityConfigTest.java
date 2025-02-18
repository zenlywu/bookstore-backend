package com.example.Homework1.config;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.Homework1.entity.Book;
import com.example.Homework1.controller.BookController;
import com.example.Homework1.controller.UserController;
import com.example.Homework1.dto.UserDto;
import com.example.Homework1.service.BookService;
import com.example.Homework1.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;
    @Mock
    private BookService bookService;
    @Mock
    private UserService userService;
    @InjectMocks
    private BookController bookController;
    @InjectMocks 
    private UserController userController;

    @Test
    @WithMockUser(username = "admin1", roles = {"ADMIN"})  // ✅ 模擬 ADMIN Get Users
    void testAdminCanGetUsers(){
        when (userService.getAllUsers()).thenReturn(List.of());

        ResponseEntity<List<UserDto>> response = userController.getAllUsers();

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @WithMockUser(username = "admin1", roles = {"ADMIN"})  // ✅ 模擬 ADMIN Create Users
    void testAdminCanCreateUsers() throws Exception {
        // ✅ 使用 `null` 代表自動設置 `EMPLOYEE`
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

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isOk()); // ✅ 預期 HTTP 200 OK
    }


    @Test
    @WithMockUser(username = "employee1", roles = {"EMPLOYEE"})
    void testEmployeeCanGetBooks() {
        when(bookService.getAllBooks()).thenReturn(List.of());

        ResponseEntity<List<Book>> response = bookController.getAllBooks();

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @WithMockUser(username = "employee1", roles = {"EMPLOYEE"})  // ✅ 模擬 EMPLOYEE
    void testEmployeeCannotCreateBooks() throws Exception {
        // ✅ 使用 JSON 字符串，避免建構子錯誤
        String bookJson = """
        {
            "title": "Forbidden Book",
            "author": "Unknown",
            "category": "Fiction",
            "price": 100.0,
            "salePrice": 80.0,
            "description": "BOOK"
        }
        """;

        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson))
                .andExpect(status().isForbidden()); // ✅ 預期 HTTP 403 Forbidden
    }

}
