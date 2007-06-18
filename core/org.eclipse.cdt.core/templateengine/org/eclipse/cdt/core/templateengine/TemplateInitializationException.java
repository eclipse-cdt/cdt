/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
