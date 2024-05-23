package com.noly.shortlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.hash.Hashing;
import com.noly.shortlink.entity.ShortLink;
import com.noly.shortlink.mapper.ShortLinkMapper;
import com.noly.shortlink.service.IShortLinkService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.noly.shortlink.utils.Base62Utils;
import com.noly.shortlink.utils.SnowFlakeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLink> implements IShortLinkService {
    
    @Value("${domain}")
    private String DOMAIN;

    
    @Autowired
    private  ShortLinkMapper shortLinkMapper;

    // 通过长链接地址接查询获取短链接地址
    @Override
    public String getShortLinkUrlByLongLinkUrl(String longLinkUrl) {
        // 从数据库中查找是否存在长链接地址对应的短链接地址
        LambdaQueryWrapper<ShortLink> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(ShortLink::getLongLinkUrl, longLinkUrl)
                .eq(ShortLink::getStatus, 1);
        ShortLink shortLink = this.getOne(lambdaQueryWrapper);

        // 先判断该长链接地址是否在数据库中有对应的短链接地址映射
        if (shortLink == null) {    // 不存在对应映射关系，直接返回 null
            return null;
        }

        // 存在对应关系映射
        // 返回前，先判断短链接地址是否过期
        boolean flag = hasAlreadyExpired(shortLink);

        // 如果不存在对应的短链接地址，则返回 null，否则返回查询到的短链接地址
        return flag ? null : shortLink.getShortLinkUrl();
    }

    // 通过短链接地址获取长链接地址
    @Override
    public String getLongLinkUrlByShortLinkUrl(String shortLinkUrl) {
        // 从数据库中查找是否存在 短链接地址对应的长链接地址
        LambdaQueryWrapper<ShortLink> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(ShortLink::getShortLinkUrl, shortLinkUrl)
                .eq(ShortLink::getStatus, 1);
        ShortLink shortLink = this.getOne(lambdaQueryWrapper);
        
        // 先判断该短链接地址是否在数据库中有对应的长链接地址映射
        if (shortLink == null) {    // 不存在对应映射关系，直接返回 null
            return null;
        }
        
        // 存在对应关系映射
        // 返回前，先判断短链接地址是否过期
        boolean flag = hasAlreadyExpired(shortLink);
        
        // 如果短链接地址过期了，则返回 null，否则返回查询到的长链接地址
        return flag ? null : shortLink.getLongLinkUrl();
    }

    // 生成短链接地址
    @Override
    public String generateShortLinkUrl(String longLinkUrl) {
        
        // 在长链接生成短链接之前，先查找一下是否已经存在了对应的短链接，如果存在，则直接返回查找得到的短链接。
        String shortLinkUrl = getShortLinkUrlByLongLinkUrl(longLinkUrl);
        if (shortLinkUrl != null) {
            return shortLinkUrl;
        }
        
        // 生成短链接
        // 计算长链接的 Hash 值
        long longLinkHash = Hashing.murmur3_32_fixed().hashString(longLinkUrl, StandardCharsets.UTF_8).padToLong();
        // 判断长链接Hash值是否已经存在数据库中了,如果存在了，则发生了 Hash 冲突，需要重新计算 Hash 值
        if (hasLongLinkHahRepeated(longLinkHash)) {
            return regenerateOnHashConflict(longLinkUrl, longLinkHash);
        } else {
            // 创建需要存储的对象
            LocalDateTime createdTime = LocalDateTime.now();
            LocalDateTime expiredTime = createdTime.plusDays(30);
            shortLinkUrl = DOMAIN + Base62Utils.encodeToBase62String(longLinkHash);
            ShortLink shortLink = new ShortLink()
                    .setShortLinkUrl(shortLinkUrl)
                    .setLongLinkHash(longLinkHash)
                    .setLongLinkUrl(longLinkUrl)
                    .setStatus(1)
                    .setCreatedTime(createdTime)
                    .setExpiredTime(expiredTime);
            this.save(shortLink);
            return shortLinkUrl; 
        }
    }

    // 判断短链是否重复。重复:true, 不重复:false 
    @Override
    public boolean hasShortLinkUrlRepeated(String shortLinkUrl) {
        LambdaQueryWrapper<ShortLink> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .select(ShortLink::getShortLinkUrl)
                .eq(ShortLink::getShortLinkUrl, shortLinkUrl)
                .eq(ShortLink::getStatus, 1);
        long count = this.count(lambdaQueryWrapper);
        return count > 0;
    }

    // 判断长链接地址 Hash 值是否重复。重复:true, 不重复:false 
    @Override
    public boolean hasLongLinkHahRepeated(Long longLinkHash) {
        LambdaQueryWrapper<ShortLink> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .select(ShortLink::getLongLinkHash)
                .eq(ShortLink::getLongLinkHash, longLinkHash)
                .eq(ShortLink::getStatus, 1);
        ShortLink shortLink = this.getOne(lambdaQueryWrapper);
        return shortLink != null;
    }

    // 更新短链的过期时间
    @Override
    public void updateShortLinkUrlExpiredTime(String shortLinkUrl) {
        // 通过短链接地址获取短链实体对象
        ShortLink shortLink = getShortLinkByShortLinkUrl(shortLinkUrl);
        // 更新过期时间（从当前时间往后延 30 天）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredTime = now.plusDays(30);
        shortLink.setExpiredTime(expiredTime);
        // 根据主键 ID 更新短链实体对象
        this.updateById(shortLink);
    }

    // 通过短链接地址查询短链实体
    @Override
    public ShortLink getShortLinkByShortLinkUrl(String shortLinkUrl) {
        // TODO: 判断短链接是否过期
        LambdaQueryWrapper<ShortLink> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(ShortLink::getShortLinkUrl, shortLinkUrl)
                .eq(ShortLink::getStatus, 1);
        ShortLink shortLink = this.getOne(lambdaQueryWrapper);
        return shortLink;
    }

    // 判断短链接是否过期（传入短链接实体）
    @Override
    public boolean hasAlreadyExpired(ShortLink shortLink) {
        // 当前时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(shortLink.getExpiredTime())) {
            // 短链接地址过期了，要更新修改它的可用状态 status 为 0，表示不可用
            shortLink.setStatus(0);
        } else {
            // 短链接地址没过期，要更新它的过期时间，更新为当前时间的 30 天后
            LocalDateTime newExpiredTime = now.plusDays(30);
            shortLink.setExpiredTime(newExpiredTime);
        }
        this.updateById(shortLink); // 更新入数据库

        // 如果短链接地址过期了，则返回 true，否则返回 false
        return shortLink.getStatus() == 0 ? true : false;

    }

    // 处理 Hash 冲突，重新生成 Hash 值 和短链，返回短链地址
    private String regenerateOnHashConflict(String longLinkUrl, long longLinkHash) {
        // 自增序列作随机盐
        long uniqueIdHash = Hashing.murmur3_32_fixed().hashLong(SnowFlakeUtils.uniqueLong()).padToLong();
        // 相减主要是为了让哈希值更小
        String shortLinkUrl = Base62Utils.encodeToBase62String(Math.abs(longLinkHash - uniqueIdHash));
        // 如果数据库中不存在相同的短链，则创建、存储短链对象，并返回短链地址
        if (!hasShortLinkUrlRepeated(shortLinkUrl)) {
            // 创建需要存储的对象
            LocalDateTime createdTime = LocalDateTime.now();
            LocalDateTime expiredTime = createdTime.plusDays(30);
            shortLinkUrl = Base62Utils.encodeToBase62String(longLinkHash);
            ShortLink shortLink = new ShortLink()
                    .setShortLinkUrl(shortLinkUrl)
                    .setLongLinkHash(longLinkHash)
                    .setLongLinkUrl(longLinkUrl)
                    .setStatus(1)
                    .setCreatedTime(createdTime)
                    .setExpiredTime(expiredTime);
            this.save(shortLink);
            return shortLinkUrl;
        }
        // 否则重新
        return regenerateOnHashConflict(longLinkUrl, longLinkHash);
    }
    


}
