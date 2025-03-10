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

import org.apache.shardingsphere.agent.plugin.metrics.prometheus.ProxyContextRestorer;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public final class ProxyMetaDataInfoCollectorTest extends ProxyContextRestorer {
    
    @Test
    public void assertCollect() {
        ProxyContext.init(mock(ContextManager.class, RETURNS_DEEP_STUBS));
        assertFalse(new ProxyMetaDataInfoCollector().collect().isEmpty());
    }
}
