package com.coremedia.caas.test.schema;

import com.coremedia.caas.config.loader.ClasspathConfigResourceLoader;
import com.coremedia.caas.pd.ProcessingDefinition;
import com.coremedia.caas.pd.ProcessingDefinitionLoader;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class SchemaTest {

  @Autowired
  private ApplicationContext applicationContext;

  @Mock
  ContentRepository contentRepository;

  @Mock
  SitesService sitesService;


  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }


  @Test
  public void loadMinimalSchema() throws IOException {

    when(contentRepository.getContentTypes()).thenReturn(Collections.emptyList());

    ProcessingDefinition minimal = new ProcessingDefinitionLoader("minimal", new ClasspathConfigResourceLoader("pd/test/minimal/"), contentRepository, sitesService, applicationContext).load();

    assertTrue(minimal.getSchemaService().hasType("Content_"));
    assertTrue(minimal.getSchemaService().hasType("Content_Impl"));
  }
}
