package com.wing.mybatis.product.domain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

import com.wing.mybatis.sample.common.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * table name：user
 *
 * @mbg.generated
 * @date 2022-06-05 09:39:08
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User implements Cloneable, Serializable {
    /**
     * user.id
     * 主键
     *
     * @mbg.generated
     */
    private Long id;

    /**
     * user.name
     * 姓名
     *
     * @mbg.generated
     */
    private String name;

    /**
     * user.age
     * 年龄
     *
     * @mbg.generated
     */
    private Integer age;

    /**
     * user.address
     * 地址
     *
     * @mbg.generated
     */
    private String location;

    /**
     * user.status
     * 状态
     *
     * @mbg.generated
     */
    private Status status;

    /**
     * user.create_time
     * 创建时间
     *
     * @mbg.generated
     */
    private Date createTime;

    /**
     * user.update_time
     * 更新时间
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * user.feature
     * 属性
     *
     * @mbg.generated
     */
    private byte[] feature;

    @Override
    public User clone() throws CloneNotSupportedException {
        return (User)super.clone();
    }

    public User deepClone() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(this);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        User target = (User)objectInputStream.readObject();
        objectOutputStream.close();
        objectInputStream.close();
        return target;
    }
}