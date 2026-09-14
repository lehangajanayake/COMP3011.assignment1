package com.myname.speechapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "openai.api-key=test-only-placeholder")
class SpeechAppApplicationTests {

    @Test
    void contextLoads() {
    }
}