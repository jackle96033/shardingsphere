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

package org.apache.shardingsphere.agent.plugin.tracing.zipkin.advice;

import brave.Span;
import brave.Tracing;
import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.api.advice.type.InstanceMethodAdvice;
import org.apache.shardingsphere.agent.plugin.core.util.AgentReflectionUtil;
import org.apache.shardingsphere.agent.plugin.tracing.zipkin.constant.ZipkinConstants;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;

import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * JDBC executor callback advice executor.
 */
public final class JDBCExecutorCallbackAdvice implements InstanceMethodAdvice {
    
    private static final String OPERATION_NAME = "/ShardingSphere/executeSQL/";
    
    @Override
    @SneakyThrows({ReflectiveOperationException.class, SQLException.class})
    @SuppressWarnings("unchecked")
    public void beforeMethod(final TargetAdviceObject target, final Method method, final Object[] args, final String pluginType) {
        Span root = (Span) ((Map<String, Object>) args[2]).get(ZipkinConstants.ROOT_SPAN);
        Span span = null == root ? Tracing.currentTracer().nextSpan().name(OPERATION_NAME) : Tracing.currentTracer().newChild(root.context()).name(OPERATION_NAME);
        span.tag(ZipkinConstants.Tags.COMPONENT, ZipkinConstants.COMPONENT_NAME);
        span.tag(ZipkinConstants.Tags.DB_TYPE, ZipkinConstants.DB_TYPE_VALUE);
        JDBCExecutionUnit executionUnit = (JDBCExecutionUnit) args[0];
        Map<String, DatabaseType> storageTypes = AgentReflectionUtil.getFieldValue(target, "storageTypes");
        DataSourceMetaData metaData = AgentReflectionUtil.invokeMethod(
                JDBCExecutorCallback.class.getDeclaredMethod("getDataSourceMetaData", DatabaseMetaData.class, DatabaseType.class),
                target, executionUnit.getStorageResource().getConnection().getMetaData(), storageTypes.get(executionUnit.getExecutionUnit().getDataSourceName()));
        span.tag(ZipkinConstants.Tags.DB_INSTANCE, executionUnit.getExecutionUnit().getDataSourceName());
        span.tag(ZipkinConstants.Tags.PEER_HOSTNAME, metaData.getHostname());
        span.tag(ZipkinConstants.Tags.PEER_PORT, String.valueOf(metaData.getPort()));
        span.tag(ZipkinConstants.Tags.DB_STATEMENT, executionUnit.getExecutionUnit().getSqlUnit().getSql());
        span.tag(ZipkinConstants.Tags.DB_BIND_VARIABLES, executionUnit.getExecutionUnit().getSqlUnit().getParameters().toString());
        span.start();
        target.setAttachment(span);
    }
    
    @Override
    public void afterMethod(final TargetAdviceObject target, final Method method, final Object[] args, final Object result, final String pluginType) {
        ((Span) target.getAttachment()).finish();
    }
    
    @Override
    public void onThrowing(final TargetAdviceObject target, final Method method, final Object[] args, final Throwable throwable, final String pluginType) {
        ((Span) target.getAttachment()).error(throwable);
    }
}
