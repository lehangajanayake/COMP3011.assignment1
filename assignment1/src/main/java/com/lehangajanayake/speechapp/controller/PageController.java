package com.lehangajanayake.speechapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Handles the application landing page.
 *
 * Spring Boot also serves files in src/main/resources/static automatically;
 * this explicit mapping makes the root URL forward to that static index file.
 */
@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}