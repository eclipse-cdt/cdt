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

import org.eclipse.cdt.make.core.makefile.gnu.IUnExport;

public class UnExport extends Directive implements IUnExport {

	String variable;

	public UnExport(Directive parent, String var) {
		super(parent);
		variable = var;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(GNUMakefileConstants.DIRECTIVE_UNEXPORT);
		sb.append(' ').append(variable);
		return sb.toString();
	}

	@Override
	public String getVariable() {
		return variable;
	}

}
