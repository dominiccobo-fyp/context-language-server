package com.dominiccobo.fyp.langserver;

import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;

/**
 * TODO: add class description.
 *
 * @author Dominic Cobo (contact@dominiccobo.com)
 */
public class CustomTextDocumentService implements org.eclipse.lsp4j.services.TextDocumentService {
    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        // TODO: stub
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        // TODO: stub
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        // TODO: stub
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        // TODO: stub
    }
}
