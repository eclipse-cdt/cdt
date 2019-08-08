/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings;

import org.eclipse.cdt.core.dom.ast.IParameter;

public class C99Parameter extends C99Variable implements IParameter {

	public C99Parameter() {
	}

	public C99Parameter(String name) {
		super(name);
	}

	public static C99Parameter valueOf(C99Variable var) {
		C99Parameter param = new C99Parameter(var.getName());
		param.setType(var.getType());
		param.setAuto(var.isAuto());
		param.setExtern(var.isExtern());
		param.setRegister(var.isRegister());
		param.setStatic(var.isStatic());
		return param;
	}
}
