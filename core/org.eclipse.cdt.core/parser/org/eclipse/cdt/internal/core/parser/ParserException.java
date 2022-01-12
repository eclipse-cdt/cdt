/*******************************************************************************
 * Copyright (c) 2001, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Rational Software - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

public class ParserException extends Exception {
	private static final long serialVersionUID = -1589821762220079641L;

	public ParserException(String msg) {
		super(msg);
	}
}
