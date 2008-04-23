/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser;

import org.eclipse.cdt.core.parser.IToken;

/**
 * Maps tokens returned by CPreprocessor to the corresponding
 * token kind used by an LPG based parser.
 * 
 * @author Mike Kucera
 */
public interface IDOMTokenMap {

	/**
	 * Returns the LPG token kind for the given DOM token.
	 * @throws NullPointerException if token is null
	 */
	int mapKind(IToken token);
	
	/**
	 * Returns the LPG token type for End Of File (TK_EOF_TOKEN) token.
	 */
	int getEOFTokenKind(); 
	
	/**
	 * Returns the LPG token type for End Of Completion (TK_EndOfCompletion) token.
	 */
	int getEOCTokenKind();
	
}
