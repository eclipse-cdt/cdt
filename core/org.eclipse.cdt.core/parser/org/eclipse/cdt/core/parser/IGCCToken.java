/*******************************************************************************
 * Copyright (c) 2002, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *     Ed Swartz (Nokia)
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IGCCToken extends IToken {
	public static final int t_typeof = 					FIRST_RESERVED_IGCCToken;
	public static final int t___alignof__ = 			FIRST_RESERVED_IGCCToken + 1;
	public static final int tMAX = 						FIRST_RESERVED_IGCCToken + 2;
	public static final int tMIN = 						FIRST_RESERVED_IGCCToken + 3;
	public static final int t__attribute__ = 			FIRST_RESERVED_IGCCToken + 4;
	public static final int t__declspec = 				FIRST_RESERVED_IGCCToken + 5;
	// Type traits used by g++
	/** @since 5.3 */ int tTT_has_nothrow_assign= 		FIRST_RESERVED_IGCCToken + 6;
	/** @since 5.3 */ int tTT_has_nothrow_copy= 		FIRST_RESERVED_IGCCToken + 7;
	/** @since 5.3 */ int tTT_has_nothrow_constructor= 	FIRST_RESERVED_IGCCToken + 8;
	/** @since 5.3 */ int tTT_has_trivial_assign= 		FIRST_RESERVED_IGCCToken + 9;
	/** @since 5.3 */ int tTT_has_trivial_copy= 		FIRST_RESERVED_IGCCToken + 10;
	/** @since 5.3 */ int tTT_has_trivial_constructor= 	FIRST_RESERVED_IGCCToken + 11;
	/** @since 5.3 */ int tTT_has_trivial_destructor= 	FIRST_RESERVED_IGCCToken + 12;
	/** @since 5.3 */ int tTT_has_virtual_destructor= 	FIRST_RESERVED_IGCCToken + 13;
	/** @since 5.3 */ int tTT_is_abstract= 				FIRST_RESERVED_IGCCToken + 14;
	/** @since 5.3 */ int tTT_is_base_of= 				FIRST_RESERVED_IGCCToken + 15;
	/** @since 5.3 */ int tTT_is_class= 				FIRST_RESERVED_IGCCToken + 16;
	/** @since 5.3 */ int tTT_is_empty= 				FIRST_RESERVED_IGCCToken + 17;
	/** @since 5.3 */ int tTT_is_enum= 					FIRST_RESERVED_IGCCToken + 18;
	/** @since 5.3 */ int tTT_is_pod= 					FIRST_RESERVED_IGCCToken + 19;
	/** @since 5.3 */ int tTT_is_polymorphic= 			FIRST_RESERVED_IGCCToken + 20;
	/** @since 5.3 */ int tTT_is_union= 				FIRST_RESERVED_IGCCToken + 21;
	/** @since 5.5 */ int tTT_is_literal_type= 			FIRST_RESERVED_IGCCToken + 22;
	/** @since 5.5 */ int tTT_is_standard_layout= 		FIRST_RESERVED_IGCCToken + 23;
	/** @since 5.5 */ int tTT_is_trivial= 				FIRST_RESERVED_IGCCToken + 24;

	/** @since 5.5 */ int t__int128= 					FIRST_RESERVED_IGCCToken + 25;
	/** @since 5.5 */ int t__float128=					FIRST_RESERVED_IGCCToken + 26;
	
	/** @since 5.6 */ int tTT_is_final=					FIRST_RESERVED_IGCCToken + 27;
	/** @since 5.6 */ int tTT_underlying_type=			FIRST_RESERVED_IGCCToken + 28;

	/** @since 5.10 */ int t_decimal32=					FIRST_RESERVED_IGCCToken + 29;
	/** @since 5.10 */ int t_decimal64=					FIRST_RESERVED_IGCCToken + 30;
	/** @since 5.10 */ int t_decimal128=				FIRST_RESERVED_IGCCToken + 31;
}
