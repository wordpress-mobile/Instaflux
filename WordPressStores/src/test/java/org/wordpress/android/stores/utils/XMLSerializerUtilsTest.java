package org.wordpress.android.stores.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.wordpress.android.stores.utils.xmlrpc.XMLSerializerUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

@RunWith(RobolectricTestRunner.class)
public class XMLSerializerUtilsTest {
    @Test
    public void testXmlRpcResponseScrubWithJunk() {
        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><nothing></nothing>";
        final String junk = "this is junk text 12345,./;'pp<<><><;;";
        final String result = scrub(junk + xml, xml.length());
        Assert.assertEquals(xml, result);
    }

    @Test
    public void testXmlRpcResponseScrubWithoutJunk() {
        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><nothing></nothing>";
        final String result = scrub(xml, xml.length());
        Assert.assertEquals(xml, result);
    }

    private String scrub(String input, int xmlLength) {
        try {
            final InputStream is = new ByteArrayInputStream(input.getBytes("UTF-8"));
            final InputStream resultStream = XMLSerializerUtils.scrubXmlResponse(is);
            byte[] bb = new byte[xmlLength];
            int val = -1;
            for (int i = 0; i < bb.length && ((val = resultStream.read()) != -1); ++i) {
                bb[i] = (byte) val;
            }

            is.close();
            resultStream.close();

            return new String(bb, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        } catch (IOException e) {
        }
        return null;
    }
}
