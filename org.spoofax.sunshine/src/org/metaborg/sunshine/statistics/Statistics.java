/**
 * 
 */
package org.metaborg.sunshine.statistics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.Environment;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class Statistics {

	private static final Logger logger = LogManager.getLogger(Statistics.class.getName());

	private final Stack<DataRecording> recordingStack = new Stack<DataRecording>();
	private final File targetFile;

	private static Statistics INSTANCE;

	private Statistics() {
		File f = new File(Environment.INSTANCE().getMainArguments().statstarget);
		targetFile = new File(f.getParent(), FilenameUtils.getBaseName(f.getName()) + "_"
				+ System.currentTimeMillis() + "." + FilenameUtils.getExtension(f.getName()));
		if (targetFile.exists()) {
			logger.fatal("Stats file {} already exists. Refusing to overwrite", targetFile);
			throw new RuntimeException("Stats file already exists");
		}
		targetFile.getParentFile().mkdir();
		try {
			targetFile.createNewFile();
		} catch (IOException ioex) {
			logger.fatal("Failed to create stats file {} because of exception {}", targetFile, ioex);
			throw new RuntimeException("Failed to create statistics target file");
		}
	}

	public static Statistics INSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new Statistics();
		}
		return INSTANCE;
	}

	private DataRecording next() {
		if (recordingStack.size() > 0) {
			recordingStack.peek().close();
		}
		final DataRecording recording = new DataRecording();
		recordingStack.push(recording);
		return recording;
	}

	private DataRecording current() {
		return !recordingStack.empty() ? recordingStack.peek() : next();
	}

	private static final String STR_FORMAT_STRING = "%s ";
	private static final String NEWL_FORMAT_STRING = "%n";
	private static final String SEQNUM_HEAD = "SEQNUM";
	private static final String COMMA_STRING = ",";

	private static final String DEFAULT_VALUE = "-1";

	private List<String> getAllKeys() {
		final Enumeration<DataRecording> recordingsEnum = recordingStack.elements();
		final Set<String> keySet = new HashSet<String>(recordingStack.size());
		while (recordingsEnum.hasMoreElements()) {
			keySet.addAll(recordingsEnum.nextElement().getKeys());
		}
		final List<String> keys = new ArrayList<String>(keySet);
		Collections.sort(keys);
		return keys;
	}

	private String toStringRows(List<String> keys) {
		final StringBuilder row = new StringBuilder();
		final Formatter rowFormatter = new Formatter(row, Locale.US);
		final String rowFormat = makeFormatString(keys);
		for (int idx = alreadyWritten; recordingStack.size() > 0; idx++) {
			final List<String> values = new ArrayList<String>(keys.size() + 1);
			values.add("" + idx);
			values.addAll(recordingStack.pop().toOrderedStrings(keys, DEFAULT_VALUE));
			rowFormatter.format(rowFormat, (Object[]) values.toArray());
		}
		rowFormatter.close();

		return row.toString();
	}

	private String toStringHeader(List<String> keys) {
		final StringBuilder header = new StringBuilder();
		final Formatter headerFormatter = new Formatter(header, Locale.US);
		final String headerFormat = makeFormatString(keys);
		final List<String> headerKeys = new ArrayList<String>(keys.size() + 1);
		headerKeys.add(SEQNUM_HEAD);
		headerKeys.addAll(keys);
		headerFormatter.format(headerFormat, (Object[]) headerKeys.toArray(new String[0]));
		headerFormatter.close();
		return header.toString();
	}

	private String makeFormatString(List<String> keys) {
		final StringBuilder headerFormat = new StringBuilder();
		headerFormat.append(STR_FORMAT_STRING);
		for (int idx = 0; idx < keys.size(); idx++) {
			headerFormat.append(COMMA_STRING);
			headerFormat.append(STR_FORMAT_STRING);
		}
		headerFormat.append(NEWL_FORMAT_STRING);
		return headerFormat.toString();
	}

	private int alreadyWritten = 0;

	private void incrementalWriteToFile() throws IOException {
		final List<String> keys = getAllKeys();
		assert targetFile.exists();
		final int additionalRowCount = recordingStack.size();
		final String header = alreadyWritten > 0 ? "" : toStringHeader(keys);
		final String rows = toStringRows(keys);
		recordingStack.clear();

		FileUtils.writeStringToFile(targetFile, header + rows, alreadyWritten > 0);

		alreadyWritten += additionalRowCount;
	}

	public static void toNext() {
		if (Environment.INSTANCE().isStatEnabled()) {
			try {
				INSTANCE().incrementalWriteToFile();
			} catch (IOException ioex) {
				logger.error("Failed to save statistics to file", ioex);
			}
			INSTANCE().next();
		}
	}

	public static void startTimer(String name) {
		if (Environment.INSTANCE().isStatEnabled()) {
			INSTANCE().current().startTimer(name);
		}
	}

	public static void stopTimer() {
		if (Environment.INSTANCE().isStatEnabled()) {
			INSTANCE().current().stopTimer();
		}
	}

	public static void addDataPoint(String name, long time) {
		if (Environment.INSTANCE().isStatEnabled()) {
			INSTANCE().current().addDataPoint(name, time);
		}
	}

	public static void addDataPoint(String name, IValidatable<?> value) {
		if (Environment.INSTANCE().isStatEnabled()) {
			INSTANCE().current().addDataPoint(name, value);
		}
	}
}
