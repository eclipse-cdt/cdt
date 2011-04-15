/*******************************************************************************
 *  Copyright (c) 2004, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *    IBM - Initial API and implementation
 *    Ed Swartz (Nokia)
 *    Anton Leherbauer (Wind River Systems)
 *    Markus Schorn (Wind River Systems)
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.parser.GNUScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.GCCKeywords;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.Keywords;

/**
 * Configures the preprocessor for c++-sources as accepted by g++.
 */
public class GPPScannerExtensionConfiguration extends GNUScannerExtensionConfiguration {

	private static GPPScannerExtensionConfiguration sInstance= new GPPScannerExtensionConfiguration();
	/**
	 * @since 5.1
	 */
	public static GPPScannerExtensionConfiguration getInstance() {
		return sInstance;
	}
	
	@SuppressWarnings("nls")
	public GPPScannerExtensionConfiguration() {
		addMacro("__null", "0");  
		addMacro("__builtin_offsetof(T,m)", "(reinterpret_cast <size_t>(&reinterpret_cast <const volatile char &>(static_cast<T*> (0)->m)))");
		addKeyword(Keywords.c_COMPLEX, IToken.t__Complex);
		addKeyword(Keywords.c_IMAGINARY, IToken.t__Imaginary);
		
		// Type-traits
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
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerConfiguration#supportMinAndMaxOperators()
     */
    @Override
	public boolean supportMinAndMaxOperators() {
        return true;
    }
}
