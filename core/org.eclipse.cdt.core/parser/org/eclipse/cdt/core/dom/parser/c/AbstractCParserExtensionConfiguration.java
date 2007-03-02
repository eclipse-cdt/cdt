/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.c;

import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.GCCBuiltinSymbolProvider;


/**
 * Abstract C parser extension configuration to help model C dialects.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 * 
 * @since 4.0
 */
public abstract class AbstractCParserExtensionConfiguration implements ICParserExtensionConfiguration {

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration#supportAlignOfUnaryExpression()
	 */
	public boolean supportAlignOfUnaryExpression() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration#supportAttributeSpecifiers()
	 */
	public boolean supportAttributeSpecifiers() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration#supportDeclspecSpecifiers()
	 */
	public boolean supportDeclspecSpecifiers() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration#supportGCCOtherBuiltinSymbols()
	 */
	public boolean supportGCCOtherBuiltinSymbols() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration#supportGCCStyleDesignators()
	 */
	public boolean supportGCCStyleDesignators() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration#supportKnRC()
	 */
	public boolean supportKnRC() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration#supportStatementsInExpressions()
	 */
	public boolean supportStatementsInExpressions() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration#supportTypeofUnaryExpressions()
	 */
	public boolean supportTypeofUnaryExpressions() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration#getBuiltinSymbolProvider()
	 */
	public IBuiltinBindingsProvider getBuiltinBindingsProvider() {
		if (supportGCCOtherBuiltinSymbols()) {
			return new GCCBuiltinSymbolProvider(ParserLanguage.C);
		}
		return null;
	}

}
