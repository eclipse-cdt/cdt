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
package org.eclipse.cdt.internal.core.parser;

import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.internal.core.model.IDebugLogConstants;
import org.eclipse.cdt.internal.core.model.Util;

/**
 * @author jcamelon
 *
 */
public class ParserLogService implements IParserLogService
{

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserLogService#traceLog(java.lang.String)
	 */
	public void traceLog(String message)
	{
		Util.debugLog( message, IDebugLogConstants.PARSER );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserLogService#errorLog(java.lang.String)
	 */
	public void errorLog(String message)
	{
		Util.log( message, ICLogConstants.CDT );
	}

}
