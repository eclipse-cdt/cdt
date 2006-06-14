/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.parser.util;

import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;

/**
 * @author ddaoust
 */
public class TraceUtil {
	public static void outputTrace(IParserLogService log, String preface, IProblem problem, String first, String second, String third ) {
		if( log.isTracing() ) {
			StringBuffer buffer = new StringBuffer();
			if( preface != null ) buffer.append( preface );
			if( problem != null ) buffer.append( problem.getMessage());
			if( first   != null ) buffer.append( first );
			if( second  != null ) buffer.append( second );
			if( third   != null ) buffer.append( third );
			log.traceLog( buffer.toString() );
		}
	}
	public static void outputTrace(IParserLogService log, String preface, IProblem problem ) {
		if( log.isTracing() ) {
			StringBuffer buffer = new StringBuffer();
			if( preface != null ) buffer.append( preface );
			if( problem != null ) buffer.append( problem.getMessage());
			log.traceLog( buffer.toString() );
		}
	}
	public static void outputTrace(IParserLogService log, String preface, IProblem problem, char[] first, String second, String third ) {
		if( log.isTracing() ) {
			StringBuffer buffer = new StringBuffer();
			if( preface != null ) buffer.append( preface );
			if( problem != null ) buffer.append( problem.getMessage());
			if( first   != null ) buffer.append( first );
			if( second  != null ) buffer.append( second );
			if( third   != null ) buffer.append( third );
			log.traceLog( buffer.toString() );
		}
	}
	public static void outputTrace(IParserLogService log, String preface, IProblem problem, int first, String second, int third ) {
		if( log.isTracing() ) {
			outputTrace(
					log, 
					preface, 
					problem, 
					Integer.toString( first ), 
					second, 
					Integer.toString( third ) );
		}
	}
	public static void outputTrace(IParserLogService log, String preface, String first, String second, String third ) {
			outputTrace(log, preface, null, first, second, third);
	}
	public static void outputTrace(IParserLogService log, String preface) {
		if( log.isTracing() ){
			if ( preface != null )
				log.traceLog( preface );
		}				
	}
	/**
	 * @param logService
	 * @param preface
	 * @param data
	 */
	public static void outputTrace(IParserLogService logService, String preface, String data) {
		if( logService.isTracing() ) {
			StringBuffer buffer = new StringBuffer();
			if( preface != null ) buffer.append( preface );
			if( data   != null ) buffer.append( data );
			logService.traceLog( buffer.toString() );
		}
	}
}
