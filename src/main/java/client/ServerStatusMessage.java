package client;

/**
 * @author alex 
 * 
 * Incoming message format.
 * This is in the "client" package because the callers will need access to this.
 */
public class ServerStatusMessage {

	protected String serverName;
	protected double cpuLoad;
	protected double ramLoad;

	public ServerStatusMessage(String serverName, double cpuLoad, double ramLoad) {
		super();
		this.serverName = serverName;
		this.cpuLoad = cpuLoad;
		this.ramLoad = ramLoad;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public double getCpuLoad() {
		return cpuLoad;
	}

	public void setCpuLoad(double cpuLoad) {
		this.cpuLoad = cpuLoad;
	}

	public double getRamLoad() {
		return ramLoad;
	}

	public void setRamLoad(double ramLoad) {
		this.ramLoad = ramLoad;
	}

	@Override
	public String toString() {
		return "ServerStatusMessage [serverName=" + serverName + ", cpuLoad=" + cpuLoad + ", ramLoad=" + ramLoad + "]";
	}

}
