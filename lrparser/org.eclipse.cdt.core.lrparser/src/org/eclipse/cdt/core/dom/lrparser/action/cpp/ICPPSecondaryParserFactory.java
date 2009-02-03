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

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.IParserActionTokenProvider;
import org.eclipse.cdt.core.dom.lrparser.action.ISecondaryParserFactory;


/**
 * Secondary parsers for resolving ambiguities specific to C++.
 * 
 * @author Mike Kucera
 */
public interface ICPPSecondaryParserFactory extends ISecondaryParserFactory {

	IParser<ICPPASTTemplateParameter> getTemplateTypeParameterParser(IParserActionTokenProvider parser);
	
	IParser<IASTDeclarator> getNoFunctionDeclaratorParser(IParserActionTokenProvider parser);
	
}
