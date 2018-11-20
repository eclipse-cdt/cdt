/*******************************************************************************
 * Copyright (c) 2011, 2012 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
