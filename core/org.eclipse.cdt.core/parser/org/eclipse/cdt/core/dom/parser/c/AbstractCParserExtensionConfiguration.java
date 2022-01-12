/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.c;

import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.GCCBuiltinSymbolProvider;

/**
 * Abstract C parser extension configuration to help model C dialects.
 * @since 4.0
 */
public abstract class AbstractCParserExtensionConfiguration implements ICParserExtensionConfiguration {

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration#supportAlignOfUnaryExpression()
	 */
	@Override
	public boolean supportAlignOfUnaryExpression() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration#supportAttributeSpecifiers()
	 */
	@Override
	public boolean supportAttributeSpecifiers() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration#supportDeclspecSpecifiers()
	 */
	@Override
	public boolean supportDeclspecSpecifiers() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration#supportGCCOtherBuiltinSymbols()
	 */
	@Override
	public boolean supportGCCOtherBuiltinSymbols() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration#supportGCCStyleDesignators()
	 */
	@Override
	public boolean supportGCCStyleDesignators() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration#supportKnRC()
	 */
	@Override
	public boolean supportKnRC() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration#supportStatementsInExpressions()
	 */
	@Override
	public boolean supportStatementsInExpressions() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration#supportTypeofUnaryExpressions()
	 */
	@Override
	public boolean supportTypeofUnaryExpressions() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration#getBuiltinSymbolProvider()
	 */
	@Override
	public IBuiltinBindingsProvider getBuiltinBindingsProvider() {
		return new GCCBuiltinSymbolProvider(ParserLanguage.C, supportGCCOtherBuiltinSymbols());
	}

	/**
	 * {@inheritDoc}
	 * @since 5.1
	 */
	@Override
	public boolean supportParameterInfoBlock() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 * @since 5.1
	 */
	@Override
	public boolean supportExtendedSizeofOperator() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 * @since 5.1
	 */
	@Override
	public boolean supportFunctionStyleAssembler() {
		return false;
	}
}
