package com.search.service;

import java.util.List;

import com.search.model.FileName;

public interface SearchService {

	List<FileName> searchAllFiles(String keywords, String directory);
}
