package com.search;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.search.service.MonitoringService;
import com.search.util.SearchUtil;

@Component
public class ApplicationStartup 
implements ApplicationListener<ApplicationReadyEvent> {

	public static final Logger logger = LoggerFactory.getLogger(ApplicationStartup.class);
	
	@Autowired
	MonitoringService monitoringService;
	
	@Value("${directory}")
  	private String directory;
	
	@Autowired
	SearchUtil searchUtil;
	
   /**
    * This event is executed as late as conceivably possible to indicate that 
    * the application is ready to service requests.
    */
   @Override
   public void onApplicationEvent(final ApplicationReadyEvent event) {
	   
	   logger.info(" Inside ApplicationStartup : onApplicationEvent method ");
	   
	   boolean recursive = true;
	   
	   String classPath = searchUtil.getClassPath();
    	   
       logger.debug("Classpath is {}",classPath);

	   Path searchDirectory = Paths.get(classPath+directory);
	   
	   try {
		   monitoringService.directoryWatcher(searchDirectory, recursive);
	   } catch (IOException e) {
		   logger.trace("Error in onApplicationEvent {}", e.getMessage());
	   }
     return;
   }
 
}
