/*******************************************************************************
 *  Copyright 2017 Vincenzo-Maria Cappelleri <vincenzo.cappelleri@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package raw.logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import raw.settings.LoggerProperties;
import raw.settings.ModuleProperty;
import raw.settings.PropertiesManager;

/**
 * Convenience logging/output class.<br>
 * <br>
 * Uses {@link Level} objects to set verbosity
 * and can be configured programmatically.
 * Both prints to {@link System#out} and to a 
 * txt file. 
 * 
 * @author vic
 *
 */
public class Log {
		
	private static Log instance = null;
	
	private static Logger log = null;
	private static boolean initiallyConfigured = false;
	
	private static java.util.logging.Level consoleLoggingLevel = null;
	private static java.util.logging.Level fileLoggingLevel = null;
	private static String fileName;
	
	private Log() {
		setUpLogger();
	}
	
	/**
	 * Use this to get a reference to the logging
	 * singleton. 
	 * 
	 * @return a reference to the logging singleton
	 */
	public static Log getLogger(){
		if(instance == null){
			instance = new Log();
		}		
		return instance;
	}
	
	/**
	 * This method sets up the shared instance
	 * of this class that should then be returned.
	 * 
	 */
	private static void setUpLogger(){
		if(!loggingReady()){
			log = Logger.getAnonymousLogger();
			standardConfiguration();
		}
	}
	
	/**
	 * Utility method to discriminate when and if
	 * issuing the set up methods.
	 *  
	 * @return true if everything is OK false if
	 * internal objects n set up.
	 */
	private static boolean loggingReady(){
		return ((log != null) && initiallyConfigured);
	}
	
	private static void retrieveDefaultConfigurations(){
		LoggerProperties properties = (LoggerProperties) PropertiesManager.getManager().getProperties(ModuleProperty.LOGGER);
		
		Level retrievedLevel = Level.fromLevelString(properties.getConsoleLogLevel());
		consoleLoggingLevel = convertToJdkLevels(retrievedLevel);
		retrievedLevel = Level.fromLevelString(properties.getFileLogLevel());
		fileLoggingLevel = convertToJdkLevels(retrievedLevel);
		
		String loggingDir = properties.getLogDir();

		if(!Files.exists(FileSystems.getDefault().getPath(loggingDir))){
			try {
				Files.createDirectories(FileSystems.getDefault().getPath(loggingDir));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String beginningTime = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Timestamp(System.currentTimeMillis()));
		fileName = loggingDir+"log"+beginningTime+".txt";
	}
	
	private static void standardConfiguration(){
		retrieveDefaultConfigurations();
				
		for (Handler hand : log.getHandlers()) {
			log.removeHandler(hand);
		}
		
		log.setUseParentHandlers(false);
		
		LogFormatter consoleFormatter = new LogFormatter(false);
		
		
		Handler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(consoleLoggingLevel);
		consoleHandler.setFormatter(consoleFormatter);
		
		log.addHandler(consoleHandler);
				
		Handler fileHandler = null;
		try {
			fileHandler = new FileHandler(fileName);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		fileHandler.setLevel(fileLoggingLevel);
		
		LoggerProperties properties = (LoggerProperties) PropertiesManager.getManager().getProperties(ModuleProperty.LOGGER);
		SimpleFormatter fileFormatter = null;
		if(properties.getFileLogType().equals("JSON")){
			fileFormatter = new LogJsonFormatter();
		} else if (properties.getFileLogType().equals("TXT")){
			fileFormatter = new LogFormatter(true);
		}
		
		fileHandler.setFormatter(fileFormatter);
		
		log.addHandler(fileHandler);
				
//		log.setLevel(consoleLoggingLevel);
		log.setLevel(convertToJdkLevels(Level.ALL));

		initiallyConfigured= true;
	}
	
	/**
	 * Sets output verbosity in console and on file.
	 * 
	 * @param console the desired verbosity {@link Level} for console stream
	 * @param file the desired verbosity {@link Level} for txt file log
	 */
	public static void setLogLevels(Level console, Level file){
		java.util.logging.Level consoleEquivalentLevel = convertToJdkLevels(console);
		java.util.logging.Level fileEquivalentLevel = convertToJdkLevels(file);
				
		for (Handler hand : log.getHandlers()) {
			if(hand instanceof ConsoleHandler){
				hand.setLevel(consoleEquivalentLevel);
			}
			if(hand instanceof FileHandler){
				hand.setLevel(fileEquivalentLevel);
			}
		}
	}
	
	private static java.util.logging.Level convertToJdkLevels(Level level){
		switch (level) {
		case ALL:
			return java.util.logging.Level.ALL;
		case EXCEPTION:
			return java.util.logging.Level.SEVERE;
		case WARNING:
			return java.util.logging.Level.WARNING;
		case INFO:
			return java.util.logging.Level.INFO;
		case DEBUG:
			return java.util.logging.Level.FINE;
		case VERBOSE:
			return java.util.logging.Level.FINER;
		case OFF:
			return java.util.logging.Level.OFF;
		default:
			return java.util.logging.Level.INFO;
		}
	}
	
	protected static Level convertJdkLevel(java.util.logging.Level level){
		if(level == java.util.logging.Level.ALL){
			return Level.ALL;
		} else if(level == java.util.logging.Level.SEVERE){
			return Level.EXCEPTION;
		} else if(level == java.util.logging.Level.WARNING){
			return Level.WARNING;
		} else if(level == java.util.logging.Level.INFO){
			return Level.INFO;
		} else if(level == java.util.logging.Level.FINE){
			return Level.DEBUG;
		} else if(level == java.util.logging.Level.FINER){
			return Level.VERBOSE;
		} else if(level == java.util.logging.Level.OFF){
			return Level.OFF;
		}
		return Level.INFO;
	}
	
	/**
	 * Enter a string marked as {@link Level#DEBUG}.
	 * 
	 * @param str the string to be debug-logged
	 */
	public synchronized void debug(String str){
		log.log(compileLogRecord(java.util.logging.Level.FINE, str));
	}
	
	/**
	 * Enter a string marked as {@link Level#VERBOSE}.
	 * 
	 * @param str the string to be verbosely debug-logged
	 */
	public synchronized void verboseDebug(String str){
		log.log(compileLogRecord(java.util.logging.Level.FINER, str));
	}
	
	/**
	 * Enter a string marked as {@link Level#INFO}.
	 * 
	 * @param str the string to be info-logged
	 */
	public synchronized void info(String str) {
		log.log(compileLogRecord(java.util.logging.Level.INFO, str));
	}
	
	/**
	 * Enter a string marked as {@link Level#WARNING}.
	 * 
	 * @param str the string to be warning-logged
	 */
	public synchronized void warning(String str){
		log.log(compileLogRecord(java.util.logging.Level.WARNING, str));
	}
	
	/**
	 * Log the StackTrace for an {@link Exception}
	 * 
	 * @param e the {@link Exception} to be logged
	 */
	public synchronized void exception(Throwable e) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		StringBuilder stringStackTrace = new StringBuilder();
		stringStackTrace.append("StackTrace follows\n");
		stringStackTrace.append(stringWriter.toString());
		
		log.log(compileLogRecord(java.util.logging.Level.SEVERE, stringStackTrace.toString()));
	}
	
	/**
	 * Flush this logger.
	 */
	public synchronized void flushLogger(){
		debug("Flushing "+log.getHandlers().length+" logger handlers.");
		for (Handler hand : log.getHandlers()) {
			hand.flush();
			hand.close();
		}
	}
	
	private String getCallingMethod() {
		String caller = "Unknown Class Method";
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		
		if(trace.length > 3){
//			caller = trace[4].getClassName() + "." + trace[4].getMethodName()+"()";
			caller = trace[4].getMethodName()+"()";
		}		
		
		return caller;
	}
	
	private String getCallingClass(){
		String caller = "Unknown Class Method";
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		
		if(trace.length > 3){
			caller = trace[4].getClassName();
		}		
		
		return caller;
	}
	
	private LogRecord compileLogRecord(java.util.logging.Level level, String str){
		LogRecord record = new LogRecord(level, str);
		record.setSourceMethodName(getCallingMethod());
		record.setSourceClassName(getCallingClass());
		return record;
	}

	/**
	 * @param consoleLoggingLevel the desired verbosity {@link Level} for console stream
	 */
	public static void setConsoleLoggingLevel(Level consoleLoggingLevel) {
		Log.consoleLoggingLevel = convertToJdkLevels(consoleLoggingLevel);
		for (Handler hand : log.getHandlers()) {
			if(hand instanceof ConsoleHandler){
				hand.setLevel(Log.consoleLoggingLevel);
			}
		}
	}


	/**
	 * @param fileLoggingLevel the desired verbosity {@link Level} for txt file log
	 */
	public static void setFileLoggingLevel(Level fileLoggingLevel) {
		Log.fileLoggingLevel = convertToJdkLevels(fileLoggingLevel);
		for (Handler hand : log.getHandlers()) {
			if(hand instanceof FileHandler){
				hand.setLevel(Log.fileLoggingLevel);
			}
		}
	}
}
