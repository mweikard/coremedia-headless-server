package com.coremedia.caas.pd;

import com.coremedia.caas.config.loader.ConfigResourceLoader;
import com.coremedia.caas.config.reader.YamlConfigReader;
import com.coremedia.caas.endpoint.caas.CaasEndpoint;
import com.coremedia.caas.endpoint.caas.QueryReader;
import com.coremedia.caas.endpoint.caas.QueryRegistry;
import com.coremedia.caas.endpoint.graphql.ContentQueryFieldBuilder;
import com.coremedia.caas.endpoint.graphql.GraphQLEndpoint;
import com.coremedia.caas.richtext.RichtextTransformerReader;
import com.coremedia.caas.richtext.RichtextTransformerRegistry;
import com.coremedia.caas.schema.SchemaReader;
import com.coremedia.caas.schema.SchemaService;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;

import org.springframework.context.ApplicationContext;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.IOException;

public class ProcessingDefinitionReader extends YamlConfigReader {

  static final String TAG_CAAS_ENDPOINT = "!CaasEndpoint";
  static final String TAG_GRAPHQL_ENDPOINT = "!GraphQLEndpoint";

  static final String TAG_CONTENT_FIELD_BUILDER = "!ContentQueryFieldBuilder";


  private String name;
  private ApplicationContext applicationContext;
  private ContentRepository contentRepository;
  private SitesService sitesService;


  ProcessingDefinitionReader(String name, ApplicationContext applicationContext, ContentRepository contentRepository, SitesService sitesService, ConfigResourceLoader resourceLoader) {
    super(resourceLoader);
    this.name = name;
    this.applicationContext = applicationContext;
    this.contentRepository = contentRepository;
    this.sitesService = sitesService;
  }


  public ProcessingDefinition read() throws IOException {
    // GraphQL schema service
    SchemaService schemaService = new SchemaReader(getResourceLoader()).read(applicationContext, contentRepository);
    // GraphQL queries
    QueryRegistry queryRegistry = new QueryReader(getResourceLoader()).read(schemaService);
    // Richtext transformation registry
    RichtextTransformerRegistry richtextTransformerRegistry = new RichtextTransformerReader(getResourceLoader()).read();
    // create definition with customized constructor
    Constructor constructor = new ProcessingDefinitionConstructor(name,
                                                                  applicationContext,
                                                                  contentRepository,
                                                                  sitesService,
                                                                  schemaService,
                                                                  queryRegistry,
                                                                  richtextTransformerRegistry);
    constructor.addTypeDescription(new TypeDescription(ContentQueryFieldBuilder.class, new Tag(TAG_CONTENT_FIELD_BUILDER)));
    constructor.addTypeDescription(new TypeDescription(CaasEndpoint.class, new Tag(TAG_CAAS_ENDPOINT)));
    constructor.addTypeDescription(new TypeDescription(GraphQLEndpoint.class, new Tag(TAG_GRAPHQL_ENDPOINT)));
    return new Yaml(constructor).loadAs(getResource("definition.yml").asString(), ProcessingDefinition.class);
  }
}
