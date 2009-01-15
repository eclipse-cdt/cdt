/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;

/**
 * Visitor to resolve ast ambiguities in the right order, which is simply a depth
 * first traversal.
 */
public final class CASTAmbiguityResolver extends ASTVisitor {
	public CASTAmbiguityResolver() {
		super(false);
		shouldVisitAmbiguousNodes= true;
	}

	@Override
	public int visit(ASTAmbiguousNode astAmbiguousNode) {
		astAmbiguousNode.resolveAmbiguity(this);
		return PROCESS_SKIP;
	}
}
