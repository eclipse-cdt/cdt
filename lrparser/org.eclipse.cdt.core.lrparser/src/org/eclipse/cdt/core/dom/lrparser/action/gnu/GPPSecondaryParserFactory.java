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
package org.eclipse.cdt.core.dom.lrparser.action.gnu;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.IParserActionTokenProvider;
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
	
	
	public IParser<ICPPASTTemplateParameter> getTemplateTypeParameterParser(IParserActionTokenProvider parser) {
		return new CPPTemplateTypeParameterParser(parser);
	}
	
	public IParser<IASTDeclarator> getNoFunctionDeclaratorParser(IParserActionTokenProvider parser) {
		return new CPPNoFunctionDeclaratorParser(parser); 
	}

	public IParser<IASTExpression> getExpressionParser(IParserActionTokenProvider parser) {
		return new CPPExpressionParser(parser);
	}

	public IParser<IASTExpression> getNoCastExpressionParser(IParserActionTokenProvider parser) {
		return new CPPNoCastExpressionParser(parser);
	}

	public IParser<IASTExpression> getSizeofExpressionParser(IParserActionTokenProvider parser) {
		return new GPPSizeofExpressionParser(parser);
	}
	
}
