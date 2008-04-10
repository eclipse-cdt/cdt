/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Anton Leherbauer (Wind River Systems)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser;

import org.eclipse.cdt.core.parser.GCCKeywords;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.IPreprocessorDirective;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;

/**
 * @author jcamelon
 */
public abstract class GNUScannerExtensionConfiguration extends AbstractScannerExtensionConfiguration {

	private static IMacro[] sAdditionalMacros= new IMacro[] {
		createMacro("__complex__", "_Complex"), //$NON-NLS-1$ //$NON-NLS-2$
		createMacro("__extension__", ""), //$NON-NLS-1$ //$NON-NLS-2$
		createMacro("__imag__", "(int)"), //$NON-NLS-1$ //$NON-NLS-2$
		createMacro("__null", "(void *)0"), //$NON-NLS-1$ //$NON-NLS-2$
		createMacro("__real__", "(int)"), //$NON-NLS-1$ //$NON-NLS-2$
		createMacro("__stdcall", ""), //$NON-NLS-1$ //$NON-NLS-2$

		createMacro("__builtin_va_arg(ap,type)", "*(type *)ap"),  //$NON-NLS-1$//$NON-NLS-2$
		createMacro("__builtin_constant_p(exp)", "0") //$NON-NLS-1$//$NON-NLS-2$
	};

	public static IMacro[] getAdditionalGNUMacros() {
		return sAdditionalMacros;
	}
	
	public static void addAdditionalGNUKeywords(CharArrayIntMap target) {
		target.put(GCCKeywords.cp__ALIGNOF__, IGCCToken.t___alignof__ );
		target.put(GCCKeywords.cp__ASM__, IToken.t_asm); 
		target.put(GCCKeywords.cp__ATTRIBUTE, IGCCToken.t__attribute__ );
		target.put(GCCKeywords.cp__ATTRIBUTE__, IGCCToken.t__attribute__ );
		target.put(GCCKeywords.cp__CONST, IToken.t_const); 
		target.put(GCCKeywords.cp__CONST__, IToken.t_const); 
		target.put(GCCKeywords.cp__DECLSPEC, IGCCToken.t__declspec );
		target.put(GCCKeywords.cp__INLINE__, IToken.t_inline); 
		target.put(GCCKeywords.cp__RESTRICT, IToken.t_restrict); 
		target.put(GCCKeywords.cp__RESTRICT__, IToken.t_restrict); 
		target.put(GCCKeywords.cp__VOLATILE__, IToken.t_volatile); 
		target.put(GCCKeywords.cp__SIGNED__, IToken.t_signed); 
		target.put(GCCKeywords.cp__TYPEOF__, IGCCToken.t_typeof); 
		target.put(GCCKeywords.cpTYPEOF, IGCCToken.t_typeof );
	}

    @Override
	public boolean support$InIdentifiers() {
        return true;
    }

    @Override
	public char[] supportAdditionalNumericLiteralSuffixes() {
        return "ij".toCharArray(); //$NON-NLS-1$
    }
        
    @Override
	public IMacro[] getAdditionalMacros() {
    	return sAdditionalMacros;
    }
    
    /*
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerExtensionConfiguration#getAdditionalPreprocessorKeywords()
     */
    @Override
	public CharArrayIntMap getAdditionalPreprocessorKeywords() {
    	CharArrayIntMap additionalPPKeywords= new CharArrayIntMap(8, IPreprocessorDirective.ppInvalid);
        additionalPPKeywords.put(Keywords.cINCLUDE_NEXT, IPreprocessorDirective.ppInclude_next); 
        additionalPPKeywords.put(Keywords.cIMPORT, IPreprocessorDirective.ppImport);
        additionalPPKeywords.put(Keywords.cWARNING, IPreprocessorDirective.ppWarning);
        additionalPPKeywords.put(Keywords.cIDENT, IPreprocessorDirective.ppIgnore);
        additionalPPKeywords.put(Keywords.cSCCS, IPreprocessorDirective.ppIgnore);
        additionalPPKeywords.put(Keywords.cASSERT, IPreprocessorDirective.ppIgnore);
        additionalPPKeywords.put(Keywords.cUNASSERT, IPreprocessorDirective.ppIgnore);
    	return additionalPPKeywords;
    }
}
