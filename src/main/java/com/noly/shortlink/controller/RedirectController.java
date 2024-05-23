package com.noly.shortlink.controller;

import com.noly.shortlink.service.IShortLinkService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller // 注意这里不能用@RestController,RestController由@Controller + @ResponseBody组成，返回的是数据中支持跳转视图
@RequestMapping()
public class RedirectController {

    @Value("${domain}")
    private String DOMAIN;

    @Autowired
    private IShortLinkService shortLinkService;
    

    // HttpServletResponse redirect 方式
    @GetMapping("/{shortLinkUrl}")
    public void redirect1(@PathVariable String shortLinkUrl, HttpServletResponse response) {
        System.out.println("收到了短链接重定向");
        System.out.println("短链接：" + shortLinkUrl);

        shortLinkUrl = DOMAIN + shortLinkUrl;
        System.out.println("拼接后的短链接：" + shortLinkUrl);

        // 通过短链接地址获取对应的长链接地址
        String longLinkUrl = shortLinkService.getLongLinkUrlByShortLinkUrl(shortLinkUrl);

        System.out.println("长链接：" + longLinkUrl);
        


        try {
            // 只有拼接了 http:// 的地址才是绝对路径地址，否则是相对路径地址，会带上当前相对路径 URL
            // 判断传入的长链接地址是否带了 http:// 或 https://
            if (longLinkUrl.startsWith("http")) {
                response.sendRedirect(longLinkUrl);
            } else {
                response.sendRedirect("http://" + longLinkUrl);
            }
            
            //            response.sendRedirect("http://www.baidu.com");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    // spring redirect 方式（有问题，会一直重定向）
//    @GetMapping("/{shortLinkUrl}")
//    public String redirect2(@PathVariable String shortLinkUrl) {
//        System.out.println("收到了短链接重定向");
//        System.out.println("短链接：" + shortLinkUrl);
//
//        shortLinkUrl = DOMAIN + shortLinkUrl;
//        System.out.println("拼接后的短链接：" + shortLinkUrl);
//
//        // 通过短链接地址获取对应的长链接地址
//        String longLinkUrl = shortLinkService.getLongLinkUrlByShortLinkUrl(shortLinkUrl);
//
//        System.out.println("长链接：" + longLinkUrl);
//
////        return "redirect:http://www.baidu.com"; 
//        longLinkUrl = "http://" + longLinkUrl;
//        return "redirect:longLinkUrl";
//    }

}
