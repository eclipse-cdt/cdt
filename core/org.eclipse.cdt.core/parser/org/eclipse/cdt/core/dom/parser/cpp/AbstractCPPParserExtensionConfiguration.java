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

package org.eclipse.cdt.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.GCCBuiltinSymbolProvider;


/**
 * Abstract C++ parser extension configuration to help model C++ dialects.
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
public abstract class AbstractCPPParserExtensionConfiguration implements ICPPParserExtensionConfiguration {

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#allowRestrictPointerOperators()
	 */
	public boolean allowRestrictPointerOperators() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportAlignOfUnaryExpression()
	 */
	public boolean supportAlignOfUnaryExpression() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportAttributeSpecifiers()
	 */
	public boolean supportAttributeSpecifiers() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportComplexNumbers()
	 */
	public boolean supportComplexNumbers() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportDeclspecSpecifiers()
	 */
	public boolean supportDeclspecSpecifiers() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportExtendedTemplateSyntax()
	 */
	public boolean supportExtendedTemplateSyntax() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportGCCOtherBuiltinSymbols()
	 */
	public boolean supportGCCOtherBuiltinSymbols() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportKnRC()
	 */
	public boolean supportKnRC() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportLongLongs()
	 */
	public boolean supportLongLongs() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportMinAndMaxOperators()
	 */
	public boolean supportMinAndMaxOperators() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportRestrictKeyword()
	 */
	public boolean supportRestrictKeyword() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportStatementsInExpressions()
	 */
	public boolean supportStatementsInExpressions() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#supportTypeofUnaryExpressions()
	 */
	public boolean supportTypeofUnaryExpressions() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration#getBuiltinBindingsProvider()
	 */
	public IBuiltinBindingsProvider getBuiltinBindingsProvider() {
		if (supportGCCOtherBuiltinSymbols()) {
			return new GCCBuiltinSymbolProvider(ParserLanguage.CPP);
		}
		return null;
	}

}
