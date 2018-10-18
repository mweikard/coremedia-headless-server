package com.coremedia.caas.pd;

import com.coremedia.caas.endpoint.caas.CaasEndpoint;
import com.coremedia.caas.endpoint.graphql.GraphQLEndpoint;
import com.coremedia.caas.link.LinkBuilder;
import com.coremedia.caas.richtext.RichtextTransformerRegistry;
import com.coremedia.caas.schema.SchemaService;

import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.stream.Collectors;

public class ProcessingDefinition {

  public static ProcessingDefinition INVALID = new ProcessingDefinition();


  private String name;
  private String description;
  private String defaultRichtextFormat;

  private Map<String, LinkBuilder> linkBuilders;

  private CaasEndpoint caasEndpoint;
  private GraphQLEndpoint graphQLEndpoint;

  private RichtextTransformerRegistry richtextTransformerRegistry;
  private SchemaService schemaService;

  private ApplicationContext applicationContext;


  private ProcessingDefinition() {
  }

  public ProcessingDefinition(String name, SchemaService schemaService, RichtextTransformerRegistry richtextTransformerRegistry, ApplicationContext applicationContext) {
    this.name = name;
    this.schemaService = schemaService;
    this.richtextTransformerRegistry = richtextTransformerRegistry;
    this.applicationContext = applicationContext;
  }


  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getDefaultRichtextFormat() {
    return defaultRichtextFormat;
  }

  public LinkBuilder getLinkBuilder() {
    return getLinkBuilder("default");
  }

  public LinkBuilder getLinkBuilder(String name) {
    return linkBuilders.get(name);
  }

  public RichtextTransformerRegistry getRichtextTransformerRegistry() {
    return richtextTransformerRegistry;
  }

  public SchemaService getSchemaService() {
    return schemaService;
  }

  public CaasEndpoint getCaasEndpoint() {
    return caasEndpoint;
  }

  public GraphQLEndpoint getGraphQLEndpoint() {
    return graphQLEndpoint;
  }


  /*
   * YAML setters
   */

  @SuppressWarnings("unused")
  public void setDescription(String description) {
    this.description = description;
  }

  @SuppressWarnings("unused")
  public void setDefaultRichtextFormat(String defaultRichtextFormat) {
    this.defaultRichtextFormat = defaultRichtextFormat;
  }

  @SuppressWarnings("unused")
  public void setLinkBuilders(Map<String, String> linkBuilderMapping) {
    this.linkBuilders = linkBuilderMapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> applicationContext.getBean(e.getValue(), LinkBuilder.class)));
  }

  @SuppressWarnings("unused")
  public void setCaasEndpoint(CaasEndpoint caasEndpoint) {
    this.caasEndpoint = caasEndpoint;
  }

  @SuppressWarnings("unused")
  public void setGraphQLEndpoint(GraphQLEndpoint graphQLEndpoint) {
    this.graphQLEndpoint = graphQLEndpoint;
  }
}
