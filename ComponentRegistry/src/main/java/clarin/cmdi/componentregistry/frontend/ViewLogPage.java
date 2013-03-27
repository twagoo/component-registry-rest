package clarin.cmdi.componentregistry.frontend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.NumberFormat;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author paucas
 */
public class ViewLogPage extends SecureAdminWebPage {

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(ViewLogPage.class);

    public ViewLogPage(final PageParameters pageParameters) {
	super(pageParameters);
	addLinks();
	addLogFileContent();
    }

    private void addLinks() {
	add(new Link("home") {
	    @Override
	    public void onClick() {
		setResponsePage(AdminHomePage.class);
	    }
	});
    }

    private void addLogFileContent() {
	final int tailSize = 1000000; // Megabyte

	final NumberFormat numberFormat = NumberFormat.getIntegerInstance();
	numberFormat.setGroupingUsed(true);

	final File logFile = getLogFile();
	if (logFile != null) {
	    try {
		RandomAccessFile raLogFile = null;
		try {
		    raLogFile = geFileTail(logFile, tailSize);
		    final String content = getLogFileContent(raLogFile);

		    add(new Label("logLabel", String.format("Showing final %s bytes (or less) of total %s in %s:", numberFormat.format(tailSize), numberFormat.format(raLogFile.length()), logFile)));
		    add(new TextArea("logText", new Model(content)));

		    add(new DownloadLink("logDownloadLink", logFile));
		} catch (IOException ioEx) {
		    add(new Label("logLabel", "Could not read from log file. See error message below."));
		    add(new TextArea("logText", new Model(ioEx.getMessage())));
		    throw (ioEx);
		} finally {
		    if (raLogFile != null) {
			raLogFile.close();
		    }
		}
	    } catch (IOException ioEx) {
		logger.error("Error in reading log file", ioEx);
	    }
	}
    }

    private String getLogFileContent(final RandomAccessFile randomAccessFile) throws IOException {
	String currentLine;
	StringBuilder contentBuilder = new StringBuilder();
	while ((currentLine = randomAccessFile.readLine()) != null) {
	    contentBuilder.append(currentLine).append("\n");
	}
	String content = contentBuilder.toString();
	return content;
    }

    private RandomAccessFile geFileTail(File logFile, int tailLength) throws IOException, FileNotFoundException {
	// Skip to tail of file
	final RandomAccessFile raLogFile = new RandomAccessFile(logFile, "r");
	final long startPosition = raLogFile.length() - tailLength;
	if (startPosition > 0) {
	    raLogFile.seek(startPosition);
	    // Read until end of line so we don't end up halfway some random line
	    raLogFile.readLine();
	}
	return raLogFile;
    }

    private File getLogFile() {
	// Get file from appender
	final FileAppender appender = (FileAppender) Logger.getRootLogger().getAppender("ROOT");
	if (appender != null) {
	    return new File(appender.getFile());
	}
	return null;
    }
}