/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry.frontend;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

/**
 *
 * @author paucas
 */
public class ViewLogPage extends SecureAdminWebPage {

    public ViewLogPage(final PageParameters pageParameters) throws IOException {
        super(pageParameters);
        addLinks();

        File file = getLogFile();
        String content = FileUtils.readFileToString(file, "UTF-8");
        add(new TextArea("logText", new Model(content)));
    }

    private void addLinks() {
        add(new Link("home") {

            @Override
            public void onClick() {
                setResponsePage(AdminHomePage.class);
            }
        });
    }

    private File getLogFile() {
        FileAppender appender = (FileAppender) Logger.getRootLogger().getAppender("ROOT");
        File logFile = new File(appender.getFile());
        return logFile;
    }
}
