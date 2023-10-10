package com.cetc36.chameleon.mq.util;

import lombok.Getter;
import lombok.Setter;

/**
 * 动态注入bean的一些参数
 */
@Getter
@Setter
public class BeanArgBuilder {

    private Object[] constructorArgs;

    private String initMethodName;

    private String destroyMethodName;

    public BeanArgBuilder(Object[] constructorArgs, String initMethodName, String destroyMethodName) {
        this.constructorArgs = constructorArgs;
        this.initMethodName = initMethodName;
        this.destroyMethodName = destroyMethodName;
    }

    public BeanArgBuilder() {
    }
}
