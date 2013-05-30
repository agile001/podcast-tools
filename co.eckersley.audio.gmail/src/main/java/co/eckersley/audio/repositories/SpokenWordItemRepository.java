package co.eckersley.audio.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import co.eckersley.audio.common.SpokenWordFeed;
import co.eckersley.audio.common.SpokenWordItem;

public interface SpokenWordItemRepository extends CrudRepository<SpokenWordItem, String> {

	public List<SpokenWordItem> findByFileNameIsNull();
	public List<SpokenWordItem> findByCleanTextIsNullOrderByDateAsc();
	public List<SpokenWordItem> findByFeedAndCleanTextIsNotNullAndFileNameIsNotNullOrderByDateAsc(SpokenWordFeed feed);
}
