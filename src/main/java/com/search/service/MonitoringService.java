package com.search.service;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.search.util.SearchUtil;

/**
 * Example to watch a directory (or tree) for changes to files.
 */
@Service("monitoringService")
public class MonitoringService {

	public static final Logger logger = LoggerFactory
			.getLogger(MonitoringService.class);

	private WatchService watcher;
	private Map<WatchKey, Path> keys;
	private boolean recursive;
	private boolean trace = false;

	@Autowired
	SearchUtil searchUtil;

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	/**
	 * Register the given directory with the WatchService
	 */
	private void register(Path dir) throws IOException {
		WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE,
				ENTRY_MODIFY);
		if (trace) {
			Path prev = keys.get(key);
			if (prev == null) {
				logger.debug("Register: {}", dir);
			} else {
				if (!dir.equals(prev)) {
					logger.debug("Previous : ", prev);
					logger.debug("Directory: ", dir);
				}
			}
		}
		keys.put(key, dir);
	}

	/**
	 * Register the given directory, and all its sub-directories, with the
	 * WatchService.
	 */
	private void registerAll(final Path start) throws IOException {
		// register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir,
					BasicFileAttributes attrs) throws IOException {
				register(dir);
				return FileVisitResult.CONTINUE;
			}
		});

		logger.info("");
	}

	/**
	 * Creates a WatchService and registers the given directory
	 */
	public void directoryWatcher(Path directory, boolean recursive)
			throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey, Path>();
		this.recursive = recursive;

		if (recursive) {
			logger.info("Scanning started for search directory ...", directory);
			registerAll(directory);
			logger.info("Done....................");
		} else {
			register(directory);
		}

		// enable trace after initial registration
		this.trace = true;

		for (;;) {

			// wait for key to be signalled
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException e) {
				return;
			}

			Path path = keys.get(key);
			if (path == null) {
				logger.info("WatchKey not recognized!!");
				continue;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind kind = event.kind();

				// TBD - provide example of how OVERFLOW event is handled
				if (kind == OVERFLOW) {
					continue;
				}

				// Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = cast(event);
				Path name = ev.context();
				Path child = path.resolve(name);

				String eventString = event.kind().name();
				logger.info("Event : {} ",eventString);
				logger.info("Path : {} ",child);

				// if directory is created, and watching recursively, then
				// register it and its sub-directories
				if (recursive && (kind == ENTRY_CREATE)) {
					try {
						if (child.toFile().isDirectory()) {
							registerAll(child);
						}
					} catch (IOException e) {
						logger.error("Error - directoryWatcher- {}",e.getMessage());
					}
				}
				if (kind == ENTRY_DELETE) {
					logger.info("Calling delete method");
					searchUtil.deleteFile(child.toFile());
				}
				if (child.toFile().isFile()
						&& (kind == ENTRY_CREATE || kind == ENTRY_MODIFY)
						&& !child.toString().contains("_indxs.txt")) {

					searchUtil.uniqueWordsInFile(child.toFile());
				}
			}

			// reset key and remove from set if directory no longer accessible
			boolean valid = key.reset();
			if (!valid) {
				keys.remove(key);

				// all directories are inaccessible
				if (keys.isEmpty()) {
					break;
				}
			}
		}
	}
}
