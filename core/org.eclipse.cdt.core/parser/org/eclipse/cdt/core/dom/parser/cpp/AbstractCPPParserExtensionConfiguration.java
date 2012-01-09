/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.GCCBuiltinSymbolProvider;


/**
 * Abstract C++ parser extension configuration to help model C++ dialects.
 *
 * @since 4.0
 */
public abstract class AbstractCPPParserExtensionConfiguration implements ICPPParserExtensionConfiguration {

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#allowRestrictPointerOperators()
	 */
	@Override
	public boolean allowRestrictPointerOperators() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportAlignOfUnaryExpression()
	 */
	@Override
	public boolean supportAlignOfUnaryExpression() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportAttributeSpecifiers()
	 */
	@Override
	public boolean supportAttributeSpecifiers() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportComplexNumbers()
	 */
	@Override
	public boolean supportComplexNumbers() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportDeclspecSpecifiers()
	 */
	@Override
	public boolean supportDeclspecSpecifiers() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportExtendedTemplateSyntax()
	 */
	@Override
	public boolean supportExtendedTemplateSyntax() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportGCCOtherBuiltinSymbols()
	 */
	@Override
	public boolean supportGCCOtherBuiltinSymbols() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportKnRC()
	 */
	@Override
	public boolean supportKnRC() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportLongLongs()
	 */
	@Override
	public boolean supportLongLongs() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportMinAndMaxOperators()
	 */
	@Override
	public boolean supportMinAndMaxOperators() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportRestrictKeyword()
	 */
	@Override
	public boolean supportRestrictKeyword() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportStatementsInExpressions()
	 */
	@Override
	public boolean supportStatementsInExpressions() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportTypeofUnaryExpressions()
	 */
	@Override
	public boolean supportTypeofUnaryExpressions() {
		return false;
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

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#getBuiltinBindingsProvider()
	 */
	@Override
	public IBuiltinBindingsProvider getBuiltinBindingsProvider() {
		return new GCCBuiltinSymbolProvider(ParserLanguage.CPP, supportGCCOtherBuiltinSymbols());
	}
}
