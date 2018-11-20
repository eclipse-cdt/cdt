/*******************************************************************************
 * Copyright (c) 2004, 2014 Wind River Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;

public abstract class ASTSpecificNameVisitor extends ASTNameVisitor {
	private final String fSearchForName;

	public ASTSpecificNameVisitor(String name) {
		super(null);
		fSearchForName = name;
	}

	@Override
	public final int visitName(IASTName name) {
		if (name instanceof ICPPASTTemplateId || name instanceof ICPPASTQualifiedName)
			return PROCESS_CONTINUE;

		String nameStr = name.toString();
		if (nameStr != null) {
			final int len = nameStr.length();
			final int searchForLen = fSearchForName.length();
			if (len == searchForLen) {
				if (nameStr.equals(fSearchForName)) {
					return visitName(name, false);
				}
			} else if (len == searchForLen + 1) {
				if (nameStr.charAt(0) == '~' && nameStr.endsWith(fSearchForName)) {
					return visitName(name, true);
				}
			}
		}
		return ASTVisitor.PROCESS_CONTINUE;
	}

	protected abstract int visitName(IASTName name, boolean isDestructor);
}
