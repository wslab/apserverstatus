package storage;

import entity.QueuedServerStatusMessage;

/**
 * @author alex
 *
 * Storage engine interface for MessageProcessor
 */
public interface IStatStorage {
	void addToMaps(QueuedServerStatusMessage message);
}
