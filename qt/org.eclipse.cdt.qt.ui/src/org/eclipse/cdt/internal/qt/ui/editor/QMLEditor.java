/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.editor;

import java.io.File;

import javax.script.ScriptException;

import org.eclipse.cdt.internal.qt.ui.Activator;
import org.eclipse.cdt.internal.qt.ui.actions.OpenDeclarationsAction;
import org.eclipse.cdt.qt.core.IQMLAnalyzer;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

/**
 * Basic editor for QML. Thus far has only syntax highlighting capabilities.
 */
public class QMLEditor extends TextEditor {
	public static final String EDITOR_ID = "org.eclipse.cdt.qt.ui.QMLEditor"; //$NON-NLS-1$

	public static final String BRACKET_MATCHING_COLOR_PREFERENCE = "org.eclipse.cdt.qt.ui.qmlMatchingBracketsColor"; //$NON-NLS-1$
	private static final String BRACKET_MATCHING_PREFERENCE = "org.eclipse.cdt.qt.ui.qmlMatchingBrackets"; //$NON-NLS-1$
	private static final char[] BRACKETS = { '{', '}', '(', ')', '[', ']' };

	private final IQMLAnalyzer analyzer = Activator.getService(IQMLAnalyzer.class);

	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		IPreferenceStore prefStore = new ChainedPreferenceStore(new IPreferenceStore[] {
				Activator.getDefault().getPreferenceStore(), CUIPlugin.getDefault().getPreferenceStore(),
				CUIPlugin.getDefault().getCorePreferenceStore(), EditorsUI.getPreferenceStore() });
		setPreferenceStore(prefStore);
		setSourceViewerConfiguration(new QMLSourceViewerConfiguration(this, prefStore));
	}

	@Override
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		((QMLSourceViewerConfiguration) getSourceViewerConfiguration()).handlePreferenceStoreChanged(event);
		super.handlePreferenceStoreChanged(event);
	}

	@Override
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		if (((QMLSourceViewerConfiguration) getSourceViewerConfiguration()).affectsTextPresentation(event)) {
			return true;
		} else {
			return super.affectsTextPresentation(event);
		}
	}

	@Override
	public void doSave(IProgressMonitor progressMonitor) {
		IFileEditorInput fileInput = (IFileEditorInput) getEditorInput();
		String fileName = new File(fileInput.getFile().getLocationURI()).getAbsolutePath();
		IDocument document = getSourceViewer().getDocument();

		try {
			analyzer.deleteFile(fileName);
			analyzer.addFile(fileName, document.get());
		} catch (NoSuchMethodException e) {
			Activator.log(e);
		} catch (ScriptException e) {
			Activator.log(e);
		}
		super.doSave(progressMonitor);
	}

	@Override
	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		super.configureSourceViewerDecorationSupport(support);

		// Setup bracket matching with default color being gray
		ICharacterPairMatcher matcher = new DefaultCharacterPairMatcher(BRACKETS,
				IDocumentExtension3.DEFAULT_PARTITIONING);
		support.setCharacterPairMatcher(matcher);
		support.setMatchingCharacterPainterPreferenceKeys(BRACKET_MATCHING_PREFERENCE,
				BRACKET_MATCHING_COLOR_PREFERENCE);

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(BRACKET_MATCHING_PREFERENCE, true);
		store.setDefault(BRACKET_MATCHING_COLOR_PREFERENCE, "155,155,155"); //$NON-NLS-1$
	}

	@Override
	protected void createActions() {
		super.createActions();

		IAction action = new OpenDeclarationsAction();
		action.setActionDefinitionId(IQMLEditorActionDefinitionIds.OPEN_DECLARATION);
		setAction(OpenDeclarationsAction.ID, action);
	}

	public static IRegion findWord(IDocument document, int offset) {
		int start = -2;
		int end = -1;

		try {
			int pos = offset;
			char c;

			while (--pos >= 0) {
				c = document.getChar(pos);
				if (!Character.isJavaIdentifierPart(c)) {
					break;
				}
			}

			start = pos;

			pos = offset;
			int length = document.getLength();

			while (pos < length) {
				c = document.getChar(pos);
				if (!Character.isJavaIdentifierPart(c))
					break;
				++pos;
			}

			end = pos;
		} catch (BadLocationException x) {
		}

		if (start >= -1 && end > -1) {
			if (start == offset && end == offset)
				return new Region(offset, 0);
			else if (start == offset)
				return new Region(start, end - start);
			else
				return new Region(start + 1, end - start - 1);
		}

		return null;
	}

}
