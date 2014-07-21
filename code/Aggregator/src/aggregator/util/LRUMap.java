package aggregator.util;

import java.util.LinkedHashMap;

public class LRUMap<K,V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = 201407040228L;
	private int maxSize;
	
	/**
	 * Constructs an empty insertion-ordered {@code LRUMap} instance with the specified initial capacity and a default load factor (0.75).
	 * @param maxSize maximum size of the map
	 * @throws IllegalArgumentException
	 */
	public LRUMap(int maxSize) throws IllegalArgumentException {
		super(maxSize);
		
		this.maxSize = maxSize;
	}
	
	
	
	
	/**
	 * Constructs an empty insertion-ordered {@code LRUMap} instance with the specified initial capacity and load factor.
	 * @param maxSize maximum size of the map
	 * @param loadFactor the load factor
	 * @throws IllegalArgumentException
	 */
	public LRUMap(int maxSize, float loadFactor) throws IllegalArgumentException {
		super(maxSize, loadFactor);
		
		this.maxSize = maxSize;
	}
	
	
	
	
	/**
	 * Constructs an empty {@code LRUMap} instance with the specified initial capacity, load factor and ordering mode.
	 * @param maxSize maximum size of the map
	 * @param loadFactor the load factor
	 * @param accessOrder the ordering mode - {@code true} for access-order, {@code false} for insertion-order
	 * @throws IllegalArgumentException
	 */
	public LRUMap(int maxSize, float loadFactor, boolean accessOrder) throws IllegalArgumentException {
		super(maxSize, loadFactor, accessOrder);
	}
	
	
	
	
	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
		return size() > maxSize;
	}
}
