/*******************************************************************************
 * Copyright (c) 2000 - 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

import org.eclipse.cdt.core.parser.BacktrackException;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IFilenameProvider;
import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTScope;

/**
 * @author jcamelon
 */
public interface IExpressionParser extends IFilenameProvider {

	/**
	 * Request a parse from a pre-configured parser to parse an expression.    
	 * 
	 * @param key TODO
	 * @param expression	Optional parameter representing an expression object that 
 * 						your particular IParserCallback instance would appreciate 
	 * @throws BacktrackException	thrown if the Scanner/Stream provided does not yield a valid
	 * 						expression	
	 */
	public IASTExpression expression(IASTScope scope, IASTCompletionNode.CompletionKind kind, KeywordSetKey key) throws BacktrackException, EndOfFileException;
	
}
