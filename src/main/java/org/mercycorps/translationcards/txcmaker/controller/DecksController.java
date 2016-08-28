package org.mercycorps.translationcards.txcmaker.controller;

import com.google.api.services.drive.Drive;
import org.mercycorps.translationcards.txcmaker.auth.AuthUtils;
import org.mercycorps.translationcards.txcmaker.model.Error;
import org.mercycorps.translationcards.txcmaker.model.importDeckForm.Constraint;
import org.mercycorps.translationcards.txcmaker.model.importDeckForm.ImportDeckForm;
import org.mercycorps.translationcards.txcmaker.response.ImportDeckResponse;
import org.mercycorps.translationcards.txcmaker.response.ResponseFactory;
import org.mercycorps.translationcards.txcmaker.service.DriveService;
import org.mercycorps.translationcards.txcmaker.service.ImportDeckService;
import org.mercycorps.translationcards.txcmaker.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/decks")
public class DecksController {

    private ImportDeckService importDeckService;
    private AuthUtils authUtils;
    private ServletContext servletContext;
    private ResponseFactory responseFactory;
    private TaskService taskService;
    private DriveService driveService;

    @Autowired
    public DecksController(ImportDeckService importDeckService, AuthUtils authUtils, ServletContext servletContext, ResponseFactory responseFactory, TaskService taskService, DriveService driveService) {
        this.importDeckService = importDeckService;
        this.authUtils = authUtils;
        this.servletContext = servletContext;
        this.responseFactory = responseFactory;
        this.taskService = taskService;
        this.driveService = driveService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity importDeck(@RequestBody ImportDeckForm importDeckForm, HttpServletRequest request) throws URISyntaxException {
        ImportDeckResponse importDeckResponse = responseFactory.newImportDeckResponse();
        Drive drive = getDrive(request, importDeckResponse);
        final String sessionId = request.getSession().getId();
        final List<Constraint> fieldsToVerify = importDeckForm.getFieldsToVerify(drive);
        if(drive != null) {
            importDeckService.processForm(importDeckForm, request, importDeckResponse, drive, sessionId, fieldsToVerify);
            taskService.kickoffVerifyDeckTask(importDeckResponse, sessionId, importDeckForm);
        }
        return importDeckResponse.build();
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.POST)
    public void assembleDeck(@PathVariable String id) {
        taskService.kickoffBuildDeckTask(id);
    }

    private Drive getDrive(HttpServletRequest request, ImportDeckResponse importDeckResponse) {
        Drive drive = null;
        try {
            drive = authUtils.getDriveOrOAuth(servletContext, request, null, false, request.getSession().getId());
        } catch(IOException e) {
            importDeckResponse.addError(new Error("Authorization failed", true));
        }
        return drive;
    }


}
