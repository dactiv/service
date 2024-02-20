package com.github.dactiv.service.dmp.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.service.commons.service.SystemConstants;
import com.github.dactiv.service.commons.service.domain.meta.ElasticsearchSyncMeta;
import com.github.dactiv.service.dmp.config.ApplicationConfig;
import com.rabbitmq.client.Channel;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * es 同步数据消费者
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchSyncConsumer {

    public static final String MAPPING_FILE_SUFFIX = ".json";

    public static final String MAPPING_PROPERTIES = "properties";

    private static final Map<String, Set<String>> MAPPING_FIELD_CACHE = new LinkedHashMap<>();

    private final ElasticsearchOperations elasticsearchOperations;

    private final ApplicationConfig applicationConfig;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = SystemConstants.ELASTICSEARCH_SYNC_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.DMP_RABBIT_EXCHANGE),
                    key = SystemConstants.ELASTICSEARCH_SYNC_QUEUE_NAME
            )
    )
    public void onMessage(String data,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        ElasticsearchSyncMeta meta = Casts.readValue(data, ElasticsearchSyncMeta.class);

        if (Objects.isNull(meta.getObject())) {
            log.warn("开始同步:" + meta + "到 es 时，发现 object 内容为空，不做同步操作。");
            channel.basicNack(tag, false, false);
            return ;
        }

        IndexCoordinates indexCoordinates = IndexCoordinates.of(meta.getIndexName());
        createIndexIfNotExists(indexCoordinates);

        Object object = meta.getObject();

        Set<String> fields = MAPPING_FIELD_CACHE.get(meta.getIndexName());
        if (CollectionUtils.isNotEmpty(fields)) {
            Map<String, Object> mappingValue = new LinkedHashMap<>();
            fields.forEach(s -> mappingValue.put(s, meta.getObject().get(s)));
            object = mappingValue;
        }

        if (StringUtils.isEmpty(meta.getId())) {
            meta.setId(UUID.randomUUID().toString());
        }

        IndexQuery indexQuery = new IndexQueryBuilder()
                .withId(meta.getId())
                .withObject(object)
                .build();

        elasticsearchOperations.index(indexQuery, indexCoordinates);

        channel.basicAck(tag, true);
    }

    private void createIndexIfNotExists(IndexCoordinates indexCoordinates) throws IOException {

        IndexOperations indexOperations = elasticsearchOperations.indexOps(indexCoordinates);

        if (indexOperations.exists()) {
            return ;
        }

        indexOperations.create();

        Resource resource = new ClassPathResource(applicationConfig.getElasticsearchMappingPath() + AntPathMatcher.DEFAULT_PATH_SEPARATOR + indexCoordinates.getIndexName() + MAPPING_FILE_SUFFIX);
        if (!resource.exists()) {
            return ;
        }

        if (log.isDebugEnabled()) {
            log.debug("对:" + indexCoordinates.getIndexName() + "找到对应的 mapping.json 文件，同步 mapping 信息");
        }

        try (InputStream input = resource.getInputStream()) {
            Map<String, Object> mapping = Casts.readValue(input, new TypeReference<>() {});
            Map<String, Object> properties = Casts.cast(mapping.get(MAPPING_PROPERTIES));

            MAPPING_FIELD_CACHE.put(indexCoordinates.getIndexName(), properties.keySet());
            indexOperations.putMapping(Document.from(mapping));
        }
    }

}
