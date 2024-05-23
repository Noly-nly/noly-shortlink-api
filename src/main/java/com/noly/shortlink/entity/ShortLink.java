package com.noly.shortlink.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("short_link")
public class ShortLink implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 短链接
     */
    @TableField("short_link_url")
    private String shortLinkUrl;

    /**
     * 长链接 hash 值
     */
    @TableField("long_link_hash")
    private Long longLinkHash;

    /**
     * 长链接
     */
    @TableField("long_link_url")
    private String longLinkUrl;

    /**
     * 短链接可用状态 1:可用，0:不可用
     */
    @TableField("status")
    private Integer status;

    /**
     * 短链接过期时间
     */
    @TableField("expired_time")
    private LocalDateTime expiredTime;

    /**
     * 短链接创建时间
     */
    @TableField("created_time")
    private LocalDateTime createdTime;


}
