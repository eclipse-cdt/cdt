package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditor;

public class CRulerSingleclickAction extends Action 
{
	final static private String ACTION_ID = "HighlightLine";
	ITextEditor fEditor;
	IVerticalRuler fRuler;
	ISourceViewer fViewer;

	/**
	 * Constructor for CRulerSingleclickAction
	 */
	public CRulerSingleclickAction(IVerticalRuler ruler, ITextEditor editor, ISourceViewer viewer) {
		super();
		fRuler = ruler;
		fEditor = editor;
		fViewer = viewer;
		setEnabled( true );
		setId( ACTION_ID );
	}

	/**
	 * @see IAction#run()
	 */
	public void run() {
		int line= fRuler.getLineOfLastMouseButtonActivity();
		FileEditorInput editorInput = (FileEditorInput)fEditor.getEditorInput();
		IDocument document = fEditor.getDocumentProvider().getDocument(editorInput);
		if((fEditor instanceof AbstractTextEditor) && (fViewer != null)) {
			try {

				int start = document.getLineOffset( line );
				int length = document.getLineLength( line );
				fViewer.setSelectedRange(start, length);
			} catch (BadLocationException e) {}
		}
	}

}

