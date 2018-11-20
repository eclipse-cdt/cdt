/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.templateengine;

/**
 * Represents an exception in initializing a template. Typically this will be caused
 * by an I/O or XML parsing failure.
 */
public class TemplateInitializationException extends Exception {
	private static final long serialVersionUID = -8138820172406447119L;

	public TemplateInitializationException() {
		super();
	}

	public TemplateInitializationException(String message) {
		super(message);
	}

	public TemplateInitializationException(String message, Throwable cause) {
		super(message);
		initCause(cause);
	}
}
