package com.ensurebit.ark.kvm.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("turn_activity")
@AllArgsConstructor
@NoArgsConstructor
public class TurnActivityDO implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer bytesSent;

    private Integer bytesReceived;

    private Date createdAt;

}
