package com.coremedia.caas.endpoint.graphql;

import com.coremedia.caas.schema.Types;
import com.coremedia.caas.schema.datafetcher.query.ContentProxyDataFetcher;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;

import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

public class ContentQueryFieldBuilder implements QueryFieldBuilder {

  private ContentRepository contentRepository;
  private SitesService sitesService;

  private Map<String, String> fields;


  public ContentQueryFieldBuilder(ContentRepository contentRepository, SitesService sitesService) {
    this.contentRepository = contentRepository;
    this.sitesService = sitesService;
  }


  @Override
  public List<GraphQLFieldDefinition> build() {
    if (fields != null && !fields.isEmpty()) {
      return fields.entrySet().stream()
              .map(e -> newFieldDefinition()
                      .name(e.getKey())
                      .argument(GraphQLArgument.newArgument().name("id").type(Scalars.GraphQLInt).build())
                      .type(Types.getType(e.getValue(), true))
                      .dataFetcher(new ContentProxyDataFetcher(e.getValue(), contentRepository, sitesService))
                      .build())
              .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }


  /*
   * YAML setters
   */

  @SuppressWarnings("unused")
  public void setFields(Map<String, String> fields) {
    this.fields = fields;
  }
}
