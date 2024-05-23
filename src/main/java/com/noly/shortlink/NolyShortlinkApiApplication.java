package com.noly.shortlink;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.noly.shortlink.mapper")
public class NolyShortlinkApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(NolyShortlinkApiApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  短链生成系统启动成功   ლ(´ڡ`ლ)ﾞ  \n" );
    }
    
}
