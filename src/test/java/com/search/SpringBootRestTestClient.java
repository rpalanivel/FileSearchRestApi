package com.search;
 
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.ws.rs.core.GenericEntity;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.search.controller.SearchController;
import com.search.model.FileName;
 
public class SpringBootRestTestClient {
 
	public static final Logger logger = LoggerFactory
			.getLogger(SpringBootRestTestClient.class);
	
	public static final String REST_SERVICE_URI = "http://localhost:8080/FileSearchRestApi/searchfiles/searchkey/";
 

	@LocalServerPort
	private int port;
	
	/**
	 * Test method to test FileSearchRestApi with different searchkeys
	 */
    private static void getFileList(){
        logger.info("        Testing FileSearchRestApi      ");
        RestTemplate restTemplate = new RestTemplate();
    	HttpHeaders headers = new HttpHeaders();
    	headers.setAccept(Collections.singletonList(new MediaType("application","json")));
    	HttpEntity<String> entity = new HttpEntity<String>(null, headers);
    	
        String searchKeys = "";
    	/* Test with proper search keys */
        searchKeys = "key1,key2,key3,";
    	ResponseEntity<String> response = restTemplate.exchange(REST_SERVICE_URI+searchKeys, 
                 		HttpMethod.GET, entity, String.class);
        logger.info("Result Json {}",response.getBody());
       
        /* Test with no match for the searchkey */
        searchKeys ="testonetwothree,";
        response = restTemplate.exchange(REST_SERVICE_URI+searchKeys, 
         		HttpMethod.GET, entity, String.class);
        logger.info("Result Json {}",response.getBody());
        
        /* Test with invalid searchkey */
        searchKeys ="testonetwothree ,";
        response = restTemplate.exchange(REST_SERVICE_URI+searchKeys, 
         		HttpMethod.GET, entity, String.class);
        logger.info("Result Json {}",response.getBody());

        searchKeys =" , ,";
        response = restTemplate.exchange(REST_SERVICE_URI+searchKeys, 
         		HttpMethod.GET, entity, String.class);
        logger.info("Result Json {}",response.getBody());     
    }
     
    public static void main(String args[]){
        
    	getFileList();
        
    }
}