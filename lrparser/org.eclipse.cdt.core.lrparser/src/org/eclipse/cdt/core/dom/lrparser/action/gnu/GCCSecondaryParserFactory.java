/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.action.gnu;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.lrparser.ISecondaryParser;
import org.eclipse.cdt.core.dom.lrparser.action.ISecondaryParserFactory;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenStream;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.C99ExpressionParser;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.C99NoCastExpressionParser;
import org.eclipse.cdt.internal.core.dom.lrparser.gcc.GCCSizeofExpressionParser;

public class GCCSecondaryParserFactory implements ISecondaryParserFactory {

	public static final GCCSecondaryParserFactory DEFAULT_INSTANCE = new GCCSecondaryParserFactory();

	public static GCCSecondaryParserFactory getDefault() {
		return DEFAULT_INSTANCE;
	}

	@Override
	public ISecondaryParser<IASTExpression> getExpressionParser(ITokenStream stream, Map<String, String> properties) {
		return new C99ExpressionParser(stream, properties);
	}

	@Override
	public ISecondaryParser<IASTExpression> getNoCastExpressionParser(ITokenStream stream,
			Map<String, String> properties) {
		return new C99NoCastExpressionParser(stream, properties);
	}

	@Override
	public ISecondaryParser<IASTExpression> getSizeofExpressionParser(ITokenStream stream,
			Map<String, String> properties) {
		return new GCCSizeofExpressionParser(stream, properties);
	}
}