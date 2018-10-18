package com.coremedia.caas.server.controller.caas;

import com.coremedia.caas.endpoint.caas.CaasEndpoint;
import com.coremedia.caas.endpoint.caas.QueryDefinition;
import com.coremedia.caas.execution.ExecutionContext;
import com.coremedia.caas.pd.ProcessingDefinition;
import com.coremedia.caas.server.controller.base.GraphQLControllerBase;
import com.coremedia.caas.server.controller.base.ResponseStatusException;
import com.coremedia.caas.server.controller.interceptor.QueryExecutionInterceptor;
import com.coremedia.caas.server.service.request.ClientIdentification;
import com.coremedia.caas.service.repository.RootContext;

import com.google.common.collect.Lists;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.introspection.IntrospectionQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;

public class AbstractCaasController extends GraphQLControllerBase {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractCaasController.class);


  protected AbstractCaasController(String timerName) {
    super(timerName);
  }


  private CaasEndpoint getCaasEndpoint(ProcessingDefinition processingDefinition) {
    CaasEndpoint endpoint = processingDefinition.getCaasEndpoint();
    if (!endpoint.isEnabled()) {
      LOG.error("CaaS endpoint not enabled for processing definition '{}'", processingDefinition.getName());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    return endpoint;
  }

  private QueryDefinition getQueryDefinition(ProcessingDefinition processingDefinition, String queryName, String viewName) {
    CaasEndpoint endpoint = getCaasEndpoint(processingDefinition);
    QueryDefinition queryDefinition = endpoint.getQueryRegistry().getQueryDefinition(queryName, viewName);
    if (queryDefinition == null) {
      LOG.error("No query '{}#{}' found in processing definition '{}'", queryName, viewName, processingDefinition.getName());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    return queryDefinition;
  }


  @SuppressWarnings("WeakerAccess")
  protected Object introspectQuery(RootContext rootContext, ClientIdentification clientIdentification, String queryName, String viewName) {
    ProcessingDefinition processingDefinition = getProcessingDefinition(rootContext, clientIdentification);
    QueryDefinition queryDefinition = getQueryDefinition(processingDefinition, queryName, viewName);
    // run introspection query on base type
    GraphQL graphQL = GraphQL.newGraphQL(queryDefinition.getIntrospectionSchema()).build();
    ExecutionResult result = graphQL.execute(IntrospectionQuery.INTROSPECTION_QUERY);
    if (!result.getErrors().isEmpty()) {
      for (GraphQLError error : result.getErrors()) {
        LOG.error("GraphQL execution error: {}", error.toString());
      }
    }
    return result;
  }


  @SuppressWarnings("WeakerAccess")
  protected Object executeQuery(@NotNull String tenantId, @NotNull String siteId, @NotNull RootContext rootContext, @NotNull ClientIdentification clientIdentification, @NotNull String queryName, @NotNull String viewName, Map<String, Object> queryArgs, ServletWebRequest request) {
    ProcessingDefinition processingDefinition = getProcessingDefinition(rootContext, clientIdentification);
    CaasEndpoint endpoint = getCaasEndpoint(processingDefinition);
    QueryDefinition queryDefinition = getQueryDefinition(processingDefinition, queryName, viewName);
    // run pre query interceptors
    if (queryInterceptors != null) {
      for (QueryExecutionInterceptor executionInterceptor : queryInterceptors) {
        if (!executionInterceptor.preQuery(tenantId, siteId, clientIdentification, rootContext, processingDefinition, queryDefinition, queryArgs, request)) {
          return null;
        }
      }
    }
    Object target = rootContext.getTarget();
    // resolve specialized query string based on target type
    String query;
    if (target instanceof List) {
      query = queryDefinition.getQuery();
    }
    else {
      query = queryDefinition.getQuery(processingDefinition.getSchemaService().getObjectType(target).getName());
    }
    // create new runtime context for capturing all required runtime services and state
    ExecutionContext context = new ExecutionContext(processingDefinition, serviceRegistry, rootContext);
    // run query
    ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            .query(query)
            .root(target)
            .context(context)
            .variables(queryArgs)
            .build();
    ExecutionResult result = GraphQL.newGraphQL(queryDefinition.getQuerySchema(target))
            .preparsedDocumentProvider(endpoint.getDocumentProvider())
            .build()
            .execute(executionInput);
    if (!result.getErrors().isEmpty()) {
      for (GraphQLError error : result.getErrors()) {
        LOG.error("GraphQL execution error: {}", error.toString());
      }
    }
    Object resultData = result.getData();
    // run post query interceptors
    if (queryInterceptors != null) {
      for (QueryExecutionInterceptor executionInterceptor : Lists.reverse(queryInterceptors)) {
        Object transformedData = executionInterceptor.postQuery(resultData, tenantId, siteId, clientIdentification, rootContext, processingDefinition, queryDefinition, queryArgs, request);
        if (transformedData != null) {
          resultData = transformedData;
        }
      }
    }
    // send response with appropriate cache headers
    CacheControl cacheControl;
    if (serviceConfig.isPreview()) {
      cacheControl = CacheControl.noCache();
    }
    else {
      long cacheFor = serviceConfig.getCacheTime();
      // allow individual query override
      String queryCacheFor = queryDefinition.getOption("cacheFor");
      if (queryCacheFor != null) {
        try {
          cacheFor = Long.parseLong(queryCacheFor);
        } catch (NumberFormatException e) {
          // just log a warning and use default
          LOG.warn("Invalid query cache time specified: {}", queryCacheFor);
        }
      }
      long maxAge = getMaxAge(cacheFor);
      cacheControl = CacheControl.maxAge(maxAge, TimeUnit.SECONDS).mustRevalidate();
    }
    return ResponseEntity.ok()
            .cacheControl(cacheControl)
            .contentType(MediaType.APPLICATION_JSON)
            .body(resultData);
  }
}
