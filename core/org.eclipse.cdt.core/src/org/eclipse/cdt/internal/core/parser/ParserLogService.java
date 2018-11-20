/*******************************************************************************
 * Copyright (c) 2002, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Mike Kucera (IBM)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.parser.AbstractParserLogService;
import org.eclipse.cdt.internal.core.model.DebugLogConstants;
import org.eclipse.cdt.internal.core.model.Util;
import org.eclipse.cdt.internal.core.util.Canceler;
import org.eclipse.cdt.internal.core.util.ICancelable;
import org.eclipse.cdt.internal.core.util.ICanceler;
import org.eclipse.core.runtime.Platform;

/**
 * @author jcamelon
 */
public class ParserLogService extends AbstractParserLogService implements ICanceler {
	private final DebugLogConstants topic;
	private final boolean fIsTracing;
	private final boolean fIsTracingExceptions;
	private final ICanceler fCanceler;

	public ParserLogService(DebugLogConstants constant) {
		this(constant, new Canceler());
	}

	public ParserLogService(DebugLogConstants constant, ICanceler canceler) {
		if (canceler == null)
			throw new NullPointerException();
		topic = constant;
		if (CCorePlugin.getDefault() == null) {
			fIsTracingExceptions = false;
			fIsTracing = false;
		} else {
			fIsTracingExceptions = Util.PARSER_EXCEPTIONS;
			fIsTracing = Util.isActive(topic);
		}
		fCanceler = canceler;
	}

	@Override
	public void traceLog(String message) {
		Util.debugLog(message, topic);
	}

	@Override
	public boolean isTracing(String option) {
		return Boolean.parseBoolean(Platform.getDebugOption(option));
	}

	@Override
	public void traceLog(String option, String message) {
		if (isTracing(option))
			System.out.println(message);
	}

	@Override
	public void errorLog(String message) {
		Util.log(message, ICLogConstants.CDT);
	}

	@Override
	public boolean isTracing() {
		return fIsTracing;
	}

	@Override
	public boolean isTracingExceptions() {
		return fIsTracingExceptions;
	}

	@Override
	public void setCancelable(ICancelable cancelable) {
		fCanceler.setCancelable(cancelable);
	}

	@Override
	public void setCanceled(boolean value) {
		fCanceler.setCanceled(value);
	}

	@Override
	public boolean isCanceled() {
		return fCanceler.isCanceled();
	}
}
