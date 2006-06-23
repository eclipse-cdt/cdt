/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

/**
 * @author jcamelon
 *
 */
public class DefaultLogService implements IParserLogService
{

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserLogService#traceLog(java.lang.String)
	 */
	public void traceLog(String message)
	{
		// do nothing
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserLogService#errorLog(java.lang.String)
	 */
	public void errorLog(String message)
	{
		// do nothing
	}
	
	public boolean isTracing(){
		return false;
	}

}
