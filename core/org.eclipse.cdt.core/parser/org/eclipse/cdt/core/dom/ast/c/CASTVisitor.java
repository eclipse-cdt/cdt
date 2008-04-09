/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Yuan Zhang / Beth Tibbitts (IBM Research)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;

/**
 * This subclass of ASTVisitor that allows for better control in traversing C.
 */
public abstract class CASTVisitor extends ASTVisitor implements ICASTVisitor {


	public int visit(ICASTDesignator designator) {
		return PROCESS_CONTINUE;
	}


	public int leave(ICASTDesignator designator) {
		return PROCESS_CONTINUE;
	}
}
