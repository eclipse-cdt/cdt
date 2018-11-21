/*******************************************************************************
 * Copyright (c) 2007, 2015 Wind River Systems, Inc. and others.
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
 *     Richard Eames
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.cpp;

import java.util.Collections;
import java.util.Map;

import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.parser.IToken.ContextSensitiveTokenType;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.GCCBuiltinSymbolProvider;

/**
 * Abstract C++ parser extension configuration to help model C++ dialects.
 *
 * @since 4.0
 */
public abstract class AbstractCPPParserExtensionConfiguration implements ICPPParserExtensionConfiguration {
	@Override
	public boolean allowRestrictPointerOperators() {
		return false;
	}

	@Override
	public boolean supportAlignOfUnaryExpression() {
		return false;
	}

	@Override
	public boolean supportAttributeSpecifiers() {
		return false;
	}

	@Override
	public boolean supportComplexNumbers() {
		return false;
	}

	@Override
	public boolean supportDeclspecSpecifiers() {
		return false;
	}

	@Override
	public boolean supportExtendedTemplateSyntax() {
		return false;
	}

	@Override
	public boolean supportGCCOtherBuiltinSymbols() {
		return false;
	}

	/**
	 * @since 6.0
	 */
	@Override
	public boolean supportGCCStyleDesignators() {
		return false;
	}

	@Override
	public boolean supportKnRC() {
		return false;
	}

	@Override
	public boolean supportLongLongs() {
		return false;
	}

	@Override
	public boolean supportMinAndMaxOperators() {
		return false;
	}

	@Override
	public boolean supportRestrictKeyword() {
		return false;
	}

	@Override
	public boolean supportStatementsInExpressions() {
		return false;
	}

	@Override
	public boolean supportTypeofUnaryExpressions() {
		return false;
	}

	/**
	 * @since 5.1
	 */
	@Override
	public boolean supportParameterInfoBlock() {
		return false;
	}

	/**
	 * @since 5.1
	 */
	@Override
	public boolean supportExtendedSizeofOperator() {
		return false;
	}

	/**
	 * @since 5.1
	 */
	@Override
	public boolean supportFunctionStyleAssembler() {
		return false;
	}

	/**
	 * @since 5.11
	 */
	@Override
	public boolean supportUserDefinedLiterals() {
		return true;
	}

	@Override
	public IBuiltinBindingsProvider getBuiltinBindingsProvider() {
		return new GCCBuiltinSymbolProvider(ParserLanguage.CPP, supportGCCOtherBuiltinSymbols());
	}

	/**
	 * @since 5.9
	 */
	@Override
	public Map<String, ContextSensitiveTokenType> getAdditionalContextSensitiveKeywords() {
		return Collections.emptyMap();
	}
}
