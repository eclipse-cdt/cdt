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
package org.eclipse.cdt.internal.core.parser.problem;

import java.util.Map;

import org.eclipse.cdt.core.parser.IProblem;

/**
 * @author jcamelon
 *
 */
public class Problem implements IProblem {

	private final Map arguments;
	private final int id;
	private final int sourceStart;
	private final int sourceEnd;
	private final int lineNumber;

	private final boolean isError;
	private final boolean isWarning;	
	private final char[] originatingFileName;
	private final String message;

	public Problem( int id, int start, int end, int line, char [] file, String message, Map arguments, boolean warn, boolean error )
	{
		this.id = id;
		this.sourceStart = start;
		this.sourceEnd = end;
		this.lineNumber = line;
		this.originatingFileName = file;
		this.message = message;
		this.arguments = arguments;
		this.isWarning = warn;
		this.isError = error;
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IProblem#getID()
	 */
	public int getID() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IProblem#getOriginatingFileName()
	 */
	public char[] getOriginatingFileName() {
		return originatingFileName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IProblem#getSourceEnd()
	 */
	public int getSourceEnd() {
		return sourceEnd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IProblem#getSourceLineNumber()
	 */
	public int getSourceLineNumber() {
		return lineNumber;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IProblem#getSourceStart()
	 */
	public int getSourceStart() {
		return sourceStart;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IProblem#isError()
	 */
	public boolean isError() {
		return isError;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IProblem#isWarning()
	 */
	public boolean isWarning() {
		return isWarning;
	}


	public String getMessage()
	{
		return message;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IProblem#checkCategory(int)
	 */
	public boolean checkCategory(int bitmask) {
		return ((id & bitmask) != 0 );
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IProblem#getArguments()
	 */
	public Map getArguments() {
		return arguments;
	}

}
