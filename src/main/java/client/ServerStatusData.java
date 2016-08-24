package client;

import entity.ServerStatusRecord;

/**
 * @author alex
 * This is the basic object that will be returned to the caller.
 * timestampUtc is the UTC timestamp corresponding to start of the time interval.
 * For example, timestampUtc "1472020260" will mark the record correpoding to the minute that started on Wed Aug 24 06:31:00 2016 UTC
 */
public class ServerStatusData {

	private long timestampUtc;
	private double cpuLoad;
	private double memoryLoad;

	public ServerStatusData(long timestampUtc, double cpuLoad, double memoryLoad) {
		super();
		this.timestampUtc = timestampUtc;
		this.cpuLoad = cpuLoad;
		this.memoryLoad = memoryLoad;
	}

	public ServerStatusData(long timestampUtc, ServerStatusRecord serverStatusRecord) {
		this.timestampUtc = timestampUtc;
		this.cpuLoad = serverStatusRecord.getCpuLoadValue();
		this.memoryLoad = serverStatusRecord.getMemoryLoadValue();		
	}
	
	public long getTimestampUtc() {
		return timestampUtc;
	}

	public double getCpuLoad() {
		return cpuLoad;
	}

	public double getMemoryLoad() {
		return memoryLoad;
	}

	@Override
	public String toString() {
		return "ServerStatusData [timestampUtc=" + timestampUtc + ", cpuLoad=" + cpuLoad + ", memoryLoad=" + memoryLoad
				+ "]";
	}

}
