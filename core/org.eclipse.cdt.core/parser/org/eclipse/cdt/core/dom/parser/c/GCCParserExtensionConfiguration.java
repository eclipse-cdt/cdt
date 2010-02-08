/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
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
package org.eclipse.cdt.core.dom.parser.c;

import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.GCCBuiltinSymbolProvider;

/**
 * Configures the parser for c-source code as accepted by gcc.
 */
public class GCCParserExtensionConfiguration extends AbstractCParserExtensionConfiguration {
	private static GCCParserExtensionConfiguration sInstance= new GCCParserExtensionConfiguration();
	/**
	 * @since 5.1
	 */
	public static GCCParserExtensionConfiguration getInstance() {
		return sInstance;
	}

    /*
     * @see org.eclipse.cdt.core.dom.parser.c.AbstractCParserExtensionConfiguration#supportStatementsInExpressions()
     */
    @Override
	public boolean supportStatementsInExpressions() {
        return true;
    }

    /*
     * @see org.eclipse.cdt.core.dom.parser.c.AbstractCParserExtensionConfiguration#supportGCCStyleDesignators()
     */
    @Override
	public boolean supportGCCStyleDesignators() {
        return true;
    }

    /*
     * @see org.eclipse.cdt.core.dom.parser.c.AbstractCParserExtensionConfiguration#supportTypeofUnaryExpressions()
     */
    @Override
	public boolean supportTypeofUnaryExpressions() {
        return true;
    }

    /*
     * @see org.eclipse.cdt.core.dom.parser.c.AbstractCParserExtensionConfiguration#supportAlignOfUnaryExpression()
     */
    @Override
	public boolean supportAlignOfUnaryExpression() {
        return true;
    }

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.AbstractCParserExtensionConfiguration#supportKnRC()
	 */
	@Override
	public boolean supportKnRC() {
		return true;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.AbstractCParserExtensionConfiguration#supportAttributeSpecifiers()
	 */
	@Override
	public boolean supportAttributeSpecifiers() {
		return true;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.AbstractCParserExtensionConfiguration#supportDeclspecSpecifiers()
	 */
	@Override
	public boolean supportDeclspecSpecifiers() {		
		return true;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.AbstractCParserExtensionConfiguration#getBuiltinSymbolProvider()
	 */
	@Override
	public IBuiltinBindingsProvider getBuiltinBindingsProvider() {
		return new GCCBuiltinSymbolProvider(ParserLanguage.C, true);
	}
}
