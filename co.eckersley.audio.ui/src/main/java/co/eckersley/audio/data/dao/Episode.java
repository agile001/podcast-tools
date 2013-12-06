package co.eckersley.audio.data.dao;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "podcast_episode")
public class Episode {

    @Id
    @GeneratedValue(generator = "hibernate-uuid")
    @GenericGenerator(name = "hibernate-uuid", strategy = "uuid")
    @Column(name = "id", length = 32)
    private String id;
    
    @NotNull
    private String subject;
    
    @NotNull
    private Date date;
    
    @Column(columnDefinition = "TEXT")
    private String text;
    
    @Column(name = "published_text", columnDefinition = "TEXT")
    private String publishedText;
    
    @Column(name = "error_text", columnDefinition = "TEXT")
    private String errorText;
    
    private String voice;

    @Column(name = "file_name")
    private String fileName;
    
    private byte[] audio;
    
    private boolean published;
    
    @OneToOne
    private Feed feed;

    public Episode() {
        super();
    }

    public Episode(Feed feed) {
        super();
        this.subject = "New Episode";
        this.date = new Date();
        this.feed = feed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getPublishedText() {
        return publishedText;
    }

    public void setPublishedText(String publishedText) {
        this.publishedText = publishedText;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
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
    
    public boolean isPublished() {
        return published;
    }
    
    public void setPublished(boolean published) {
        this.published = published;
    }
}
