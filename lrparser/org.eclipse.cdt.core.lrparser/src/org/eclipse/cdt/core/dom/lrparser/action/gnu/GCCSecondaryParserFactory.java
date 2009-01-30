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

import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.IParserActionTokenProvider;
import org.eclipse.cdt.core.dom.lrparser.action.ISecondaryParserFactory;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.C99ExpressionParser;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.C99NoCastExpressionParser;
import org.eclipse.cdt.internal.core.dom.lrparser.gcc.GCCSizeofExpressionParser;

public class GCCSecondaryParserFactory implements ISecondaryParserFactory {

	public static final GCCSecondaryParserFactory DEFAULT_INSTANCE = new GCCSecondaryParserFactory();
	
	public static GCCSecondaryParserFactory getDefault() {
		return DEFAULT_INSTANCE;
	}
	
	
	public IParser getExpressionParser(IParserActionTokenProvider parser) {
		return new C99ExpressionParser(parser); 
	}

	public IParser getNoCastExpressionParser(IParserActionTokenProvider parser) {
		return new C99NoCastExpressionParser(parser);
	}
	
	public IParser getSizeofExpressionParser(IParserActionTokenProvider parser) {
		return new GCCSizeofExpressionParser(parser);
	}
}