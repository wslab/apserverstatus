import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.ServerStatusMessage;
import client.ServerStatusResult;
import processor.ServerTrack;

public class ServerStatusMonitor {

	private static long getTimestampFromLine(String line) {
		String[] parts = line.split(",");
		long timestamp = Long.parseLong(parts[0]);
		// negative means this many seconds ago
		if (timestamp < 0)
			timestamp = System.currentTimeMillis() / 1000 + timestamp;
		return timestamp;
	}

	private static ServerStatusMessage line2record(String line) {
		String[] parts = line.split(",");
		String serverName = parts[1];
		double cpuLoad = Double.parseDouble(parts[2]);
		double memoryLoad = Double.parseDouble(parts[3]);
		return new ServerStatusMessage(serverName, cpuLoad, memoryLoad);
	}

	public static void main(String[] args) {
		Logger logger = LoggerFactory.getLogger(ServerStatusMonitor.class);
		logger.info("start");
		String filename = "./input.csv";
		if ( args.length > 0 ) filename = args[0];
		ServerTrack serverTrack = new ServerTrack();
		logger.info("trying to open file " + filename);
		File csvFile = new File(filename);
		long start = System.currentTimeMillis();
		int count = 0;
		Set<String> servers = new HashSet<String>();

		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				if ( line.equals("")) continue;
				long timestamp = getTimestampFromLine(line);
				ServerStatusMessage serverStatusMessage = line2record(line);
				serverTrack.reportData(serverStatusMessage, timestamp);
				servers.add(serverStatusMessage.getServerName());
				count++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		waitForQueue(serverTrack);
		long end = System.currentTimeMillis();
		logger.info("processed " + count + " messages in " + (end - start) + "ms ("
				+ (60.0 / ((end - start) / (double) count)) + " messages per minute)");
		for (String server : servers) {
			ServerStatusResult result = serverTrack.getDataForLast60Minutes(server);
			logger.info("result by minute for " + server + ": " + result.toString());
			ServerStatusResult result2 = serverTrack.getDataForLast24Hours(server);
			logger.info("result by hour for " + server + ": " + result2.toString());
		}
		logger.info("done");
		System.exit(0);
	}

	/**
	 * @param serverTrack
	 * 
	 *            Wait for the queue to finish processing. This is not crucial
	 *            during operation, but we want our tests be exact.
	 */
	private static void waitForQueue(ServerTrack serverTrack) {
		while (!serverTrack.queueIsEmpty()) {
			try {
				Thread.sleep(10);
			} catch (Exception e) {

			}
		}
	}

}
