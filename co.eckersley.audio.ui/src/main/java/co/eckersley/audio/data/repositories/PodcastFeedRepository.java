package co.eckersley.audio.data.repositories;

import org.springframework.data.repository.CrudRepository;

import co.eckersley.audio.data.dao.Feed;

public interface PodcastFeedRepository extends CrudRepository<Feed, String> {

    Feed findByTitle(String title);
}
