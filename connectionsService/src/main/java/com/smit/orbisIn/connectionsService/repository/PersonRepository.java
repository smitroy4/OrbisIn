package com.smit.orbisIn.connectionsService.repository;

import com.smit.orbisIn.connectionsService.entity.Person;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends Neo4jRepository<Person, Long> {

    Optional<Person> findByUserId(Long userId);

    @Query("match (personA:Person) -[:CONNECTED_TO]- (personB:Person) " +
            "where personA.userId = $userId " +
            "return personB")
    List<Person> getFirstDegreeConnections(Long userId);

    @Query("match (a:Person) -[:CONNECTED_TO]- (b:Person) " +
            "where a.userId = $a and b.userId = $b " +
            "return count(*) > 0")
    boolean areConnected(Long a, Long b);

    @Query("match (a:Person), (b:Person) " +
            "where a.userId = $a and b.userId = $b " +
            "merge (a) -[:CONNECTED_TO]-> (b) " +
            "return count(*)")
    Long createConnection(Long a, Long b);

    @Query("match (p:Person) " +
            "where not p.userId in $excluded " +
            "and ($q = '' or toLower(p.name) contains toLower($q)) " +
            "return p order by p.name limit 25")
    List<Person> discover(List<Long> excluded, String q);

}
