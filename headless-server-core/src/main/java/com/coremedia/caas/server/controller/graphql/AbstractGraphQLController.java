package com.coremedia.caas.server.controller.graphql;

import com.coremedia.caas.endpoint.graphql.GraphQLEndpoint;
import com.coremedia.caas.pd.ProcessingDefinition;
import com.coremedia.caas.server.controller.base.GraphQLControllerBase;
import com.coremedia.caas.server.controller.base.ResponseStatusException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class AbstractGraphQLController extends GraphQLControllerBase {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractGraphQLController.class);


  protected AbstractGraphQLController(String timerName) {
    super(timerName);
  }


  @SuppressWarnings("WeakerAccess")
  protected GraphQLEndpoint getGraphQLEndpoint(ProcessingDefinition processingDefinition) {
    GraphQLEndpoint endpoint = processingDefinition.getGraphQLEndpoint();
    if (!endpoint.isEnabled()) {
      LOG.error("GraphQL endpoint not enabled for processing definition '{}'", processingDefinition.getName());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    return endpoint;
  }
}
