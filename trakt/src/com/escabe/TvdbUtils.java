/*******************************************************************************
 * Copyright 2011 EscAbe
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.escabe;

import java.io.IOException;
import java.net.URLEncoder;
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
				Document doc = db.parse("http://www.thetvdb.com/api/GetSeries.php?seriesname=" + URLEncoder.encode(title));
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
