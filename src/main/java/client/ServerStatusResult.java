package client;

import java.util.List;

/**
 * @author alex
 *
 * The main object that will be returned.
 * In the "client" package because the caller will need access to it.
 *
 */
public class ServerStatusResult {

	private String serverName;
	private List<ServerStatusData> data;

	public ServerStatusResult(String serverName, List<ServerStatusData> result) {
		this.serverName = serverName;
		this.data = result;
	}

	public String getServerName() {
		return serverName;
	}

	public List<ServerStatusData> getData() {
		return data;
	}

	@Override
	public String toString() {
		String result = "ServerStatusResult [serverName=" + serverName + "\n";
		for (ServerStatusData ssd : data) {
			result += "[" + ssd.toString() + "]\n";
		}
		result += "]";
		return result;
	}

}
