package it.epocaricerca.standalone.continuityChange.repository;

import java.util.List;

import it.epocaricerca.standalone.continuityChange.model.Tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagRepository extends JpaRepository<Tag, Long>{

	@Query("SELECT DISTINCT t.citation FROM Tag t WHERE t.firm = :firm AND t.year = :year")
	List<String> findByFirmAndYear(@Param("firm") String firm, @Param("year") String year);
	
	@Query("SELECT COUNT(t) FROM Tag t WHERE t.firm = :firm AND t.year = :year AND t.citation = :citation")
	int countCitationRepetitions(@Param("firm") String firm, @Param("year") String year, @Param("citation") String citation);
	
}
