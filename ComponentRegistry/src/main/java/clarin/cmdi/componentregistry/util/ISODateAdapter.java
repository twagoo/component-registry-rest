package clarin.cmdi.componentregistry.util;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import clarin.cmdi.componentregistry.DatesHelper;

/**
 * Formats dates for XML (de)serialization
 * @author george.georgovassilis@mpi.nl
 *
 */
public class ISODateAdapter extends XmlAdapter<String, Date> {

    @Override
    public String marshal(Date v) throws Exception {
	String string = DatesHelper.isoFormat(v);
	return string;
    }

    @Override
    public Date unmarshal(String v) throws Exception {
	Date date = DatesHelper.parseIso(v);
	return date;
    }

}