/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.internal.core.parser.util;

import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;

/**
 * @author ddaoust
 */
public class TraceUtil {
	public static void outputTrace(IParserLogService log, String preface, IProblem problem, String first, String second, String third ) {
		if( log.isTracing() ){
			StringBuffer buffer = new StringBuffer();
			buffer.append( preface != null ? preface : "" );
			buffer.append( problem != null ? problem.getMessage() : "" );
			buffer.append( first   != null ? first   : "" );
			buffer.append( second  != null ? second  : "" );
			buffer.append( third   != null ? third   : "" );
			log.traceLog( buffer.toString() );
		}
	}
	public static void outputTrace(IParserLogService log, String preface, IProblem problem, int first, String second, int third ) {
		if( log.isTracing() ){
			StringBuffer buffer = new StringBuffer();
			buffer.append( preface != null ? preface : "" );
			buffer.append( problem != null ? problem.getMessage() : "" );
			buffer.append( Integer.toString( first ) );
			buffer.append( second  != null ? second  : "" );
			buffer.append( Integer.toString( third ) );
			log.traceLog( buffer.toString() );
		}	
	}
	public static void outputTrace(IParserLogService log, String preface, String first, String second, String third ) {
		if( log.isTracing() ){
			StringBuffer buffer = new StringBuffer();
			buffer.append( preface != null ? preface : "" );
			buffer.append( first   != null ? first   : "" );
			buffer.append( second  != null ? second  : "" );
			buffer.append( third   != null ? third   : "" );
			log.traceLog( buffer.toString() );
		}
	}
	public static void outputTrace(IParserLogService log, String preface) {
		if( log.isTracing() ){
			StringBuffer buffer = new StringBuffer();
			buffer.append( preface != null ? preface : "" );
			log.traceLog( buffer.toString() );
		}				
	}
}
