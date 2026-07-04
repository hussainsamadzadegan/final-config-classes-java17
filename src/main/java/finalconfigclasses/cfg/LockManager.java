package finalconfigclasses.cfg;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This singelton class holds the readwrite lock based its lockID.
 * It will deliver the readwrite locks to clients.
 * It is the manager of locks.
 * 
 * This class should be deployed as shared library (because it would maintain the
 * locks of config beans).
 */
public final class LockManager {
	private static class SingletonHolder {
		static final LockManager THE_ONE = new LockManager();
	}
	
	private final HashMap<String, ReentrantReadWriteLock> map = new HashMap<String, ReentrantReadWriteLock>();
	private final Object mapLock = new Object();
	
	private LockManager() {}
	
	public static LockManager getInstance() {
		return SingletonHolder.THE_ONE;
	}
	
	/**
	 * @param lockID the id of lock
	 * @param lock the lock associated with properties file
	 */
	public void putLock(String lockID, ReentrantReadWriteLock lock) {
		synchronized (mapLock) {
			if (map.containsKey(lockID))
				throw new IllegalArgumentException("The lock with ID '"+lockID+"' already exists.");
			map.put(lockID, lock);			
		}
	}
	
	public ReentrantReadWriteLock getLock(String lockID) {		
		return getLock(lockID, false);
	}
	
	public ReentrantReadWriteLock getLock(String lockID, boolean createLockIfAbsent) {
		synchronized (mapLock) {
			ReentrantReadWriteLock lock = map.get(lockID);
			if(createLockIfAbsent && (!map.containsKey(lockID))) {
				lock = new ReentrantReadWriteLock(true);
				map.put(lockID, lock);				
			}
			return lock;
		}
	}
	
	public ReentrantReadWriteLock removeLock(String lockID) {
		synchronized (mapLock) {
			if (!map.containsKey(lockID))
				throw new IllegalArgumentException("The lock with ID '"+lockID+"' not found.");
			ReentrantReadWriteLock lock = map.remove(lockID);
			return lock;
		}
	}
	
}
