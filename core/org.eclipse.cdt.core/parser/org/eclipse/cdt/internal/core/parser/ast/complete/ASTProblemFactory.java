/*******************************************************************************
 * Copyright (c) 2001, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rational Software - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.complete;

import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.internal.core.parser.problem.BaseProblemFactory;
import org.eclipse.cdt.internal.core.parser.problem.IProblemFactory;

/**
 * @author jcamelon
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ASTProblemFactory extends BaseProblemFactory implements IProblemFactory
{
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.problem.IProblemFactory#createProblem(int, int, int, int, char[], java.util.Map, boolean, boolean)
	 */
	public IProblem createProblem(int id, int start, int end, int line,
			char[] file, char[] arg, boolean warn, boolean error) {

		if( checkBitmask( id, IProblem.INTERNAL_RELATED ) )  
			return createInternalProblem( id, start, end, line, file, arg, warn, error );		
		
		if ( checkBitmask( id, IProblem.SEMANTICS_RELATED ) )
			return super.createProblem(
				id,
				start,
				end,
				line,
				file,
				arg,
				warn,
				error);
				
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.problem.IProblemFactory#getRequiredAttributesForId(int)
	 */
	public String getRequiredAttributesForId(int id) {
		switch (id)
		{
			case IProblem.SEMANTIC_UNIQUE_NAME_PREDEFINED :
			case IProblem.SEMANTIC_NAME_NOT_FOUND:
			case IProblem.SEMANTIC_AMBIGUOUS_LOOKUP:
				return IProblem.A_SYMBOL_NAME; 
			case IProblem.SEMANTIC_INVALID_TYPE:
				return IProblem.A_TYPE_NAME;
			case IProblem.SEMANTIC_INVALID_USING: 
				return IProblem.A_NAMESPACE_NAME;
			case IProblem.SEMANTIC_NAME_NOT_PROVIDED:
				return null;
		}
		return null;
	}
}
