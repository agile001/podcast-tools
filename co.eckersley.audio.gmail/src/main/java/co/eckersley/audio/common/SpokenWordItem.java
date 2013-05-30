package co.eckersley.audio.common;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "spoken_word_item")
public class SpokenWordItem {

	@Id
	private String id;
	@NotNull
	private String fromName;
	@NotNull
	private String voice;
	@NotNull
	private String subject;
	@NotNull
	private Date date;
	@NotNull
	private String text;
	private String cleanText;
	@NotNull
	@OneToOne
	private SpokenWordFeed feed;
	private String fileName;
	private byte[] audio;
	
	public SpokenWordItem() {
		super();
	}
	
	public SpokenWordItem(String id, SpokenWordFeed feed) {
		super();
		this.id = id;
		this.feed = feed;
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

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getCleanText() {
		return cleanText;
	}

	public void setCleanText(String cleanText) {
		this.cleanText = cleanText;
	}

	public SpokenWordFeed getFeed() {
		return feed;
	}

	public void setFeed(SpokenWordFeed feed) {
		this.feed = feed;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public byte[] getAudio() {
		return audio;
	}

	public void setAudio(byte[] audio) {
		this.audio = audio;
	}
	
}
