/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
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
	 * implementation.
	 * 
	 * @param designator
	 * @return
	 */
	public int visit(ICASTDesignator designator) {
		return PROCESS_CONTINUE;
	}
}