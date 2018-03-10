package com.search;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.search.service.SearchIndexerService;
import com.search.util.SearchUtil;

@Component
public class StartUp {
 
	public static final Logger logger = LoggerFactory.getLogger(StartUp.class);
	
	@Autowired
	SearchIndexerService searchIndexerService;
  	
	@Value("${directory}")
  	private String directory;
	
	@Autowired
	SearchUtil searchUtil;
	
	@PostConstruct
	public void init(){
		
		String classPath = searchUtil.getClassPath();
		
		classPath += directory;
 	   
	    logger.info("Classpath is : "+classPath);

	    try {
			searchIndexerService.searchIndexer(classPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

 	}
}
