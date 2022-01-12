/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.indexer;

import org.eclipse.cdt.core.parser.AbstractParserLogService;

/**
 * Implementation of parser log service logging to stdout.
 */
public class StdoutLogService extends AbstractParserLogService {
	@Override
	public void errorLog(String message) {
		System.out.println("Parser Error Trace: " + message); //$NON-NLS-1$
		System.out.flush();
	}

	@Override
	public boolean isTracing() {
		return true;
	}

	@Override
	public boolean isTracingExceptions() {
		return true;
	}

	@Override
	public void traceLog(String message) {
		System.out.println("Parser Trace: " + message); //$NON-NLS-1$
		System.out.flush();
	}
}
