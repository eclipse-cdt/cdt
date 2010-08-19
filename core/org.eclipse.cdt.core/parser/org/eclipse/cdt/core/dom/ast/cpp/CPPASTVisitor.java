/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;

/**
 * @deprecated you can use {@link ASTVisitor}, instead.
 */
@Deprecated
public abstract class CPPASTVisitor extends ASTVisitor implements ICPPASTVisitor {
	
	/**
	 * @see ASTVisitor#ASTVisitor()
	 */
	public CPPASTVisitor() {
	}

	/**
	 * @see ASTVisitor#ASTVisitor(boolean)
	 * @since 5.1
	 */
	public CPPASTVisitor(boolean visitNodes) {
		super(visitNodes);
	}
}
