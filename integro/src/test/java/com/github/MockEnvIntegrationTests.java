package com.github;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class MockEnvIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExchangeRateClient exchangeRateClient;

    @Test
    void createOrder() throws Exception {
        // TODO: протестируйте успешное создание заказа на 100 евро
    }

    @Test
    @Sql("/unpaid-order.sql")
    void payOrder() throws Exception {
        // TODO: протестируйте успешную оплату ранее созданного заказа валидной картой
    }

    @Test
    @Sql("/paid-order.sql")
    void getReceipt() throws Exception {
        // TODO: Протестируйте получение чека на заказ №1 c currency = USD
        // Примечание: используйте мок для ExchangeRateClient
    }
}
