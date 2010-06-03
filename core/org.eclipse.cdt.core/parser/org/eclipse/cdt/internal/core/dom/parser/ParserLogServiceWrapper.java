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

package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.parser.AbstractParserLogService;
import org.eclipse.cdt.core.parser.IParserLogService;

public class ParserLogServiceWrapper extends AbstractParserLogService {

	private IParserLogService fDelegate;

	public ParserLogServiceWrapper(IParserLogService log) {
		fDelegate= log;
	}
	@Override
	public boolean isTracing() {
		return fDelegate.isTracing();
	}

	@Override
	public void traceLog(String message) {
		fDelegate.traceLog(message);
	}
}
