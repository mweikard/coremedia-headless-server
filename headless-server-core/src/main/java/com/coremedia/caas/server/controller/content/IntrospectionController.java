package com.coremedia.caas.server.controller.content;

import com.coremedia.caas.config.ProcessingDefinition;
import com.coremedia.caas.query.QueryDefinition;
import com.coremedia.caas.server.controller.base.GraphQLControllerBase;
import com.coremedia.caas.server.controller.base.ResponseStatusException;
import com.coremedia.caas.server.service.request.ClientIdentification;
import com.coremedia.caas.service.repository.RootContext;
import com.coremedia.caas.service.security.AccessControlViolation;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.introspection.IntrospectionQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

@RestController
@RequestMapping("/caas/v1/{tenantId}/sites/{siteId}/introspection")
public class IntrospectionController extends GraphQLControllerBase {

  private static final Logger LOG = LoggerFactory.getLogger(IntrospectionController.class);


  public IntrospectionController() {
    super("caas.server.content.introspection");
  }


  private Object introspect(RootContext rootContext, ClientIdentification clientIdentification, String queryName, String viewName) {
    ProcessingDefinition processingDefinition = getProcessingDefinition(rootContext, clientIdentification);
    QueryDefinition queryDefinition = getQueryDefinition(processingDefinition, queryName, viewName);
    // run introspection query on base type
    GraphQL g = GraphQL.newGraphQL(queryDefinition.getIntrospectionSchema()).build();
    ExecutionResult result = g.execute(IntrospectionQuery.INTROSPECTION_QUERY);
    if (!result.getErrors().isEmpty()) {
      for (GraphQLError error : result.getErrors()) {
        LOG.error("GraphQL execution error: {}", error.toString());
      }
    }
    return result;
  }


  @RequestMapping(path = "/{queryName}/{viewName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public Object introspect(
          @PathVariable("tenantId") String tenantId,
          @PathVariable("siteId") String siteId,
          @PathVariable("queryName") String queryName,
          @PathVariable("viewName") String viewName,
          ServletWebRequest request) {
    try {
      RootContext rootContext = resolveRootContext(tenantId, siteId, request);
      // determine client
      ClientIdentification clientIdentification = resolveClient(rootContext, request);
      String clientId = clientIdentification.getId().toString();
      String definitionName = clientIdentification.getDefinitionName();
      // run query
      return execute(() -> introspect(rootContext, clientIdentification, queryName, viewName), "tenant", tenantId, "site", siteId, "client", clientId, "pd", definitionName, "query", queryName, "view", viewName);
    } catch (AccessControlViolation e) {
      return handleError(e, request);
    } catch (ResponseStatusException e) {
      return handleError(e, request);
    } catch (Exception e) {
      return handleError(e, request);
    }
  }
}
