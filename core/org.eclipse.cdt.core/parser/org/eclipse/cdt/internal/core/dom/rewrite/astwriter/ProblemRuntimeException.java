/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;

/**
 * This exception is thrown if a problem node is passed to the ASTWriter. The exception 
 * contains the <code>IASTProblemHolder</code> that was passed to the writer.
 * 
 * @see IASTProblem
 * @author Emanuel Graf IFS
 */
public class ProblemRuntimeException extends RuntimeException {
	private static final long serialVersionUID = -3661425564246498786L;
	private IASTProblemHolder problem;

	public ProblemRuntimeException(IASTProblemHolder statement) {
		problem = statement;
	}
	
	public IASTProblemHolder getProblem() {
		return problem;
	}
}
