package it.epocaricerca.standalone.continuityChange.repository;

import java.util.List;

import it.epocaricerca.standalone.continuityChange.model.Tag;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagRepository extends JpaRepository<Tag, Long>{

	@Cacheable("attributes")
	@Query("SELECT t.attribute FROM Tag t WHERE t.entityId = :entityId AND t.time = :time")
	List<String> findByEntityIdAndTime(@Param("entityId") String entityId, @Param("time") int time);

	@Cacheable("tagsCountForMe")
	@Query("SELECT COUNT(t.id) FROM Tag t WHERE t.entityId = :entityId AND t.time = :time AND t.attribute = :attribute")
	int countAttributeRepetitionsForEntity(@Param("entityId") String entityId, @Param("time") int time, @Param("attribute") String attribute);

	@Cacheable("tagsCountForOthers")
	@Query("SELECT COUNT(t.id) FROM Tag t WHERE t.entityId != :entityId AND t.time = :time AND t.attribute = :attribute")
	int countAttributeRepetitionsForOthers(@Param("entityId") String entityId, @Param("time") int time, @Param("attribute") String attribute);

	@Cacheable("tagsCountForAll")
	@Query("SELECT COUNT(t.id) FROM Tag t WHERE t.time = :time AND t.attribute = :attribute")
	int countAttributeRepetitionsForAll(@Param("time") int time, @Param("attribute") String attribute);

	@Cacheable("distinctAttrtibutes")
	@Query("SELECT DISTINCT t.attribute FROM Tag t")
	List<String> findDistinctAttributes();

	@Cacheable("distinctEntities")
	@Query("SELECT DISTINCT t.entityId FROM Tag t")
	List<String> findDistinctEntities();

	@Cacheable("distinctTimes")
	@Query("SELECT DISTINCT t.time FROM Tag t WHERE t.entityId = :entityId")
	List<Integer> findDistinctTimesForEntity(@Param("entityId") String entityId);
	
	@Query("SELECT MAX(t.time) - MIN(t.time) FROM Tag t WHERE t.entityId = :entityId GROUP BY t.entityId")
	Integer getIntervalForEntity(@Param("entityId") String entityId);

	
}
