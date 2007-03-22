/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ParseError;

/**
 * Interface for an AST source code parser.
 * <p>
 * This interface is not intended to be implemented directly.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 * 
 * @author jcamelon
 */
public interface ISourceCodeParser {

    /**
     * Compute an abstract syntax tree (AST).
     * @return the AST, should not return <code>null</code>
     * 
     * @throws ParseError  if parsing has been cancelled or for other reasons
     */
    public IASTTranslationUnit parse();

	/**
	 * Cancel the parsing.
	 */
    public void cancel();

    /**
     * Check whether there were errors.
     * @return <code>true</code> if there were errors
     */
    public boolean encounteredError();

    /**
     * Compute an {@link IASTCompletionNode} for code completion.
     * @return a completion node or <code>null</code> if none could be computed
     * 
     * @throws ParseError  if parsing has been cancelled or for other reasons
     */
    public IASTCompletionNode getCompletionNode();
    
}
