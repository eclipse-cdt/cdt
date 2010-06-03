/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.core.parser;
 
public abstract class AbstractParserLogService implements IParserLogService {

	public void traceLog(String message) {
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
