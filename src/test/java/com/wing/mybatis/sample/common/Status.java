package com.wing.mybatis.sample.common;

import java.util.Arrays;
import java.util.Optional;

import lombok.Getter;

public enum Status {
    /**
     * 无效状态
     */
    DISABLE(0, "无效状态"),
    /**
     * 有效状态
     */
    AVAILABLE(1, "有效状态"),
    /**
     * 软删除态
     */
    DELETE(-1, "软删除态"),
    ;

    @Getter
    final private Integer index;
    @Getter
    final private String description;

    Status(int index, String description) {
        this.index = index;
        this.description = description;
    }

    public static Status of(final int index) {
        final Optional<Status> optional = Arrays.stream(values())
            .filter(unit -> unit.getIndex().equals(index)).findFirst();
        return optional.orElse(null);
    }

}
