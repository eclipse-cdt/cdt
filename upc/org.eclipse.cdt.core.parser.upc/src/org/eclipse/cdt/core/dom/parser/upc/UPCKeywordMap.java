/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.upc;

import org.eclipse.cdt.core.dom.parser.c99.LPGKeywordMap;
import org.eclipse.cdt.core.dom.parser.c99.C99KeywordMap;
import org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym;


/**
 * Provides UPC specific keywords in addition to the C99 keywords.
 * 
 * Keyword token mappings from the superclass must be overridden.
 * 
 * @author Mike Kucera
 */
public class UPCKeywordMap extends LPGKeywordMap {
	
	
	public UPCKeywordMap() {
		putKeyword(UPCParsersym.TK_MYTHREAD);
		putKeyword(UPCParsersym.TK_THREADS);
		putKeyword(UPCParsersym.TK_UPC_MAX_BLOCKSIZE);
		putKeyword(UPCParsersym.TK_relaxed);
		putKeyword(UPCParsersym.TK_shared);
		putKeyword(UPCParsersym.TK_strict);
		putKeyword(UPCParsersym.TK_upc_barrier);
		putKeyword(UPCParsersym.TK_upc_localsizeof);
		putKeyword(UPCParsersym.TK_upc_blocksizeof);
		putKeyword(UPCParsersym.TK_upc_elemsizeof);
		putKeyword(UPCParsersym.TK_upc_notify);
		putKeyword(UPCParsersym.TK_upc_fence);
		putKeyword(UPCParsersym.TK_upc_wait);
		putKeyword(UPCParsersym.TK_upc_forall);
		
		// The keyword token mappings from the superclass must be overridden.
		// This is because LPG generates totally different values for token
		// kinds every time you generate a parser.
		putKeyword(UPCParsersym.TK_auto);
		putKeyword(UPCParsersym.TK_break);
		putKeyword(UPCParsersym.TK_case);
		putKeyword(UPCParsersym.TK_char);
		putKeyword(UPCParsersym.TK_const);
		putKeyword(UPCParsersym.TK_continue);
		putKeyword(UPCParsersym.TK_default);
		putKeyword(UPCParsersym.TK_do);
		putKeyword(UPCParsersym.TK_double);
		putKeyword(UPCParsersym.TK_else);
		putKeyword(UPCParsersym.TK_enum);
		putKeyword(UPCParsersym.TK_extern);
		putKeyword(UPCParsersym.TK_float);
		putKeyword(UPCParsersym.TK_for);
		putKeyword(UPCParsersym.TK_goto);
		putKeyword(UPCParsersym.TK_if);
		putKeyword(UPCParsersym.TK_inline);
		putKeyword(UPCParsersym.TK_int);
		putKeyword(UPCParsersym.TK_long);
		putKeyword(UPCParsersym.TK_register);
		putKeyword(UPCParsersym.TK_restrict);
		putKeyword(UPCParsersym.TK_return);
		putKeyword(UPCParsersym.TK_short);
		putKeyword(UPCParsersym.TK_signed);
		putKeyword(UPCParsersym.TK_sizeof);
		putKeyword(UPCParsersym.TK_static);
		putKeyword(UPCParsersym.TK_struct);
		putKeyword(UPCParsersym.TK_switch);
		putKeyword(UPCParsersym.TK_typedef);
		putKeyword(UPCParsersym.TK_union);
		putKeyword(UPCParsersym.TK_unsigned);
		putKeyword(UPCParsersym.TK_void);
		putKeyword(UPCParsersym.TK_volatile);
		putKeyword(UPCParsersym.TK_while);
		putKeyword(UPCParsersym.TK__Bool);
		putKeyword(UPCParsersym.TK__Complex);
		putKeyword(UPCParsersym.TK__Imaginary);
		
		
		addBuiltinType(UPCParsersym.TK_char);
		addBuiltinType(UPCParsersym.TK_double);
		addBuiltinType(UPCParsersym.TK_float);
		addBuiltinType(UPCParsersym.TK_int);
		addBuiltinType(UPCParsersym.TK_long);
		addBuiltinType(UPCParsersym.TK_short);
		addBuiltinType(UPCParsersym.TK_signed);
		addBuiltinType(UPCParsersym.TK_unsigned);
		addBuiltinType(UPCParsersym.TK_void);
		addBuiltinType(UPCParsersym.TK__Bool);
		addBuiltinType(UPCParsersym.TK__Complex);
		addBuiltinType(UPCParsersym.TK__Imaginary);
	}

	
	protected String[] getOrderedTerminalSymbols() {
		return UPCParsersym.orderedTerminalSymbols;
	}
	

}
