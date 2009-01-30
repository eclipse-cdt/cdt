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

import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.IParserActionTokenProvider;
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
	
	
	public IParser getTemplateTypeParameterParser(IParserActionTokenProvider parser) {
		return new CPPTemplateTypeParameterParser(parser);
	}
	
	public IParser getNoFunctionDeclaratorParser(IParserActionTokenProvider parser) {
		return new CPPNoFunctionDeclaratorParser(parser); 
	}

	public IParser getExpressionParser(IParserActionTokenProvider parser) {
		return new CPPExpressionParser(parser);
	}

	public IParser getNoCastExpressionParser(IParserActionTokenProvider parser) {
		return new CPPNoCastExpressionParser(parser);
	}

	public IParser getSizeofExpressionParser(IParserActionTokenProvider parser) {
		return new CPPSizeofExpressionParser(parser);
	}
	
}
