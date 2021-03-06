package com.coremedia.caas.server.service.media;

import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;

import com.google.common.base.Objects;
import org.springframework.core.io.AbstractResource;
import org.springframework.http.MediaType;

import java.io.InputStream;

class ContentBlobMediaResource extends AbstractResource implements MediaResource {

  private Blob blob;
  private Content content;
  private String propertyName;


  ContentBlobMediaResource(Blob blob, Content content, String propertyName) {
    this.blob = blob;
    this.content = content;
    this.propertyName = propertyName;
  }


  @Override
  public String getETag() {
    return blob.getETag();
  }

  @Override
  public MediaType getMediaType() {
    return MediaType.parseMediaType(blob.getContentType().toString());
  }


  @Override
  public String getDescription() {
    return IdHelper.formatBlobId(content.getId(), propertyName) + "#" + blob.getETag();
  }

  @Override
  public InputStream getInputStream() {
    return blob.getInputStream();
  }


  @Override
  public int hashCode() {
    return Objects.hashCode(blob, content);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ContentBlobMediaResource that = (ContentBlobMediaResource) o;
    return Objects.equal(blob, that.blob) &&
           Objects.equal(content, that.content);
  }
}
