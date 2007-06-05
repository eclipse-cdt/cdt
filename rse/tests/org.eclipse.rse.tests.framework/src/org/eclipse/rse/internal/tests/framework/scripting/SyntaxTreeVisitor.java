/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 *******************************************************************************/
package org.eclipse.rse.internal.tests.framework.scripting;

/**
 * A visitor visits a node in a syntax tree.
 */
public abstract class SyntaxTreeVisitor {
	
	/**
	 * Inform the visitor that a node is being entered.
	 * @param node
	 */
	public abstract void enter(SyntaxNode node);
	
	/**
	 * Inform the visitor that a node is being left.
	 * @param node
	 */
	public abstract void leave(SyntaxNode node);

}
