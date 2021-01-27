/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.config.spring.util;

import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.config.spring.beans.factory.annotation.DubboConfigAliasPostProcessor;
import org.apache.dubbo.config.spring.beans.factory.annotation.ReferenceAnnotationBeanPostProcessor;
import org.apache.dubbo.config.spring.beans.factory.config.DubboConfigDefaultPropertyValueBeanPostProcessor;
import org.apache.dubbo.config.spring.beans.factory.config.DubboConfigEarlyInitializationPostProcessor;
import org.apache.dubbo.config.spring.context.DubboApplicationListenerRegistrar;
import org.apache.dubbo.config.spring.context.DubboBootstrapApplicationListener;
import org.apache.dubbo.config.spring.context.DubboLifecycleComponentApplicationListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.ArrayList;
import java.util.List;

import static com.alibaba.spring.util.BeanRegistrar.registerInfrastructureBean;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * Dubbo Bean utilities class
 *
 * @since 2.7.6
 */
public abstract class DubboBeanUtils {

    private static final Logger logger = LoggerFactory.getLogger(DubboBeanUtils.class);

    /**
     * Register the common beans
     *
     * @param registry {@link BeanDefinitionRegistry}
     * @see ReferenceAnnotationBeanPostProcessor
     * @see DubboConfigDefaultPropertyValueBeanPostProcessor
     * @see DubboConfigAliasPostProcessor
     * @see DubboLifecycleComponentApplicationListener
     * @see DubboBootstrapApplicationListener
     */
    public static void registerCommonBeans(BeanDefinitionRegistry registry) {

        // Since 2.5.7 Register @Reference Annotation Bean Processor as an infrastructure Bean
        registerInfrastructureBean(registry, ReferenceAnnotationBeanPostProcessor.BEAN_NAME,
                ReferenceAnnotationBeanPostProcessor.class);

        // Since 2.7.4 [Feature] https://github.com/apache/dubbo/issues/5093
        registerInfrastructureBean(registry, DubboConfigAliasPostProcessor.BEAN_NAME,
                DubboConfigAliasPostProcessor.class);

        // Since 2.7.9 Register DubboApplicationListenerRegister as an infrastructure Bean
        // https://github.com/apache/dubbo/issues/6559

        // Since 2.7.5 Register DubboLifecycleComponentApplicationListener as an infrastructure Bean
        // registerInfrastructureBean(registry, DubboLifecycleComponentApplicationListener.BEAN_NAME,
        //        DubboLifecycleComponentApplicationListener.class);

        // Since 2.7.4 Register DubboBootstrapApplicationListener as an infrastructure Bean
        // registerInfrastructureBean(registry, DubboBootstrapApplicationListener.BEAN_NAME,
        //        DubboBootstrapApplicationListener.class);

        registerInfrastructureBean(registry, DubboApplicationListenerRegistrar.BEAN_NAME,
                DubboApplicationListenerRegistrar.class);

        // Since 2.7.6 Register DubboConfigDefaultPropertyValueBeanPostProcessor as an infrastructure Bean
        registerInfrastructureBean(registry, DubboConfigDefaultPropertyValueBeanPostProcessor.BEAN_NAME,
                DubboConfigDefaultPropertyValueBeanPostProcessor.class);

        // Since 2.7.9 Register DubboConfigEarlyInitializationPostProcessor as an infrastructure Bean
        registerInfrastructureBean(registry, DubboConfigEarlyInitializationPostProcessor.BEAN_NAME,
                DubboConfigEarlyInitializationPostProcessor.class);
    }

    /**
     * Get bean by name and type
     * @param beanFactory
     * @param beanName
     * @param beanType
     * @param <T>
     * @return
     */
    public static <T> T getBean(ListableBeanFactory beanFactory, String beanName, Class<T> beanType) {
        Object bean = null;
        try {
            bean = beanFactory.getBean(beanName);
        } catch (NoSuchBeanDefinitionException e) {
            // ignore NoSuchBeanDefinitionException
        } catch (BeansException e) {
            logger.warn(String.format("get bean failure, name: %s, type: %s", beanName, beanType.getName()), e);
        }
        if (bean == null) {
            return null;
        }
        if (beanType.isAssignableFrom(bean.getClass())) {
            return (T) bean;
        }
        logger.warn(String.format("bean type not match, name: %s, expected type: %s, actual type: %s",
                beanName, beanType.getName(), bean.getClass().getName()));
        return null;
    }

    /**
     * Get beans by names and filter by type
     * @param beanFactory
     * @param beanNames
     * @param beanType
     * @param <T>
     * @return
     */
    public static <T> List<T> getBeans(ListableBeanFactory beanFactory, String[] beanNames, Class<T> beanType) {
        if (isEmpty(beanNames)) {
            return emptyList();
        }

        List<T> beans = new ArrayList<T>(beanNames.length);
        for (String beanName : beanNames) {
            T bean = getBean(beanFactory, beanName, beanType);
            if (bean != null) {
                beans.add(bean);
            }
        }
        return unmodifiableList(beans);
    }
}
