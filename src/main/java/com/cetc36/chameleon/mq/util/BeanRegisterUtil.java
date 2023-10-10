package com.cetc36.chameleon.mq.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

import static org.springframework.util.StringUtils.hasText;

/**
 * 工具类
 */
@Slf4j
public class BeanRegisterUtil {

    /**
     * Register Bean
     *
     * @param beanDefinitionRegistry {@link BeanDefinitionRegistry}
     * @param beanType               the type of bean
     * @param beanName               the name of bean
     * @param beanArgBuilder         the args of bean
     * @return if it's a first time to register, return <code>true</code>, or <code>false</code>
     */
    public static boolean registerBean(
            BeanDefinitionRegistry beanDefinitionRegistry, String beanName, Class<?> beanType, BeanArgBuilder beanArgBuilder) {

        if (beanDefinitionRegistry.containsBeanDefinition(beanName)) {
            log.error("Bean register fail, beanName:{}", beanName);
            return false;
        }

        // 构造函数，初始化 、销毁方法设置
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(beanType);
        if (beanArgBuilder != null) {
            if (beanArgBuilder.getConstructorArgs() != null) {
                for (Object object : beanArgBuilder.getConstructorArgs()) {
                    beanDefinitionBuilder.addConstructorArgValue(object);
                }
            }
            if (hasText(beanArgBuilder.getInitMethodName())) {
                beanDefinitionBuilder.setInitMethodName(beanArgBuilder.getInitMethodName());
            }
            if (hasText(beanArgBuilder.getDestroyMethodName())) {
                beanDefinitionBuilder.setDestroyMethodName(beanArgBuilder.getDestroyMethodName());
            }
        }

        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        beanDefinitionRegistry.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());

        if (log.isInfoEnabled()) {
            if (beanDefinition instanceof RootBeanDefinition) {
                log.info("The Infrastructure bean definition [" + beanDefinition
                        + "with name [" + beanName + "] has been registered.");
            }
        }

        return true;
    }
}
