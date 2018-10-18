package com.coremedia.caas.server.controller.graphql;

import com.coremedia.caas.endpoint.graphql.GraphQLEndpoint;
import com.coremedia.caas.pd.ProcessingDefinition;
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

@RestController("graphqlIntrospectionController")
public class IntrospectionController extends AbstractGraphQLController {

  private static final Logger LOG = LoggerFactory.getLogger(IntrospectionController.class);


  public IntrospectionController() {
    super("graphql.server.introspection");
  }


  private Object introspect(RootContext rootContext, ClientIdentification clientIdentification, ServletWebRequest request) {
    ProcessingDefinition processingDefinition = getProcessingDefinition(rootContext, clientIdentification);
    GraphQLEndpoint endpoint = getGraphQLEndpoint(processingDefinition);
    GraphQL graphQL = GraphQL.newGraphQL(endpoint.getQuerySchema()).build();
    ExecutionResult result = graphQL.execute(IntrospectionQuery.INTROSPECTION_QUERY);
    if (!result.getErrors().isEmpty()) {
      for (GraphQLError error : result.getErrors()) {
        LOG.error("GraphQL execution error: {}", error.toString());
      }
    }
    return result;
  }


  @RequestMapping(path = "/graphql/v1/{tenantId}/sites/{siteId}/introspection", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public Object introspect(@PathVariable("tenantId") String tenantId,
                           @PathVariable("siteId") String siteId,
                           ServletWebRequest request) {
    try {
      RootContext rootContext = resolveRootContext(tenantId, siteId, request);
      // determine client
      ClientIdentification clientIdentification = resolveClient(rootContext, request);
      String clientId = clientIdentification.getId().toString();
      String definitionName = clientIdentification.getDefinitionName();
      // run query
      return execute(() -> introspect(rootContext, clientIdentification, request), "tenant", tenantId, "site", siteId, "client", clientId, "pd", definitionName);
    } catch (AccessControlViolation e) {
      return handleError(e, request);
    } catch (ResponseStatusException e) {
      return handleError(e, request);
    } catch (Exception e) {
      return handleError(e, request);
    }
  }
}
