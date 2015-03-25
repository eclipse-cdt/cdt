/*******************************************************************************
 * Copyright (c) 2013 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - Support for Step into selection (bug 244865)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.actions;

import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.debug.core.model.IStepIntoSelectionHandler;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.internal.ui.Messages;
import org.eclipse.cdt.dsf.debug.internal.ui.sourcelookup.DsfSourceSelectionResolver;
import org.eclipse.cdt.dsf.debug.internal.ui.sourcelookup.DsfSourceSelectionResolver.LineLocation;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @since 2.4
 * 
 */
public class DsfStepIntoSelectionHyperlinkDetector extends AbstractHyperlinkDetector {
	private class DsfStepIntoSelectionHyperlink implements IHyperlink {

		private ITextSelection fSelection = null;
		private IDsfStepIntoSelection fStepIntoSelectionCommand = null;
		private DsfSourceSelectionResolver fSelectionResolver = null;

		/**
		 * Constructor
		 * 
		 * @param stepIntoSelectionCommand
		 * @param region
		 */
		public DsfStepIntoSelectionHyperlink(DsfSourceSelectionResolver selectionResolver, IDsfStepIntoSelection stepIntoSelectionCommand) {
			fSelection = selectionResolver.resolveSelection();
			fStepIntoSelectionCommand = stepIntoSelectionCommand;
			fSelectionResolver = selectionResolver;
		}

		/**
		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkRegion()
		 */
		@Override
		public IRegion getHyperlinkRegion() {
			return new Region(fSelection.getOffset(), fSelection.getLength());
		}

		/**
		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkText()
		 */
		@Override
		public String getHyperlinkText() {
			return Messages.DsfUIStepIntoEditorSelection;
		}

		/**
		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getTypeLabel()
		 */
		@Override
		public String getTypeLabel() {
			return null;
		}

		/**
		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#open()
		 */
		@Override
		public void open() {
			// Resolve the debug context
			final IExecutionDMContext dmc = resolveDebugContext();
			if (fSelectionResolver.isSuccessful() && dmc != null) {
				LineLocation location = fSelectionResolver.getLineLocation();
				fStepIntoSelectionCommand.runToSelection(location.getFileName(), location.getLineNumber(), fSelectionResolver.getFunction(), dmc);
			} else {
				String message = null;
				if (dmc == null) {
					message = "DSfStepIntoSelection: Unable to resolve the debug context"; //$NON-NLS-1$
				} else {
					message = "DSfStepIntoSelection: Unable to resolve a selected function"; //$NON-NLS-1$
				}
				DsfUIPlugin.debug(message);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion, boolean)
	 */
	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, final IRegion region, boolean canShowMultipleHyperlinks) {
		// Only valid in the context of a selection within the CEditor
		ITextEditor editor = (ITextEditor) getAdapter(ITextEditor.class);
		if (editor == null || region == null || !(editor instanceof CEditor))
			return null;

		ITextSelection selection = resolveSelection(editor, region);
		if (selection == null) {
			return null;
		}

		// Shall only enable hyper link step into selection within a cdt debug execution context
		IExecutionDMContext dmc = resolveDebugContext();
		if (dmc == null) {
			return null;
		}

		final DsfSession session = DsfSession.getSession(dmc.getSessionId());
		if (session == null || !session.isActive()) {
			return null;
		}

		IDsfStepIntoSelection stepIntoSelectionCommand = null;
		IStepIntoSelectionHandler stepIntoSelectionHandler = (IStepIntoSelectionHandler) session.getModelAdapter(IStepIntoSelectionHandler.class);
		if (stepIntoSelectionHandler instanceof IDsfStepIntoSelection) {
			stepIntoSelectionCommand = (IDsfStepIntoSelection)stepIntoSelectionHandler;
		} else {
			return null;
		}

		if (!stepIntoSelectionCommand.isExecutable(dmc)) {
			return null;
		}

		DsfSourceSelectionResolver functionResolver = new DsfSourceSelectionResolver(editor, selection);
		functionResolver.run();
		// Resolve to a selected function
		if (!functionResolver.isSuccessful()) {
			// We are not pointing to a valid function
			return null;
		}

		return new IHyperlink[] { new DsfStepIntoSelectionHyperlink(functionResolver, stepIntoSelectionCommand) };
	}

	private ITextSelection resolveSelection(ITextEditor editor, IRegion region) {
		ITextSelection selection = null;
		if (editor != null) {
			IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
			final IWorkingCopy workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput());

			if (document != null && workingCopy != null) {
				// Check partition type.
				String partitionType;
				try {
					partitionType = TextUtilities.getContentType(document, ICPartitions.C_PARTITIONING, region.getOffset(), false);
					if (IDocument.DEFAULT_CONTENT_TYPE.equals(partitionType)) {
						// Regular code i.e. Not a Preprocessor directive.
						IRegion wregion = getIdentifier(document, region.getOffset(), workingCopy.getLanguage());
						if (wregion != null) {
							selection = new TextSelection(document, wregion.getOffset(), wregion.getLength());
						}
					}
				} catch (BadLocationException e) {
					// Ignore to return null
				} catch (CoreException e) {
					// Ignore to return null
				}
			}
		}

		return selection;
	}

	/**
	 * Returns the identifier at the given offset, or {@code null} if the there is no identifier at the offset.
	 */
	private static IRegion getIdentifier(IDocument document, int offset, ILanguage language) throws BadLocationException {
		IRegion wordRegion = CWordFinder.findWord(document, offset);
		if (wordRegion != null && wordRegion.getLength() > 0) {
			String word = document.get(wordRegion.getOffset(), wordRegion.getLength());
			if (!Character.isDigit(word.charAt(0)) && !isLanguageKeyword(language, word)) {
				return wordRegion;
			}
		}
		return null;
	}

	private static boolean isLanguageKeyword(ILanguage lang, String word) {
		ICLanguageKeywords keywords = lang.getAdapter(ICLanguageKeywords.class);
		if (keywords != null) {
			for (String keyword : keywords.getKeywords()) {
				if (keyword.equals(word))
					return true;
			}
			for (String type : keywords.getBuiltinTypes()) {
				if (type.equals(word))
					return true;
			}
			for (String keyword : keywords.getPreprocessorKeywords()) {
				if (keyword.charAt(0) == '#' && keyword.length() == word.length() + 1 && keyword.regionMatches(1, word, 0, word.length())) {
					return true;
				}
			}
		}
		return false;
	}

	private IExecutionDMContext resolveDebugContext() {
		IExecutionDMContext execContext = null;
		IAdaptable adaptableContext = DebugUITools.getDebugContext();
		IDMContext debugContext = null;
		if (adaptableContext instanceof IDMVMContext) {
			debugContext = ((IDMVMContext) adaptableContext).getDMContext();
		}

		if (debugContext != null) {
			execContext = DMContexts.getAncestorOfType(debugContext, IExecutionDMContext.class);
		}

		return execContext;
	}
}
