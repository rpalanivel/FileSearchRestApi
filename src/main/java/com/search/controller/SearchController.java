package com.search.controller;

import java.util.List;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.search.model.FileName;
import com.search.service.SearchService;
import com.search.util.CustomErrorType;
import com.search.util.SearchJsonUtil;
import com.search.util.SearchUtil;

@RestController
@RequestMapping("/searchfiles")
public class SearchController {

	public static final Logger logger = LoggerFactory
			.getLogger(SearchController.class);

	@Value("${directory}")
	private String directory;

	@Autowired
	SearchService searchService;

	@Autowired
	SearchUtil searchUtil;

	@Autowired
	SearchJsonUtil searchJsonUtil;
	
	@RequestMapping(value = "/searchkey/{keywords}", method = RequestMethod.GET)
	public Response getFileList(@PathVariable("keywords") String searchKeys) {

		long startTime = System.currentTimeMillis();

		// Declare string variable to hold search list
		List<FileName> filenames;

		logger.info(" SearchKeys {}", searchKeys);

		logger.info(" Directory ()", directory);

		String classPath = searchUtil.getClassPath();

		classPath += directory;

		// Perform validation on syntax of search keywords
		if (searchKeys.indexOf(',') == -1
				|| (searchKeys.lastIndexOf(',') != (searchKeys.length() - 1)))
		{
			return Response
					.status(200)
					.entity(new CustomErrorType(
							"Please enter proper keywords for search."
							+"Ex: test,testone,"))
					.build();
		}
		if ((searchKeys.indexOf(' ') != -1)){
			return Response
					.status(200)
					.entity(new CustomErrorType(
					"Space is not allowed in search keys,"))
					.build();
		}

		// Call the getSeacrhFileList operation of SearchUtil
		filenames = searchService.searchAllFiles(searchKeys, classPath);

		// Throw validation message if the searchFileList is empty else
		if (filenames.isEmpty()) {
			long endTime = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			logger.info(" TIME TAKEN FOR SEARCH  {}", totalTime);
			return Response.status(Status.OK)
					.entity(searchJsonUtil.noMatchesFoundMessage()).build();
		}

		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		logger.info(" TIME TAKEN FOR SEARCH {} ", totalTime);

		GenericEntity entity = new GenericEntity<List<FileName>>(filenames) {
		};

		return Response.ok(entity).build();
	}
}
