/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Yuan Zhang / Beth Tibbitts (IBM Research)
 *******************************************************************************/

/*
 * Created on Mar 8, 2005
 */
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;

/**
 * This subclass of ASTVisitor that allows for better control in traversing C.
 * 
 * @ref ASTVisitor
 * @author jcamelon
 */
public abstract class CASTVisitor extends ASTVisitor {
	/**
	 * Override this value in your subclass if you do wish to visit designators.
	 */
	public boolean shouldVisitDesignators = false;

	/**
	 * Function to override if you wish to visit designators in your
	 * implementation.  This does a top-down traversal.
	 * 
	 * @param designator
	 * @return
	 */
	public int visit(ICASTDesignator designator) {
		return PROCESS_CONTINUE;
	}
	/**
	 * Function to override if you wish to visit designators in your
	 * implementation: this does a bottom-up traversal.
	 * @param designator
	 * @return
	 */
	public int leave(ICASTDesignator designator) {
		return PROCESS_CONTINUE;
	}
}
