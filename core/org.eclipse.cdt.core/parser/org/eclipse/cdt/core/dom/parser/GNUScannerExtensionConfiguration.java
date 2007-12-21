/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
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

import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.IPreprocessorDirective;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;

/**
 * @author jcamelon
 */
public abstract class GNUScannerExtensionConfiguration extends AbstractScannerExtensionConfiguration {

	private static IMacro[] sAdditionalMacros= new IMacro[] {
		createMacro("__asm__", "asm"),  //$NON-NLS-1$//$NON-NLS-2$
		createMacro("__complex__", "_Complex"), //$NON-NLS-1$ //$NON-NLS-2$
		createMacro("__const__", "const"), //$NON-NLS-1$ //$NON-NLS-2$
		createMacro("__const", "const"), //$NON-NLS-1$ //$NON-NLS-2$
		createMacro("__extension__", ""), //$NON-NLS-1$ //$NON-NLS-2$
		createMacro("__inline__", "inline"), //$NON-NLS-1$ //$NON-NLS-2$
		createMacro("__imag__", "(int)"), //$NON-NLS-1$ //$NON-NLS-2$
		createMacro("__null", "(void *)0"), //$NON-NLS-1$ //$NON-NLS-2$
		createMacro("__real__", "(int)"), //$NON-NLS-1$ //$NON-NLS-2$
		createMacro("__restrict__", "restrict"), //$NON-NLS-1$ //$NON-NLS-2$
		createMacro("__restrict", "restrict"), //$NON-NLS-1$ //$NON-NLS-2$
		createMacro("__volatile__", "volatile"), //$NON-NLS-1$ //$NON-NLS-2$
		createMacro("__signed__", "signed"), //$NON-NLS-1$ //$NON-NLS-2$
		createMacro("__stdcall", ""), //$NON-NLS-1$ //$NON-NLS-2$
		createMacro("__typeof__", "typeof"), //$NON-NLS-1$ //$NON-NLS-2$

		createMacro("__builtin_va_arg(ap,type)", "*(type *)ap"),  //$NON-NLS-1$//$NON-NLS-2$
		createMacro("__builtin_constant_p(exp)", "0") //$NON-NLS-1$//$NON-NLS-2$
	};

	public static IMacro[] getAdditionalGNUMacros() {
		return sAdditionalMacros;
	}
   

    public boolean initializeMacroValuesTo1() {
        return true;
    }

    public boolean support$InIdentifiers() {
        return true;
    }

    public char[] supportAdditionalNumericLiteralSuffixes() {
        return "ij".toCharArray(); //$NON-NLS-1$
    }
        
    public IMacro[] getAdditionalMacros() {
    	return sAdditionalMacros;
    }
    
    /*
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerExtensionConfiguration#getAdditionalPreprocessorKeywords()
     */
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
