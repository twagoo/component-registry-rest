/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import javax.xml.bind.JAXBException;
import static org.junit.Assert.*;

/**
 * non-automated developers. test, with and input (a valid
 *         component/prifile xml file and a path to it) and the output (the file
 *         where all filenames are set to null, in the same directory as the
 *         input file)
 * @author olhsha 
 * @author George.Georgovassilis@mpi.nl
 */
public class CMDComponentSetFilenamesToNullTestRunner {

    /**
     * 
     * @author olhsha
     */
    /**
     * makes a new component (with filled-in filename)
     * 
     * @param filename
     *            is assigned to the filed "filename" of the new component
     * @param childcomponents
     *            is assigned to the list of child components of the new
     *            component
     * @return the reference to the new component
     */
    protected CMDComponentType makeTestComponent(String filename,
	    List<CMDComponentType> childcomponents) {

	CMDComponentType component = new CMDComponentType();
	component.setFilename(filename);
	List<CMDComponentType> kids = component.getCMDComponent();
	assertFalse(kids == null);
	assertEquals(kids.size(), 0);
	if (childcomponents != null) {
	    kids.addAll(childcomponents);
	}
	;
	return component;
    }

    /*
     * checks if all the filenames related to the component are nulls
     */
    private void checkNullnessOfFilenamesInComponent(CMDComponentType component) {
	assertEquals(component.getFilename(), null);
	checkNullnessOfFilenamesInListOfComponents(component.getCMDComponent());
    }

    /*
     * checks if all the filenames related to the list of component are nulls
     */
    private void checkNullnessOfFilenamesInListOfComponents(
	    List<CMDComponentType> listofcomponents) {
	for (CMDComponentType currentcomponent : listofcomponents) {
	    checkNullnessOfFilenamesInComponent(currentcomponent);
	}
    }

    /*
     * returns true if all the filenames related to the input component are not
     * null
     */
    private boolean checkNonNullnessOfFilenamesInComponent(
	    CMDComponentType component) {

	String filename = component.getFilename();
	System.out.println(filename);
	boolean check = (filename == null);
	if (check) {
	    return false;
	}
	return checkNonNullnessOfFilenamesInListOfComponents(component
		.getCMDComponent());
    }

    /*
     * returns true if all the filenames related to the list of components are
     * not null
     */
    private boolean checkNonNullnessOfFilenamesInListOfComponents(
	    List<CMDComponentType> listofcomponents) {
	boolean check;
	for (CMDComponentType currentcomponent : listofcomponents) {
	    check = checkNonNullnessOfFilenamesInComponent(currentcomponent);
	    if (!check) {
		return false;
	    }
	}
	return true;
    }

    /*
     * adding dummy filenames to a component
     */
    private void addDummyFilenamesToComponent(CMDComponentType component) {
	if (component != null) {
	    component.setFilename("Dummy");
	    List<CMDComponentType> listofcomponents = component
		    .getCMDComponent();
	    addDummyFilenamesToListOfComponents(listofcomponents);
	}
    }

    /*
     * adding dummy filenames to the list of components
     */
    private void addDummyFilenamesToListOfComponents(
	    List<CMDComponentType> listofcomponents) {
	for (CMDComponentType currentcomponent : listofcomponents) {
	    addDummyFilenamesToComponent(currentcomponent);
	}
    }

    /*
     * adds dummy filenames to the content of largeProfile.XML
     */
    private CMDComponentSpec makeTestFromFile(String filename)
	    throws IOException, JAXBException {
	FileInputStream is = new FileInputStream(filename);
	String profilestring = RegistryTestHelper.getStringFromStream(is);
	CMDComponentSpec compspec = RegistryTestHelper
		.getComponentFromString(profilestring); // calling unmarchaller
	List<CMDComponentType> listofcomponents = compspec.getCMDComponent();
	addDummyFilenamesToListOfComponents(listofcomponents);
	assertTrue(checkNonNullnessOfFilenamesInListOfComponents(listofcomponents));
	return compspec;
    }

    /*
     * creating the profile with filenames filled by "Dummy" and writing it into
     * the file
     */
    private void writeDummiedXML(String filenamein, String filenameout)
	    throws IOException, JAXBException {
	CMDComponentSpec compspec = makeTestFromFile(filenamein);
	String os = RegistryTestHelper.getXml(compspec);
	RegistryTestHelper.writeStringToFile(os, filenameout);
    }

    /*
     * generic test-from-file read/write
     */
    private void setFileNamesToNullInFile(String dirName, String fileNameInit,
	    String fileNameDummied, String fileNameUnDummied)
	    throws IOException, JAXBException {

	String path = RegistryTestHelper.openTestDir(dirName);
	writeDummiedXML(path + fileNameInit, path + fileNameDummied);

	FileInputStream is = new FileInputStream(path + fileNameDummied);
	String dummiedcontent = RegistryTestHelper.getStringFromStream(is);
	CMDComponentSpec compspec = RegistryTestHelper
		.getComponentFromString(dummiedcontent); // calling unmarchaller

	List<CMDComponentType> listofcomponents = compspec.getCMDComponent();

	IComponentRegistryRestService testrestservice = new ComponentRegistryRestService();
	testrestservice.setFileNamesFromListToNull(listofcomponents);
	checkNullnessOfFilenamesInListOfComponents(listofcomponents);

	String os = RegistryTestHelper.getXml(compspec);
	RegistryTestHelper.writeStringToFile(os, path + fileNameUnDummied);
    }

    /*
     * developer test method: nulling filenames in an arbitrary (component) file
     * user's input: the name of the file, together with the sub-directory
     * inside target/
     */
    public static void main(String args[]) throws java.io.IOException,
	    JAXBException {

	BufferedReader buffer = new BufferedReader(new InputStreamReader(
		System.in));

	System.out.println("");
	System.out
		.print("Sub-directory (of target/) name? (up to 32 symbols): ");
	String dirName = buffer.readLine();
	System.out.println("");
	System.out
		.println("(Watch out: this is a temorary directory, which is removed after any new clean+build)");
	System.out.println(dirName);

	System.out.println("");
	System.out.println("Check if your file is in this temorary directory");
	System.out.print("and input the file name (up to 32 symbols): ");
	String fileName = buffer.readLine();
	System.out.println(fileName);

	System.out.println("Bedankt, ff wachten .. ");

	String fileNameDummied = "Dummied" + fileName;
	String fileNameUnDummied = "Nulled" + fileName;

	CMDComponentSetFilenamesToNullTestRunner helper = new CMDComponentSetFilenamesToNullTestRunner();

	helper.setFileNamesToNullInFile(dirName, fileName, fileNameDummied,
		fileNameUnDummied);

	System.out.println("Now look up the directory target/" + dirName);
    }
}
