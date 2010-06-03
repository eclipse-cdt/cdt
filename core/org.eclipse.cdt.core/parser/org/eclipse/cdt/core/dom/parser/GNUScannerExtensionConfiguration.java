/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *    IBM - Initial API and implementation
 *    Anton Leherbauer (Wind River Systems)
 *    Markus Schorn (Wind River Systems)
 *    Sergey Prigogin (Google)
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
 * Base class for all gnu scanner configurations. Provides gnu-specific macros and keywords.
 * @since 5.0
 */
public abstract class GNUScannerExtensionConfiguration extends AbstractScannerExtensionConfiguration {
	private static GNUScannerExtensionConfiguration sInstance;
	
	@SuppressWarnings("nls")
	public GNUScannerExtensionConfiguration() {
		addMacro("__complex__", "_Complex");
		addMacro("__extension__", ""); 
		addMacro("__imag__", "(int)"); 
		addMacro("__real__", "(int)");
		addMacro("__stdcall", "");
		addMacro("__thread", "");

		addMacro("__builtin_va_arg(ap,type)", "*(typeof(type) *)ap");
		addMacro("__builtin_constant_p(exp)", "0");
		addMacro("__builtin_types_compatible_p(x,y)", "__builtin_types_compatible_p(sizeof(x),sizeof(y))");
		addMacro("__offsetof__(x)", "(x)");

    	addPreprocessorKeyword(Keywords.cINCLUDE_NEXT, IPreprocessorDirective.ppInclude_next); 
    	addPreprocessorKeyword(Keywords.cIMPORT, IPreprocessorDirective.ppImport);
    	addPreprocessorKeyword(Keywords.cWARNING, IPreprocessorDirective.ppWarning);
    	addPreprocessorKeyword(Keywords.cIDENT, IPreprocessorDirective.ppIgnore);
    	addPreprocessorKeyword(Keywords.cSCCS, IPreprocessorDirective.ppIgnore);
    	addPreprocessorKeyword(Keywords.cASSERT, IPreprocessorDirective.ppIgnore);
    	addPreprocessorKeyword(Keywords.cUNASSERT, IPreprocessorDirective.ppIgnore);

		addKeyword(GCCKeywords.cp__ALIGNOF, IGCCToken.t___alignof__ );
		addKeyword(GCCKeywords.cp__ALIGNOF__, IGCCToken.t___alignof__ );
		addKeyword(GCCKeywords.cp__ASM, IToken.t_asm); 
		addKeyword(GCCKeywords.cp__ASM__, IToken.t_asm); 
		addKeyword(GCCKeywords.cp__ATTRIBUTE, IGCCToken.t__attribute__ );
		addKeyword(GCCKeywords.cp__ATTRIBUTE__, IGCCToken.t__attribute__ );
		addKeyword(GCCKeywords.cp__CONST, IToken.t_const); 
		addKeyword(GCCKeywords.cp__CONST__, IToken.t_const); 
		addKeyword(GCCKeywords.cp__DECLSPEC, IGCCToken.t__declspec );
		addKeyword(GCCKeywords.cp__INLINE, IToken.t_inline); 
		addKeyword(GCCKeywords.cp__INLINE__, IToken.t_inline); 
		addKeyword(GCCKeywords.cp__RESTRICT, IToken.t_restrict); 
		addKeyword(GCCKeywords.cp__RESTRICT__, IToken.t_restrict); 
		addKeyword(GCCKeywords.cp__VOLATILE, IToken.t_volatile); 
		addKeyword(GCCKeywords.cp__VOLATILE__, IToken.t_volatile); 
		addKeyword(GCCKeywords.cp__SIGNED, IToken.t_signed); 
		addKeyword(GCCKeywords.cp__SIGNED__, IToken.t_signed); 
		addKeyword(GCCKeywords.cp__TYPEOF, IGCCToken.t_typeof); 
		addKeyword(GCCKeywords.cp__TYPEOF__, IGCCToken.t_typeof); 
		addKeyword(GCCKeywords.cpTYPEOF, IGCCToken.t_typeof );
	}

    @Override
	public boolean support$InIdentifiers() {
        return true;
    }

    @Override
	public char[] supportAdditionalNumericLiteralSuffixes() {
        return "ij".toCharArray(); //$NON-NLS-1$
    }
        	
	/**
	 * @deprecated simply derive from this class and use {@link #addMacro(String, String)} to
	 * add additional macros.
	 */
	@Deprecated
	public static IMacro[] getAdditionalGNUMacros() {
		if (sInstance == null) {
			sInstance= new GNUScannerExtensionConfiguration() {};
		}
		return sInstance.getAdditionalMacros();
	}
	
	/**
	 * @deprecated simply derive from this class and use {@link #addKeyword(char[], int)} to
	 * add additional keywords.
	 */
	@Deprecated
	public static void addAdditionalGNUKeywords(CharArrayIntMap target) {
		if (sInstance == null) {
			sInstance= new GNUScannerExtensionConfiguration() {};
		}
		target.putAll(sInstance.getAdditionalKeywords());
	}
}
