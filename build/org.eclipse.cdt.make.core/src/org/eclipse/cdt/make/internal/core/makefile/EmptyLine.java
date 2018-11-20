/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.core.makefile;

import org.eclipse.cdt.make.core.makefile.IEmptyLine;

public class EmptyLine extends Directive implements IEmptyLine {

	final public static char NL = '\n';
	final public static String NL_STRING = "\n"; //$NON-NLS-1$

	public EmptyLine(Directive parent) {
		super(parent);
	}

	@Override
	public String toString() {
		return NL_STRING;
	}

	public boolean equals(IEmptyLine stmt) {
		return true;
	}
}
