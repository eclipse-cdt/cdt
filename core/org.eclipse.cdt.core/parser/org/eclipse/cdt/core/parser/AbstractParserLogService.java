/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
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

	@Override
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

	@Override
	public boolean isTracing() {
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
