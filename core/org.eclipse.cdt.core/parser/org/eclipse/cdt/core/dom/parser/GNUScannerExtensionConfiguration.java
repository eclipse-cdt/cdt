/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser;

import org.eclipse.cdt.core.parser.IPreprocessorDirective;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.internal.core.parser.scanner2.FunctionStyleMacro;
import org.eclipse.cdt.internal.core.parser.scanner2.ObjectStyleMacro;

/**
 * @author jcamelon
 */
public abstract class GNUScannerExtensionConfiguration extends AbstractScannerExtensionConfiguration {

    protected static final char[] emptyCharArray = "".toCharArray(); //$NON-NLS-1$

    public boolean initializeMacroValuesTo1() {
        return true;
    }

    public boolean support$InIdentifiers() {
        return true;
    }

    public char[] supportAdditionalNumericLiteralSuffixes() {
        return "ij".toCharArray(); //$NON-NLS-1$
    }
    
    private static final ObjectStyleMacro __asm__ = new ObjectStyleMacro(
            "__asm__".toCharArray(), "asm".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$


    private static final ObjectStyleMacro __inline__ = new ObjectStyleMacro(
            "__inline__".toCharArray(), "inline".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$

    private static final ObjectStyleMacro __extension__ = new ObjectStyleMacro(
            "__extension__".toCharArray(), emptyCharArray); //$NON-NLS-1$


    private static final ObjectStyleMacro __restrict__ = new ObjectStyleMacro(
            "__restrict__".toCharArray(), "restrict".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$

    private static final ObjectStyleMacro __restrict = new ObjectStyleMacro(
            "__restrict".toCharArray(), "restrict".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$

    private static final ObjectStyleMacro __volatile__ = new ObjectStyleMacro(
            "__volatile__".toCharArray(), "volatile".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$

    private static final ObjectStyleMacro __const__ = new ObjectStyleMacro(
            "__const__".toCharArray(), "const".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$

    private static final ObjectStyleMacro __const = new ObjectStyleMacro(
            "__const".toCharArray(), "const".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$

    private static final ObjectStyleMacro __signed__ = new ObjectStyleMacro(
            "__signed__".toCharArray(), "signed".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$

    private static final ObjectStyleMacro __complex__ = new ObjectStyleMacro(
            "__complex__".toCharArray(), "_Complex".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$

    private static final ObjectStyleMacro __real__ = new ObjectStyleMacro(
            "__real__".toCharArray(), "(int)".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$

    private static final ObjectStyleMacro __imag__ = new ObjectStyleMacro(
            "__imag__".toCharArray(), "(int)".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
    
    private static final ObjectStyleMacro __null = new ObjectStyleMacro(
    		"__null".toCharArray(), "(void *)0".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$

    private static final FunctionStyleMacro __builtin_va_arg = new FunctionStyleMacro(
            "__builtin_va_arg".toCharArray(), //$NON-NLS-1$
            "*(type *)ap".toCharArray(), //$NON-NLS-1$
            new char[][] { "ap".toCharArray(), "type".toCharArray() }); //$NON-NLS-1$//$NON-NLS-2$

    private static final FunctionStyleMacro __builtin_constant_p = new FunctionStyleMacro(
            "__builtin_constant_p".toCharArray(), //$NON-NLS-1$
            "0".toCharArray(), //$NON-NLS-1$
            new char[][] { "exp".toCharArray() }); //$NON-NLS-1$

    // Kludge for MSVC support until we get a real extension
    private static final ObjectStyleMacro __stdcall = new ObjectStyleMacro(
    		"__stdcall".toCharArray(), emptyCharArray); //$NON-NLS-1$
    
    /**
     * @return
     */
    public CharArrayObjectMap getAdditionalMacros() {
        CharArrayObjectMap realDefinitions = new CharArrayObjectMap(16);
        realDefinitions.put(__inline__.name, __inline__);
        realDefinitions.put(__const__.name, __const__);
        realDefinitions.put(__const.name, __const);
        realDefinitions.put(__extension__.name, __extension__);
        realDefinitions.put(__restrict__.name, __restrict__);
        realDefinitions.put(__restrict.name, __restrict);
        realDefinitions.put(__volatile__.name, __volatile__);
        realDefinitions.put(__signed__.name, __signed__);
        realDefinitions.put(__complex__.name, __complex__);
        realDefinitions.put(__imag__.name, __imag__);
        realDefinitions.put( __null.name, __null );
        realDefinitions.put(__real__.name, __real__);
        realDefinitions.put(__builtin_va_arg.name, __builtin_va_arg);
        realDefinitions.put(__builtin_constant_p.name, __builtin_constant_p);
        realDefinitions.put( __asm__.name, __asm__ );
        
        realDefinitions.put(__stdcall.name, __stdcall);
        
        return realDefinitions;
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
