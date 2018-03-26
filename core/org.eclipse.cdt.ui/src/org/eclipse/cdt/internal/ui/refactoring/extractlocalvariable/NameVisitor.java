/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Institute for Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractlocalvariable;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class NameVisitor extends ASTVisitor {
	List<IASTName> names;
	{
		shouldVisitNames = true;
	}

	NameVisitor() {
		names = new ArrayList<IASTName>();
	}

	@Override
	public int visit(IASTName name) {
		names.add(name);
		return PROCESS_CONTINUE;
	}

	public List<IASTName> findNames(IASTNode node) {
		node.accept(this);
		return names;
	}
}