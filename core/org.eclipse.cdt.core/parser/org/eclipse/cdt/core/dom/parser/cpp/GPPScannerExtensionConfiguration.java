/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Ed Swartz (Nokia)
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Richard Eames
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.cpp;

import java.util.Map;

import org.eclipse.cdt.core.dom.parser.GNUScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.GCCKeywords;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.Keywords;

/**
 * Configures the preprocessor for c++-sources as accepted by g++.
 */
public class GPPScannerExtensionConfiguration extends GNUScannerExtensionConfiguration {
	private static enum CompilerType {
		GCC, Clang, ClangCl, MSVC
	}

	private static final int VERSION_4_2 = version(4, 2);
	private static final int VERSION_4_3 = version(4, 3);
	private static final int VERSION_4_6 = version(4, 6);
	private static final int VERSION_4_7 = version(4, 7);
	private static final int VERSION_5_0 = version(5, 0);
	private static final int VERSION_6_0 = version(6, 0);
	private static final int VERSION_8_0 = version(8, 0);
	private static final int VERSION_10_0 = version(10, 0);
	private static final int VERSION_11_1 = version(11, 1);
	private static final int VERSION_14_0 = version(14, 0);
	private static GPPScannerExtensionConfiguration CONFIG = new GPPScannerExtensionConfiguration();
	private static GPPScannerExtensionConfiguration CONFIG_4_2 = new GPPScannerExtensionConfiguration(VERSION_4_2);
	private static GPPScannerExtensionConfiguration CONFIG_4_3 = new GPPScannerExtensionConfiguration(VERSION_4_3);
	private static GPPScannerExtensionConfiguration CONFIG_4_6 = new GPPScannerExtensionConfiguration(VERSION_4_6);
	private static GPPScannerExtensionConfiguration CONFIG_4_7 = new GPPScannerExtensionConfiguration(VERSION_4_7);
	private static GPPScannerExtensionConfiguration CONFIG_5_0 = new GPPScannerExtensionConfiguration(VERSION_5_0);
	private static GPPScannerExtensionConfiguration CONFIG_6_0 = new GPPScannerExtensionConfiguration(VERSION_6_0);
	private static GPPScannerExtensionConfiguration CONFIG_8_0 = new GPPScannerExtensionConfiguration(VERSION_8_0);
	private static GPPScannerExtensionConfiguration CONFIG_10_0 = new GPPScannerExtensionConfiguration(VERSION_10_0);
	private static GPPScannerExtensionConfiguration CONFIG_11_1 = new GPPScannerExtensionConfiguration(VERSION_11_1);
	private static GPPScannerExtensionConfiguration CONFIG_14_0 = new GPPScannerExtensionConfiguration(VERSION_14_0);
	private static GPPScannerExtensionConfiguration CONFIG_CLANG = new GPPScannerExtensionConfiguration(
			CompilerType.Clang, 0 /* version is ignored for now */);
	private static GPPScannerExtensionConfiguration CONFIG_CLANG_CL = new GPPScannerExtensionConfiguration(
			CompilerType.ClangCl, 0 /* version is ignored for now */);
	private static GPPScannerExtensionConfiguration CONFIG_MSVC = new GPPScannerExtensionConfiguration(
			CompilerType.MSVC, 0 /* version is ignored for now */);

	public static GPPScannerExtensionConfiguration getInstance() {
		return CONFIG;
	}

	/**
	 * @since 5.4
	 */
	public static GPPScannerExtensionConfiguration getInstance(IScannerInfo info) {
		if (info != null) {
			try {
				final Map<String, String> definedSymbols = info.getDefinedSymbols();

				// Clang. Needs to be checked first since it pretends to be GCC and MSVC too.
				String clang = definedSymbols.get("__clang__"); //$NON-NLS-1$
				String mscVer = definedSymbols.get("_MSC_VER"); //$NON-NLS-1$
				boolean hasMsc = mscVer != null && Integer.valueOf(mscVer) > 0;
				if (clang != null && Integer.valueOf(clang) > 0) {
					if (hasMsc)
						return CONFIG_CLANG_CL;
					return CONFIG_CLANG;
				}

				if (hasMsc) {
					return CONFIG_MSVC;
				}

				// GCC
				int major = Integer.valueOf(definedSymbols.get("__GNUC__")); //$NON-NLS-1$
				int minor = Integer.valueOf(definedSymbols.get("__GNUC_MINOR__")); //$NON-NLS-1$
				int version = version(major, minor);
				if (version >= VERSION_11_1) {
					return CONFIG_11_1;
				}
				if (version >= VERSION_10_0) {
					return CONFIG_10_0;
				}
				if (version >= VERSION_8_0) {
					return CONFIG_8_0;
				}
				if (version >= VERSION_6_0) {
					return CONFIG_6_0;
				}
				if (version >= VERSION_5_0) {
					return CONFIG_5_0;
				}
				if (version >= VERSION_4_7) {
					return CONFIG_4_7;
				}
				if (version >= VERSION_4_6) {
					return CONFIG_4_6;
				}
				if (version >= VERSION_4_3) {
					return CONFIG_4_3;
				}
				if (version >= VERSION_4_2) {
					return CONFIG_4_2;
				}
			} catch (Exception e) {
				// Fall-back to the default configuration.
			}
		}
		return CONFIG;
	}

	public GPPScannerExtensionConfiguration() {
		this(CompilerType.GCC, 0);
	}

	/**
	 * @since 5.4
	 */
	public GPPScannerExtensionConfiguration(int version) {
		this(CompilerType.GCC, version);
	}

	/**
	 * @since 6.3
	 */
	@SuppressWarnings("nls")
	public GPPScannerExtensionConfiguration(CompilerType compiler, int version) {
		addMacro("__null", "0");
		addMacro("__builtin_offsetof(T,m)",
				"(reinterpret_cast <size_t>(&reinterpret_cast <const volatile char &>(static_cast<T*> (0)->m)))");
		addKeyword(Keywords.c_COMPLEX, IToken.t__Complex);
		addKeyword(Keywords.c_IMAGINARY, IToken.t__Imaginary);

		if (!(compiler == CompilerType.MSVC || compiler == CompilerType.ClangCl)) {
			// MSVC only defines this when compiling in C mode and /Za is used.
			addMacro("__STDC__", "1");
		}

		if (compiler == CompilerType.GCC) {
			if (version >= VERSION_4_2) {
				addKeyword(GCCKeywords.cp_decimal32, IGCCToken.t_decimal32);
				addKeyword(GCCKeywords.cp_decimal64, IGCCToken.t_decimal64);
				addKeyword(GCCKeywords.cp_decimal128, IGCCToken.t_decimal128);
			}
			// Type-traits supported by gcc 4.3
			if (version >= VERSION_4_3) {
				addKeyword(GCCKeywords.cp__has_nothrow_assign, IGCCToken.tTT_has_nothrow_assign);
				addKeyword(GCCKeywords.cp__has_nothrow_constructor, IGCCToken.tTT_has_nothrow_constructor);
				addKeyword(GCCKeywords.cp__has_nothrow_copy, IGCCToken.tTT_has_nothrow_copy);
				addKeyword(GCCKeywords.cp__has_trivial_assign, IGCCToken.tTT_has_trivial_assign);
				addKeyword(GCCKeywords.cp__has_trivial_constructor, IGCCToken.tTT_has_trivial_constructor);
				addKeyword(GCCKeywords.cp__has_trivial_copy, IGCCToken.tTT_has_trivial_copy);
				addKeyword(GCCKeywords.cp__has_trivial_destructor, IGCCToken.tTT_has_trivial_destructor);
				addKeyword(GCCKeywords.cp__has_virtual_destructor, IGCCToken.tTT_has_virtual_destructor);
				addKeyword(GCCKeywords.cp__is_abstract, IGCCToken.tTT_is_abstract);
				addKeyword(GCCKeywords.cp__is_base_of, IGCCToken.tTT_is_base_of);
				addKeyword(GCCKeywords.cp__is_class, IGCCToken.tTT_is_class);
				addKeyword(GCCKeywords.cp__is_empty, IGCCToken.tTT_is_empty);
				addKeyword(GCCKeywords.cp__is_enum, IGCCToken.tTT_is_enum);
				addKeyword(GCCKeywords.cp__is_pod, IGCCToken.tTT_is_pod);
				addKeyword(GCCKeywords.cp__is_polymorphic, IGCCToken.tTT_is_polymorphic);
				addKeyword(GCCKeywords.cp__is_union, IGCCToken.tTT_is_union);
			}
			if (version >= VERSION_4_6) {
				addKeyword(GCCKeywords.cp__is_literal_type, IGCCToken.tTT_is_literal_type);
				addKeyword(GCCKeywords.cp__is_standard_layout, IGCCToken.tTT_is_standard_layout);
				addKeyword(GCCKeywords.cp__is_trivial, IGCCToken.tTT_is_trivial);
			}
			if (version >= VERSION_4_7) {
				addKeyword(GCCKeywords.cp__float128, IGCCToken.t__float128);
				addKeyword(GCCKeywords.cp__int128, IGCCToken.t__int128);
				addKeyword(GCCKeywords.cp__is_final, IGCCToken.tTT_is_final);
				addKeyword(GCCKeywords.cp__underlying_type, IGCCToken.tTT_underlying_type);
			}
			if (version >= VERSION_5_0) {
				addKeyword(GCCKeywords.cp__is_trivially_copyable, IGCCToken.tTT_is_trivially_copyable);
				addKeyword(GCCKeywords.cp__is_trivially_constructible, IGCCToken.tTT_is_trivially_constructible);
				addKeyword(GCCKeywords.cp__is_trivially_assignable, IGCCToken.tTT_is_trivially_assignable);
			}
			if (version >= VERSION_6_0) {
				addKeyword(GCCKeywords.cp__is_same_as, IGCCToken.tTT_is_same);
			}
			if (version >= VERSION_8_0) {
				addKeyword(GCCKeywords.cp__has_unique_object_representations,
						IGCCToken.tTT_has_unique_object_representations);
				addKeyword(GCCKeywords.cp__is_aggregate, IGCCToken.tTT_is_aggregate);
				addKeyword(GCCKeywords.cp__is_assignable, IGCCToken.tTT_is_assignable);
				addKeyword(GCCKeywords.cp__is_constructible, IGCCToken.tTT_is_constructible);
				addKeyword(GCCKeywords.cp__integer_pack, IGCCToken.tTT_integer_pack);
			}
			if (version >= VERSION_10_0) {
				addKeyword(GCCKeywords.cp__is_same, IGCCToken.tTT_is_same);
			}
			if (version >= VERSION_11_1) {
				addKeyword(GCCKeywords.cp__is_nothrow_assignable, IGCCToken.tTT_is_nothrow_assignable);
				addKeyword(GCCKeywords.cp__is_nothrow_constructible, IGCCToken.tTT_is_nothrow_constructible);
			}
			if (version >= VERSION_14_0) {
				addKeyword(GCCKeywords.cp__is_function, IGCCToken.tTT_is_function);
			}
		} else if (compiler == CompilerType.Clang || compiler == CompilerType.ClangCl) {
			// As documented at
			// http://clang.llvm.org/docs/LanguageExtensions.html#checks-for-type-trait-primitives.
			// For now we don't make it dependent on the version.
			// Missing ones are in comments
			addKeyword(GCCKeywords.cp__has_nothrow_assign, IGCCToken.tTT_has_nothrow_assign);
			// __has_nothrow_move_assign
			addKeyword(GCCKeywords.cp__has_nothrow_copy, IGCCToken.tTT_has_nothrow_copy);
			addKeyword(GCCKeywords.cp__has_nothrow_constructor, IGCCToken.tTT_has_nothrow_constructor);
			addKeyword(GCCKeywords.cp__has_trivial_assign, IGCCToken.tTT_has_trivial_assign);
			// __has_trivial_move_assign
			addKeyword(GCCKeywords.cp__has_trivial_copy, IGCCToken.tTT_has_trivial_copy);
			addKeyword(GCCKeywords.cp__has_trivial_constructor, IGCCToken.tTT_has_trivial_constructor);
			addKeyword(GCCKeywords.cp__has_trivial_destructor, IGCCToken.tTT_has_trivial_destructor);
			addKeyword(GCCKeywords.cp__has_unique_object_representations,
					IGCCToken.tTT_has_unique_object_representations);
			addKeyword(GCCKeywords.cp__has_virtual_destructor, IGCCToken.tTT_has_virtual_destructor);
			addKeyword(GCCKeywords.cp__is_abstract, IGCCToken.tTT_is_abstract);
			addKeyword(GCCKeywords.cp__is_aggregate, IGCCToken.tTT_is_aggregate);
			// __is_arithmetic
			// __is_array
			addKeyword(GCCKeywords.cp__is_assignable, IGCCToken.tTT_is_assignable);
			addKeyword(GCCKeywords.cp__is_base_of, IGCCToken.tTT_is_base_of);
			addKeyword(GCCKeywords.cp__is_class, IGCCToken.tTT_is_class);
			// __is_complete_type
			// __is_compound
			// __is_const
			addKeyword(GCCKeywords.cp__is_constructible, IGCCToken.tTT_is_constructible);
			// __is_convertible
			// __is_convertible_to
			// __is_destructible
			addKeyword(GCCKeywords.cp__is_empty, IGCCToken.tTT_is_empty);
			addKeyword(GCCKeywords.cp__is_enum, IGCCToken.tTT_is_enum);
			addKeyword(GCCKeywords.cp__is_final, IGCCToken.tTT_is_final);
			// __is_floating_point
			addKeyword(GCCKeywords.cp__is_function, IGCCToken.tTT_is_function);
			// __is_fundamental
			// __is_integral
			// __is_interface_class
			addKeyword(GCCKeywords.cp__is_literal, IGCCToken.tTT_is_literal_type);
			addKeyword(GCCKeywords.cp__is_literal_type, IGCCToken.tTT_is_literal_type);
			// __is_lvalue_reference
			// __is_member_object_pointer
			// __is_member_function_pointer
			// __is_member_pointer
			addKeyword(GCCKeywords.cp__is_nothrow_assignable, IGCCToken.tTT_is_nothrow_assignable);
			addKeyword(GCCKeywords.cp__is_nothrow_constructible, IGCCToken.tTT_is_nothrow_constructible);
			// __is_nothrow_destructible
			// __is_object
			addKeyword(GCCKeywords.cp__is_pod, IGCCToken.tTT_is_pod);
			// __is_pointer
			addKeyword(GCCKeywords.cp__is_polymorphic, IGCCToken.tTT_is_polymorphic);
			// __is_reference
			// __is_rvalue_reference
			addKeyword(GCCKeywords.cp__is_same, IGCCToken.tTT_is_same);
			addKeyword(GCCKeywords.cp__is_same_as, IGCCToken.tTT_is_same);
			// __is_scalar
			// __is_sealed
			// __is_signed
			addKeyword(GCCKeywords.cp__is_standard_layout, IGCCToken.tTT_is_standard_layout);
			addKeyword(GCCKeywords.cp__is_trivial, IGCCToken.tTT_is_trivial);
			addKeyword(GCCKeywords.cp__is_trivially_assignable, IGCCToken.tTT_is_trivially_assignable);
			addKeyword(GCCKeywords.cp__is_trivially_constructible, IGCCToken.tTT_is_trivially_constructible);
			addKeyword(GCCKeywords.cp__is_trivially_copyable, IGCCToken.tTT_is_trivially_copyable);
			// __is_trivially_destructible
			addKeyword(GCCKeywords.cp__is_union, IGCCToken.tTT_is_union);
			// __is_unsigned
			// __is_void
			// __reference_binds_to_temporary
			addKeyword(GCCKeywords.cp__underlying_type, IGCCToken.tTT_underlying_type);
			addKeyword(GCCKeywords.cp__integer_pack, IGCCToken.tTT_integer_pack);

			addKeyword(GCCKeywords.cp__float128, IGCCToken.t__float128);
			addKeyword(GCCKeywords.cp__int128, IGCCToken.t__int128);
			//TODO verify other gcc ones
		} else if (compiler == CompilerType.MSVC) {
			// As documented at
			// https://docs.microsoft.com/en-us/cpp/extensions/compiler-support-for-type-traits-cpp-component-extensions?view=vs-2017
			// For now we don't make it dependent on the version.
			addKeyword(GCCKeywords.cp__has_nothrow_assign, IGCCToken.tTT_has_nothrow_assign);
			addKeyword(GCCKeywords.cp__has_nothrow_constructor, IGCCToken.tTT_has_nothrow_constructor);
			addKeyword(GCCKeywords.cp__has_nothrow_copy, IGCCToken.tTT_has_nothrow_copy);
			addKeyword(GCCKeywords.cp__has_trivial_assign, IGCCToken.tTT_has_trivial_assign);
			addKeyword(GCCKeywords.cp__has_trivial_constructor, IGCCToken.tTT_has_trivial_constructor);
			addKeyword(GCCKeywords.cp__has_trivial_copy, IGCCToken.tTT_has_trivial_copy);
			addKeyword(GCCKeywords.cp__has_trivial_destructor, IGCCToken.tTT_has_trivial_destructor);
			addKeyword(GCCKeywords.cp__has_virtual_destructor, IGCCToken.tTT_has_virtual_destructor);
			addKeyword(GCCKeywords.cp__is_abstract, IGCCToken.tTT_is_abstract);
			addKeyword(GCCKeywords.cp__is_base_of, IGCCToken.tTT_is_base_of);
			addKeyword(GCCKeywords.cp__is_class, IGCCToken.tTT_is_class);
			addKeyword(GCCKeywords.cp__is_empty, IGCCToken.tTT_is_empty);
			addKeyword(GCCKeywords.cp__is_enum, IGCCToken.tTT_is_enum);
			addKeyword(GCCKeywords.cp__is_pod, IGCCToken.tTT_is_pod);
			addKeyword(GCCKeywords.cp__is_polymorphic, IGCCToken.tTT_is_polymorphic);
			addKeyword(GCCKeywords.cp__is_union, IGCCToken.tTT_is_union);
			// Missing from that reference page:
			// - __has_assign
			// - __has_copy
			// - __has_finalizer
			// - __has_user_destructor
			// - __is_convertible_to
			// - __is_delegate
			// - __is_interface_class
			// - __is_ref_array
			// - __is_ref_class
			// - __is_simple_value_class
			// - __is_value_class

			// These are according to:
			// http://clang.llvm.org/docs/LanguageExtensions.html#checks-for-type-trait-primitives.
			addKeyword(GCCKeywords.cp__is_final, IGCCToken.tTT_is_final);
			addKeyword(GCCKeywords.cp__underlying_type, IGCCToken.tTT_underlying_type);
			addKeyword(GCCKeywords.cp__is_trivially_constructible, IGCCToken.tTT_is_trivially_constructible);
			addKeyword(GCCKeywords.cp__is_trivially_assignable, IGCCToken.tTT_is_trivially_assignable);
			addKeyword(GCCKeywords.cp__is_constructible, IGCCToken.tTT_is_constructible);
			// Missing from that page:
			// - __is_assignable
			// - __is_destructible
			// - __is_nothrow_destructible
			// - __is_nothrow_assignable
			// - __is_nothrow_constructible

			// Found by looking at some headers
			addKeyword(GCCKeywords.cp__is_standard_layout, IGCCToken.tTT_is_standard_layout);
			addKeyword(GCCKeywords.cp__is_literal_type, IGCCToken.tTT_is_literal_type);
			addKeyword(GCCKeywords.cp__is_trivial, IGCCToken.tTT_is_trivial);
			addKeyword(GCCKeywords.cp__is_trivially_copyable, IGCCToken.tTT_is_trivially_copyable);
			// Missing:
			// - __is_trivially_destructible

		}
	}

	@Override
	public boolean supportMinAndMaxOperators() {
		return true;
	}

	/**
	 * @since 5.5
	 */
	@Override
	public boolean supportRawStringLiterals() {
		return true;
	}

	/**
	 * User Defined Literals
	 * @since 5.10
	 */
	@Override
	public boolean supportUserDefinedLiterals() {
		return true;
	}

	@Override
	public boolean supportDigitSeparators() {
		return true;
	}
}
