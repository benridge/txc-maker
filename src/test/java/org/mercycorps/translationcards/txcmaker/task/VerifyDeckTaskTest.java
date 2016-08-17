package org.mercycorps.translationcards.txcmaker.task;

import com.google.api.services.drive.Drive;
import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.Before;
import org.junit.Test;
import org.mercycorps.translationcards.txcmaker.auth.AuthUtils;
import org.mercycorps.translationcards.txcmaker.model.Deck;
import org.mercycorps.translationcards.txcmaker.service.DriveService;
import org.mercycorps.translationcards.txcmaker.language.LanguageService;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class VerifyDeckTaskTest {


    private static final String SESSION_ID = "session id";
    private static final String AUDIO_DIR_ID = "audio dir id";
    private static final String DOC_ID = "document id";
    public static final String DECK_AS_JSON = "deck as JSON";
    private static final String AUDIO_DIR_URL = "audio dir url";
    @Mock
    private AuthUtils authUtils;
    @Mock
    private DriveService driveService;
    @Mock
    private LanguageService languageService;
    @Mock
    private ServletContext servletContext;
    @Mock
    private ChannelService channelService;
    @Mock
    private TxcPortingUtility txcPortingUtility;
    @Mock
    private HttpServletRequest request;
    @Mock
    private Drive drive;

    private VerifyDeckTask verifyDeckTask;


    @Before
    public void setup() throws ServletException, IOException {
        initMocks(this);

        when(request.getParameter("sessionId")).thenReturn(SESSION_ID);
        when(request.getParameter("audioDirId")).thenReturn(AUDIO_DIR_URL);
        when(request.getParameter("docId")).thenReturn(DOC_ID);

        when(txcPortingUtility.parseAudioDirId(AUDIO_DIR_URL)).thenReturn(AUDIO_DIR_ID);

        CSVParser parser = new CSVParser(new StringReader("Sure wish I could mock this"), CSVFormat.DEFAULT);
        when(driveService.fetchParsableCsv(drive, DOC_ID)).thenReturn(parser);

        when(authUtils.getDriveOrOAuth(servletContext, null, null, false, SESSION_ID)).thenReturn(drive);

        when(txcPortingUtility.buildTxcJson(any(Deck.class))).thenReturn(DECK_AS_JSON);

        verifyDeckTask = new VerifyDeckTask(servletContext, authUtils, driveService, channelService, txcPortingUtility);
    }

    @Test
    public void shouldGetTheDriveUsingTheSessionId() throws Exception {
        verifyDeckTask.verifyDeck(request);

        verify(authUtils).getDriveOrOAuth(servletContext, null, null, false, SESSION_ID);
    }

    @Test
    public void shouldFetchCsv() throws Exception {
        verifyDeckTask.verifyDeck(request);

        verify(driveService).fetchParsableCsv(drive, DOC_ID);
    }

    @Test
    public void shouldParseTheCsvDataIntoTheDeck() throws Exception {
        verifyDeckTask.verifyDeck(request);

        verify(txcPortingUtility).parseCsvIntoDeck(any(Deck.class), any(CSVParser.class));
    }

    @Test
    public void shouldRespondOverTheCorrectChannel() throws Exception {
        verifyDeckTask.verifyDeck(request);

        ArgumentCaptor<ChannelMessage> channelMessageArgumentCaptor = ArgumentCaptor.forClass(ChannelMessage.class);
        verify(channelService).sendMessage(channelMessageArgumentCaptor.capture());

        ChannelMessage channelMessage = channelMessageArgumentCaptor.getValue();
        assertThat(channelMessage.getClientId(), is(SESSION_ID));
    }

    @Test
    public void shouldRespondWithTheDeckAsJson() throws Exception {
        verifyDeckTask.verifyDeck(request);

        ArgumentCaptor<ChannelMessage> channelMessageArgumentCaptor = ArgumentCaptor.forClass(ChannelMessage.class);
        verify(channelService).sendMessage(channelMessageArgumentCaptor.capture());

        ChannelMessage channelMessage = channelMessageArgumentCaptor.getValue();
        assertThat(channelMessage.getMessage(), is(DECK_AS_JSON));
    }
}