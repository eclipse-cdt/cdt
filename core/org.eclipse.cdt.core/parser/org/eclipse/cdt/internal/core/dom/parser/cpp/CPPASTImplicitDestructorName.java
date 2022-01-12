/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class CPPASTImplicitDestructorName extends CPPASTImplicitName implements IASTImplicitDestructorName {
	private final IASTName constructionPoint;

	public CPPASTImplicitDestructorName(char[] name, IASTNode parent, IASTName constructionPoint) {
		super(name, parent);
		this.constructionPoint = constructionPoint;
		setPropertyInParent(IASTImplicitDestructorNameOwner.IMPLICIT_DESTRUCTOR_NAME);
	}

	@Override
	public IASTName getConstructionPoint() {
		return constructionPoint;
	}
}
