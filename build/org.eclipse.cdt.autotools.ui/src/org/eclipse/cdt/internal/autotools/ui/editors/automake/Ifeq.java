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


public class Ifeq extends Conditional {

	public Ifeq(Directive parent, String cond) {
		super(parent, cond);
	}

	@Override
	public boolean isIfeq() {
		return true;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(GNUMakefileConstants.CONDITIONAL_IFEQ);
		sb.append(' ').append(getConditional());
		return sb.toString();
	}

}
