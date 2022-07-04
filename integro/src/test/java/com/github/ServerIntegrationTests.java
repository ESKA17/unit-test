package com.github;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ServerIntegrationTests {
    @Autowired
    private WebTestClient webClient;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;


    private static MockWebServer mockWebServer;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("exchange-rate-api.base-url", () -> mockWebServer.url("/").url().toString());
    }

    @BeforeAll
    static void setupMockWebServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void deleteEntities() {
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    void createOrder() {
        // TODO: протестируйте успешное создание заказа на 100 евро
        // используя webClient
        OrderRequest orderRequest = new OrderRequest();
        MonetaryAmount monetaryAmount =
                Monetary.getDefaultAmountFactory().setCurrency("EUR").setNumber(BigDecimal.valueOf(100)).create();
        orderRequest.setAmount(monetaryAmount);
        try {
        webClient.post().uri("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(orderRequest))
                .exchange()
                .expectStatus().isCreated();
            FluxExchangeResult<Order> fluxExchangeResult = webClient.get().uri("/order/1")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange().expectStatus().isOk()
                    .returnResult(Order.class);
            Order order = fluxExchangeResult.getResponseBody().blockFirst();
            assertThat(order).isNotNull();
            assertThat(order.getAmount()).isEqualTo(new BigDecimal("100.0"));
            assertThat(order.getDate()).isNotNull();
            assertThat(order.getId()).isNotNull();
            assertThat(order.getPaid()).isNotNull();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void payOrder() {
        // TODO: протестируйте успешную оплату ранее созданного заказа валидной картой
        // используя webClient
        // Получите `id` заказа из базы данных, используя orderRepository
        try {
            OrderRequest orderRequest = new OrderRequest();
            MonetaryAmount monetaryAmount =
                    Monetary.getDefaultAmountFactory().setCurrency("EUR").setNumber(BigDecimal.valueOf(100)).create();
            orderRequest.setAmount(monetaryAmount);
            webClient.post().uri("/order")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(orderRequest))
                    .exchange()
                    .expectStatus().isCreated();
            String creditCard = "4400 4200 4300 4100";
            PaymentRequest paymentRequest = new PaymentRequest(creditCard);
            EntityExchangeResult<PaymentResponse> entityExchangeResult = webClient.post().uri("/order/2/payment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(paymentRequest))
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(PaymentResponse.class)
                    .returnResult();
            PaymentResponse paymentResponse = entityExchangeResult.getResponseBody();
            assertThat(paymentResponse).isNotNull();
            Order order = orderRepository.findById(2L).orElse(null);
            assert order != null;
            assertThat(paymentResponse.getOrderId()).isEqualTo(order.getId());
            assertThat(order.isPaid()).isEqualTo(true);
            Payment payment = paymentRepository.findByOrderId(2L).orElse(null);
            assert payment != null;
            assertThat(payment.getCreditCardNumber()).isEqualTo(paymentResponse.getCreditCardNumber());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getReceipt() {
        // TODO: Протестируйте получение чека на заказ №1 c currency = USD
        // Создайте объект Order, Payment и выполните save, используя orderRepository
        // Используйте mockWebServer для получения conversion_rate
        // Сделайте запрос через webClient
        Order order = new Order(LocalDateTime.now(), BigDecimal.valueOf(100), true);
        orderRepository.save(order);
        System.out.println(order.getId());
        Payment payment = new Payment(order,"123456781234");
        paymentRepository.save(payment);
        try {
            String json = "{\"conversion_rate\": 0.1}";
            mockWebServer.enqueue(
                    new MockResponse().setResponseCode(200)
                            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .setBody(json)
            );

            EntityExchangeResult<Receipt> entityExchangeResult =
                    webClient.get().uri("/order/4/receipt?currency=USD")
                    .exchange()
                    .expectStatus().isOk().expectBody(Receipt.class).returnResult();
            Receipt receipt = entityExchangeResult.getResponseBody();
            MonetaryAmount monetaryAmount =
                    Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(BigDecimal.valueOf(10.00)).create();
            assert receipt != null;
            assertThat(receipt.getAmount()).isEqualTo(monetaryAmount);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
