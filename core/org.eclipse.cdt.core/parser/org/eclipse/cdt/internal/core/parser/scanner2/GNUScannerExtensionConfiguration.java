/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;

/**
 * @author jcamelon
 */
public abstract class GNUScannerExtensionConfiguration implements IScannerExtensionConfiguration {

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

    private static final ObjectStyleMacro __cdecl = new ObjectStyleMacro(
            "__cdecl".toCharArray(), emptyCharArray); //$NON-NLS-1$

    private static final ObjectStyleMacro __complex__ = new ObjectStyleMacro(
            "__complex__".toCharArray(), "_Complex".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$

    private static final ObjectStyleMacro __real__ = new ObjectStyleMacro(
            "__real__".toCharArray(), "(int)".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$

    private static final ObjectStyleMacro __imag__ = new ObjectStyleMacro(
            "__imag__".toCharArray(), "(int)".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$

    private static final FunctionStyleMacro __attribute__ = new FunctionStyleMacro(
            "__attribute__".toCharArray(), //$NON-NLS-1$
            emptyCharArray, new char[][] { "arg".toCharArray() }); //$NON-NLS-1$

    private static final FunctionStyleMacro __declspec = new FunctionStyleMacro(
            "__declspec".toCharArray(), //$NON-NLS-1$
            emptyCharArray, new char[][] { "arg".toCharArray() }); //$NON-NLS-1$


    private static final FunctionStyleMacro __builtin_va_arg = new FunctionStyleMacro(
            "__builtin_va_arg".toCharArray(), //$NON-NLS-1$
            "*(type *)ap".toCharArray(), //$NON-NLS-1$
            new char[][] { "ap".toCharArray(), "type".toCharArray() }); //$NON-NLS-1$//$NON-NLS-2$

    private static final FunctionStyleMacro __builtin_constant_p = new FunctionStyleMacro(
            "__builtin_constant_p".toCharArray(), //$NON-NLS-1$
            "0".toCharArray(), //$NON-NLS-1$
            new char[][] { "exp".toCharArray() }); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * @return
     */
    public CharArrayObjectMap getAdditionalMacros() {
        CharArrayObjectMap realDefinitions = new CharArrayObjectMap(16);
        realDefinitions.put(__inline__.name, __inline__);
        realDefinitions.put(__cdecl.name, __cdecl);
        realDefinitions.put(__const__.name, __const__);
        realDefinitions.put(__const.name, __const);
        realDefinitions.put(__extension__.name, __extension__);
        realDefinitions.put(__attribute__.name, __attribute__);
        realDefinitions.put(__declspec.name, __declspec);
        realDefinitions.put(__restrict__.name, __restrict__);
        realDefinitions.put(__restrict.name, __restrict);
        realDefinitions.put(__volatile__.name, __volatile__);
        realDefinitions.put(__signed__.name, __signed__);
        realDefinitions.put(__complex__.name, __complex__);
        realDefinitions.put(__imag__.name, __imag__);
        realDefinitions.put(__real__.name, __real__);
        realDefinitions.put(__builtin_va_arg.name, __builtin_va_arg);
        realDefinitions.put(__builtin_constant_p.name, __builtin_constant_p);
        realDefinitions.put( __asm__.name, __asm__ );
        return realDefinitions;
    }
    
}