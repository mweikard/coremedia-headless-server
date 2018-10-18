package com.coremedia.caas.pd;

import com.coremedia.caas.endpoint.caas.CaasEndpoint;
import com.coremedia.caas.endpoint.caas.QueryRegistry;
import com.coremedia.caas.endpoint.graphql.ContentQueryFieldBuilder;
import com.coremedia.caas.endpoint.graphql.GraphQLEndpoint;
import com.coremedia.caas.richtext.RichtextTransformerRegistry;
import com.coremedia.caas.schema.SchemaService;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;

import org.springframework.context.ApplicationContext;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.NodeId;

class ProcessingDefinitionConstructor extends Constructor {

  ProcessingDefinitionConstructor(String name,
                                  ApplicationContext applicationContext,
                                  ContentRepository contentRepository,
                                  SitesService sitesService,
                                  SchemaService schemaService,
                                  QueryRegistry queryRegistry,
                                  RichtextTransformerRegistry richtextTransformerRegistry) {
    super(ProcessingDefinition.class);
    // use special class constructor to create beans with constructor arguments
    this.yamlClassConstructors.put(NodeId.mapping, new ConstructMapping() {
      @Override
      protected Object createEmptyJavaBean(MappingNode node) {
        if (node.getTag().matches(ProcessingDefinition.class)) {
          return new ProcessingDefinition(name, schemaService, richtextTransformerRegistry, applicationContext);
        }
        else if (ProcessingDefinitionReader.TAG_CAAS_ENDPOINT.equals(node.getTag().getValue())) {
          return new CaasEndpoint(queryRegistry);
        }
        else if (ProcessingDefinitionReader.TAG_GRAPHQL_ENDPOINT.equals(node.getTag().getValue())) {
          return new GraphQLEndpoint(schemaService);
        }
        else if (ProcessingDefinitionReader.TAG_CONTENT_FIELD_BUILDER.equals(node.getTag().getValue())) {
          return new ContentQueryFieldBuilder(contentRepository, sitesService);
        }
        return super.createEmptyJavaBean(node);
      }
    });
  }
}
