/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast;

import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.ast.IASTMacro;


/**
 * @author jcamelon
 *
 */
public class BaseASTFactory  {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IASTFactory#createMacro(java.lang.String, int, int, int)
	 */
	public IASTMacro createMacro(String name, int startingOffset, int endingOffset, int nameOffset) {
		IASTMacro m = new ASTMacro( name );
		m.setStartingOffset( startingOffset );
		m.setEndingOffset( endingOffset );
		m.setElementNameOffset( nameOffset );
		return m;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IASTFactory#createInclusion(java.lang.String, java.lang.String, boolean)
	 */
	public IASTInclusion createInclusion(String name, String fileName, boolean local, int startingOffset, int endingOffset, int nameOffset) {
		IASTInclusion inclusion = new ASTInclusion( name, fileName, local );
		inclusion.setStartingOffset( startingOffset );
		inclusion.setEndingOffset( endingOffset );
		inclusion.setElementNameOffset( nameOffset );
		return inclusion;
	}


}
