package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.rss.Rss;
import clarin.cmdi.componentregistry.rss.RssChannel;
import clarin.cmdi.componentregistry.rss.RssItem;
import java.io.ByteArrayOutputStream;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class RssMarshallingTest extends BaseUnitTest{

    @Test
    public void testMarshall() throws Exception {
	final Rss rss = new Rss();
        
        rss.setVersion(2.0);

	final RssChannel channel = new RssChannel();
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
	marshaller.marshal(rss, os);
	String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
		+ "<rss version=\"2.0\""
                + ">\n"
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
