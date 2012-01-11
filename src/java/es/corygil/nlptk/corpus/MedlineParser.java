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

public class MedlineParser {
    public static List<MedlineCitation> parse(InputStream in) {
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            MedlineHandler handler = new MedlineHandler();
            parser.parse(in, handler);
            return handler.getCitations();
        } catch (Exception e) {e.printStackTrace();return null;}
    }

}

class MedlineHandler extends DefaultHandler {
    private String currently = null;
    private MedlineCitation citation;
    private List<MedlineCitation> citations = new ArrayList<MedlineCitation>();

    public List<MedlineCitation> getCitations() {
        return citations;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        currently = qName;
         if ("MedlineCitation".equals(currently))
            citation = new MedlineCitation();

    }

    public void characters(char[] ch, int start, int length) {
        String chars = new String(ch, start, length);
        if ("PMID".equals(currently)) {
            citation.pmid = Integer.parseInt(chars);
        }
        else if ("ArticleTitle".equals(currently))
            citation.title += chars;
        else if ("AbstractText".equals(currently))
            citation.abstrct += chars + " ";
    }
    public void endElement(String uri, String localName, String qName) {
        if ("MedlineCitation".equals(qName)) {
            citation.abstrct = citation.abstrct.trim();
            citations.add(citation);
        }
        currently = null;
    }
}
