/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Ed Swartz (Nokia)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.c;

import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.GCCBuiltinSymbolProvider;
import org.eclipse.core.runtime.Platform;

/**
 * @author jcamelon
 */
public class GCCParserExtensionConfiguration extends AbstractCParserExtensionConfiguration {

    /*
     * @see org.eclipse.cdt.core.dom.parser.c.AbstractCParserExtensionConfiguration#supportStatementsInExpressions()
     */
    public boolean supportStatementsInExpressions() {
        return true;
    }

    /*
     * @see org.eclipse.cdt.core.dom.parser.c.AbstractCParserExtensionConfiguration#supportGCCStyleDesignators()
     */
    public boolean supportGCCStyleDesignators() {
        return true;
    }

    /*
     * @see org.eclipse.cdt.core.dom.parser.c.AbstractCParserExtensionConfiguration#supportTypeofUnaryExpressions()
     */
    public boolean supportTypeofUnaryExpressions() {
        return true;
    }

    /*
     * @see org.eclipse.cdt.core.dom.parser.c.AbstractCParserExtensionConfiguration#supportAlignOfUnaryExpression()
     */
    public boolean supportAlignOfUnaryExpression() {
        return true;
    }

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.AbstractCParserExtensionConfiguration#supportKnRC()
	 */
	public boolean supportKnRC() {
		return true;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.AbstractCParserExtensionConfiguration#supportAttributeSpecifiers()
	 */
	public boolean supportAttributeSpecifiers() {
		return true;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.AbstractCParserExtensionConfiguration#supportDeclspecSpecifiers()
	 */
	public boolean supportDeclspecSpecifiers() {
		// XXX Yes, this is a hack -- should use the target platform
		if (Platform.getOS().equals(Platform.OS_WIN32))
			return true;
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.AbstractCParserExtensionConfiguration#getBuiltinSymbolProvider()
	 */
	public IBuiltinBindingsProvider getBuiltinBindingsProvider() {
		return new GCCBuiltinSymbolProvider(ParserLanguage.C);
	}
}
