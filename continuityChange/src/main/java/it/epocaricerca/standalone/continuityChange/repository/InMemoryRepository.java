package it.epocaricerca.standalone.continuityChange.repository;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InMemoryRepository {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	private HashMap<String, Integer> entityAttributeCount = new HashMap<String, Integer>();
	
	private HashMap<String, Integer> entityIntervalTime = new HashMap<String, Integer>();
	
	private HashMap<String, HashMap<String, Integer>> allOtherEntitiesAttributeCount = new HashMap<String, HashMap<String, Integer>>();
	
	private HashMap<String, Integer> allEntitiesAttributeCount = new HashMap<String, Integer>();
	
	private HashMap<String, List<Integer>> entityYearsMap = new HashMap<String, List<Integer>>();
	
	private HashMap<String, List<String>> entityAttributesMap = new HashMap<String, List<String>>();

	public HashMap<String, Integer> getEntityAttributeCount() {
		return entityAttributeCount;
	}

	public HashMap<String, List<Integer>> getEntityYearsMap() {
		return entityYearsMap;
	}

	public HashMap<String, List<String>> getEntityAttributesMap() {
		return entityAttributesMap;
	}
	
	public HashMap<String, Integer> getAllEntitiesAttributeCount() {
		return allEntitiesAttributeCount;
	}

	public HashMap<String, HashMap<String, Integer>> getAllOtherEntitiesAttributeCount() {
		return allOtherEntitiesAttributeCount;
	}

	public HashMap<String, Integer> getEntityIntervalTime() {
		return entityIntervalTime;
	}

	public void resetHashMaps() {
		this.entityAttributeCount.clear();
		this.allEntitiesAttributeCount.clear();
		this.entityAttributesMap.clear();
		this.entityYearsMap.clear();
		this.allOtherEntitiesAttributeCount.clear();
		this.entityIntervalTime.clear();
	}
	
	public synchronized void addEntityAttributeCount(String key, String attribute) {
		if(this.entityAttributeCount.containsKey(key)) {
			Integer oldValue = this.entityAttributeCount.get(key);
			this.entityAttributeCount.put(key, new Integer(oldValue.intValue()+1));
		} else 
			this.entityAttributeCount.put(key, new Integer(1));
	}
	
	public synchronized void addAllOtherEntitiesAttributeCount(String key, String entityId, String attribute) {
		
		if(this.allOtherEntitiesAttributeCount.containsKey(key)) {
			
			HashMap<String, Integer> oldMap = this.allOtherEntitiesAttributeCount.get(key);
			
			if(oldMap.containsKey(entityId)) {
				Integer oldValue = oldMap.get(entityId);
				oldMap.put(entityId, new Integer(oldValue.intValue()+1));
			} else  {
				oldMap.put(entityId, new Integer(1));
			}

			this.allOtherEntitiesAttributeCount.put(key, oldMap);
		} else {
			HashMap<String, Integer> newMap = new HashMap<String, Integer>();
			newMap.put(entityId, new Integer(1));
			this.allOtherEntitiesAttributeCount.put(key, newMap);
		}
	}
	
	public synchronized int countAttributeRepetitionsForOthers(int time, String attribute, String entityId) {
		int result = 0;
		String key = time + "|" + attribute;
		if(this.allOtherEntitiesAttributeCount.containsKey(key)) {
			HashMap<String, Integer> map = this.allOtherEntitiesAttributeCount.get(key);
			
			for (String currentEntityId : map.keySet()) {
				
				if(!currentEntityId.equals(entityId))
					result += map.get(currentEntityId).intValue();
			}
		}
		return result;
	}
	
	public synchronized void addAllEntitiesAttributeCount(String key, String attribute) {
		if(this.allEntitiesAttributeCount.containsKey(key)) {
			Integer oldValue = this.allEntitiesAttributeCount.get(key);
			this.allEntitiesAttributeCount.put(key, new Integer(oldValue.intValue()+1));
		} else 
			this.allEntitiesAttributeCount.put(key, new Integer(1));
	}
	
	public void addEntityYear(String entityId, List<Integer> years) {
		this.entityYearsMap.put(entityId, years);
	}
	
	public void addEntityAttributes(String entityIdYear, List<String> attributes) {
		this.entityAttributesMap.put(entityIdYear, attributes);
	}
}
