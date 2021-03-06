package com.coremedia.caas.extension.link;

import com.coremedia.caas.link.LinkBuilder;
import com.coremedia.caas.server.CaasServiceConfig;
import com.coremedia.caas.server.link.SimpleLinkBuilder;
import com.coremedia.caas.server.service.media.MediaResourceModel;
import com.coremedia.caas.server.service.media.MediaResourceModelFactory;
import com.coremedia.caas.service.repository.RootContext;
import com.coremedia.caas.service.repository.content.BlobProxy;
import com.coremedia.caas.service.repository.content.ContentProxy;
import com.coremedia.cap.common.IdHelper;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;

import static com.coremedia.caas.extension.link.LinkBuilderSetup.REQUEST_MEDIA_URI_COMPONENTS;
import static com.coremedia.caas.server.link.SimpleLinkBuilder.EMPTY_LINK;

@Component("extendedLinkBuilder")
public class ExtendedLinkBuilder implements LinkBuilder<String> {

  private SimpleLinkBuilder delegate;
  private CaasServiceConfig serviceConfig;


  public ExtendedLinkBuilder(SimpleLinkBuilder delegate, CaasServiceConfig serviceConfig) {
    this.delegate = delegate;
    this.serviceConfig = serviceConfig;
  }


  @Override
  public String createLink(Object target, RootContext rootContext) {
    if (target instanceof ContentProxy) {
      ContentProxy content = (ContentProxy) target;
      if (content.isSubtypeOf("CMImage")) {
        return createBinaryLink(content, rootContext, "data");
      }
      else if (content.isSubtypeOf("CMPicture")) {
        return createBinaryLink(content, rootContext, "data");
      }
      else if (content.isSubtypeOf("CMDownload")) {
        return createBinaryLink(content, rootContext, "data");
      }
      else if (content.isSubtypeOf("CMVisual") || content.isSubtypeOf("CMAudio")) {
        String link = createBinaryLink(content, rootContext, "data");
        if (EMPTY_LINK.equals(link)) {
          link = content.getString("dataUrl");
        }
        return link;
      }
    }
    return delegate.createLink(target, rootContext);
  }


  private String createBinaryLink(ContentProxy content, RootContext rootContext, String propertyName) {
    BlobProxy blob = content.getBlob(propertyName);
    if (blob != null && !blob.isEmpty()) {
      Integer mediaId = IdHelper.parseContentId(content.getId());
      if (serviceConfig.isBinaryUriHashesEnabled()) {
        // fetch hash from media model
        MediaResourceModel model = rootContext.getModelFactory().createModel(MediaResourceModelFactory.MODEL_NAME, content, propertyName);
        String mediaHash = model.getHash();
        // use pre-instantiated URI component
        UriComponents uriComponents = rootContext.getRequestContext().getProperty(REQUEST_MEDIA_URI_COMPONENTS, UriComponents.class);
        return uriComponents.expand(mediaId, propertyName, mediaHash).encode().toUriString();
      }
      else {
        // use pre-instantiated URI component
        UriComponents uriComponents = rootContext.getRequestContext().getProperty(REQUEST_MEDIA_URI_COMPONENTS, UriComponents.class);
        return uriComponents.expand(mediaId, propertyName).encode().toUriString();
      }
    }
    return EMPTY_LINK;
  }
}
