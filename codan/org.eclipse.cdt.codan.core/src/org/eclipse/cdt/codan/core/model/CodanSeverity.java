/*******************************************************************************
 * Copyright (c) 2009, 2012 Alena Laskavaia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *    IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import org.eclipse.core.resources.IMarker;

/**
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
			svalues[i] = sev.toTranslatableString();
		}
		return svalues;
	}

	/**
	 * @param intValue
	 * @return value of severity by its integer value
	 * @since 2.0
	 */
	public static CodanSeverity valueOf(int intValue) {
		if (intValue == IMarker.SEVERITY_INFO)
			return Info;
		if (intValue == IMarker.SEVERITY_WARNING)
			return Warning;
		if (intValue == IMarker.SEVERITY_ERROR)
			return Error;
		return null;
	}

	/**
	 * @return translated string value of this CodanSeverity
	 * @since 2.0
	 */
	public String toTranslatableString() {
		switch (this) {
		case Info:
			return Messages.CodanSeverity_Info;

		case Warning:
			return Messages.CodanSeverity_Warning;

		case Error:
		default:
			return Messages.CodanSeverity_Error;
		}
	}
}
