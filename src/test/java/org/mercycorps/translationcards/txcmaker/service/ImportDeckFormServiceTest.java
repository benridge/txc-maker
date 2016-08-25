package org.mercycorps.translationcards.txcmaker.service;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.taskqueue.Queue;
import org.junit.Before;
import org.junit.Test;
import org.mercycorps.translationcards.txcmaker.model.Error;
import org.mercycorps.translationcards.txcmaker.model.importDeckForm.Field;
import org.mercycorps.translationcards.txcmaker.model.importDeckForm.ImportDeckForm;
import org.mercycorps.translationcards.txcmaker.response.ImportDeckResponse;
import org.mockito.Mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ImportDeckFormServiceTest {

    ImportDeckFormService importDeckFormService;

    List<Field> fields;

    Error error;
    @Mock
    private ChannelService channelService;
    @Mock
    private Queue taskQueue;
    @Mock
    private TxcMakerParser txcMakerParser;

    private ImportDeckResponse importDeckResponse;
    private ImportDeckForm importDeckForm;

    @Before
    public void setup() throws IOException{
        initMocks(this);

        fields = new ArrayList<>();
        Field field = mock(Field.class);
        when(field.verify()).thenReturn(Collections.<Error>emptyList());
        fields.add(field);

        importDeckResponse = new ImportDeckResponse();
        error = new Error("some message", true);
        importDeckForm = new ImportDeckForm()
                .setDeckName("deck name")
                .setAudioDirId("audio dir id string")
                .setDocId("doc id string")
                .setPublisher("publisher");

        importDeckFormService = new ImportDeckFormService(txcMakerParser);
    }

    @Test
    public void verifyFormData_shouldAddErrorsToTheResponseWhenThereAreErrors() throws Exception {
        Field failedField = mock(Field.class);
        List<Error> fieldErrors = Collections.singletonList(error);
        when(failedField.verify()).thenReturn(fieldErrors);
        fields.add(failedField);
        importDeckFormService.verifyFormData(importDeckResponse, fields);

        assertThat(importDeckResponse.getErrors(), is(fieldErrors));
    }

    @Test
    public void preProcessForm_shouldParseTheDocumentId() throws Exception {
        when(txcMakerParser.parseDocId("doc id string"))
                .thenReturn("doc id");

        importDeckFormService.preProcessForm(importDeckForm);

        assertThat(importDeckForm.getDocId(), is("doc id"));
    }

    @Test
    public void preProcessForm_shouldParseTheAudioDirectoryId() throws Exception {
        when(txcMakerParser.parseAudioDirId("audio dir id string"))
                .thenReturn("audio dir id");

        importDeckFormService.preProcessForm(importDeckForm);

        assertThat(importDeckForm.getAudioDirId(), is("audio dir id"));

    }
}