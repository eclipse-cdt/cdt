/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.dom.parser.upc;

import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.IParserActionTokenProvider;
import org.eclipse.cdt.core.dom.lrparser.action.ISecondaryParserFactory;
import org.eclipse.cdt.internal.core.dom.parser.upc.UPCExpressionParser;
import org.eclipse.cdt.internal.core.dom.parser.upc.UPCNoCastExpressionParser;
import org.eclipse.cdt.internal.core.dom.parser.upc.UPCSizeofExpressionParser;

public class UPCSecondaryParserFactory implements ISecondaryParserFactory{

	private static final UPCSecondaryParserFactory DEFAULT_INSTANCE = new UPCSecondaryParserFactory();
	
	public static UPCSecondaryParserFactory getDefault() {
		return DEFAULT_INSTANCE;
	}

	public IParser getExpressionParser(IParserActionTokenProvider parser) {
		return new UPCExpressionParser(parser);
	}

	public IParser getNoCastExpressionParser(IParserActionTokenProvider parser) {
		return new UPCNoCastExpressionParser(parser);
	}

	public IParser getSizeofExpressionParser(IParserActionTokenProvider parser) {
		return new UPCSizeofExpressionParser(parser);
	}
	
}
