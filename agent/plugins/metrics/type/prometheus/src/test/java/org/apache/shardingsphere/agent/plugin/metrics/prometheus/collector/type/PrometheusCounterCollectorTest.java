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
import org.junit.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class PrometheusCounterCollectorTest {
    
    @Test
    public void assertCreate() throws ReflectiveOperationException {
        PrometheusCounterCollector collector = new PrometheusCounterCollector(new MetricConfiguration("foo_counter", "COUNTER", "foo_help", Collections.emptyList(), Collections.emptyMap()));
        collector.inc();
        Counter counter = (Counter) Plugins.getMemberAccessor().get(PrometheusCounterCollector.class.getDeclaredField("counter"), collector);
        assertThat(counter.get(), is(1d));
    }
}
