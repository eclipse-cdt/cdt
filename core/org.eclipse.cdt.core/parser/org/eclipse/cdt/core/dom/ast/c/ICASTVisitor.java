/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;


/**
 * Interface for visitors to visit c-specific nodes.
 * @since 5.0
 */
public interface ICASTVisitor {

	/**
	 * Visits a designator.
	 * @return {@link ASTVisitor#PROCESS_CONTINUE}, {@link ASTVisitor#PROCESS_SKIP} or {@link ASTVisitor#PROCESS_ABORT}
	 */
	int visit(ICASTDesignator designator);

	/**
	 * Visits a designator.
	 * @return {@link ASTVisitor#PROCESS_CONTINUE} or {@link ASTVisitor#PROCESS_ABORT}
	 */
	int leave(ICASTDesignator designator);
}
