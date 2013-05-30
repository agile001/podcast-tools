package co.eckersley.audio.common;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "spoken_word_feed")
public class SpokenWordFeed {

	@Id
	private String id;
	@NotNull
	private String fromName;
	@NotNull
	private String voice;
	@NotNull
	private String tile;
	private String description;
	private String cleanerClass;
    @OneToMany
    private List<GmailFeed> gmailFeeds;
	
	public SpokenWordFeed() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public String getVoice() {
		return voice;
	}

	public void setVoice(String voice) {
		this.voice = voice;
	}

	public String getTile() {
		return tile;
	}

	public void setTile(String tile) {
		this.tile = tile;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCleanerClass() {
		return cleanerClass;
	}

	public void setCleanerClass(String cleanerClass) {
		this.cleanerClass = cleanerClass;
	}

    public List<GmailFeed> getGmailFeeds() {
        return gmailFeeds;
    }

    public void setGmailFeeds(List<GmailFeed> gmailFeeds) {
        this.gmailFeeds = gmailFeeds;
    }
	
}
