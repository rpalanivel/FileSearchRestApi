package com.search.controller;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import com.search.service.SearchIndexerService;
import com.search.service.SearchIndexerServiceImpl;
import com.search.util.SearchJsonUtil;

@RestController
@Path("/searchindexer")
public class SearchIndexController {
 
	public static final Logger logger = LoggerFactory.getLogger(SearchIndexController.class);
	
    @GET
    @Produces("application/json") 
    public Response getFileList(@Context UriInfo uriInfo) throws IOException{
        
    	long startTime = System.currentTimeMillis();
    	
    	//Utility for search operation
    	SearchIndexerService searchIndexer = new SearchIndexerServiceImpl();
    	
    	// Call the getSeacrhFileList operation of SearchUtil
    	searchIndexer.searchIndexer("");
    	
    	SearchJsonUtil messageUtil = new SearchJsonUtil();
		
    	long endTime = System.currentTimeMillis();
    	long totalTime = endTime - startTime;
    	logger.info(" TIME TAKEN FOR SEARCH "+ totalTime);
    	
    	return Response.ok(messageUtil.noMatchesFoundMessage()).build();
    }
}
