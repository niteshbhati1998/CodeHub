package com.cognizant.lineage.util;

import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.springframework.stereotype.Component;

import com.cognizant.lineage.exception.LineageRuntimeException;

@Component
public class LoggerUtil {

	private static String logLevel = Level.INFO.getLocalizedName();

	public synchronized Logger getLogger(final FileHandler handler, final String applicationlogFilePath) throws Exception {
		Logger LOGGER = null;
		try {
			Logger mainLogger = Logger.getLogger(applicationlogFilePath);
			mainLogger.setUseParentHandlers(false);
			handler.setFormatter(new SimpleFormatter() {
				private static final String format = "[%1$tm/%1$td/%1$tY %1$tH:%1$tM:%1$tS][%2$-5s] %3$s %n";

				@Override
				public synchronized String format(final LogRecord lr) {
					return String.format(format, new Date(lr.getMillis()), lr.getLevel().getLocalizedName(),
							lr.getMessage());
				}
			});
			mainLogger.addHandler(handler);
			LOGGER = mainLogger;
			LOGGER.setLevel(Level.parse(logLevel));
		} catch (SecurityException e) {
			if (LOGGER != null) {
				LOGGER.log(Level.SEVERE, "Exception occurred in transportation LoggerUtil.getLogger ::", e);
			}
			throw new LineageRuntimeException(e.getMessage());
		}
		return LOGGER;
	}

	public static void closeLogHandler(final Logger log) {
		java.util.logging.Handler[] handler = log.getHandlers();
		for (java.util.logging.Handler h : handler) {
			h.close();
		}
	}

	public Logger getUtilLogObject(String logFileLocation, String logFileName, String jobId) throws Exception {
		Logger LOGGER = null;
		FileHandler handler = null;
		synchronized (this) {
			logFileName = new String(logFileLocation + "/" + logFileName + "_" + jobId + ".log");
			handler = new FileHandler(logFileName);
			LoggerUtil loggerUtil = new LoggerUtil();
			LOGGER = loggerUtil.getLogger(handler, logFileName);
		}
		return LOGGER;
	}
}
