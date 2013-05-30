package co.eckersley.audio.repositories;

import org.springframework.data.repository.CrudRepository;

import co.eckersley.audio.common.SpokenWordFeed;

public interface SpokenWordFeedRepository extends CrudRepository<SpokenWordFeed, String> {

	SpokenWordFeed findByFromName(String fromName);
}
