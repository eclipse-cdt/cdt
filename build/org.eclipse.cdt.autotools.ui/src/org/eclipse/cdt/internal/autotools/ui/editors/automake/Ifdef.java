/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;


public class Ifdef extends Conditional {
    private static final String EMPTY = ""; //$NON-NLS-1$
    
	public Ifdef(Directive parent, String var) {
		super(parent, var, EMPTY, EMPTY);
	}

	@Override
	public boolean isIfdef() {
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(GNUMakefileConstants.CONDITIONAL_IFDEF);
		sb.append(' ').append(getVariable());
		return sb.toString();
	}

	public String getVariable() {
		return getConditional();
	}

}
