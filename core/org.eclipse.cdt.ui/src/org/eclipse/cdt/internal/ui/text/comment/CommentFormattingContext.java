/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin, Google
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.comment;

import org.eclipse.jface.text.formatter.FormattingContext;

import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;

/**
 * Formatting context for the comment formatter.
 *
 * @since 4.0
 */
public class CommentFormattingContext extends FormattingContext {

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingContext#getPreferenceKeys()
	 */
	public String[] getPreferenceKeys() {
		return new String[] {
			    DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT,
			    DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_HEADER,
			    DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_SOURCE,
			    DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH,
			    DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES};
	}


	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingContext#isBooleanPreference(java.lang.String)
	 */
	public boolean isBooleanPreference(String key) {
		return !key.equals(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH);
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingContext#isIntegerPreference(java.lang.String)
	 */
	public boolean isIntegerPreference(String key) {
		return key.equals(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH);
	}
}
