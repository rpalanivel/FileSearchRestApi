package com.search.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.search.model.FileName;

/**
 * Perform the search of files within directories and sub-directories for the
 * given keywords.
 * 
 * @Param keywords
 * 
 * @return searchFileList
 */
@Service
public class SearchUtil {

	public static final Logger logger = LoggerFactory
			.getLogger(SearchUtil.class);
	
	private static final String indexfileExtension = "_indxs.txt";

	// Declare variable for list holding completeFileList from directories and
	// sub-directories
	List<File> completeFileList = new ArrayList<File>();

	/**
	 * Method to search files in directories and sub-directories for given
	 * keywords
	 * 
	 * @param searchString
	 * @return
	 */
	public List<FileName> getSearchFileList(List<File> completeFileList,
			String searchString) {

		logger.info(" Inside getSearchFileList method ");
		
		List<FileName> fileNames = new ArrayList<>();

		try {
			// Loop the completeFileList for getting individual files
			for (File fileName : completeFileList) {
				/**
				 * Call to check the file met the search criteria provided.
				 * 
				 * @Param fileName,searchString
				 * 
				 * @Return boolean isFileMetSearchCriteria
				 */
				if (isFileMetSearchCriteria(fileName, searchString)) {

					String searchedFileName = "";
					searchedFileName = fileName.getAbsolutePath();
					fileNames.add(new FileName(fileName.getAbsolutePath()));

					logger.info(" Matched Files {}",searchedFileName);
				}
			}
		} catch (Exception ex) {
			logger.error(" Error inside getSearchFileList {}",ex.getMessage() );
		}

		return fileNames;
	}

	/***
	 * Utility to return all files in the directories and sub-directories
	 * 
	 * @param searchDirectory
	 * @param searchKey
	 * @return
	 */
	public synchronized List<File> listAllFilesInDirectory(
			String searchDirectory) {

		logger.info(" Inside listAllFilesInDirectory method ");
		
		Path path = Paths.get(searchDirectory);
		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file,
						BasicFileAttributes attrs) throws IOException {
					if (!attrs.isDirectory() && !stringValue(file.toFile()).contains("_indxs.")){
							completeFileList.add(file.toFile());
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException ex) {
			logger.error(" Error inside getSearchFileList {}",ex.getMessage() );
		}
		return completeFileList;
	}

	/**
	 * Utility to check keywords present in the file and return
	 * isFileMetSearchCriteria
	 * 
	 * @param file
	 * @param keyWords
	 * @return
	 * @throws IOException 
	 */

	public synchronized boolean isFileMetSearchCriteria(File file,
			String keyWords) throws IOException {
		
		boolean metCriteria = false;
		String currentLine = "";

		String searchFileName = getIndexFileName(file.toString());
		
		try (BufferedReader brReader = new BufferedReader(new FileReader(searchFileName));){

			// Split the keywords list based on comma as parameters to keywords
			// array
			String[] keyWordsStr = keyWords.split(",");

			// Read each line from the BufferedReader
			while ((currentLine = brReader.readLine()) != null) {

				// Search for the all the keyword present in the line
				for (String keyWord : keyWordsStr) {
					// If keyword found in the line remove from keywords list
					if (currentLine.contains(' '+keyWord+',')) {
						keyWords = keyWords.replace(keyWord + ",", "");
					}
				}
				// If all keywords are found in the file break the loop to avoid
				// further readings
				if (keyWords.indexOf(',') == -1)
					break;
			}
			// return all search criteria met for the given file
			if (keyWords.indexOf(',') == -1) {
				metCriteria = true;
			}
			return metCriteria;
		}
	}

	/**
	 * Utility to createIndex for the files in search directory
	 * 
	 * @param completeFileList
	 *            ** List of files**
	 */
	public synchronized String createSearchIndex(List<File> completeFileList)
			throws IOException {

		String result = "";

		// Loop the completeFileList for getting individual files
		for (File fileName : completeFileList) {
			uniqueWordsInFile(fileName);
		}
		result = "Sucess";

		return result;
	}
 
	/**
	 * Utility to extract unique words and create index files.
	 * @param fileName
	 * @return String
	 * @throws IOException
	 */
	public String uniqueWordsInFile(File fileName) throws IOException {

		Set<String> wordsSet = Collections
				.synchronizedSet(new HashSet<String>());

		Path indexFile;
        
		if (!stringValue(fileName).contains(indexfileExtension)){
			indexFile = Paths.get(fileName.getPath().split("\\.")[0]
					+ indexfileExtension);
		}else{
			indexFile = Paths.get(fileName.getPath());
		}
		
		if (!Files.exists(indexFile)) {
			if (fileName.getName().contains(".pdf")) {
				pdfFileReader(wordsSet, fileName);
			} else if (fileName.getName().contains(".xls")
					|| fileName.getName().contains(".xlsx")) {
				excelFileReader(wordsSet, fileName);
			} else {
				textFileReader(wordsSet, fileName);
			}

			String indexFileName = getIndexFileName(stringValue(fileName)); 
			
			Path newFile = Paths.get(indexFileName);
			
			
			try (BufferedWriter writer = Files.newBufferedWriter(newFile)) {
				writer.write(wordsSet.toString());
			}
		}
		return "Success";
	}
    
	/**
	 * Utility to get index file name
	 * @param fileName
	 * @return indexFileName
	 * @throws IOException
	 */
	public String getIndexFileName(String fileName) throws IOException {
		
		logger.info(" Inside getIndexFileName method ");

		StringBuilder indexFileName = new StringBuilder();

		String[] pathSplit = fileName.split(Matcher.quoteReplacement("\\"));
		for (int i = 0; i < pathSplit.length; i++) {
			if (i == 1) {
				if (indexFileName.indexOf("/Indexes") == -1)
					indexFileName.append(pathSplit[i] + "/Indexes/");
			} else {
				if (i == (pathSplit.length - 1)) {
					
					Path path = new File(indexFileName.toString()).toPath();
					
					if (!path.toFile().exists()) {
						Files.createDirectories(path);
					}
					if (stringNotNull(pathSplit[i]))
						indexFileName.append(pathSplit[i].substring(0,
								pathSplit[i].lastIndexOf('.'))
								+ "_indxs.txt");
				} else {
					indexFileName.append(pathSplit[i] + "/");
				}
			}
		}
		return indexFileName.toString();
	}

	/**
	 * Utility to read text based files.
	 * @param wordsSet
	 * @param fileName
	 * @return Set<String> words
	 * @throws IOException
	 */
	public Set<String> textFileReader(Set<String> wordsSet, File fileName)
			throws IOException {

		logger.info(" Inside textFileReader method ");
		
		byte[] bFile = new byte[(int) fileName.length()];
		char delimeter = ' ';

		if (stringNotNull(fileName.toString())
				&& fileName.toString().contains(".csv"))
			delimeter = ',';

		try (FileInputStream fileInputStream = new FileInputStream(fileName);) {
			fileInputStream.read(bFile);
			fileInputStream.close();
			StringBuilder word = new StringBuilder();
			for (int i = 0; i < bFile.length; i++) {
				char character = (char) bFile[i];
				if (character == delimeter || character == '\n') {
					wordsSet.add(word.toString());
					word = new StringBuilder();

				} else {
					if (Character.isAlphabetic(character) || ((i + 1) < fileName.length()
							&& Character.isAlphabetic((char) bFile[i + 1])
							&& word.length() > 0)) {
						word.append(character);
					}
				}
			}
			wordsSet.add(word.toString());
		}
		return wordsSet;
	}
    
	/**
	 * Utility to read PDF files.
	 * @param wordsSet
	 * @param fileName
	 * @return Set<String> words
	 * @throws IOException
	 */
	public Set<String> pdfFileReader(Set<String> wordsSet, File fileName)
			throws IOException {
 
		logger.info(" Inside pdfFileReader method ");
		
		try (PDDocument document = PDDocument.load(fileName)) {
			if (!document.isEncrypted()) {

				int numPages = document.getNumberOfPages();

				PDFTextStripper textStripper = new PDFTextStripper();

				for (int i = 0; i <= numPages; i++) {
					textStripper.setStartPage(i);
					textStripper.setEndPage(i);
					String pageText = textStripper.getText(document);

					String[] words = pageText.split("\\W");
					for (String word : words) {
						wordsSet.add(word);
					}
				}
			}
		}
		return wordsSet;
	}
    
	/**
	 * Utility to read Document Files
	 * @param wordsSet
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public Set<String> docFileReader(Set<String> wordsSet, File fileName)
			throws IOException {

		logger.info(" Inside docFileReader method ");
		
		try (InputStream fis = new FileInputStream(fileName);) {

			if (fileName.getName().toLowerCase().endsWith(".doc")) {

				POIFSFileSystem fs = new POIFSFileSystem(fis);

				HWPFDocument document = new HWPFDocument(fs);

				WordExtractor docWords = new WordExtractor(document);

				String[] paragraphs = docWords.getParagraphText();

				logger.info("Total no of paragraph {}",paragraphs.length);
				for (String paragraph : paragraphs) {
					String[] words = paragraph.split("\\W");
					for (String word : words) {
						if (stringNotNull(word)) {
							wordsSet.add(word);
						}
					}
				}
				docWords.close();
			} else if (fileName.getName().toLowerCase().endsWith(".docx")) {

				try {
					XWPFDocument documentx = new XWPFDocument(fis);

					List<XWPFParagraph> paragraphs = documentx.getParagraphs();

					logger.info("Total no of paragraph {}", paragraphs.size());
					for (XWPFParagraph para : paragraphs) {
						if (stringNotNull(para.getText())) {
							String[] words = para.getText().split("\\W");
							for (String word : words) {
								if (stringNotNull(word)) {
									wordsSet.add(word);
								}
							}
						}
					}
				} catch (IOException e) {
					logger.info(e.getMessage());
				}
			}
		}
		return wordsSet;
	}
    
	/**
	 * Utility to read Excel Files.
	 * @param wordsSet
	 * @param fileName
	 * @return Set<String> words
	 * @throws IOException
	 */
	public Set<String> excelFileReader(Set<String> wordsSet, File fileName)
			throws IOException {
		
		logger.info(" Inside excelFileReader method ");
		
		try (FileInputStream fis = new FileInputStream(fileName);) {

			Workbook workbook = null;

			if (fileName.getName().toLowerCase().endsWith(".xls")) {

				workbook = new HSSFWorkbook(fis);

			} else if (fileName.getName().toLowerCase().endsWith(".xlsx")) {

				workbook = new XSSFWorkbook(fis);
			}
			int numberOfSheets = workbook.getNumberOfSheets();

			for (int i = 0; i < numberOfSheets; i++) {

				Sheet sheet = workbook.getSheetAt(i);

				Iterator<Row> rowIterator = sheet.iterator();
				while (rowIterator.hasNext()) {
					Row currentRow = rowIterator.next();
					Iterator<Cell> cellIterator = currentRow.iterator();

					while (cellIterator.hasNext()) {

						Cell currentCell = cellIterator.next();
						switch (currentCell.getCellType()) {
						case Cell.CELL_TYPE_STRING:
							if (stringNotNull(currentCell.getStringCellValue())) {
								String[] words = currentCell
										.getStringCellValue().split("\\W");
								for (String word : words) {
									if (stringNotNull(word)) {
										wordsSet.add(word);
									}
								}
							}
							break;
						}
					}
				}
			}
		}
		return wordsSet;
	}

	/**
	 * Utility checks string not null,empty 
	 * @param value
	 * @return
	 */
	public static boolean stringNotNull(String value) {
		boolean notNull = false;
		if (value != null && !value.isEmpty() && value.trim().length() > 0)
			notNull = true;
		
		return notNull;
	}

	/**
	 * Utility to return classpath
	 * @return classpath
	 */
	public final String getClassPath() {

		logger.info(" Inside getClassPath method ");
		
		final String os = System.getProperty("os.name").toLowerCase();

		String userPath = new File("").getAbsolutePath();

		String classPath = "";

		if (os.indexOf("win") >= 0)
			classPath = userPath.substring(0, userPath.indexOf(':') + 1);
		else if (os.indexOf("aix") >= 0 ||
				 os.indexOf("nix") >= 0 || 
				 os.indexOf("nux") >= 0)
			classPath = userPath;

		return classPath;
	}

	/**
	 * Utility to delete index files
	 * @param deleteFile
	 * @return boolean
	 */
	public final boolean deleteFile(File deleteFile) {

		logger.info(" Inside deleteFile method ");
		
		boolean isDeleted = false;
		try {
			String indexFolder = indexFolder(deleteFile);
			if (!deleteFile.toString().contains(".")) {
				deleteDirectory(indexFolder);
			} else {
				Path indxsFile = new File(
						getIndexFileName(deleteFile.toString())).toPath();
				if (indxsFile.toFile().exists())
					Files.delete(indxsFile);
			}
			isDeleted = deleteFile.delete();
		} catch (IOException e) {
			logger.error("Exception Inside deleteFile : {} ",e.getMessage());
		}

		return isDeleted;
	}
    
	/**
	 * Utility to return string value  of file.
	 * @param file
	 * @return stringValue
	 */
	public String stringValue(File file) {

		String stringValue = "";

		if (stringNotNull(file.toString()))
			stringValue = file.toString();

		return stringValue;
	}
	
    /**
     * Utility to return index folder name
     * @param file
     * @return indexFolder
     */
	public String indexFolder(File file) {

		int index = 0;
		int length = 0;
		String stringValue = "";

		index = ordinalIndexOf(file.toString(), "\\", 2);
		length = file.toString().length();

		if (stringNotNull(file.toString()))
			stringValue += file.toString().substring(0, index) + "\\Indexes"
					+ file.toString().substring(index, length);

		return stringValue;
	}
	
    /**
     * Utility to Delete the directory 
     * @param directory
     */
	public static void deleteDirectory(String directory) {
	    try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths
	            .get(directory))) {
	        for (Path path : stream) {
	            if(path.toFile().isDirectory()) {
	                deleteDirectory(path.toString());
	            }
	            else {
	                Files.delete(path);
	            }
	        }
	        Files.delete(Paths.get(directory));
	    }
	    catch (IOException e) {
	        if(e.getClass() == DirectoryNotEmptyException.class) {
	            deleteDirectory(directory);
	        }
	    }
	}
	
    /**
     * Utility to return nth occurrence of character
     * @param str
     * @param substr
     * @param n
     * @return nth position of the character
     */
	public static int ordinalIndexOf(String str, String substr, int n) {
		int pos = str.indexOf(substr);
		while (--n > 0 && pos != -1)
			pos = str.indexOf(substr, pos + 1);
		return pos;
	}
}
