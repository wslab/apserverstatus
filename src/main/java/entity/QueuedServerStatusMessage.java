package entity;

import client.ServerStatusMessage;

/**
 * @author alex
 * This internal class wraps the ServerStatusMessage and adds a timestamp. 
 * We add this timestamp on the way in before the message is queued for processing.
 */
public class QueuedServerStatusMessage extends ServerStatusMessage {

	private long timestampUtc;

	/**
	 * @param serverName
	 * @param cpuLoad
	 * @param ramLoad
	 * @param timestampUtc
	 * Constructor that allows us to pass a timestamp, mostly for testing.
	 */
	public QueuedServerStatusMessage(String serverName, double cpuLoad, double ramLoad, long timestampUtc) {
		super(serverName, cpuLoad, ramLoad);
		this.timestampUtc = timestampUtc;
	}
	
	/**
	 * @param serverName
	 * @param cpuLoad
	 * @param ramLoad
	 * Constructor that automatically inserts timestamp.
	 */
	public QueuedServerStatusMessage(String serverName, double cpuLoad, double ramLoad) {
		super(serverName, cpuLoad, ramLoad);
		this.timestampUtc = System.currentTimeMillis()/1000;
	}

	public long getTimestampUtc() {
		return timestampUtc;
	}

	@Override
	public String toString() {
		return "QueuedServerStatusMessage [timestampUtc=" + timestampUtc + ", serverName=" + serverName + ", cpuLoad="
				+ cpuLoad + ", ramLoad=" + ramLoad + "]";
	}

}
