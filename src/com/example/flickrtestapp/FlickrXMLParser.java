package com.example.flickrtestapp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

public class FlickrXMLParser {

	private static final String ns = null;
	
	FlickrXMLParser()
	{
	}
	
	public ArrayList<FlickrPhoto> parseXML(InputStream instream) throws XmlPullParserException,IOException
	{
		if (instream == null)
			return null;
        
		XmlPullParser parser = createParser(instream);
		if (parser == null)
			return null;
		
		ArrayList<FlickrPhoto> photos = new ArrayList<FlickrPhoto>();
		
		int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT){
            String name = null;
            switch (eventType){
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if (name.equals("photo")){
                    	photos.add(readPhoto(parser));
                    }
                    break;
            }
            eventType = parser.next();
        }
        
        return photos;
	}
	
	private FlickrPhoto readPhoto(XmlPullParser parser) throws XmlPullParserException, IOException {
		
	    FlickrPhoto photo = new FlickrPhoto();
	    
	    photo.id = parser.getAttributeValue(ns, "id");
	    photo.secret = parser.getAttributeValue(ns, "secret");
	    photo.server = parser.getAttributeValue(ns, "server");
	    photo.farm = parser.getAttributeValue(ns, "farm");
	    photo.title = parser.getAttributeValue(ns, "title");
	    
	    parser.next();
	    return photo;
	}
	
	private XmlPullParser createParser(InputStream instream)
	{
		if (instream == null)
			return null;
		
		XmlPullParser parser = null;
		XmlPullParserFactory pullParserFactory;
		try {
			pullParserFactory = XmlPullParserFactory.newInstance();
			parser = pullParserFactory.newPullParser();
	
		        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
	            parser.setInput(instream, null);
	
		} catch (XmlPullParserException e) {
	
			e.printStackTrace();
		}
		
		return parser;
	}
}
