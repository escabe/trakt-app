package com.escabe;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class TvdbUtils {
	static public ArrayList<Movie> Search(String title) {
			ArrayList<Movie> list = new ArrayList<Movie>(); 
			try {
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document doc = db.parse("http://www.thetvdb.com/api/GetSeries.php?seriesname=" + title);
				NodeList series = doc.getElementsByTagName("Series");
				for (int i=0;i<series.getLength();i++) {
					Movie m = new Movie();
					Element s = (Element) series.item(i);
					m.setTitle(s.getElementsByTagName("SeriesName").item(0).getTextContent());
					m.setID(s.getElementsByTagName("id").item(0).getTextContent());
					list.add(m);
				}
				return list;
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
	
	}
}
