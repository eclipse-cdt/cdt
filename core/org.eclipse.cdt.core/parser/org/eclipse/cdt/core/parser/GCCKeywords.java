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
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings("nls")
public class GCCKeywords {
	public static final String TYPEOF = "typeof";
	public static final String __ALIGNOF__ = "__alignof__";
	public static final String __ATTRIBUTE__ = "__attribute__";
	public static final String __DECLSPEC = "__declspec";
	/** @since 5.5 */
	public static final String __DECLTYPE = "__decltype";
	/** @since 5.5 */
	public static final String __INT128 = "__int128";
	/** @since 5.5 */
	public static final String __FLOAT128 = "__float128";
	/** @since 5.9 */
	public static final String __FINAL = "__final";
	/** @since 5.10  */
	public static final String _DECIMAL32 = "_Decimal32";
	/** @since 5.10  */
	public static final String _DECIMAL64 = "_Decimal64";
	/** @since 5.10  */
	public static final String _DECIMAL128 = "_Decimal128";

	public static final char[]
		cpTYPEOF = 			TYPEOF.toCharArray(),
		cp__ALIGNOF__ = 	__ALIGNOF__.toCharArray(),
		cp__ATTRIBUTE__ = 	__ATTRIBUTE__.toCharArray(),
		cp__DECLSPEC = 		__DECLSPEC.toCharArray(),
		cp__ALIGNOF = 		"__alignof".toCharArray(),
		cp__ATTRIBUTE = 	"__attribute".toCharArray(),
		cp__ASM= 			"__asm".toCharArray(),
		cp__ASM__= 			"__asm__".toCharArray(),
		cp__CONST= 			"__const".toCharArray(),
		cp__CONST__= 		"__const__".toCharArray(),
		cp__INLINE= 		"__inline".toCharArray(),
		cp__INLINE__= 		"__inline__".toCharArray(),
		cp__RESTRICT= 		"__restrict".toCharArray(),
		cp__RESTRICT__= 	"__restrict__".toCharArray(),
		cp__VOLATILE= 		"__volatile".toCharArray(),
		cp__VOLATILE__= 	"__volatile__".toCharArray(),
		cp__SIGNED= 		"__signed".toCharArray(),
		cp__SIGNED__= 		"__signed__".toCharArray(),
		cp__TYPEOF= 		"__typeof".toCharArray(),
		cp__TYPEOF__= 		"__typeof__".toCharArray();

	/** @since 5.3 */
	public static final char[]
		cp__has_nothrow_assign= 		"__has_nothrow_assign".toCharArray(),
		cp__has_nothrow_copy= 			"__has_nothrow_copy".toCharArray(),
		cp__has_nothrow_constructor= 	"__has_nothrow_constructor".toCharArray(),
		cp__has_trivial_assign= 		"__has_trivial_assign".toCharArray(),
		cp__has_trivial_copy= 			"__has_trivial_copy".toCharArray(),
		cp__has_trivial_constructor=  	"__has_trivial_constructor".toCharArray(),
		cp__has_trivial_destructor= 	"__has_trivial_destructor".toCharArray(),
		cp__has_virtual_destructor= 	"__has_virtual_destructor".toCharArray(),
		cp__is_abstract= 				"__is_abstract".toCharArray(),
		cp__is_base_of= 				"__is_base_of".toCharArray(),
		cp__is_class= 					"__is_class".toCharArray(),
		cp__is_empty= 					"__is_empty".toCharArray(),
		cp__is_enum= 					"__is_enum".toCharArray(),
		cp__is_pod= 					"__is_pod".toCharArray(),
		cp__is_polymorphic= 			"__is_polymorphic".toCharArray(),
		cp__is_union= 					"__is_union".toCharArray();

	/** @since 5.5 */
	public static final char[]
		cp__DECLTYPE=			 		__DECLTYPE.toCharArray(),
		cp__float128= 					__FLOAT128.toCharArray(),
		cp__int128= 					__INT128.toCharArray(),
		cp__is_literal_type= 			"__is_literal_type".toCharArray(),
		cp__is_standard_layout= 		"__is_standard_layout".toCharArray(),
		cp__is_trivial= 			    "__is_trivial".toCharArray();
	
	/** @since 5.6 */
	public static final char[]
		cp__is_final= 					"__is_final".toCharArray(),
		cp__underlying_type=			"__underlying_type".toCharArray();
	
	/** @since 5.9 */
	public static final char[]
		cp__FINAL=						__FINAL.toCharArray();

	/** @since 5.10 */
	public static final char[]
		cp_decimal32=					_DECIMAL32.toCharArray(),
		cp_decimal64=					_DECIMAL64.toCharArray(),
		cp_decimal128=					_DECIMAL128.toCharArray();
}
