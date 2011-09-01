/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.core.parser;

import org.eclipse.cdt.internal.core.dom.parser.ParserLogServiceWrapper;
 
public abstract class AbstractParserLogService implements IParserLogService {

	/**
	 * @since 5.4
	 */
	public static AbstractParserLogService convert(IParserLogService log) {
		if (log instanceof AbstractParserLogService)
			return (AbstractParserLogService) log;
		return new ParserLogServiceWrapper(log);
	}
	
	public void traceLog(String message) {
	}

	/**
	 * @param traceOption an option as defined in the .options file.
	 * @since 5.4
	 */
	public boolean isTracing(String traceOption) {
		return isTracing();
	}
	
	/**
	 * @param traceOption an option as defined in the .options file.
	 * @since 5.4
	 */
	public void traceLog(String traceOption, String message) {
		traceLog(message);
	}

	public void errorLog(String message) {
	}

	public boolean isTracing(){
		return false;
	}
	
	public boolean isTracingExceptions() {
		return false;
	}
	
	public void traceException(Throwable th) {
		if (isTracingExceptions()) {
			th.printStackTrace();
		}
	}
}
