package com.coremedia.caas.server.controller.base;

import com.coremedia.caas.pd.ProcessingDefinition;
import com.coremedia.caas.pd.ProcessingDefinitionCacheKey;
import com.coremedia.caas.server.CaasServiceConfig;
import com.coremedia.caas.server.controller.interceptor.QueryExecutionInterceptor;
import com.coremedia.caas.server.service.request.ClientIdentification;
import com.coremedia.caas.server.service.request.GlobalParameters;
import com.coremedia.caas.service.ServiceRegistry;
import com.coremedia.caas.service.repository.RootContext;
import com.coremedia.cache.Cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class GraphQLControllerBase extends ControllerBase {

  private static final Logger LOG = LoggerFactory.getLogger(GraphQLControllerBase.class);


  @Autowired
  protected ApplicationContext applicationContext;

  @Autowired
  protected Cache cache;

  @Autowired
  protected CaasServiceConfig serviceConfig;

  @Autowired
  protected ServiceRegistry serviceRegistry;

  @Autowired
  @Qualifier("staticProcessingDefinitions")
  protected Map<String, ProcessingDefinition> staticProcessingDefinitions;

  @Autowired(required = false)
  protected List<QueryExecutionInterceptor> queryInterceptors;


  protected GraphQLControllerBase(String timerName) {
    super(timerName);
  }


  protected Map<String, Object> getQueryArgs(ServletWebRequest request) {
    return request.getParameterMap().entrySet().stream()
            .filter(e -> !GlobalParameters.GLOBAL_BLACKLIST.contains(e.getKey()))
            .filter(e -> {
              String[] v = e.getValue();
              return v != null && v.length > 0 && v[0] != null;
            })
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()[0]));
  }


  protected ProcessingDefinition getProcessingDefinition(RootContext rootContext, ClientIdentification clientIdentification) {
    String definitionName = clientIdentification.getDefinitionName();
    // repository defined runtime definition
    ProcessingDefinitionCacheKey processingDefinitionCacheKey = new ProcessingDefinitionCacheKey(rootContext.getSite().getSiteIndicator(), settingsService, sitesService, applicationContext);
    ProcessingDefinition processingDefinition = cache.get(processingDefinitionCacheKey).get(definitionName);
    // fallback to static definition
    if (processingDefinition == null) {
      processingDefinition = staticProcessingDefinitions.get(definitionName);
    }
    if (processingDefinition == null || processingDefinition == ProcessingDefinition.INVALID) {
      LOG.error("No valid processing definition found for name '{}'", definitionName);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    return processingDefinition;
  }
}
