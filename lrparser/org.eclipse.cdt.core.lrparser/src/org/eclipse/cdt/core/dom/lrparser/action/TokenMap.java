/*******************************************************************************
* Copyright (c) 2006, 2015 IBM Corporation and others.
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

import java.util.HashMap;

/**
 * Maps token kinds from a sub-parser back to the corresponding
 * token kinds in a base parser.
 *
 * @author Mike Kucera
 */
public class TokenMap implements ITokenMap {

	// LPG token kinds start at 0
	// the kind is not part of the base language parser
	public static final int INVALID_KIND = -1;

	private final int[] kindMap;

	/**
	 * @param toSymbols An array of symbols where the index is the token kind and the
	 * element data is a string representing the token kind. It is expected
	 * to pass the orderedTerminalSymbols field from an LPG generated symbol
	 * file, for example C99Parsersym.orderedTerminalSymbols.
	 */
	public TokenMap(String[] toSymbols, String[] fromSymbols) {
		// If this map is not being used with an extension then it becomes an "identity map".
		if (toSymbols == fromSymbols) {
			kindMap = null;
			return;
		}

		kindMap = new int[fromSymbols.length];

		HashMap<String, Integer> toMap = new HashMap<>();
		for (int i = 0, n = toSymbols.length; i < n; i++) {
			toMap.put(toSymbols[i], i);
		}

		for (int i = 0, n = fromSymbols.length; i < n; i++) {
			Integer kind = toMap.get(fromSymbols[i]);
			kindMap[i] = kind == null ? INVALID_KIND : kind;
		}
	}

	/**
	 * Maps a token kind back to the corresponding kind define in the base C99 parser.
	 */
	@Override
	public int mapKind(int kind) {
		if (kindMap == null)
			return kind;
		if (kind < 0 || kind >= kindMap.length)
			return INVALID_KIND;

		return kindMap[kind];
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder('(');
		boolean first = true;
		for (int i = 0, n = kindMap.length; i < n; i++) {
			if (!first)
				sb.append(", "); //$NON-NLS-1$
			sb.append(i).append('=').append(kindMap[i]);
			first = false;
		}
		return sb.append(')').toString();
	}
}