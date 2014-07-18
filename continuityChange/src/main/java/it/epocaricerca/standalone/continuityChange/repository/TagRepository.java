package it.epocaricerca.standalone.continuityChange.repository;

import java.util.List;

import it.epocaricerca.standalone.continuityChange.model.Tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagRepository extends JpaRepository<Tag, Long>{

	@Query("SELECT DISTINCT t.attribute FROM Tag t WHERE t.entityId = :entityId AND t.time = :time")
	List<String> findByEntityIdAndTime(@Param("entityId") String entityId, @Param("time") int time);
	
	@Query("SELECT COUNT(t) FROM Tag t WHERE t.entityId = :entityId AND t.time = :time AND t.attribute = :attribute")
	int countAttributeRepetitionsForEntity(@Param("entityId") String entityId, @Param("time") int time, @Param("attribute") String attribute);
	
	@Query("SELECT COUNT(t) FROM Tag t WHERE t.entityId != :entityId AND t.time = :time AND t.attribute = :attribute")
	int countAttributeRepetitionsForOthers(@Param("entityId") String entityId, @Param("time") int time, @Param("attribute") String attribute);
	
	@Query("SELECT COUNT(t) FROM Tag t WHERE t.time = :time AND t.attribute = :attribute")
	int countAttributeRepetitionsForAll(@Param("time") int time, @Param("attribute") String attribute);

	@Query("SELECT DISTINCT t.attribute FROM Tag t")
	List<String> findDistinctAttributes();

	@Query("SELECT DISTINCT t.entityId FROM Tag t")
	List<String> findDistinctEntities();

	@Query("SELECT DISTINCT t.time FROM Tag t WHERE t.entityId = :entityId")
	List<Integer> findDistinctTimesForEntity(@Param("entityId") String entityId);
}
