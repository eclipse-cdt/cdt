/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.examples.checkers.cppcheck;

import org.eclipse.cdt.core.IMarkerGenerator;

enum Severity {
	ERROR(IMarkerGenerator.SEVERITY_ERROR_RESOURCE, "error"), //$NON-NLS-1$
	WARNING(IMarkerGenerator.SEVERITY_WARNING, "warning"), //$NON-NLS-1$
	STYLE(IMarkerGenerator.SEVERITY_INFO, "style"); //$NON-NLS-1$

	private final int code;
	private final String text;

	private Severity(int code, String text) {
		this.code = code;
		this.text = text;
	}

	static int findSeverityCode(String text) {
		for (Severity severity : values()) {
			if (severity.text.equals(text)) {
				return severity.code;
			}
		}
		return STYLE.code;
	}

	static Severity findSeverity(int code) {
		for (Severity severity : values()) {
			if (severity.code == code) {
				return severity;
			}
		}
		return STYLE;
	}

	@Override
	public String toString() {
		return text;
	}
}