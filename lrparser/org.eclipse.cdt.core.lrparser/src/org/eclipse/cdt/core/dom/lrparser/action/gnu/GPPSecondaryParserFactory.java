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

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.lrparser.ISecondaryParser;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenStream;
import org.eclipse.cdt.core.dom.lrparser.action.cpp.ICPPSecondaryParserFactory;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPExpressionParser;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPNoCastExpressionParser;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPNoFunctionDeclaratorParser;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPTemplateTypeParameterParser;
import org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPSizeofExpressionParser;

public class GPPSecondaryParserFactory implements ICPPSecondaryParserFactory {

	private static final GPPSecondaryParserFactory DEFAULT_INSTANCE = new GPPSecondaryParserFactory();

	public static GPPSecondaryParserFactory getDefault() {
		return DEFAULT_INSTANCE;
	}

	@Override
	public ISecondaryParser<ICPPASTTemplateParameter> getTemplateTypeParameterParser(ITokenStream stream,
			Map<String, String> properties) {
		return new CPPTemplateTypeParameterParser(stream, properties);
	}

	@Override
	public ISecondaryParser<IASTDeclarator> getNoFunctionDeclaratorParser(ITokenStream stream,
			Map<String, String> properties) {
		return new CPPNoFunctionDeclaratorParser(stream, properties);
	}

	@Override
	public ISecondaryParser<IASTExpression> getExpressionParser(ITokenStream stream, Map<String, String> properties) {
		return new CPPExpressionParser(stream, properties);
	}

	@Override
	public ISecondaryParser<IASTExpression> getNoCastExpressionParser(ITokenStream stream,
			Map<String, String> properties) {
		return new CPPNoCastExpressionParser(stream, properties);
	}

	@Override
	public ISecondaryParser<IASTExpression> getSizeofExpressionParser(ITokenStream stream,
			Map<String, String> properties) {
		return new GPPSizeofExpressionParser(stream, properties);
	}

}
