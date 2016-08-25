package processor;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.ServerStatusData;
import client.ServerStatusMessage;
import client.ServerStatusResult;

public class ServerTrackTest {

	Logger logger = LoggerFactory.getLogger(ServerTrackTest.class);

	private static double EPSILON = 1e-6;

	private static ServerTrack serverTrack;

	@BeforeClass
	public static void setupSuite() {
		serverTrack = new ServerTrack();
	}

	@Before
	public void setupTest() {
	}

	@After
	public void resetTest() {
		serverTrack.Reset();
	}

	@Test
	public void testServerTrackCanInitialize() {
		ServerTrack serverTrack = null;
		try {
			serverTrack = new ServerTrack();
		} catch (Exception e) {
			fail("exception in initialization: " + e.getMessage());
		}
	}

	@Test
	public void testWithoutDataHours() {
		ServerStatusResult result = serverTrack.getDataForLast24Hours("myserver");
		assertEquals("myserver", result.getServerName());
		assertEquals(24, result.getData().size());
		for (ServerStatusData data : result.getData()) {
			assertEquals(0.0, data.getCpuLoad(), EPSILON);
			assertEquals(0.0, data.getMemoryLoad(), EPSILON);
		}
		// make sure timestamps are correct
		long latestTimestamp = result.getData().get(0).getTimestampUtc();
		for (int i = 1; i < 24; i++) {
			assertEquals(latestTimestamp - i * 3600, result.getData().get(i).getTimestampUtc());
		}
	}

	@Test
	public void testWithoutDataMinutes() {
		ServerStatusResult result = serverTrack.getDataForLast60Minutes("myserver");
		assertEquals("myserver", result.getServerName());
		assertEquals(60, result.getData().size());
		for (ServerStatusData data : result.getData()) {
			assertEquals(0.0, data.getCpuLoad(), EPSILON);
			assertEquals(0.0, data.getMemoryLoad(), EPSILON);
		}
		// make sure timestamps are correct
		long latestTimestamp = result.getData().get(0).getTimestampUtc();
		for (int i = 1; i < 60; i++) {
			assertEquals(latestTimestamp - i * 60, result.getData().get(i).getTimestampUtc());
		}
	}

	@Test public void testFieldsAreNotSwapped() {
		serverTrack.reportData(new ServerStatusMessage("myserver1", 1.0, 2.0));
		waitForQueue(serverTrack);
		ServerStatusResult resultMinutes = serverTrack.getDataForLast60Minutes("myserver1");
		assertEquals(1.0, resultMinutes.getData().get(0).getCpuLoad(), EPSILON);
		assertEquals(2.0, resultMinutes.getData().get(0).getMemoryLoad(), EPSILON);
		ServerStatusResult resultHours = serverTrack.getDataForLast24Hours("myserver1");
		assertEquals(1.0, resultHours.getData().get(0).getCpuLoad(), EPSILON);
		assertEquals(2.0, resultHours.getData().get(0).getMemoryLoad(), EPSILON);
	}

	@Test
	public void testServersAreNotMixed() {
		serverTrack.reportData(new ServerStatusMessage("myserver1", 1.0, 2.0));
		serverTrack.reportData(new ServerStatusMessage("myserver2", 3.0, 4.0));
		waitForQueue(serverTrack);
		ServerStatusResult result1 = serverTrack.getDataForLast60Minutes("myserver1");
		ServerStatusResult result2 = serverTrack.getDataForLast60Minutes("myserver2");
		assertEquals(1.0, result1.getData().get(0).getCpuLoad(), EPSILON);
		assertEquals(2.0, result1.getData().get(0).getMemoryLoad(), EPSILON);
		assertEquals(3.0, result2.getData().get(0).getCpuLoad(), EPSILON);
		assertEquals(4.0, result2.getData().get(0).getMemoryLoad(), EPSILON);
	}

	@Test
	public void testResultsAreAveraged() {
		serverTrack.reportData(new ServerStatusMessage("myserver1", 1.0, 2.0));
		serverTrack.reportData(new ServerStatusMessage("myserver1", 2.0, 4.0));
		waitForQueue(serverTrack);
		ServerStatusResult result1 = serverTrack.getDataForLast60Minutes("myserver1");
		assertEquals(1.5, result1.getData().get(0).getCpuLoad(), EPSILON);
		assertEquals(3.0, result1.getData().get(0).getMemoryLoad(), EPSILON);
		ServerStatusResult result2 = serverTrack.getDataForLast24Hours("myserver1");
		assertEquals(1.5, result2.getData().get(0).getCpuLoad(), EPSILON);
		assertEquals(3.0, result2.getData().get(0).getMemoryLoad(), EPSILON);
	}

	@Test
	public void testResultsAreByTimestampMminutes() {
		long timestampLess1Minute = System.currentTimeMillis() / 1000 - 60;
		serverTrack.reportData(new ServerStatusMessage("myserver1", 1.0, 2.0));
		serverTrack.reportData(new ServerStatusMessage("myserver1", 0.1, 0.2), timestampLess1Minute);
		waitForQueue(serverTrack);
		ServerStatusResult result1 = serverTrack.getDataForLast60Minutes("myserver1");
		assertEquals(1.0, result1.getData().get(0).getCpuLoad(), EPSILON);
		assertEquals(2.0, result1.getData().get(0).getMemoryLoad(), EPSILON);
		assertEquals(0.1, result1.getData().get(1).getCpuLoad(), EPSILON);
		assertEquals(0.2, result1.getData().get(1).getMemoryLoad(), EPSILON);
	}

	@Test
	public void testResultsAreByTimestampHours() {
		long timestampLess1Hour = System.currentTimeMillis() / 1000 - 3600;
		serverTrack.reportData(new ServerStatusMessage("myserver1", 1.0, 2.0));
		serverTrack.reportData(new ServerStatusMessage("myserver1", 0.1, 0.2), timestampLess1Hour);
		waitForQueue(serverTrack);
		ServerStatusResult result1 = serverTrack.getDataForLast24Hours("myserver1");
		assertEquals(1.0, result1.getData().get(0).getCpuLoad(), EPSILON);
		assertEquals(2.0, result1.getData().get(0).getMemoryLoad(), EPSILON);
		assertEquals(0.1, result1.getData().get(1).getCpuLoad(), EPSILON);
		assertEquals(0.2, result1.getData().get(1).getMemoryLoad(), EPSILON);
	}

	@Test
	public void testHotServer() {
		int count = 10000;
		Random r = new Random();
		double totalCpuLoad = 0.0;
		double totalMemoryLoad = 0.0;
		double minLoad = 0.0;
		double maxLoad = 5.0;
		for (int i = 0; i < count; i++) {
			double cpuLoad = minLoad + (maxLoad - minLoad) * r.nextDouble();
			double memoryLoad = minLoad + (maxLoad - minLoad) * r.nextDouble();
			totalCpuLoad += cpuLoad;
			totalMemoryLoad += memoryLoad;
			serverTrack.reportData(new ServerStatusMessage("myserver", cpuLoad, memoryLoad));
		}
		waitForQueue(serverTrack);
		ServerStatusResult result1 = serverTrack.getDataForLast60Minutes("myserver");
		assertEquals(totalCpuLoad / count, result1.getData().get(0).getCpuLoad(), EPSILON);
		assertEquals(totalMemoryLoad / count, result1.getData().get(0).getMemoryLoad(), EPSILON);
		ServerStatusResult result2 = serverTrack.getDataForLast24Hours("myserver");
		assertEquals(totalCpuLoad / count, result2.getData().get(0).getCpuLoad(), EPSILON);
	}

	@Test
	public void testMessagesAreValidated() {
		assertFalse(serverTrack.reportData(new ServerStatusMessage(null, 1.0, 1.0)));
		assertFalse(serverTrack.reportData(new ServerStatusMessage("", 1.0, 1.0)));
		assertFalse(serverTrack.reportData(new ServerStatusMessage("myserver", -1.0, 1.0)));
		assertFalse(serverTrack.reportData(new ServerStatusMessage("myserver", 1.0, -1.0)));
		
		ServerStatusResult result = serverTrack.getDataForLast24Hours("myserver");
		assertEquals("myserver", result.getServerName());
		assertEquals(24, result.getData().size());
		for (ServerStatusData data : result.getData()) {
			assertEquals(0.0, data.getCpuLoad(), EPSILON);
			assertEquals(0.0, data.getMemoryLoad(), EPSILON);
		}

		ServerStatusResult result2 = serverTrack.getDataForLast24Hours("myserver");
		assertEquals("myserver", result2.getServerName());
		assertEquals(24, result2.getData().size());
		for (ServerStatusData data : result2.getData()) {
			assertEquals(0.0, data.getCpuLoad(), EPSILON);
			assertEquals(0.0, data.getMemoryLoad(), EPSILON);
		}}
	
	/**
	 * @param serverTrack
	 * 
	 * Wait for the queue to finish processing. This is not crucial during operation,
	 * but we want our tests be exact.
	 */
	private void waitForQueue(ServerTrack serverTrack) {
		while (!serverTrack.queueIsEmpty()) {
			try {
				Thread.sleep(10);
			} catch (Exception e) {

			}
		}
	}


}
