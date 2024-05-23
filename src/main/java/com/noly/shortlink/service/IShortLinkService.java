package com.noly.shortlink.service;

import com.noly.shortlink.entity.ShortLink;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IShortLinkService extends IService<ShortLink> {
    
    // 通过长链接地址接获取短链接地址
    String getShortLinkUrlByLongLinkUrl(String longLinkUrl);
    
    // 通过短链接地址获取长链接地址
    String getLongLinkUrlByShortLinkUrl(String shortLinkUrl);
    
    // 生成短链接地址
    String generateShortLinkUrl(String longLinkUrl);
    
    // 判断短链是否重复。重复:true, 不重复:false 
    boolean hasShortLinkUrlRepeated(String shortLinkUrl);
    
    // 判断长链接地址 Hash 值是否重复。重复:true, 不重复:false 
    boolean hasLongLinkHahRepeated(Long longLinkHash);
    
    // 更新短链接的过期时间
    void updateShortLinkUrlExpiredTime(String shortLinkUrl);

    // 通过短链接地址查询短链实体
    ShortLink getShortLinkByShortLinkUrl(String shortLinkUrl);
    
    // 判断短链接是否过期（传入短链接实体）
    boolean hasAlreadyExpired(ShortLink shortLink);

}
