/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.cdt.make.core.makefile.gnu.ITerminal;

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
