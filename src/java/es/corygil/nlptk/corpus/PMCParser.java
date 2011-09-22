package es.corygil.nlptk.corpus;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;

public class PMCParser {
    static final SAXParserFactory factory = SAXParserFactory.newInstance();

    public static List<PMCCitation> parse(InputStream in) {
        try {
            SAXParser parser = factory.newSAXParser();
            PMCHandler handler = new PMCHandler();
            parser.parse(in, handler);
            return handler.getCitations();
        } catch (Exception e) {e.printStackTrace();return null;}
    }

    public static void main(String[] args) throws Exception {
        File file = new File(args[0]);
        List<PMCCitation> citations = parse(new FileInputStream(file));
    }

}


class PMCHandler extends DefaultHandler {
    private String currently = null;
    private boolean pmcId = false;
    private boolean body = false;
    private StringBuilder builder;
    private PMCCitation citation;
    private List<PMCCitation> citations = new ArrayList<PMCCitation>();

    public List<PMCCitation> getCitations() {
        return citations;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        currently = qName;
        if ("article".equals(currently)) {
            citation = new PMCCitation();
            builder = new StringBuilder();
        }
         else if ("article-id".equals(currently) && attributes.getValue("pub-id-type").equals("pmc"))
             pmcId = true;
         else if ("body".equals(currently))
             body = true;
         else if (body && "p".equals(currently))
             builder.append("\n");
    }

    public void characters(char[] ch, int start, int length) {
        if (body) {
            builder.append(ch);
        }
        else if (pmcId) {
            String chars = new String(ch, start, length);
            citation.id = "PMC" + chars;
            pmcId = false;
        }
    }
    public void endElement(String uri, String localName, String qName) {
        if ("body".equals(qName)) {
            citation.text = builder.toString();
            citations.add(citation);
        }
        currently = null;
    }

    //Don't need or want DTDs
    static final String header = "<?xml version='1.0' encoding='UTF-8'?>";
    public InputSource resolveEntity(String publicId, String systemId) {
        return new InputSource(new java.io.ByteArrayInputStream(header.getBytes()));
    }
}
