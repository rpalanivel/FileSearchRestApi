package com.search.util;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class SearchJsonUtil {

	public String noMatchesFoundMessage() {
		
		ObjectMapper mapper = new ObjectMapper();
		
		ObjectNode messageJson = mapper.createObjectNode();
		
		messageJson.put("Message", "No files match the keywords");
		
		return messageJson.toString();
	}
}
