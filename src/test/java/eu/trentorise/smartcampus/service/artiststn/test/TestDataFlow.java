package eu.trentorise.smartcampus.service.artiststn.test;

import it.sayservice.platform.servicebus.test.DataFlowTestHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.google.protobuf.Message;

import eu.trentorise.smartcampus.service.artiststn.data.message.Artiststn.Artist;
import eu.trentorise.smartcampus.service.artiststn.impl.GetArtistsDataFlow;

public class TestDataFlow extends TestCase {
	
	public void testRun() throws Exception {
		DataFlowTestHelper helper = new DataFlowTestHelper();
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		Map<String, Object> out = helper.executeDataFlow("smartcampus.service.artiststn", "GetArtists", new GetArtistsDataFlow(), parameters);
		List<Message> data = (List<Message>)out.get("data");
		for (Message msg: data) {
//			System.out.println("\"" + ((Artist)msg).getTimesList() + "\",\"\"");
			System.out.println(((Artist)msg).getTimesList());
		}

	}
}
