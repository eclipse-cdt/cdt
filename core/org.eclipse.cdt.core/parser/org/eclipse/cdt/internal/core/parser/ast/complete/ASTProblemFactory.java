/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.HashMap;
import java.util.Map;

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
	
	protected static final Map errorMessages;
	static {
		errorMessages = new HashMap();
		errorMessages.put( new Integer( IProblem.SEMANTIC_UNIQUE_NAME_PREDEFINED),"Attempt to introduce unique symbol failed : ");
		errorMessages.put( new Integer( IProblem.SEMANTIC_NAME_NOT_FOUND), "Attempt to use symbol failed : ");
		errorMessages.put( new Integer( IProblem.SEMANTIC_NAME_NOT_PROVIDED), "Name not provided.");
	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.problem.BaseProblemFactory#createMessage(int, java.util.Map, int, char[])
	 */
	public String createMessage(int id, Map arguments, int lineNumber,
			char[] fileName) {
		StringBuffer buffer = new StringBuffer();

		buffer.append(PROBLEM);
		buffer.append(errorMessages.get(new Integer(id)));
		switch (id)
		{
			case IProblem.SEMANTIC_UNIQUE_NAME_PREDEFINED:
			case IProblem.SEMANTIC_NAME_NOT_FOUND:
				buffer.append(arguments.get(IProblem.A_SYMBOL_NAME));
				break;
			case IProblem.SEMANTIC_NAME_NOT_PROVIDED:
				break;
			default :
				return null;
		}

		if( fileName != null )
		{
			buffer.append( IN_FILE );
			buffer.append(fileName);
		}
		
		if( lineNumber > 0 )
		{
			buffer.append( ON_LINE );
			buffer.append(lineNumber);
		}
		return buffer.toString();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.problem.IProblemFactory#createProblem(int, int, int, int, char[], java.util.Map, boolean, boolean)
	 */
	public IProblem createProblem(int id, int start, int end, int line,
			char[] file, Map arguments, boolean warn, boolean error) {

		if( checkBitmask( id, IProblem.INTERNAL_RELATED ) )  
			return createInternalProblem( id, start, end, line, file, arguments, warn, error );		
		
		if ( checkBitmask( id, IProblem.SEMANTICS_RELATED ) )
			return super.createProblem(
				id,
				start,
				end,
				line,
				file,
				createMessage(id, arguments, line, file),
				arguments,
				warn,
				error);
				
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.problem.IProblemFactory#getRequiredAttributesForId(int)
	 */
	public String[] getRequiredAttributesForId(int id) {
		String [] result = new String[1]; 
		switch (id)
		{
			case IProblem.SEMANTIC_UNIQUE_NAME_PREDEFINED :
			case IProblem.SEMANTIC_NAME_NOT_FOUND:
			case IProblem.SEMANTIC_AMBIGUOUS_LOOKUP:
				result[0] = IProblem.A_SYMBOL_NAME; 
				break;
			case IProblem.SEMANTIC_INVALID_TYPE:
				result[0] = IProblem.A_TYPE_NAME;
				break;
			case IProblem.SEMANTIC_INVALID_USING: 
				result[0] = IProblem.A_NAMESPACE_NAME;
				break;
			case IProblem.SEMANTIC_NAME_NOT_PROVIDED:
				result = new String[0];
				break;
		}
		return result;
	}
}
