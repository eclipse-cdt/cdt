/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
package org.eclipse.cdt.core.dom.lrparser.action.cpp;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.lrparser.ISecondaryParser;
import org.eclipse.cdt.core.dom.lrparser.action.ISecondaryParserFactory;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenStream;

/**
 * Secondary parsers for resolving ambiguities specific to C++.
 *
 * @author Mike Kucera
 */
public interface ICPPSecondaryParserFactory extends ISecondaryParserFactory {

	ISecondaryParser<ICPPASTTemplateParameter> getTemplateTypeParameterParser(ITokenStream stream,
			Map<String, String> properties);

	ISecondaryParser<IASTDeclarator> getNoFunctionDeclaratorParser(ITokenStream stream, Map<String, String> properties);

}
