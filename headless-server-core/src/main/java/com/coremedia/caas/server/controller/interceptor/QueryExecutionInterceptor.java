package com.coremedia.caas.server.controller.interceptor;

import com.coremedia.caas.endpoint.caas.QueryDefinition;
import com.coremedia.caas.pd.ProcessingDefinition;
import com.coremedia.caas.server.service.request.ClientIdentification;
import com.coremedia.caas.service.repository.RootContext;

import org.springframework.web.context.request.ServletWebRequest;

import java.util.Map;

public interface QueryExecutionInterceptor {

  boolean preQuery(String tenantId, String siteId, ClientIdentification clientIdentification, RootContext rootContext, ProcessingDefinition processingDefinition, QueryDefinition queryDefinition, Map<String, Object> queryArgs, ServletWebRequest request);

  Object postQuery(Object resultData, String tenantId, String siteId, ClientIdentification clientIdentification, RootContext rootContext, ProcessingDefinition processingDefinition, QueryDefinition queryDefinition, Map<String, Object> queryArgs, ServletWebRequest request);
}