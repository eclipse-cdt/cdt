/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - Adapted for CDT
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.swt.graphics.RGB;

/**
 * Semantic highlighting.
 * Cloned from JDT.
 * 
 * @since 4.0
 */
public abstract class SemanticHighlighting {

	/**
	 * @return the preference key, will be augmented by a prefix and a suffix for each preference
	 */
	public abstract String getPreferenceKey();

	/**
	 * @return the default text color
	 */
	public RGB getDefaultTextColor() {
		return new RGB(0, 0, 0);
	}

	/**
	 * @return <code>true</code> if the text attribute bold is set by default
	 */
	public boolean isBoldByDefault() {
		return false;
	}

	/**
	 * @return <code>true</code> if the text attribute italic is set by default
	 */
	public boolean isItalicByDefault() {
		return false;
	}

	/**
	 * @return <code>true</code> if the text attribute strikethrough is set by default
	 */
	public boolean isStrikethroughByDefault() {
		return false;
	}

	/**
	 * @return <code>true</code> if the text attribute underline is set by default
	 * @since 3.1
	 */
	public boolean isUnderlineByDefault() {
		return false;
	}

	/**
	 * @return <code>true</code> if the text attribute italic is enabled by default
	 */
	public abstract boolean isEnabledByDefault();

	/**
	 * @return the display name
	 */
	public abstract String getDisplayName();

	/**
	 * Returns <code>true</code> iff the semantic highlighting consumes the semantic token.
	 * <p>
	 * NOTE: Implementors are not allowed to keep a reference on the token or on any object
	 * retrieved from the token.
	 * </p>
	 *
	 * @param token the semantic token for a {@link org.eclipse.cdt.core.dom.ast.IASTName}
	 * @return <code>true</code> iff the semantic highlighting consumes the semantic token
	 */
	public abstract boolean consumes(SemanticToken token);
}
