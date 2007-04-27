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

import org.eclipse.cdt.core.dom.parser.c99.C99KeywordMap;
import org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym;


/**
 * Provides UPC specific keywords in addition to the C99 keywords.
 * 
 * Keyword token mappings from the superclass must be overridden.
 * 
 * @author Mike Kucera
 */
public class UPCKeywordMap extends C99KeywordMap {

	
	public static final String
		MYTHREAD = "MYTHREAD",//$NON-NLS-1$
		THREADS = "THREADS",//$NON-NLS-1$
		UPC_MAX_BLOCKSIZE = "UPC",//$NON-NLS-1$
		relaxed = "relaxed",//$NON-NLS-1$
		shared = "shared",//$NON-NLS-1$
		strict = "strict",//$NON-NLS-1$
		upc_barrier  = "upc_barrier",//$NON-NLS-1$
		upc_localsizeof = "upc_localsizeof",//$NON-NLS-1$
		upc_blocksizeof = "upc_blocksizeof",//$NON-NLS-1$
		upc_elemsizeof = "upc_elemsizeof",//$NON-NLS-1$
		upc_notify = "upc_notify",//$NON-NLS-1$
		upc_fence = "upc_fence",//$NON-NLS-1$
		upc_wait = "upc_wait",//$NON-NLS-1$
		upc_forall = "upc_forall";//$NON-NLS-1$
	
	
	public UPCKeywordMap() {
		putKeyword(MYTHREAD,          UPCParsersym.TK_MYTHREAD);
		putKeyword(THREADS,           UPCParsersym.TK_THREADS);
		putKeyword(UPC_MAX_BLOCKSIZE, UPCParsersym.TK_UPC_MAX_BLOCKSIZE);
		putKeyword(relaxed,           UPCParsersym.TK_relaxed);
		putKeyword(shared,            UPCParsersym.TK_shared);
		putKeyword(strict,            UPCParsersym.TK_strict);
		putKeyword(upc_barrier,       UPCParsersym.TK_upc_barrier);
		putKeyword(upc_localsizeof,   UPCParsersym.TK_upc_localsizeof);
		putKeyword(upc_blocksizeof,   UPCParsersym.TK_upc_blocksizeof);
		putKeyword(upc_elemsizeof,    UPCParsersym.TK_upc_elemsizeof);
		putKeyword(upc_notify,        UPCParsersym.TK_upc_notify);
		putKeyword(upc_fence,         UPCParsersym.TK_upc_fence);
		putKeyword(upc_wait,          UPCParsersym.TK_upc_wait);
		putKeyword(upc_forall,        UPCParsersym.TK_upc_forall);
		
		// The keyword token mappings from the superclass must be overridden.
		// This is because LPG generates totally different values for token
		// kinds every time you genereate a parser.
		putKeyword(AUTO,       UPCParsersym.TK_auto);
		putKeyword(BREAK,      UPCParsersym.TK_break);
		putKeyword(CASE,       UPCParsersym.TK_case);
		putKeyword(CHAR,       UPCParsersym.TK_char);
		putKeyword(CONST,      UPCParsersym.TK_const);
		putKeyword(CONTINUE,   UPCParsersym.TK_continue);
		putKeyword(DEFAULT,    UPCParsersym.TK_default);
		putKeyword(DO,         UPCParsersym.TK_do);
		putKeyword(DOUBLE,     UPCParsersym.TK_double);
		putKeyword(ELSE,       UPCParsersym.TK_else);
		putKeyword(ENUM,       UPCParsersym.TK_enum);
		putKeyword(EXTERN,     UPCParsersym.TK_extern);
		putKeyword(FLOAT,      UPCParsersym.TK_float);
		putKeyword(FOR,        UPCParsersym.TK_for);
		putKeyword(GOTO,       UPCParsersym.TK_goto);
		putKeyword(IF,         UPCParsersym.TK_if);
		putKeyword(INLINE,     UPCParsersym.TK_inline);
		putKeyword(INT,        UPCParsersym.TK_int);
		putKeyword(LONG,       UPCParsersym.TK_long);
		putKeyword(REGISTER,   UPCParsersym.TK_register);
		putKeyword(RESTRICT,   UPCParsersym.TK_restrict);
		putKeyword(RETURN,     UPCParsersym.TK_return);
		putKeyword(SHORT,      UPCParsersym.TK_short);
		putKeyword(SIGNED,     UPCParsersym.TK_signed);
		putKeyword(SIZEOF,     UPCParsersym.TK_sizeof);
		putKeyword(STATIC,     UPCParsersym.TK_static);
		putKeyword(STRUCT,     UPCParsersym.TK_struct);
		putKeyword(SWITCH,     UPCParsersym.TK_switch);
		putKeyword(TYPEDEF,    UPCParsersym.TK_typedef);
		putKeyword(UNION,      UPCParsersym.TK_union);
		putKeyword(UNSIGNED,   UPCParsersym.TK_unsigned);
		putKeyword(VOID,       UPCParsersym.TK_void);
		putKeyword(VOLATILE,   UPCParsersym.TK_volatile);
		putKeyword(WHILE,      UPCParsersym.TK_while);
		putKeyword(_BOOL,      UPCParsersym.TK__Bool);
		putKeyword(_COMPLEX,   UPCParsersym.TK__Complex);
		putKeyword(_IMAGINARY, UPCParsersym.TK__Imaginary);
	}
	

}
