/*******************************************************************************
 * Copyright (c) 2002, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation
 *    Ed Swartz (Nokia)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.GCCBuiltinSymbolProvider;

/**
 * Configures the parser for c++-sources as accepted by g++.
 */
public class GPPParserExtensionConfiguration extends AbstractCPPParserExtensionConfiguration {
	private static GPPParserExtensionConfiguration sInstance= new GPPParserExtensionConfiguration();
	/**
	 * @since 5.1
	 */
	public static GPPParserExtensionConfiguration getInstance() {
		return sInstance;
	}

    /*
     * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#allowRestrictPointerOperators()
     */
    @Override
	public boolean allowRestrictPointerOperators() {
        return true;
    }

    /*
     * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#supportTypeofUnaryExpressions()
     */
    @Override
	public boolean supportTypeofUnaryExpressions() {
        return true;
    }

    /*
     * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#supportAlignOfUnaryExpression()
     */
    @Override
	public boolean supportAlignOfUnaryExpression() {
        return true;
    }

    /*
     * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#supportExtendedTemplateSyntax()
     */
    @Override
	public boolean supportExtendedTemplateSyntax() {
        return true;
    }

    /*
     * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#supportMinAndMaxOperators()
     */
    @Override
	public boolean supportMinAndMaxOperators() {
        return true;
    }

    /*
     * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#supportStatementsInExpressions()
     */
    @Override
	public boolean supportStatementsInExpressions() {
        return true;
    }

    /*
     * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#supportComplexNumbers()
     */
    @Override
	public boolean supportComplexNumbers() {
        return true;
    }

    /*
     * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#supportRestrictKeyword()
     */
    @Override
	public boolean supportRestrictKeyword() {
        return true;
    }

    /*
     * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#supportLongLongs()
     */
    @Override
	public boolean supportLongLongs() {
        return true;
    }

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#supportKnRC()
	 */
	@Override
	public boolean supportKnRC() {
		return false;
	}
	
	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#supportAttributeSpecifiers()
	 */
	@Override
	public boolean supportAttributeSpecifiers() {
		return true;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#supportDeclspecSpecifiers()
	 */
	@Override
	public boolean supportDeclspecSpecifiers() {
		return true;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration#getBuiltinBindingsProvider()
	 */
	@Override
	public IBuiltinBindingsProvider getBuiltinBindingsProvider() {
		return new GCCBuiltinSymbolProvider(ParserLanguage.CPP, true);
	}
}
