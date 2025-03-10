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

package org.apache.shardingsphere.agent.plugin.metrics.prometheus.collector.business.proxy;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.collector.PrometheusCollectorFactory;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Proxy meta data information collector.
 */
@Slf4j
public final class ProxyMetaDataInfoCollector extends Collector {
    
    private static final String PROXY_METADATA_INFO_METRIC_KEY = "proxy_meta_data_info";
    
    private static final String LOGIC_DB_COUNT = "schema_count";
    
    private static final String ACTUAL_DB_COUNT = "database_count";
    
    private static final PrometheusCollectorFactory FACTORY = new PrometheusCollectorFactory();
    
    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> result = new LinkedList<>();
        GaugeMetricFamily metaDataInfo = FACTORY.createGaugeMetricFamily(PROXY_METADATA_INFO_METRIC_KEY);
        if (null != ProxyContext.getInstance().getContextManager()) {
            collectProxy(metaDataInfo);
            result.add(metaDataInfo);
        }
        return result;
    }
    
    private void collectProxy(final GaugeMetricFamily metricFamily) {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        metricFamily.addMetric(Collections.singletonList(LOGIC_DB_COUNT), metaDataContexts.getMetaData().getDatabases().size());
        metricFamily.addMetric(Collections.singletonList(ACTUAL_DB_COUNT), getDatabaseNames(metaDataContexts).size());
    }
    
    private Collection<String> getDatabaseNames(final MetaDataContexts metaDataContexts) {
        Collection<String> result = new HashSet<>();
        for (ShardingSphereDatabase each : metaDataContexts.getMetaData().getDatabases().values()) {
            result.addAll(getDatabaseNames(each));
        }
        return result;
    }
    
    private Collection<String> getDatabaseNames(final ShardingSphereDatabase database) {
        Collection<String> result = new HashSet<>();
        for (DataSource each : database.getResourceMetaData().getDataSources().values()) {
            getDatabaseName(each).ifPresent(result::add);
        }
        return result;
    }
    
    private Optional<String> getDatabaseName(final DataSource dataSource) {
        Object jdbcUrl = DataSourcePropertiesCreator.create(dataSource).getAllStandardProperties().get("url");
        if (null == jdbcUrl) {
            log.info("Can not get JDBC URL.");
            return Optional.empty();
        }
        try {
            URI uri = new URI(jdbcUrl.toString().substring(5));
            if (null != uri.getPath()) {
                return Optional.of(uri.getPath());
            }
        } catch (final URISyntaxException | NullPointerException ignored) {
            log.info("Unsupported JDBC URL by URI: {}.", jdbcUrl);
        }
        return Optional.empty();
    }
}
