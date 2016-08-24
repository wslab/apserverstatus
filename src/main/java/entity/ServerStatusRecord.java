package entity;

/**
 * @author alex 
 * 
 * The internal storage format for server status data.
 * The record contains the sum of cpuLoad and memoryLoad values, and the count of measurements.
 * This allows us to accumulate the data without multiplication or division and always be able to get the average.
 */
public class ServerStatusRecord {

	private int count;
	private double cpuLoadValue;
	private double memoryLoadValue;

	public ServerStatusRecord(int count, double cpuLoadValue, double memoryLoadValue) {
		super();
		this.count = count;
		this.cpuLoadValue = cpuLoadValue;
		this.memoryLoadValue = memoryLoadValue;
	}

	/**
	 * @param originalRecord
	 * Copy constructor.
	 */
	public ServerStatusRecord(ServerStatusRecord originalRecord) {
		this.count = originalRecord.count;
		this.cpuLoadValue = originalRecord.cpuLoadValue;
		this.memoryLoadValue = originalRecord.memoryLoadValue;
	}

	public int getCount() {
		return count;
	}

	public double getCpuLoadValue() {
		return cpuLoadValue;
	}

	public double getMemoryLoadValue() {
		return memoryLoadValue;
	}

	/**
	 * @return a record containing the averages for the given time period.
	 */
	public ServerStatusRecord getAverage() {
		if (count == 0) {
			return new ServerStatusRecord(0, 0.0, 0.0);
		}
		return new ServerStatusRecord(count, cpuLoadValue / count, memoryLoadValue / count);
	}

	@Override
	public String toString() {
		return "ServerStatusRecord [count=" + count + ", cpuLoadValue=" + cpuLoadValue + ", memoryLoadValue="
				+ memoryLoadValue + "]";
	}

	public void update(double cpuLoadValue, double memoryLoadValue) {
		count += 1;
		this.cpuLoadValue += cpuLoadValue;
		this.memoryLoadValue += memoryLoadValue;
	}
}
