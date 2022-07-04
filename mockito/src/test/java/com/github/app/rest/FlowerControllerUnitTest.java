package com.github.app.rest;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.github.app.api.Flower;
import com.github.app.rest.FlowerController;
import com.github.domain.service.FlowerService;

/*
    Необходимо протестировать MessageController используя моки
    TODO
        создайте мок для FlowerService
        создайте мок для FlowerController
        напишите метод для тестирования flowerController.isAFlower, создав мок для flowerService.isABigFlower, который возвращает true
        напишите метод для тестирования flowerController.isABigFlower, создав мок для flowerService.isABigFlower, который возвращает true
*/
@RunWith(MockitoJUnitRunner.class)
public class FlowerControllerUnitTest {
    @Mock
    private FlowerService flowerService;
    @InjectMocks
    private FlowerController flowerController;

    @Test
    public void isAFlower() {
        when(flowerService.analize(eq("Poppy"))).thenReturn("Flower");
        String flower = flowerController.isAFlower("Poppy");
        Assert.assertEquals("Flower", flower);
    }
    @Test
    public void isABigFlower() {
        when(flowerService.isABigFlower(eq("Poppy"), anyInt())).thenReturn(true);
        Flower flower = new Flower("Poppy", 15);
        Boolean response = flowerController.isABigFlower(flower);
        Assert.assertTrue(response);
    }
}
