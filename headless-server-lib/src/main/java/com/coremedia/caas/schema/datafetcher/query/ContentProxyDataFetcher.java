package com.coremedia.caas.schema.datafetcher.query;

import com.coremedia.caas.execution.ExecutionContext;
import com.coremedia.caas.schema.datafetcher.DataFetcherException;
import com.coremedia.caas.schema.datafetcher.common.AbstractDataFetcher;
import com.coremedia.caas.service.repository.content.ContentProxy;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;

import graphql.schema.DataFetchingEnvironment;

public class ContentProxyDataFetcher extends AbstractDataFetcher {

  private String fieldType;
  private ContentRepository contentRepository;
  private SitesService sitesService;


  public ContentProxyDataFetcher(String fieldType, ContentRepository contentRepository, SitesService sitesService) {
    this.fieldType = fieldType;
    this.contentRepository = contentRepository;
    this.sitesService = sitesService;
  }


  @Override
  public Object get(DataFetchingEnvironment environment) {
    Integer id = environment.getArgument("id");
    if (id == null) {
      throw new DataFetcherException("No target id provided");
    }
    Content target = contentRepository.getContent(IdHelper.formatContentId(id));
    if (target != null) {
      ExecutionContext context = environment.getContext();
      // check if content is below target site
      if (!sitesService.isContentInSite(context.getRootContext().getSite(), target)) {
        throw new DataFetcherException("Target not within site");
      }
      // create proxy
      ContentProxy contentProxy = context.getRootContext().getProxyFactory().makeContentProxy(target);
      // check type
      if (!context.getProcessingDefinition().getSchemaService().isInstanceOf(contentProxy, fieldType)) {
        throw new DataFetcherException("Invalid target type");
      }
      return contentProxy;
    }
    return null;
  }
}
