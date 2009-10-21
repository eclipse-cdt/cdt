/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser;

import lpg.lpgjavaruntime.IToken;

/**
 * An LPG parser object is initialized with the list of tokens
 * before parsing is invoked.
 * 
 * @author Mike Kucera
 */
public interface ITokenCollector {
	
	/**
	 * Used to add one token at a time to the parser.
	 * If this method is used to add tokens then the dummy token
	 * and the EOF token must be added by the client.
	 * 
	 * This is really just an optimization, no intermediate data structures
     * are required between the preprocessor and the parser.
     * 
     * @throws NullPointerException if token is null
	 */
	public void addToken(IToken token);

}
