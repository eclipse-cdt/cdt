/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text;

/**
 * Html tag constants.
 */
public interface IHtmlTagConstants {
	/** Html tag close prefix */
	public static final String HTML_CLOSE_PREFIX = "</"; //$NON-NLS-1$

	/** Html entity characters */
	public static final char[] HTML_ENTITY_CHARACTERS = new char[] { '<', '>', ' ', '&', '^', '~', '\"' };

	/**
	 * Html entity start.
	 */
	public static final char HTML_ENTITY_START = '&';
	/**
	 * Html entity end.
	 */
	public static final char HTML_ENTITY_END = ';';

	/** Html entity codes */
	public static final String[] HTML_ENTITY_CODES = new String[] { "&lt;", "&gt;", "&nbsp;", "&amp;", "&circ;", //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
			"&tilde;", "&quot;" }; //$NON-NLS-1$ //$NON-NLS-2$

	/** Html general tags */
	public static final String[] HTML_GENERAL_TAGS = new String[] { "a", "b", "blockquote", "br", "code", "dd", "dl", //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$//$NON-NLS-6$//$NON-NLS-7$
			"dt", "em", "hr", "h1", "h2", "h3", "h4", "h5", "h6", "i", "li", "nl", "ol", "p", "pre", "q", "strong", //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$//$NON-NLS-6$//$NON-NLS-7$//$NON-NLS-8$//$NON-NLS-9$//$NON-NLS-10$//$NON-NLS-11$//$NON-NLS-12$//$NON-NLS-13$//$NON-NLS-14$//$NON-NLS-15$//$NON-NLS-16$//$NON-NLS-17$
			"tbody", "td", "th", "tr", "tt", "ul" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

	/** Html tag postfix */
	public static final char HTML_TAG_POSTFIX = '>';

	/** Html tag prefix */
	public static final char HTML_TAG_PREFIX = '<';
}
