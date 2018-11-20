/*******************************************************************************
 * Copyright (c) 2002, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation
 *    Ed Swartz (Nokia)
 *    Markus Schorn (Wind River Systems)
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.cpp;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.parser.GCCKeywords;
import org.eclipse.cdt.core.parser.IToken.ContextSensitiveTokenType;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.GCCBuiltinSymbolProvider;

/**
 * Configures the parser for c++-sources as accepted by g++.
 */
public class GPPParserExtensionConfiguration extends AbstractCPPParserExtensionConfiguration {
	private static GPPParserExtensionConfiguration sInstance = new GPPParserExtensionConfiguration();

	/**
	 * @since 5.1
	 */
	public static GPPParserExtensionConfiguration getInstance() {
		return sInstance;
	}

	@Override
	public boolean allowRestrictPointerOperators() {
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
	public boolean supportExtendedTemplateSyntax() {
		return true;
	}

	@Override
	public boolean supportMinAndMaxOperators() {
		return true;
	}

	@Override
	public boolean supportStatementsInExpressions() {
		return true;
	}

	@Override
	public boolean supportComplexNumbers() {
		return true;
	}

	@Override
	public boolean supportRestrictKeyword() {
		return true;
	}

	@Override
	public boolean supportLongLongs() {
		return true;
	}

	@Override
	public boolean supportKnRC() {
		return false;
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
	public boolean supportGCCStyleDesignators() {
		return true;
	}

	@Override
	public IBuiltinBindingsProvider getBuiltinBindingsProvider() {
		return new GCCBuiltinSymbolProvider(ParserLanguage.CPP, true);
	}

	@Override
	public Map<String, ContextSensitiveTokenType> getAdditionalContextSensitiveKeywords() {
		Map<String, ContextSensitiveTokenType> result = new HashMap<>(super.getAdditionalContextSensitiveKeywords());
		result.put(GCCKeywords.__FINAL, ContextSensitiveTokenType.FINAL);
		return result;
	}
}
