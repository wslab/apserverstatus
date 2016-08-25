package client;

/**
 * @author alex
 *
 * The main API. 
 */
public interface IServerTrack {

	/**
	 * @param serverStatusMessage ServerStatusMessage
	 * @return true if the message passed validation and will be processed, false otherwise.
	 * 
	 * Note that the reported data may not be immediately visible in the stats because aggregation process is asynchronous.
	 */
	boolean reportData(ServerStatusMessage serverStatusMessage);
	
	/**
	 * @param serverName The name of the server to report the data for.
	 * @return Stats for the last 60 minutes by minutes for the given server.
	 */
	ServerStatusResult getDataForLast60Minutes(String serverName);
	
	/**
	 * @param serverName The name of the server to report the data for.
	 * @return Stats for the last 24 hours by hour for the given server.
	 */
	ServerStatusResult getDataForLast24Hours(String serverName);
	
}
