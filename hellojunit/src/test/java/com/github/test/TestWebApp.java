package com.github.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/*
	TODO
	Добавить WebApplicationContext для тестов
	Добавить MockMvc
	Создать метод с аннотацией @Before которая создает mockMvc на основе webApplicationContext
	Написать метод тестирования метода /employee
		Должен проверяться HTTP статус ответа
		Должен проверять contentType ответа
		Должно проверять значение поля "name"
		Должно проверять значение поля "designation"
		Должно проверять значение поля "salary"
		Должно проверять значение поля "empId"
*/
@RunWith(SpringRunner.class)
@SpringBootTest
@NoArgsConstructor
@Getter
public class TestWebApp extends SpringBootHelloWorldTests {
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @Before
    public void apriori() {
        mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }
    @Test
    public void main() {
        MockMvc mvc = this.getMockMvc();
        try {
            mvc.perform(get("/employee/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content("""
                {
                    "name": "emp1",
                    "designation": "manager",
                    "empId": "1",
                    "salary": 3000
                }
                """)).andExpect(status().isOk());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
