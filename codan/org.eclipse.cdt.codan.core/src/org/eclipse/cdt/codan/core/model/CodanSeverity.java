/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import org.eclipse.core.resources.IMarker;

/**
 * 
 * Represents Severity of the codan problem. It is directly mapped to markers
 * severity.
 * 
 */
public enum CodanSeverity {
	/**
	 * Info severity
	 */
	Info(IMarker.SEVERITY_INFO),
	/**
	 * Warning severity
	 */
	Warning(IMarker.SEVERITY_WARNING),
	/**
	 * Error severity
	 */
	Error(IMarker.SEVERITY_ERROR);
	private int value;

	private CodanSeverity(int value) {
		this.value = value;
	}

	/**
	 * @return int value of the severity
	 */
	public int intValue() {
		return value;
	}

	/**
	 * @return array of string value for all severities
	 */
	public static String[] stringValues() {
		CodanSeverity[] values = values();
		String[] svalues = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			CodanSeverity sev = values[i];
			svalues[i] = sev.toString();
		}
		return svalues;
	}
}
