package com.tcg.cardmaker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TCG卡片製作工具主應用程式
 * 
 * @author TCG Card Maker
 * @version 1.0.0
 */
@SpringBootApplication
public class TcgCardMakerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TcgCardMakerApplication.class, args);
        System.out.println("=================================");
        System.out.println("TCG Card Maker 已啟動完成！");
        System.out.println("請訪問: http://localhost:8080");
        System.out.println("=================================");
    }
} 