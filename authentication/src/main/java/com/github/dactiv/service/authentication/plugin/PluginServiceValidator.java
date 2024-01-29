package com.github.dactiv.service.authentication.plugin;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.retry.Retryable;
import com.github.dactiv.framework.nacos.event.NacosService;
import com.github.dactiv.framework.nacos.event.NacosServiceListenerValidator;
import com.github.dactiv.framework.nacos.task.annotation.NacosCronScheduled;
import com.github.dactiv.framework.spring.security.plugin.PluginEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 插件服务校验
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Lazy)
public class PluginServiceValidator implements NacosServiceListenerValidator {

    @Lazy
    private final PluginResourceService pluginResourceService;

    private final List<String> exceptionServices = new LinkedList<>();

    @Override
    public boolean isSupport(NacosService nacosService) {
        return true;
    }

    @Override
    public boolean subscribeValid(NacosService nacosService) {

        if (pluginResourceService.getApplicationConfig().getIgnorePluginService().contains(nacosService.getName())) {
            return false;
        }

        if (exceptionServices.contains(nacosService.getName())) {
            return false;
        }

        Optional<Instance> optional = nacosService
                .getInstances()
                .stream()
                .max(pluginResourceService::comparingInstanceVersion);

        if (optional.isEmpty()) {
            return false;
        }

        try {

            Instance instance = optional.get();

            Map<String, Object> data = pluginResourceService.getInstanceInfo(instance);

            if (MapUtils.isEmpty(data)) {
                return false;
            }

            if (!data.containsKey(PluginEndpoint.DEFAULT_PLUGIN_KEY_NAME)) {
                return false;
            }

        } catch (Exception e) {
            log.warn("获取服务 [" + nacosService.getName() + "] 的插件内容失败");

            if (HttpStatusCodeException.class.isAssignableFrom(e.getClass())) {
                HttpStatusCodeException exception = Casts.cast(e);

                if (Retryable.DEFAULT_SUPPORT_HTTP_STATUS.contains(exception.getStatusCode().value())) {
                    return false;
                }
            }

            exceptionServices.add(nacosService.getName());
            return false;
        }

        return true;
    }

    /**
     * 清除异常服务内容
     */
    @NacosCronScheduled(cron = "${dactiv.service.authentication.event.clear-exception-services-cron:0 0/5 * * * ? }")
    public void clearExceptionServices() {
        exceptionServices.clear();
    }
}
