package com.example.flickrtestapp;

public class FlickrPhoto {

	public String id;
	public String secret;
	public String server;
	public String farm;
	public String title;
	
	public String buildPhotoURL()
	{
		return "https://farm"+farm+".staticflickr.com/"+server+"/"+id+"_"+secret+"_m.jpg";
	}
	
	@Override
	public String toString() {
		return	id + "_" + secret + "_" + server + "_" + farm;
	}
}
