/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.autotools.ui.editors.parser;

public class InvalidMacroException extends Exception {    
	
	private static final long serialVersionUID = 1L;
	private AutoconfElement badElement;

	public InvalidMacroException(String message, AutoconfElement badElement) {
		super(message);
		this.badElement = badElement;
	}

	public AutoconfElement getBadElement() {
		return this.badElement;
	}

}
