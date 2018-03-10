package com.search.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.search.model.FileName;
import com.search.util.SearchUtil;

@Service("searchService")
public class SearchServiceImpl implements SearchService{
	
	public static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);

	
	
	SearchUtil searchUtil = new SearchUtil();
	
	
	
	public List<FileName> searchAllFiles(String keywords, String searchDirectory) {
		
		List<FileName> fileNames;
		
		List<File> completeFileList = new ArrayList<File>();
			
		completeFileList = searchUtil.listAllFilesInDirectory(searchDirectory);
		
		fileNames = searchUtil.getSearchFileList(completeFileList, keywords);
		
		return fileNames;
	}
}
