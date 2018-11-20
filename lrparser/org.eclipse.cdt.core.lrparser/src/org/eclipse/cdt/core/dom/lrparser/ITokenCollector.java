/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
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
