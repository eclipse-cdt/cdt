/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
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
		StringBuilder sb = new StringBuilder(GNUMakefileConstants.CONDITIONAL_IFEQ);
		sb.append(' ').append(getConditional());
		return sb.toString();
	}

}
