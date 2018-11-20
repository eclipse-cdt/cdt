/*******************************************************************************
* Copyright (c) 2006, 2008 IBM Corporation and others.
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
*********************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.action;

/**
 * Maps tokens defined in parser extensions back to the token kinds
 * defined in the lr parsers.
 *
 *
 * @author Mike Kucera
 */
public interface ITokenMap {

	/**
	 * Maps the given token kind back to the same token kind defined in C99Parsersym.
	 */
	int mapKind(int kind);

}
