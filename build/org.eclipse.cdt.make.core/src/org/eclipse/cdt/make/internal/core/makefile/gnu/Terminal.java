/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
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
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import org.eclipse.cdt.make.core.makefile.gnu.ITerminal;
import org.eclipse.cdt.make.internal.core.makefile.Directive;

public abstract class Terminal extends Directive implements ITerminal {

	public Terminal(Directive parent) {
		super(parent);
	}

	@Override
	public boolean isEndif() {
		return false;
	}

	@Override
	public boolean isEndef() {
		return false;
	}

	@Override
	public String toString() {
		return "\n"; //$NON-NLS-1$
	}
}
