/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.editor;

import org.eclipse.cdt.internal.qt.ui.Activator;
import org.eclipse.cdt.internal.qt.ui.text.QMLSourceViewerConfiguration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

/**
 * Basic editor for QML. Thus far has only syntax highlighting capabilities.
 */
public class QMLEditor extends TextEditor {
	public static final String EDITOR_ID = "org.eclipse.cdt.qt.ui.QMLEditor"; //$NON-NLS-1$

	public static final String BRACKET_MATCHING_COLOR_PREFERENCE = "org.eclipse.cdt.qt.ui.qmlMatchingBracketsColor"; //$NON-NLS-1$
	private static final String BRACKET_MATCHING_PREFERENCE = "org.eclipse.cdt.qt.ui.qmlMatchingBrackets"; //$NON-NLS-1$

	private static final char[] BRACKETS = { '{', '}', '(', ')', '[', ']' };

	@Override
	protected void initializeEditor() {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setSourceViewerConfiguration(new QMLSourceViewerConfiguration(this));
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

		IPreferenceStore store = getPreferenceStore();
		store.setDefault(BRACKET_MATCHING_PREFERENCE, true);
		store.setDefault(BRACKET_MATCHING_COLOR_PREFERENCE, "155,155,155"); //$NON-NLS-1$
	}

}
