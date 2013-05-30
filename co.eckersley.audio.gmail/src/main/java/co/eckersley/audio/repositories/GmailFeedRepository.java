package co.eckersley.audio.repositories;

import org.springframework.data.repository.CrudRepository;

import co.eckersley.audio.common.GmailFeed;

public interface GmailFeedRepository extends CrudRepository<GmailFeed, String> {

}
