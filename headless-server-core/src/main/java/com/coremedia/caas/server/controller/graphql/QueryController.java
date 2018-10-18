package com.coremedia.caas.server.controller.graphql;

import com.coremedia.caas.endpoint.graphql.GraphQLEndpoint;
import com.coremedia.caas.execution.ExecutionContext;
import com.coremedia.caas.pd.ProcessingDefinition;
import com.coremedia.caas.server.controller.base.ResponseStatusException;
import com.coremedia.caas.server.controller.interceptor.QueryExecutionInterceptor;
import com.coremedia.caas.server.service.request.ClientIdentification;
import com.coremedia.caas.service.repository.RootContext;
import com.coremedia.caas.service.security.AccessControlViolation;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.execution.preparsed.PreparsedDocumentProvider;
import graphql.schema.GraphQLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Map;

@RestController("graphqlQueryController")
public class QueryController extends AbstractGraphQLController {

  private static final Logger LOG = LoggerFactory.getLogger(QueryController.class);


  public QueryController() {
    super("graphql.server.query");
  }


  private Object query(String tenantId, String siteId, RootContext rootContext, ClientIdentification clientIdentification, String query, Map<String, Object> queryArgs, ServletWebRequest request) {
    ProcessingDefinition processingDefinition = getProcessingDefinition(rootContext, clientIdentification);
    GraphQLEndpoint endpoint = getGraphQLEndpoint(processingDefinition);
    GraphQLSchema querySchema = endpoint.getQuerySchema();
    PreparsedDocumentProvider queryRegistry = endpoint.getDocumentProvider();
    // run pre query interceptors
    if (queryInterceptors != null) {
      for (QueryExecutionInterceptor executionInterceptor : queryInterceptors) {
        if (!executionInterceptor.preQuery(tenantId, siteId, clientIdentification, rootContext, processingDefinition, null, queryArgs, request)) {
          return null;
        }
      }
    }
    // create new runtime context for capturing all required runtime services and state
    ExecutionContext context = new ExecutionContext(processingDefinition, serviceRegistry, rootContext);
    // run query
    ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            .query(query)
            .root(null)
            .context(context)
            .variables(queryArgs)
            .build();
    ExecutionResult result = GraphQL.newGraphQL(querySchema)
            .preparsedDocumentProvider(queryRegistry)
            .build()
            .execute(executionInput);
    if (!result.getErrors().isEmpty()) {
      for (GraphQLError error : result.getErrors()) {
        LOG.error("GraphQL execution error: {}", error.toString());
      }
    }
    return result;
  }


  @RequestMapping(path = "/graphql/v1/{tenantId}/sites/{siteId}/query", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public Object query(@PathVariable("tenantId") String tenantId,
                      @PathVariable("siteId") String siteId,
                      @RequestBody QueryRequestBody requestBody,
                      ServletWebRequest request) {
    try {
      RootContext rootContext = resolveRootContext(tenantId, siteId, request);
      // determine client
      ClientIdentification clientIdentification = resolveClient(rootContext, request);
      String clientId = clientIdentification.getId().toString();
      String definitionName = clientIdentification.getDefinitionName();
      // fetch query
      String query = requestBody.getQuery();
      // determine query arguments
      Map<String, Object> queryArgs = requestBody.getVariables();
      // initialize expression evaluator
      serviceRegistry.getExpressionEvaluator().init(queryArgs);
      // run query
      return execute(() -> query(tenantId, siteId, rootContext, clientIdentification, query, queryArgs, request), "tenant", tenantId, "site", siteId, "client", clientId, "pd", definitionName);
    } catch (AccessControlViolation e) {
      return handleError(e, request);
    } catch (ResponseStatusException e) {
      return handleError(e, request);
    } catch (Exception e) {
      return handleError(e, request);
    }
  }
}
