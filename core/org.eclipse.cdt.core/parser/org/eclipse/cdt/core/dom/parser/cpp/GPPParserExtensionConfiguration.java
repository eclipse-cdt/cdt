/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Ed Swartz (Nokia)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.GCCBuiltinSymbolProvider;
import org.eclipse.core.runtime.Platform;

/**
 * @author jcamelon
 */
public class GPPParserExtensionConfiguration extends AbstractCPPParserExtensionConfiguration {

    /*
     * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#allowRestrictPointerOperators()
     */
    public boolean allowRestrictPointerOperators() {
        return true;
    }

    /*
     * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#supportTypeofUnaryExpressions()
     */
    public boolean supportTypeofUnaryExpressions() {
        return true;
    }

    /*
     * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#supportAlignOfUnaryExpression()
     */
    public boolean supportAlignOfUnaryExpression() {
        return true;
    }

    /*
     * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#supportExtendedTemplateSyntax()
     */
    public boolean supportExtendedTemplateSyntax() {
        return true;
    }

    /*
     * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#supportMinAndMaxOperators()
     */
    public boolean supportMinAndMaxOperators() {
        return true;
    }

    /*
     * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#supportStatementsInExpressions()
     */
    public boolean supportStatementsInExpressions() {
        return true;
    }

    /*
     * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#supportComplexNumbers()
     */
    public boolean supportComplexNumbers() {
        return true;
    }

    /*
     * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#supportRestrictKeyword()
     */
    public boolean supportRestrictKeyword() {
        return true;
    }

    /*
     * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#supportLongLongs()
     */
    public boolean supportLongLongs() {
        return true;
    }

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#supportKnRC()
	 */
	public boolean supportKnRC() {
		return false;
	}
	
	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#supportAttributeSpecifiers()
	 */
	public boolean supportAttributeSpecifiers() {
		return true;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#supportDeclspecSpecifiers()
	 */
	public boolean supportDeclspecSpecifiers() {
		// XXX: a hack, should use the target's platform
		return Platform.getOS().equals(Platform.OS_WIN32);
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#getBuiltinBindingsProvider()
	 */
	public IBuiltinBindingsProvider getBuiltinBindingsProvider() {
		return new GCCBuiltinSymbolProvider(ParserLanguage.CPP);
	}
}
