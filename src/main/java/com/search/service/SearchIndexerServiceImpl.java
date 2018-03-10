package com.search.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.search.util.SearchUtil;

@Service("searchIndexerService")
public class SearchIndexerServiceImpl implements SearchIndexerService {
	
	SearchUtil searchUtil = new SearchUtil();
	
	String result = "Failure";
	
	public String searchIndexer(String searchDirectory) throws IOException {
		
		List<File> completeFileList = new ArrayList<File>();
		
		completeFileList = searchUtil.listAllFilesInDirectory(searchDirectory);
		
		result = searchUtil.createSearchIndex(completeFileList);
		
		return result;
	}
}
