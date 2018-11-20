/*******************************************************************************
 *  Copyright (c) 2008, 2009 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.upc;

import static org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym.TK_MYTHREAD;
import static org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym.TK_THREADS;
import static org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym.TK_UPC_MAX_BLOCKSIZE;
import static org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym.TK_relaxed;
import static org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym.TK_shared;
import static org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym.TK_strict;
import static org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym.TK_upc_barrier;
import static org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym.TK_upc_blocksizeof;
import static org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym.TK_upc_elemsizeof;
import static org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym.TK_upc_fence;
import static org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym.TK_upc_forall;
import static org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym.TK_upc_localsizeof;
import static org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym.TK_upc_notify;
import static org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym.TK_upc_wait;

import org.eclipse.cdt.core.dom.lrparser.c99.C99Language;
import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.parser.util.CharArrayMap;

/**
 * Enumeration of keywords that UPC adds to C99.
 *
 * @author Mike Kucera
 */
public enum UPCKeyword {

	MYTHREAD(TK_MYTHREAD), THREADS(TK_THREADS), UPC_MAX_BLOCKSIZE(TK_UPC_MAX_BLOCKSIZE), relaxed(TK_relaxed),
	shared(TK_shared), strict(TK_strict), upc_barrier(TK_upc_barrier), upc_localsizeof(TK_upc_localsizeof),
	upc_blocksizeof(TK_upc_blocksizeof), upc_elemsizeof(TK_upc_elemsizeof), upc_notify(TK_upc_notify),
	upc_fence(TK_upc_fence), upc_wait(TK_upc_wait), upc_forall(TK_upc_forall);

	private final int tokenKind;

	private static final CharArrayMap<Integer> tokenMap = new CharArrayMap<>();
	private static final String[] upcKeywords;
	private static final String[] allKeywords;

	UPCKeyword(int tokenKind) {
		this.tokenKind = tokenKind;
	}

	static {
		UPCKeyword[] keywords = values();
		upcKeywords = new String[keywords.length];
		for (int i = 0; i < keywords.length; i++) {
			UPCKeyword keyword = keywords[i];
			String name = keyword.name();
			upcKeywords[i] = name;
			tokenMap.put(name.toCharArray(), keyword.tokenKind);
		}

		// TODO change to GCC language when gcc support is added
		ICLanguageKeywords c99Keywords = (ICLanguageKeywords) C99Language.getDefault()
				.getAdapter(ICLanguageKeywords.class);
		String[] c99ks = c99Keywords.getKeywords();
		allKeywords = new String[upcKeywords.length + c99ks.length];
		System.arraycopy(c99ks, 0, allKeywords, 0, c99ks.length);
		System.arraycopy(upcKeywords, 0, allKeywords, c99ks.length, upcKeywords.length);
	}

	public int getTokenKind() {
		return tokenKind;
	}

	public static String[] getUPCOnlyKeywords() {
		return upcKeywords;
	}

	public static String[] getAllKeywords() {
		return allKeywords;
	}

	public static Integer getTokenKind(char[] image) {
		if (image == null)
			return null;
		return tokenMap.get(image);
	}
}
