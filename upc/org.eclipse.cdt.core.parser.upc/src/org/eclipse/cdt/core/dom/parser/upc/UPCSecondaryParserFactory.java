/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
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

package org.eclipse.cdt.core.dom.parser.upc;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.lrparser.ISecondaryParser;
import org.eclipse.cdt.core.dom.lrparser.action.ISecondaryParserFactory;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenStream;
import org.eclipse.cdt.internal.core.dom.parser.upc.UPCExpressionParser;
import org.eclipse.cdt.internal.core.dom.parser.upc.UPCNoCastExpressionParser;
import org.eclipse.cdt.internal.core.dom.parser.upc.UPCSizeofExpressionParser;

public class UPCSecondaryParserFactory implements ISecondaryParserFactory {

	private static final UPCSecondaryParserFactory DEFAULT_INSTANCE = new UPCSecondaryParserFactory();

	public static UPCSecondaryParserFactory getDefault() {
		return DEFAULT_INSTANCE;
	}

	@Override
	public ISecondaryParser<IASTExpression> getExpressionParser(ITokenStream stream, Map<String, String> properties) {
		return new UPCExpressionParser(stream, properties);
	}

	@Override
	public ISecondaryParser<IASTExpression> getNoCastExpressionParser(ITokenStream stream,
			Map<String, String> properties) {
		return new UPCNoCastExpressionParser(stream, properties);
	}

	@Override
	public ISecondaryParser<IASTExpression> getSizeofExpressionParser(ITokenStream stream,
			Map<String, String> properties) {
		return new UPCSizeofExpressionParser(stream, properties);
	}

}
