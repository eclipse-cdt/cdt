/*******************************************************************************
 * Copyright (c) 2008, 2011 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.text.doctools.doxygen;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.doctools.IDocCommentViewerConfiguration;
import org.eclipse.cdt.ui.text.doctools.generic.AbstractGenericTagDocCommentViewerConfiguration;
import org.eclipse.cdt.ui.text.doctools.generic.GenericDocTag;

/**
 * {@link IDocCommentViewerConfiguration} implementation for doxygen multi-line documentation comments.
 * <em>This class may be sub-classed by clients</em>
 * @since 5.0
 */
public class DoxygenMultilineConfiguration extends AbstractGenericTagDocCommentViewerConfiguration {
	/**
	 * Default constructor
	 */
	public DoxygenMultilineConfiguration() {
		super(DoxygenHelper.getDoxygenTags(), new char[] {'@','\\'}, PreferenceConstants.DOXYGEN_MULTI_LINE_COLOR, PreferenceConstants.DOXYGEN_TAG_COLOR);
	}

	/**
	 * Constructor intended for use by sub-classes.
	 * @param tags a non-null array of tags this configuration should recognize
	 * @param tagMarkers a non-null array of characters used to prefix the tags (e.g. @ or \)
	 * @param defaultToken the default scanner token id
	 * @param tagToken the scanner token to use to mark used by this configuration
	 * @see AbstractGenericTagDocCommentViewerConfiguration
	 */
	protected DoxygenMultilineConfiguration(GenericDocTag[] tags, char[] tagMarkers, String defaultToken, String tagToken) {
		super(tags, tagMarkers, defaultToken, tagToken);
	}

	/*
	 * @see org.eclipse.cdt.ui.text.doctools.IDocCommentViewerConfiguration#createAutoEditStrategy()
	 */
	@Override
	public IAutoEditStrategy createAutoEditStrategy() {
		return new DoxygenMultilineAutoEditStrategy();
	}

	/*
	 * @see org.eclipse.cdt.ui.text.doctools.IDocCommentViewerConfiguration#isDocumentationComment(org.eclipse.jface.text.IDocument, int, int)
	 */
	@Override
	public boolean isDocumentationComment(IDocument doc, int offset, int length) {
		try {
			if(offset+2 < doc.getLength()) {
				char c= doc.getChar(offset+2);
				return c == '*' || c == '!';
			}
		} catch(BadLocationException ble) {
			CUIPlugin.log(ble);
		}
		return false;
	}
}
