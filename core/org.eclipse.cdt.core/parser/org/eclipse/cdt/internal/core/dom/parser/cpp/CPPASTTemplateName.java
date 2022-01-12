/*******************************************************************************
 * Copyright (c) 2018
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateName;

/**
 * Represents a template name.
 */
public class CPPASTTemplateName extends CPPASTName implements ICPPASTTemplateName {
	public CPPASTTemplateName(char[] name) {
		super(name);
	}

	public CPPASTTemplateName() {
		super();
	}

	@Override
	public CPPASTTemplateName copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTTemplateName copy(CopyStyle style) {
		CPPASTTemplateName copy = new CPPASTTemplateName(toCharArray() == null ? null : toCharArray().clone());
		return copy(copy, style);
	}
}
