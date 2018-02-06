package com.coremedia.caas.controller.content;

import com.coremedia.caas.controller.base.AbstractController;
import com.coremedia.caas.monitoring.Metrics;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.OPTIONS}, allowedHeaders = {"authorization", "content-type", "x-requested-with"})
@RequestMapping("/caas/v1/{tenantId}/sites/{siteId}")
@Api(value = "/caas/v1/{tenantId}/sites/{siteId}", tags = "Content", description = "Operations for content objects")
public class ContentViewController extends AbstractController {

  private static final String TIMER_NAME = "caas.content.timer";


  @Autowired
  private Metrics metrics;


  @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(
          value = "Site.Query",
          notes = "Run the GraphQL query with name \"sites\" and view \"default\" on the requested site indicator.\n" +
                  "JSON generated by the executed GraphQL query varies, so no specific return type can be given.",
          response = Object.class
  )
  @ApiResponses(value = {
          @ApiResponse(code = 400, message = "Invalid tenant or site"),
          @ApiResponse(code = 404, message = "No query for the site indicator and default view was found")
  })
  public Object getContent(@ApiParam(value = "The tenant's unique ID", required = true) @PathVariable String tenantId,
                           @ApiParam(value = "The site's unique ID", required = true) @PathVariable String siteId,
                           HttpServletRequest request,
                           HttpServletResponse response) {
    return executeQuery(tenantId, siteId, "default", request, response);
  }

  @RequestMapping(value = "/{viewName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(
          value = "Site.QueryWithView",
          notes = "Run the GraphQL query with name \"sites\" and given view on the requested site indicator.\n" +
                  "JSON generated by the executed GraphQL query varies, so no specific return type can be given.",
          response = Object.class
  )
  @ApiResponses(value = {
          @ApiResponse(code = 400, message = "Invalid tenant or site"),
          @ApiResponse(code = 404, message = "No query for the site indicator and given view was found")
  })
  public Object getContent(@ApiParam(value = "The tenant's unique ID", required = true) @PathVariable String tenantId,
                           @ApiParam(value = "The site's unique ID", required = true) @PathVariable String siteId,
                           @ApiParam(value = "The requested query view", required = true) @PathVariable String viewName,
                           HttpServletRequest request,
                           HttpServletResponse response) {
    return executeQuery(tenantId, siteId, viewName, request, response);
  }

  @RequestMapping(value = "/{queryName}/{targetId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(
          value = "Content.Query",
          notes = "Run the GraphQL query with given name and view \"default\" on the requested content object.\n" +
                  "JSON generated by the executed GraphQL query varies, so no specific return type can be given.",
          response = Object.class
  )
  @ApiResponses(value = {
          @ApiResponse(code = 400, message = "Invalid tenant or site"),
          @ApiResponse(code = 404, message = "No query with the given name and default view was found")
  })
  public Object getContent(@ApiParam(value = "The tenant's unique ID", required = true) @PathVariable String tenantId,
                           @ApiParam(value = "The site's unique ID", required = true) @PathVariable String siteId,
                           @ApiParam(value = "The requested query name", required = true) @PathVariable String queryName,
                           @ApiParam(value = "The content's numeric ID or alias", required = true) @PathVariable String targetId,
                           HttpServletRequest request,
                           HttpServletResponse response) {
    return executeQuery(tenantId, siteId, queryName, targetId, "default", request, response);
  }

  @RequestMapping(value = "/{queryName}/{targetId}/{viewName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(
          value = "Content.QueryWithView",
          notes = "Run the GraphQL query with given name and view on the requested content object.\n" +
                  "JSON generated by the executed GraphQL query varies, so no specific return type can be given.",
          response = Object.class
  )
  @ApiResponses(value = {
          @ApiResponse(code = 400, message = "Invalid tenant or site"),
          @ApiResponse(code = 404, message = "No content with the given name or alias or no query with the given name and view was found")
  })
  public Object getContent(@ApiParam(value = "The tenant's unique ID", required = true) @PathVariable String tenantId,
                           @ApiParam(value = "The site's unique ID", required = true) @PathVariable String siteId,
                           @ApiParam(value = "The requested query name", required = true) @PathVariable String queryName,
                           @ApiParam(value = "The content's numeric ID or alias", required = true) @PathVariable String targetId,
                           @ApiParam(value = "The requested query view", required = true) @PathVariable String viewName,
                           HttpServletRequest request,
                           HttpServletResponse response) {
    return executeQuery(tenantId, siteId, queryName, targetId, viewName, request, response);
  }


  private Object executeQuery(String tenantId, String siteId, String viewName, HttpServletRequest request, HttpServletResponse response) {
    Site localizedSite = getLocalizedSite(tenantId, siteId);
    if (localizedSite == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return null;
    }
    Content content = localizedSite.getSiteIndicator();
    return metrics.timer(() -> execute("sites", viewName, localizedSite, content, ImmutableMap.of(), request, response), TIMER_NAME, "tenant", tenantId, "site", siteId, "pd", "default", "query", "sites", "view", viewName);
  }


  private Object executeQuery(String tenantId, String siteId, String queryName, String targetId, String viewName, HttpServletRequest request, HttpServletResponse response) {
    Site localizedSite = getLocalizedSite(tenantId, siteId);
    if (localizedSite == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return null;
    }
    Content content = resolveContent(localizedSite, targetId);
    if (content == null || !siteService.isContentInSite(localizedSite, content)) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return null;
    }
    return metrics.timer(() -> execute(queryName, viewName, localizedSite, content, ImmutableMap.of(), request, response), TIMER_NAME, "tenant", tenantId, "site", siteId, "pd", "default", "query", queryName, "view", viewName);
  }
}
