package com.coremedia.caas.server.controller.interceptor;

import com.coremedia.caas.endpoint.caas.QueryDefinition;
import com.coremedia.caas.pd.ProcessingDefinition;
import com.coremedia.caas.server.service.request.ClientIdentification;
import com.coremedia.caas.service.repository.RootContext;

import org.springframework.web.context.request.ServletWebRequest;

import java.util.Map;

public class QueryExecutionInterceptorAdapter implements QueryExecutionInterceptor {

  @Override
  public boolean preQuery(String tenantId, String siteId, ClientIdentification clientIdentification, RootContext rootContext, ProcessingDefinition processingDefinition, QueryDefinition queryDefinition, Map<String, Object> queryArgs, ServletWebRequest request) {
    return true;
  }

  @Override
  public Object postQuery(Object resultData, String tenantId, String siteId, ClientIdentification clientIdentification, RootContext rootContext, ProcessingDefinition processingDefinition, QueryDefinition queryDefinition, Map<String, Object> queryArgs, ServletWebRequest request) {
    return null;
  }
}
