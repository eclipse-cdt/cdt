/*******************************************************************************
 * Copyright (c) 2015 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttributeOwner;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

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