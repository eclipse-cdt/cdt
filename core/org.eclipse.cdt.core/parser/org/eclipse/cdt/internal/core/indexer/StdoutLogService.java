/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.indexer;

import org.eclipse.cdt.core.parser.AbstractParserLogService;

/**
 * @author crecoskie
 *
 */
public class StdoutLogService extends AbstractParserLogService{

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.AbstractParserLogService#errorLog(java.lang.String)
	 */
	@Override
	public void errorLog(String message) {
		System.out.println("Parser Error Trace: " + message); //$NON-NLS-1$
		System.out.flush();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.AbstractParserLogService#isTracing()
	 */
	@Override
	public boolean isTracing() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.AbstractParserLogService#isTracingExceptions()
	 */
	@Override
	public boolean isTracingExceptions() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.AbstractParserLogService#traceLog(java.lang.String)
	 */
	@Override
	public void traceLog(String message) {
		System.out.println("Parser Trace: " + message); //$NON-NLS-1$
		System.out.flush();
	}

}
