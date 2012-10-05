package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.rss.Rss;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author olhsha, non-automatised developer's test class
 * Main method generates an Rss xml-file to validate it later using one of the on-line rss-source validators
 */
public class xmlRssGeneratingRunner {

    public void printIds(List<AbstractDescription> desc) {
	for (AbstractDescription current : desc) {
	    String currentId = current.getId();
	    System.out.println(currentId);
	}
    }

    private static void printXmlRssToFile(Rss rssObject) throws IOException, JAXBException {

	String path = RegistryTestHelper.openTestDir("testRss");
	String os = MDMarshaller.marshalToString(rssObject);
	RegistryTestHelper.writeStringToFile(os, path + "testRssResl.xml");

    }

    public static void main(String args[]) throws ComponentRegistryException, ParseException, IOException, JAXBException {

	System.out.println("Type 1, or 2, or 3, or 4, if you want to check Rss generaion ");
	System.out.print("for profiles, components, profile comments or component comments respectively: >> ");

	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	String buffer = null;

	//  read the username from the command-line; need to use try/catch with the
	//  readLine() method
	try {
	    buffer = br.readLine();
	} catch (IOException ioe) {
	    System.out.println("IO error trying get the number");
	    System.exit(1);
	}

	int kind = Integer.parseInt(buffer);

	ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/applicationContextJDBC.xml");
	ComponentRegistry registry = ((ComponentRegistryFactory) applicationContext.getBean("componentRegistryFactory")).getPublicRegistry();

	ComponentRegistryRestService restService = new ComponentRegistryRestService();
	Rss rss = null;
	String baseUri = "http://localhost:8080/ComponentRegistry/"; // used only as a nice URi-example content for the "link"-field
	// in this test we do not click on link, we just generate an Rss xml-file to validate it
	// using one of the on-line rss-source vaidators

	if (kind == 1) { // testing Rss for profiles
	    List<ProfileDescription> profs = registry.getProfileDescriptions();

	    System.out.println(profs.size());
	    Collections.sort(profs, AbstractDescription.COMPARE_ON_DATE);
	    System.out.println(profs.size());

	    System.out.println("check if the profiles are sorted in a proper way, by the dates ");
	    for (ProfileDescription pdesc : profs) {
		System.out.println(pdesc.getRegistrationDate());
	    }

	    rss = restService.getRss(Integer.toString(profs.size()), profs, "Updates for profiles", "RSS for public profiles", baseUri, false);
	    System.out.println(rss.getChannel().getItem().size());

	}


	if (kind == 2) { // testing rss for components
	    List<ComponentDescription> comps = registry.getComponentDescriptions();

	    Collections.sort(comps, AbstractDescription.COMPARE_ON_DATE);

	    System.out.println("check if the components are sorted in a proper way, by the dates");
	    for (ComponentDescription cdesc : comps) {
		System.out.println(cdesc.getRegistrationDate());
	    }
	    rss = restService.getRss(Integer.toString(comps.size()), comps, "Updates for components", "RSS for public components", baseUri, false);
	}

	if (kind == 3) { // testing Rss comments for profiles
	    String profileId = "clarin.eu:cr1:p_1284723009187";
	    List<Comment> comms = registry.getCommentsInProfile(profileId, null);

	    Collections.sort(comms, Comment.COMPARE_ON_DATE);

	    System.out.println("check if the comments are sorted in a proper way");
	    for (Comment comm : comms) {
		System.out.println(comm.getCommentDate());
	    }

	    rss = restService.getRssOfComments(Integer.toString(comms.size()), comms, "Updates for the profile " + profileId + " comments ", "RSS for profile comments", profileId, baseUri, false);
	}

	if (kind == 4) { // testing rss comments for components
	    String componentId = "clarin.eu:cr1:c_1288172614011";
	    List<Comment> comms = registry.getCommentsInComponent(componentId, null);
	    Collections.sort(comms, Comment.COMPARE_ON_DATE);

	    System.out.println("check if the comments are sorted in a proper way");
	    for (Comment comm : comms) {
		System.out.println(comm.getCommentDate());
	    }

	    rss = restService.getRssOfComments("10", comms, "Updates for the component " + componentId + " comments", "RSS for component comments", "clarin.eu:cr1:p_1284723009187", baseUri, false);
	}

	printXmlRssToFile(rss);
    }
}
