package eu.trentorise.smartcampus.service.artiststn.script;

import it.sayservice.platform.core.message.Core.Address;
import it.sayservice.platform.core.message.Core.Coordinate;
import it.sayservice.platform.core.message.Core.POI;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.jackson.map.ObjectMapper;

import au.com.bytecode.opencsv.CSVReader;

import com.google.protobuf.Message;

import eu.trentorise.smartcampus.service.artiststn.data.message.Artiststn.Artist;

public class ArtistsScript {

	private static final String ORARIO = "ORARIO: ";
	private static final String D26 = "26 luglio: ";
	private static final String D27 = "e 27 luglio: ";

	public List<Message> load() throws Exception {
		List<Message> result = new ArrayList<Message>();
		
		InputStream rs1 = Thread.currentThread().getContextClassLoader().getResourceAsStream("service/artiststn/artiststn.csv");
		InputStreamReader isr1 = new InputStreamReader(rs1, Charset.forName("UTF-8"));

		InputStream rs2 = Thread.currentThread().getContextClassLoader().getResourceAsStream("service/artiststn/artiststn.json");
		ObjectMapper mapper = new ObjectMapper();
		Map map = mapper.readValue(rs2, Map.class);
		Map<String, double[]> llmap = extractMap(map);

		InputStream rs3 = Thread.currentThread().getContextClassLoader().getResourceAsStream("service/artiststn/artiststn2.csv");
		InputStreamReader isr3 = new InputStreamReader(rs3, Charset.forName("UTF-8"));		
		
		 CSVReader r1 = new CSVReader(isr3, ',', '"');
		 List<String[]> csv = r1.readAll();
		 Map<String, String> descr = new TreeMap<String, String>();
		 Map<String, String> links = new TreeMap<String, String>();
		 Map<String, String> images = new TreeMap<String, String>();
		 for (String[] words: csv) {
			 descr.put(words[0], words[1]);
			 links.put(words[0], words[2]);
			 if (words.length > 3) {
				 images.put(words[0], words[3]);
			 }
		 }
		 
		 CSVReader r2 = new CSVReader(isr1, ',', '"');
		 List<String[]> csv2 = r2.readAll();
		 boolean first = true;
		 for (String[] words: csv2) {
			 if (first) {
				 first = false;
				 continue;
			 }
			 Artist.Builder builder = Artist.newBuilder();
				 builder.setId(words[0]);
				 String name = words[1];
				 builder.setName(name);
				 String d = (String)descr.get(name);
				 builder.setDescription(d);
				 builder.setLink(links.get(name));
				 if (images.containsKey(name)) {
					 builder.setImg(images.get(name));
				 } else {
					 builder.setImg("");
				 }
				 POI poi = buildArtistPOI(name, llmap.get(name), words[2]);
				 builder.setPoi(poi);
				 List<String> times =buildTimes(d);
				 builder.addAllTimes(times);
				 result.add(builder.build());
		 }

		return result;
	}
	
	private Map<String, double[]> extractMap(Map map) {
		Map<String, double[]> llmap = new HashMap<String, double[]>();
		List list = (List)map.get("m");
		for (Object o: list) {
			String name = (String)((Map)o).get("nm");
			double[] ll = new double[2];
			ll[0] = (Double)((Map)o).get("lat");
			ll[1] = (Double)((Map)o).get("lng");
			llmap.put(name, ll);
		}
		return llmap;
	}
	
	private List<String> buildTimes(String descr) {
		List<String> result = new ArrayList<String>();
		int start = descr.indexOf(ORARIO);
		String times = descr.substring(start + ORARIO.length()).replace(".", ":");
		int start26 = times.indexOf(D26);
		int start27 = times.indexOf(D27);
		String s26 = times.substring(start26, start27).replace(D26, "").replace(" e ", "/");
		String s27 = times.substring(start27).replace(D27, "").replace(" e ", "/");

		result.addAll(buildDates("26", s26));
		result.addAll(buildDates("27", s27));
		return result;
	}
	
	private List<String> buildDates(String day, String times) {
		List<String> result = new ArrayList<String>();
		String[] ts = times.split("/");
		for (String t1: ts) {
			String[] se = t1.split("-"); 
			for (String t2 : se) {
				String d = day + "/07/2013 " + t2.trim();
				result.add(d);
			}
		}
		return result;
	}
	
	private POI buildArtistPOI(String name, double[] latlon, String street) {
		POI.Builder poiBuilder = POI.newBuilder();
		
		Address.Builder addressBuilder = Address.newBuilder();
		addressBuilder.setStreet(street);
		addressBuilder.setCity("Trento");
		addressBuilder.setRegion("TN");
		addressBuilder.setCountry("ITA");
		addressBuilder.setState("Italy");
		addressBuilder.setLang("en");
		addressBuilder.setPostalCode("38100");
		
		poiBuilder.setAddress(addressBuilder.build());
		
		Coordinate.Builder coordBuilder = Coordinate.newBuilder();
		
		coordBuilder.setLatitude(latlon[0]);
		coordBuilder.setLongitude(latlon[1]);
		poiBuilder.setCoordinate(coordBuilder.build());
		
		poiBuilder.setDatasetId("smart");
		poiBuilder.setPoiId(name  + "@smartcampus.service.artiststn");
		
		
		return poiBuilder.build();
	}	
	

}
