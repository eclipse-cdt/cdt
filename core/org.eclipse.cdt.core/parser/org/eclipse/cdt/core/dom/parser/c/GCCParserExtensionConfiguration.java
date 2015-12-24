/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *     Ed Swartz (Nokia)
 *     Markus Schorn (Wind River Systems)
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

    @Override
	public boolean supportStatementsInExpressions() {
        return true;
    }

    @Override
	public boolean supportGCCStyleDesignators() {
        return true;
    }

    @Override
	public boolean supportTypeofUnaryExpressions() {
        return true;
    }

    @Override
	public boolean supportAlignOfUnaryExpression() {
        return true;
    }

	@Override
	public boolean supportKnRC() {
		return true;
	}

	@Override
	public boolean supportAttributeSpecifiers() {
		return true;
	}

	@Override
	public boolean supportDeclspecSpecifiers() {		
		return true;
	}

	@Override
	public IBuiltinBindingsProvider getBuiltinBindingsProvider() {
		return new GCCBuiltinSymbolProvider(ParserLanguage.C, true);
	}
}
