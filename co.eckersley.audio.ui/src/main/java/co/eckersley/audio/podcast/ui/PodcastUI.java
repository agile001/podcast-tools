package co.eckersley.audio.podcast.ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import co.eckersley.audio.common.WebApplicationInitializer;
import co.eckersley.audio.data.dao.Episode;
import co.eckersley.audio.data.dao.Feed;
import co.eckersley.audio.data.repositories.PodcastEpisodeRepository;
import co.eckersley.audio.data.repositories.PodcastFeedRepository;
import co.eckersley.audio.producers.AudioProducer;
import co.eckersley.audio.producers.FeedProducer;
import co.eckersley.audio.producers.ProcessTextProducer;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.annotations.Title;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/* 
 * UI class is the starting point for your app. You may deploy it with VaadinServlet
 * or VaadinPortlet by giving your UI class name a parameter. When you browse to your
 * app a web page showing your UI is automatically generated. Or you may choose to 
 * embed your UI to an existing web page. 
 */
@Title("Podcasts")
public class PodcastUI extends UI {

    private static final long serialVersionUID = 1L;
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private ApplicationContext appContext;
    private EntityManager em;
    
    /* User interface components are stored in session. */
    private Table episodeList = new Table();
    private TextField searchField = new TextField();
    private Button addNewEpisodeButton = new Button("New");
    private Button removeEpisodeButton = new Button("Remove this episode");
    private Button publishEpisodesButton = new Button("Publish");
    private Button testVoices = new Button("Test Voices");
    private VerticalLayout editorLayout = new VerticalLayout();
    private FieldGroup editorFields = new FieldGroup();

    private static final String FEED_ID = "1";
    
//    private static final String EPISODE_ID = "id";
    private static final String EPISODE_DATE = "date";
    private static final String EPISODE_SUBJECT = "subject";
    private static final String EPISODE_VOICE = "voice";
    private static final String EPISODE_TEXT = "text";
    private static final String EPISODE_FEED = "feed";
    private static final String EPISODE_PUBLISHED = "published";
    
    private static final String LABEL_DATE = "Date";
    private static final String LABEL_SUBJECT = "Subject";
    private static final String LABEL_VOICE = "Voice";
    private static final String LABEL_TEXT = "Text";
    private static final String LABEL_PUBLISHED = "Published";
    
    private static final String[] fieldNames = new String[] { EPISODE_DATE, EPISODE_SUBJECT, EPISODE_VOICE, EPISODE_TEXT };
    private static final String[] labelNames = new String[] { LABEL_DATE, LABEL_SUBJECT, LABEL_VOICE, LABEL_TEXT };

    /*
     * Any component can be bound to an external data source. This example uses
     * just a dummy in-memory list, but there are many more practical
     * implementations.
     */
    private JPAContainer<Episode> episodeContainer;
    
    private Feed feed;

    /*
     * After UI class is created, init() is executed. You should build and wire
     * up your user interface here.
     */
    protected void init(VaadinRequest request) {
        initContainer();
        initLayout();
        initEpisodeList();
        initEditor();
        initSearch();
        initAddRemoveButtons();
    }

    private void initContainer() {
        
        this.appContext = WebApplicationInitializer.getApplicationContext();
        this.em = appContext.getBean(EntityManagerFactory.class).createEntityManager();
        this.feed = appContext.getBean(PodcastFeedRepository.class).findOne(FEED_ID);
        this.episodeContainer = createEpisodeDatasource(feed);
    }
    /*
     * In this example layouts are programmed in Java. You may choose use a
     * visual editor, CSS or HTML templates for layout instead.
     */
    private void initLayout() {

        /* Root of the user interface component tree is set */
        HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
        setContent(splitPanel);

        /* Build the component tree */
        VerticalLayout leftLayout = new VerticalLayout();
        splitPanel.addComponent(leftLayout);
        splitPanel.addComponent(editorLayout);
        splitPanel.setSplitPosition(40);
        leftLayout.addComponent(episodeList);
        HorizontalLayout bottomLeftLayout = new HorizontalLayout();
        leftLayout.addComponent(bottomLeftLayout);
        bottomLeftLayout.addComponent(testVoices);
        bottomLeftLayout.addComponent(searchField);
        bottomLeftLayout.addComponent(addNewEpisodeButton);
        bottomLeftLayout.addComponent(publishEpisodesButton);

        /* Set the contents in the left of the split panel to use all the space */
        leftLayout.setSizeFull();

        /*
         * On the left side, expand the size of the contactList so that it uses
         * all the space left after from bottomLeftLayout
         */
        leftLayout.setExpandRatio(episodeList, 1);
        episodeList.setSizeFull();

        /*
         * In the bottomLeftLayout, searchField takes all the width there is
         * after adding addNewContactButton. The height of the layout is defined
         * by the tallest component.
         */
        bottomLeftLayout.setWidth("100%");
        searchField.setWidth("100%");
        bottomLeftLayout.setExpandRatio(searchField, 1);

        /* Put a little margin around the fields in the right side editor */
        editorLayout.setMargin(true);
        editorLayout.setVisible(false);
        logger.debug("EditorLayout spacing: {}", editorLayout.isSpacing());
        editorLayout.setSpacing(false);
        logger.debug("EditorLayout spacing: {}", editorLayout.isSpacing());
    }

    private void initEditor() {

        editorLayout.addComponent(removeEpisodeButton);
//        editorLayout.setHeight(100, Unit.PERCENTAGE);
        editorLayout.setHeight(100, Unit.PERCENTAGE);
        
        TextArea textArea = new TextArea();

        /* User interface can be created dynamically to reflect underlying data. */
        for (int idx = 0; idx < fieldNames.length; idx++) {
            String fieldName = fieldNames[idx];
            String labelName = labelNames[idx];
            Field<?> field = null;
            if (StringUtils.equals(EPISODE_DATE, fieldName)) {
                DateField dateField = new DateField(labelName);
                dateField.setDateFormat("yyyy-MM-dd h:mm a");
                dateField.setResolution(Resolution.MINUTE);
                dateField.setLenient(true);
//                dateField.setWidth(dateField.getWidth() * 5, dateField.getWidthUnits());
                dateField.setWidth(200, Unit.PIXELS);
                field = dateField;
                
            } else if (StringUtils.equals(EPISODE_TEXT, fieldName)) {
                textArea.setCaption(labelName);
                textArea.setSizeFull();
                textArea.setNullSettingAllowed(true);
                textArea.setNullRepresentation("");
                field = textArea;
                
            } else if (StringUtils.equals(EPISODE_VOICE, fieldName)) {
                ComboBox comboBox = new ComboBox(labelName, Arrays.asList("Lee", "Karen", "Tom", "Samantha", "Daniel", "Serena"));
                field = comboBox;
                
            } else {
                TextField textField = new TextField(labelName);
                textField.setWidth("100%");
                field = textField;
            }
            editorLayout.addComponent(field);

            /*
             * We use a FieldGroup to connect multiple components to a data
             * source at once.
             */
            editorFields.bind(field, fieldName);
        }

        editorLayout.setExpandRatio(textArea, 1.0f);
        /*
         * Data can be buffered in the user interface. When doing so, commit()
         * writes the changes to the data source. Here we choose to write the
         * changes automatically without calling commit().
         */
        editorFields.setBuffered(false);
    }

    private void initSearch() {

        /*
         * We want to show a subtle prompt in the search field. We could also
         * set a caption that would be shown above the field or description to
         * be shown in a tooltip.
         */
        searchField.setInputPrompt("Search episodes");

        /*
         * Granularity for sending events over the wire can be controlled. By
         * default simple changes like writing a text in TextField are sent to
         * server with the next Ajax call. You can set your component to be
         * immediate to send the changes to server immediately after focus
         * leaves the field. Here we choose to send the text over the wire as
         * soon as user stops writing for a moment.
         */
        searchField.setTextChangeEventMode(TextChangeEventMode.LAZY);

        /*
         * When the event happens, we handle it in the anonymous inner class.
         * You may choose to use separate controllers (in MVC) or presenters (in
         * MVP) instead. In the end, the preferred application architecture is
         * up to you.
         */
        searchField.addTextChangeListener(new TextChangeListener() {
            private static final long serialVersionUID = 1L;
            public void textChange(final TextChangeEvent event) {

                /* Reset the filter for the contactContainer. */
                episodeContainer.removeAllContainerFilters();
                episodeContainer.addContainerFilter(new EpisodeFilter(event.getText()));
            }
        });
    }

    /*
     * A custom filter for searching names and companies in the
     * contactContainer.
     */
    private class EpisodeFilter implements Filter {

        private static final long serialVersionUID = 1L;
        
        private String needle;

        public EpisodeFilter(String needle) {
            this.needle = needle.toLowerCase();
        }

        public boolean passesFilter(Object itemId, Item item) {
            String haystack = ("" + item.getItemProperty(EPISODE_SUBJECT).getValue() + item.getItemProperty(EPISODE_TEXT).getValue()).toLowerCase();
            return haystack.contains(needle);
        }

        public boolean appliesToProperty(Object id) {
            return true;
        }
    }

    private void initAddRemoveButtons() {
        addNewEpisodeButton.addClickListener(new ClickListener() {
            private static final long serialVersionUID = 1L;
            public void buttonClick(ClickEvent event) {
                /*
                 * Rows in the Container data model are called Item. Here we add
                 * a new row in the beginning of the list.
                 */
                episodeContainer.removeAllContainerFilters();
                Episode entity = new Episode(feed);
                Object episodeId = episodeContainer.addEntity(entity);

                /*
                 * Each Item has a set of Properties that hold values. Here we
                 * set a couple of those.
                 */
//                episodeList.getContainerProperty(episodeId, EPISODE_DATE).setValue(new Date());
//                episodeList.getContainerProperty(episodeId, EPISODE_SUBJECT).setValue("New Episode");

                /* Lets choose the newly created contact to edit it. */
                episodeList.select(episodeId);
            }
        });

        removeEpisodeButton.addClickListener(new ClickListener() {
            private static final long serialVersionUID = 1L;
            public void buttonClick(ClickEvent event) {
                Object episodeId = episodeList.getValue();
                logger.debug("Removing Episode: {}", episodeList.getContainerProperty(episodeId, EPISODE_SUBJECT).getValue());
                episodeList.removeItem(episodeId);
            }
        });
        
        publishEpisodesButton.addClickListener(new ClickListener() {
            private static final long serialVersionUID = 1L;
            @Override
            public void buttonClick(ClickEvent event) {
                logger.debug("Publishing Episodes");
                publishNewEpisodes();
            }
        });
        
        testVoices.addClickListener(new ClickListener() {
            private static final long serialVersionUID = 1L;
            @Override
            public void buttonClick(ClickEvent event) {
                try {
                    String cmd = "/usr/bin/say -v ?";
                    logger.info("Executing command: {}", cmd);
                    Process p1 = Runtime.getRuntime().exec(cmd);
                    p1.waitFor();
                    ProcessTextProducer processText = new ProcessTextProducer(p1);
                    logger.error("Listing system voices command exit value: {}", p1.exitValue());
                    if (processText.getOutputText().length() > 0)
                        logger.error("Command system output: {}", processText.getOutputText());
                    if (processText.getErrorText().length() > 0)
                        logger.error("Command error output: {}", processText.getErrorText());
                } catch (Exception e) {
                    throw new RuntimeException("Failure", e);
                }
                try {
                    String voicefile = "/Users/david/spoken_audio/tmp/voicelist.txt";
//                    String cmd = "/bin/ls /System/Library/Speech/Voices/ > " + voicefile;
                    String cmd = "/bin/ls /System/Library/Speech/Voices/";
                    logger.info("Executing command: {}", cmd);
                    Process p = Runtime.getRuntime().exec(cmd);
                    p.waitFor();
    
                    int exitValue = p.exitValue();
                    if (exitValue == 0) {
                        BufferedReader reader = new BufferedReader(new FileReader(voicefile));
                        
                        List<String> voices = new ArrayList<String>();
                        
                        String voice;
                        while ((voice = reader.readLine()) != null) {
                            voice = voice.replace("Compact.SpeechVoice","");
                            voice = voice.replace(".SpeechVoice","");
                            voices.add(voice);
                        }
                        reader.close();
                        for (String v : voices) {
                            String[] cmdarray = { "/usr/bin/say", "-v", v, "hello" };
                            logger.info("Executing command: {}", cmd);
                            Process p1 = Runtime.getRuntime().exec(cmdarray, null, null);
                            p1.waitFor();
                            ProcessTextProducer processText = new ProcessTextProducer(p1);
                            logger.error("Listing system voices command exit value: {}", p1.exitValue());
                            if (processText.getOutputText().length() > 0)
                                logger.error("Command system output: {}", processText.getOutputText());
                            if (processText.getErrorText().length() > 0)
                                logger.error("Command error output: {}", processText.getErrorText());
                        }
                    } else {
                        ProcessTextProducer processError = new ProcessTextProducer(p);
                        String msg = String.format("Listing Voice. Exit Value = %s<br/><br/>Error message: %s", exitValue, processError.getErrorText());
                        logger.error("Listing system voices command exit value: {}", exitValue);
                        logger.error("Command system output: {}", processError.getOutputText());
                        logger.error("Command error output: {}", processError.getErrorText());
                        Notification.show(msg);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failure", e);
                }
            }
        });
    }

    private void initEpisodeList() {
        
        episodeList.setContainerDataSource(episodeContainer);
//        episodeList.setTableFieldFactory(new TableFieldFactory() {
//            private static final long serialVersionUID = 1L;
//            private DefaultFieldFactory defaultFieldFactory = DefaultFieldFactory.get();
//            @Override
//            public Field<?> createField(Container container, Object itemId, Object propertyId, Component uiContext) {
//                if (ObjectUtils.equals(EPISODE_PUBLISHED, propertyId)) {
//                    CheckBox checkBox = new CheckBox();
//                    return checkBox;
//                } else {
//                    return defaultFieldFactory.createField(container, itemId, propertyId, uiContext);
//                }
//            }
//        });
        episodeList.addGeneratedColumn(EPISODE_PUBLISHED + "_1", new ColumnGenerator() {
            private static final long serialVersionUID = 1L;

            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                return new CheckBox(null, ((Boolean)source.getItem(itemId).getItemProperty(EPISODE_PUBLISHED).getValue()));
            }
        });
//        episodeList.setVisibleColumns(new String[] { EPISODE_DATE, EPISODE_SUBJECT, EPISODE_VOICE, EPISODE_PUBLISHED, EPISODE_PUBLISHED + "_1" });
//        episodeList.setColumnHeaders(new String[] { LABEL_DATE, LABEL_SUBJECT, LABEL_VOICE, LABEL_PUBLISHED, LABEL_PUBLISHED });
        episodeList.setVisibleColumns(new String[] { EPISODE_DATE, EPISODE_SUBJECT, EPISODE_VOICE, EPISODE_PUBLISHED + "_1" });
        episodeList.setColumnHeaders(new String[] { LABEL_DATE, LABEL_SUBJECT, LABEL_VOICE, LABEL_PUBLISHED });
        episodeList.setColumnAlignment(EPISODE_VOICE, Align.CENTER);
        episodeList.setColumnAlignment(EPISODE_PUBLISHED + "_1", Align.CENTER);
        episodeList.setColumnExpandRatio(EPISODE_SUBJECT, 1.0f);
        episodeList.setSelectable(true);
        episodeList.setImmediate(true);

        episodeList.addValueChangeListener(new Property.ValueChangeListener() {
            private static final long serialVersionUID = 1L;
            
            public void valueChange(ValueChangeEvent event) {
                
                Object episodeId = episodeList.getValue();
                /*
                 * When a contact is selected from the list, we want to show
                 * that in our editor on the right. This is nicely done by the
                 * FieldGroup that binds all the fields to the corresponding
                 * Properties in our contact at once.
                 */
                if (episodeId != null)
                    editorFields.setItemDataSource(episodeList.getItem(episodeId));

                editorLayout.setVisible(episodeId != null);
            }
        });
    }
    
    private void publishNewEpisodes() {
        /*
         * Get the list of Episodes to publish.
         */
        PodcastEpisodeRepository episodeRepository = appContext.getBean(PodcastEpisodeRepository.class);
        List<Episode> episodes = episodeRepository.findByTextIsNotNullAndPublishedTextIsNullOrderByDateAsc();
        /*
         * Create the Audio files.
         */
        AudioProducer audioProducer = new AudioProducer();
        for (Episode episode : episodes) {
            try {
                audioProducer.produce(episode);
                episode.setPublished(true);
                episodeRepository.save(episode);
            } catch (Exception e) {
                logger.error("Error producing audio file for: " + episode.getSubject(), e);
                throw new RuntimeException("Failed to produce audio file for: " + episode.getSubject(), e);
            }
        }
        /*
         * Update the Podcast RSS feed.
         */
        FeedProducer feedProducer = new FeedProducer(episodeRepository);
        try {
            feedProducer.produce(feed);
        } catch (Exception e) {
            logger.error("Error producing podcast feed for: " + feed.getTitle(), e);
            throw new RuntimeException("Failed to produce podcast feed for: " + feed.getTitle(), e);
        }
        /*
         * Update the Episode List.
         */
        episodeContainer.refresh();
    }

    /*
     * Generate some in-memory example data to play with. In a real application
     * we could be using SQLContainer, JPAContainer or some other to persist the
     * data.
     */
    private JPAContainer<Episode> createEpisodeDatasource(Feed feed) {
        JPAContainer<Episode> container = JPAContainerFactory.make(Episode.class, em);
        container.sort(new Object[] { EPISODE_DATE, EPISODE_SUBJECT }, new boolean[] { false, true });
        Filter filter = new Compare.Equal(EPISODE_FEED, feed);
        container.addContainerFilter(filter);
        return container;
    }
}
