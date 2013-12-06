package co.eckersley.audio.data.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import co.eckersley.audio.data.dao.Episode;
import co.eckersley.audio.data.dao.Feed;

public interface PodcastEpisodeRepository extends CrudRepository<Episode, String> {

	List<Episode> findByTextIsNotNullAndPublishedTextIsNullOrderByDateAsc();
	List<Episode> findByFeedAndPublishedTextIsNotNullAndFileNameIsNotNullOrderByDateDesc(Feed feed);
}
