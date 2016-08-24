package storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.ServerStatusData;
import client.ServerStatusResult;
import entity.QueuedServerStatusMessage;
import entity.ServerStatusRecord;

/**
 * @author alex
 *
 * Statistics storage for the server data.
 * 
 * Minute data and hourly data are stored in 2 separate concurrent HashMaps with the server name as the key.
 * 
 * For each server, we keep a concurrent HashMap with the timestapm of the interval as the key and data object as a value.
 * Since we only need the results for the last 60 minutes by minute and 24 hours by the hour, we keep the sums by minute and by hour
 * so we only need to calculate the average when we get a request for results.
 * 
 * The data is always inserted by a single thread so there is no need for locking.
 *
 */
public class StatStorage implements IStatStorage {

	private ConcurrentHashMap<String, ConcurrentHashMap<Long, ServerStatusRecord>> minuteMap;

	private ConcurrentHashMap<String, ConcurrentHashMap<Long, ServerStatusRecord>> hourMap;

	private final static Logger logger = LoggerFactory.getLogger(StatStorage.class);

	public StatStorage() {
		minuteMap = new ConcurrentHashMap<String, ConcurrentHashMap<Long, ServerStatusRecord>>();
		hourMap = new ConcurrentHashMap<String, ConcurrentHashMap<Long, ServerStatusRecord>>();
	}

	/**
	 * @param map
	 * @param timestampUtc
	 * @param message
	 * 
	 * adds the data from a message to given map with the given timestamp
	 */
	private void addToMap(ConcurrentHashMap<String, ConcurrentHashMap<Long, ServerStatusRecord>> map, Long timestampUtc,
			QueuedServerStatusMessage message) {
		String serverName = message.getServerName();
		if (!map.containsKey(serverName)) {
			map.put(serverName, new ConcurrentHashMap<Long, ServerStatusRecord>());
		}
		ConcurrentHashMap<Long, ServerStatusRecord> serverHashMap = map.get(serverName);
		if (!serverHashMap.containsKey(timestampUtc)) {
			serverHashMap.put(timestampUtc, new ServerStatusRecord(0, 0.0, 0.0));
		}
		// create a copy of the original status record and insert it into the
		// map. We want the update to be atomic to make sure the data in the map is always consistent.
		ServerStatusRecord originalRecord = serverHashMap.get(timestampUtc);
		ServerStatusRecord statusRecord = new ServerStatusRecord(originalRecord);
		statusRecord.update(message.getCpuLoad(), message.getRamLoad());
		serverHashMap.put(timestampUtc, statusRecord);
	}

	/* 
	 * @param message - the message that contains server status data
	 * 
	 * Adds data from the message to both minute and hour maps
	 */
	public void addToMaps(QueuedServerStatusMessage message) {
		long timestampUtc = message.getTimestampUtc();
		long minuteTimestampUtc = (timestampUtc / 60) * 60;
		addToMap(minuteMap, minuteTimestampUtc, message);
		long hourTimestampUtc = (timestampUtc / 3600) * 3600;
		addToMap(hourMap, hourTimestampUtc, message);
	}

	/**
	 * @param serverName
	 * @return ServerStatusResult with the data for this server for the last 60 minutes
	 */
	public ServerStatusResult getDataForLast60Minutes(String serverName) {
		return getDataForLast60Minutes(serverName, System.currentTimeMillis() / 1000);
	}

	/**
	 * @param serverName
	 * @param timestampUtc
	 * @return ServerStatusResult with the data for this server for the last 60 minutes preceding the timestamp
	 */
	public ServerStatusResult getDataForLast60Minutes(String serverName, long timestampUtc) {
		long minuteTimestampUtc = (timestampUtc / 60) * 60;
		long earliestTimestampUtc = minuteTimestampUtc - 3600;
		logger.info("getdata60 timestampUtc: " + timestampUtc + " minuteTimestampUtc: " + minuteTimestampUtc);
		HashMap<Long, ServerStatusRecord> resultHash = new HashMap<Long,ServerStatusRecord>();
		for (int i = 0; i < 60; i++) {
			resultHash.put(minuteTimestampUtc - i * 60, new ServerStatusRecord(0, 0.0, 0.0));
		}
		if (minuteMap.containsKey(serverName)) {
			minuteMap.get(serverName).forEach((key,value) -> { System.out.println("key: " + key + " value: " + value);
					if ( key > earliestTimestampUtc && key <= minuteTimestampUtc ) {
						resultHash.put(key, new ServerStatusRecord(minuteMap.get(serverName).get(key)));
					}
				});
		}
		List<ServerStatusData> result = new ArrayList<ServerStatusData>();
		for (int i = 0; i < 60; i++) {
			result.add(new ServerStatusData(minuteTimestampUtc - i * 60, resultHash.get(minuteTimestampUtc - i * 60).getAverage()));
		}
		return new ServerStatusResult(serverName, result);
	}

	/**
	 * @param serverName
	 * @return ServerStatusResult with the data for this server for the last 24 hours
	 */
	public ServerStatusResult getDataForLast24Hours(String serverName) {
		return getDataForLast60Minutes(serverName, System.currentTimeMillis() / 1000);
	}

	/**
	 * @param serverName
	 * @param timestampUtc
	 * @return ServerStatusResult with the data for this server for the last 24 hours preceding the timestamp
	 */
	public ServerStatusResult getDataForLast24Hours(String serverName, long timestampUtc) {
		long minuteTimestampUtc = (timestampUtc / 3600) * 3600;
		long earliestTimestampUtc = minuteTimestampUtc - 3600 * 24;
		logger.info("getdata24 timestampUtc: " + timestampUtc + " minuteTimestampUtc: " + minuteTimestampUtc);
		HashMap<Long, ServerStatusRecord> resultHash = new HashMap<Long,ServerStatusRecord>();
		for (int i = 0; i < 24; i++) {
			resultHash.put(minuteTimestampUtc - i * 3600, new ServerStatusRecord(0, 0.0, 0.0));
		}
		if (minuteMap.containsKey(serverName)) {
			minuteMap.get(serverName).forEach((key,value) -> { System.out.println("key: " + key + " value: " + value);
					if ( key > earliestTimestampUtc && key <= minuteTimestampUtc ) {
						resultHash.put(key, new ServerStatusRecord(minuteMap.get(serverName).get(key)));
					}
				});
		}
		List<ServerStatusData> result = new ArrayList<ServerStatusData>();
		for (int i = 0; i < 24; i++) {
			result.add(new ServerStatusData(minuteTimestampUtc - i * 3600, resultHash.get(minuteTimestampUtc - i * 3600).getAverage()));
		}
		return new ServerStatusResult(serverName, result);
	}


}
