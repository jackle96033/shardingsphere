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

package org.apache.shardingsphere.agent.plugin.metrics.prometheus.collector.type;

import io.prometheus.client.Counter;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.CounterMetricsCollector;

/**
 * Prometheus counter wrapper.
 */
public final class PrometheusCounterCollector implements CounterMetricsCollector {
    
    private final Counter counter;
    
    public PrometheusCounterCollector(final MetricConfiguration config) {
        counter = Counter.build().name(config.getId()).help(config.getHelp()).labelNames(config.getLabels().toArray(new String[0])).register();
    }
    
    @Override
    public void inc() {
        counter.inc(1d);
    }
    
    @Override
    public void inc(final String... labels) {
        counter.labels(labels).inc(1d);
    }
}
