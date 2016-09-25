/*******************************************************************************
 * Copyright (c) 2015 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

import org.eclipse.cdt.internal.core.dom.parser.ASTAttributeOwner;

/**
 * C++ attribute owners.
 *
 * These implement IASTAmbiguityParent because in C++ an attribute-specifier
 * can be an alignment-specifier, and an alignment-specifier can be an
 * ambiguous node.
 */
public abstract class CPPASTAttributeOwner extends ASTAttributeOwner implements IASTAmbiguityParent {
	@Override
	public void replace(IASTNode child, IASTNode other) {
		super.replace(child, other);
	}
}