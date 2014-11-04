package it.epocaricerca.standalone.continuityChange.repository;

import java.util.ArrayList;
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
	
	private HashMap<String, List<String>> allOtherEntitiesAttributeCount = new HashMap<String, List<String>>();
	
	private HashMap<String, List<Integer>> entityYearsMap = new HashMap<String, List<Integer>>();
	
	private HashMap<String, List<String>> entityAttributesMap = new HashMap<String, List<String>>();

	public synchronized HashMap<String, Integer> getEntityAttributeCount() {
		return entityAttributeCount;
	}

	public synchronized HashMap<String, List<Integer>> getEntityYearsMap() {
		return entityYearsMap;
	}

	public synchronized HashMap<String, List<String>> getEntityAttributesMap() {
		return entityAttributesMap;
	}

	public synchronized HashMap<String, List<String>> getAllOtherEntitiesAttributeCount() {
		return allOtherEntitiesAttributeCount;
	}

	public synchronized HashMap<String, Integer> getEntityIntervalTime() {
		return entityIntervalTime;
	}

	public void resetHashMaps() {
		this.entityAttributeCount.clear();
		this.entityAttributesMap.clear();
		this.entityYearsMap.clear();
		this.allOtherEntitiesAttributeCount.clear();
		this.entityIntervalTime.clear();
	}
	
	public synchronized void addEntityAttributeCount(String key) {
		if(this.entityAttributeCount.containsKey(key)) {
			Integer oldValue = this.entityAttributeCount.get(key);
			this.entityAttributeCount.put(key, new Integer(oldValue.intValue()+1));
		} else 
			this.entityAttributeCount.put(key, new Integer(1));
	}
	
	public synchronized void addAllOtherEntitiesAttributeCount(String key, String entityId) {
		
		if(this.allOtherEntitiesAttributeCount.containsKey(key)) {
			
			List<String> oldList = this.allOtherEntitiesAttributeCount.get(key);
			
			if(!oldList.contains(entityId)) {
				oldList.add(entityId);
			} 

			this.allOtherEntitiesAttributeCount.put(key, oldList);
		} else {
			List<String> newList = new ArrayList<String>();
			newList.add(entityId);
			this.allOtherEntitiesAttributeCount.put(key, newList);
		}
	}
	
	public synchronized int countAttributeRepetitionsForOthers(int time, String attribute, String entityId, boolean all) {
		int result = 0;
		String key = time + "|" + attribute;
		
		if(!all) {
			if(this.allOtherEntitiesAttributeCount.containsKey(key)) {
				List<String> entityList = this.allOtherEntitiesAttributeCount.get(key);

				for (String currentEntityId : entityList) {

					if(!currentEntityId.equals(entityId)) {
						result = 1;
						break;
					}
				}
			}
		} else {
			if(this.allOtherEntitiesAttributeCount.containsKey(key))
				result = 1;
		}
		return result;
	}
	
	public synchronized void addEntityYear(String entityId, List<Integer> years) {
		this.entityYearsMap.put(entityId, years);
	}
	
	public synchronized void addEntityAttributes(String entityIdYear, List<String> attributes) {
		this.entityAttributesMap.put(entityIdYear, attributes);
	}
}
