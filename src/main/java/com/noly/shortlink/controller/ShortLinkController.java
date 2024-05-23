package com.noly.shortlink.controller;


import com.noly.shortlink.service.IShortLinkService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/short-link")
public class ShortLinkController{

    @Value("${domain}")
    private String DOMAIN;
    
    @Autowired
    private IShortLinkService shortLinkService;
    
    // 生成短链接地址
    @PostMapping("/generateShortLinkUrl")
    public String generateShortLinkUrl(String longLinkUrl) {
        String shortLinkUrl = shortLinkService.generateShortLinkUrl(longLinkUrl);
        System.out.println("生成的短链为：" + shortLinkUrl);
        return shortLinkUrl;
    }
    
}
