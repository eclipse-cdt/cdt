/*******************************************************************************
 * Copyright (c) 2011, 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.autotools.ui.editors.parser;

public class InvalidMacroArgumentException extends Exception {

	public InvalidMacroArgumentException(String message) {
		super(message);
	}

	private static final long serialVersionUID = 1L;

}
