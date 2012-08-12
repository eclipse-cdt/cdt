/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *     Ed Swartz (Nokia)
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
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
	private static final int VERSION_4_3 = version(4, 3);
	private static final int VERSION_4_6 = version(4, 6);
	private static GPPScannerExtensionConfiguration CONFIG= new GPPScannerExtensionConfiguration();
	private static GPPScannerExtensionConfiguration CONFIG_4_3= new GPPScannerExtensionConfiguration(VERSION_4_3);
	private static GPPScannerExtensionConfiguration CONFIG_4_6= new GPPScannerExtensionConfiguration(VERSION_4_6);

	private static int version(int major, int minor) {
		return (major << 16) + minor;
	}

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
				int major= Integer.valueOf(definedSymbols.get("__GNUC__")); //$NON-NLS-1$
				int minor= Integer.valueOf(definedSymbols.get("__GNUC_MINOR__")); //$NON-NLS-1$
				int version= version(major, minor);
				if (version >= VERSION_4_6) {
					return CONFIG_4_6;
				}
				if (version >= VERSION_4_3) {
					return CONFIG_4_3;
				}
			} catch (Exception e) {
				// Fall-back to the default configuration.
			}
		}
		return CONFIG;
	}

	public GPPScannerExtensionConfiguration() {
		this(0);
	}
	
	/**
	 * @since 5.4
	 */
	@SuppressWarnings("nls")
	public GPPScannerExtensionConfiguration(int version) {
		addMacro("__null", "0");  
		addMacro("__builtin_offsetof(T,m)", "(reinterpret_cast <size_t>(&reinterpret_cast <const volatile char &>(static_cast<T*> (0)->m)))");
		addKeyword(Keywords.c_COMPLEX, IToken.t__Complex);
		addKeyword(Keywords.c_IMAGINARY, IToken.t__Imaginary);
		
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
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerConfiguration#supportMinAndMaxOperators()
     */
    @Override
	public boolean supportMinAndMaxOperators() {
        return true;
    }
}
