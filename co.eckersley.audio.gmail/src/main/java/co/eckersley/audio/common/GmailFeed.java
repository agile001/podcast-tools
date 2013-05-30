package co.eckersley.audio.common;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "gmail_feed")
public class GmailFeed {

	@Id
	private String id;
	@NotNull
	private String fromName;
	private String fromEmail;
	@NotNull
	private String voice;
	boolean skip;
    @OneToOne
    private SpokenWordFeed feed;
	
	public GmailFeed() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFromEmail() {
		return fromEmail;
	}

	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
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

	public boolean isSkip() {
		return skip;
	}

	public void setSkip(boolean skip) {
		this.skip = skip;
	}

    public SpokenWordFeed getFeed() {
        return feed;
    }

    public void setFeed(SpokenWordFeed feed) {
        this.feed = feed;
    }
	
}
