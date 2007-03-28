/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.parser.AbstractParserLogService;
import org.eclipse.cdt.internal.core.model.Util;
import org.eclipse.cdt.internal.core.model.IDebugLogConstants.DebugLogConstant;

/**
 * @author jcamelon
 *
 */
public class ParserLogService extends AbstractParserLogService
{

	final DebugLogConstant topic;
	final boolean fIsTracing;
	final boolean fIsTracingExceptions;
	
	/**
	 * @param constant
	 */
	public ParserLogService(DebugLogConstant constant) {
		topic = constant;
		if (CCorePlugin.getDefault() == null) {
			fIsTracing= fIsTracingExceptions= false;
		}
		else {
			fIsTracingExceptions= Util.PARSER_EXCEPTIONS;
			fIsTracing= Util.isActive(topic);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserLogService#traceLog(java.lang.String)
	 */
	public void traceLog(String message)
	{
		Util.debugLog( message, topic );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserLogService#errorLog(java.lang.String)
	 */
	public void errorLog(String message)
	{
		Util.log( message, ICLogConstants.CDT );
	}

	public boolean isTracing(){
		if( CCorePlugin.getDefault() == null )
			return false;
		
		return ( CCorePlugin.getDefault().isDebugging() && Util.isActive( topic ) ); 
	}
	
	public boolean isTracingExceptions() {
		return fIsTracingExceptions;
	}
}
