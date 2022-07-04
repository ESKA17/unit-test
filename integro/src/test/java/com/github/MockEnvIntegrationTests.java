package com.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.json.*;
import org.testcontainers.shaded.org.bouncycastle.util.test.TestFailedException;
import org.testcontainers.shaded.org.bouncycastle.util.test.TestResult;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.ExchangeRate;
import javax.money.convert.MonetaryConversions;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Slf4j
class MockEnvIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExchangeRateClient exchangeRateClient;

    @Test
    void createOrder() {
        // TODO: протестируйте успешное создание заказа на 100 евро
        try {
            mockMvc.perform(post("/order/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"amount\": \"EUR 100\"}")).andExpect(status().isCreated()).andReturn();
            MvcResult mvcResult = mockMvc.perform(get("/order/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk()).andReturn();
            String json = mvcResult.getResponse().getContentAsString();
            ObjectMapper objectMapper = JsonMapper.builder()
                    .addModule(new JavaTimeModule())
                    .build();
            Order order = objectMapper.readValue(json, Order.class);

            assertThat(order.getAmount()).isEqualTo(BigDecimal.valueOf(100));
            assertThat(order.getDate()).isNotNull();
            assertThat(order.getId()).isNotNull();
            assertThat(order.getPaid()).isNotNull();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    @Sql("/unpaid-order.sql")
    void payOrder() {
        // TODO: протестируйте успешную оплату ранее созданного заказа валидной картой
        try {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String creditCard = "4400 4200 4300 4100";
            String json = ow.writeValueAsString(new PaymentRequest(creditCard));
            MvcResult mvcResult = mockMvc.perform(post("/order/1/payment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)).andExpect(status().isCreated()).andReturn();
            String response = mvcResult.getResponse().getContentAsString();
            Gson gson = new Gson();
            PaymentResponse paymentResponse = gson.fromJson(response, PaymentResponse.class);

            assertThat(paymentResponse.getOrderId()).isEqualTo( 1);
            assertThat(paymentResponse.getCreditCardNumber()).isEqualTo(creditCard);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Sql("/paid-order.sql")
    void getReceipt() throws Exception {
        // TODO: Протестируйте получение чека на заказ №1 c currency = USD
        // Примечание: используйте мок для ExchangeRateClient
        CurrencyUnit eur = Monetary.getCurrency("EUR");
        CurrencyUnit usd = Monetary.getCurrency("USD");
        when(exchangeRateClient.getExchangeRate(eur, usd))
                .thenReturn(BigDecimal.valueOf(0.1));
        MvcResult mvcResult = mockMvc.perform(get("/order/{id}/receipt?currency=USD", 1))
                .andExpect(status().isOk()).andReturn();
        String response = mvcResult.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(response);
        String money = jsonObject.getString("amount").replaceAll("\\D", "");
        String monetaryAmount =
                Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(BigDecimal.valueOf(10.00)).create()
                        .toString().replaceAll("\\D", "");
        assertThat(money).isEqualTo(monetaryAmount);

    }
}
