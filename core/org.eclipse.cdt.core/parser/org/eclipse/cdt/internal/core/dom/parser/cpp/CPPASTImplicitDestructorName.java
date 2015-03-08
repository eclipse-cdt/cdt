/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTNode;


public class CPPASTImplicitDestructorName extends CPPASTImplicitName implements IASTImplicitDestructorName {
	private final IASTImplicitName constructionPoint;

	public CPPASTImplicitDestructorName(char[] name, IASTNode parent, IASTImplicitName constructionPoint) {
		super(name, parent);
		this.constructionPoint = constructionPoint;
		setPropertyInParent(IASTImplicitDestructorNameOwner.IMPLICIT_DESTRUCTOR_NAME);
	}

	@Override
	public IASTImplicitName getConstructionPoint() {
		return constructionPoint;
	}
}
