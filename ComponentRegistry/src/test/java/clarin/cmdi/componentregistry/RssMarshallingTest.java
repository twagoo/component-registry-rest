/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.rss.Rss;
import clarin.cmdi.componentregistry.rss.RssChannel;
import clarin.cmdi.componentregistry.rss.RssItem;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class RssMarshallingTest {

    @Test
    public void testMarshall() throws Exception {
	final Rss rss = new Rss();

	final RssChannel channel = new RssChannel();
	rss.setVersion(BigDecimal.valueOf(2.0));
	rss.setChannel(channel);

	final RssItem item1 = new RssItem();
	item1.setTitle("Item 1");
	item1.setDescription("Description 1");
	channel.getItem().add(item1);

	final RssItem item2 = new RssItem();
	item2.setTitle("Item 2");
	item2.setDescription("Description 2");
	channel.getItem().add(item2);

	ByteArrayOutputStream os = new ByteArrayOutputStream();
	MDMarshaller.marshal(rss, os);
	String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
		+ "<rss version=\"2.0\">\n"
		+ "    <channel>\n"
		+ "        <item>\n"
		+ "            <title>Item 1</title>\n"
		+ "            <description>Description 1</description>\n"
		+ "        </item>\n"
		+ "        <item>\n"
		+ "            <title>Item 2</title>\n"
		+ "            <description>Description 2</description>\n"
		+ "        </item>\n"
		+ "    </channel>\n"
		+ "</rss>";
	assertEquals(expected.trim(), os.toString().trim());
    }
}
