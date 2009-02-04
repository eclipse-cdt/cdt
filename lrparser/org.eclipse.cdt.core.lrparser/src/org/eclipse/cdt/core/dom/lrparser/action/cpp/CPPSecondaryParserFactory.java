/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.action.cpp;

import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.ISecondaryParser;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenStream;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPExpressionParser;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPNoCastExpressionParser;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPNoFunctionDeclaratorParser;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPSizeofExpressionParser;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPTemplateTypeParameterParser;

public class CPPSecondaryParserFactory implements ICPPSecondaryParserFactory {

	
	private static final CPPSecondaryParserFactory DEFAULT_INSTANCE = new CPPSecondaryParserFactory();
	
	public static CPPSecondaryParserFactory getDefault() {
		return DEFAULT_INSTANCE;
	}
	
	
	public ISecondaryParser<ICPPASTTemplateParameter> getTemplateTypeParameterParser(ITokenStream stream, Set<IParser.Options> options) {
		return new CPPTemplateTypeParameterParser(stream, options);
	}
	
	public ISecondaryParser<IASTDeclarator> getNoFunctionDeclaratorParser(ITokenStream stream, Set<IParser.Options> options) {
		return new CPPNoFunctionDeclaratorParser(stream, options); 
	}

	public ISecondaryParser<IASTExpression> getExpressionParser(ITokenStream stream, Set<IParser.Options> options) {
		return new CPPExpressionParser(stream, options);
	}

	public ISecondaryParser<IASTExpression> getNoCastExpressionParser(ITokenStream stream, Set<IParser.Options> options) {
		return new CPPNoCastExpressionParser(stream, options);
	}

	public ISecondaryParser<IASTExpression> getSizeofExpressionParser(ITokenStream stream, Set<IParser.Options> options) {
		return new CPPSizeofExpressionParser(stream, options);
	}
	
}
