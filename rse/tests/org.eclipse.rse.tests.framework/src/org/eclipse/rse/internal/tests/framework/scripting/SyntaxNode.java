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

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * A syntax tree node.  These are typically created by parsing some sort of 
 * source document.
 */
public abstract class SyntaxNode {
	
	private List children = new Vector(10); // the size is arbitrary 
	private int lineNumber;
	
	/**
	 * Constructs a syntax node for a particular line in the script. The line number
	 * is used for diagnostic purposes.
	 * @param lineNumber
	 */
	public SyntaxNode(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	/**
	 * Adds a child to the list of children maintained by this node.
	 * @param child the child node to add.
	 */
	public void add(SyntaxNode child) {
		children.add(child);
	}
	
	/**
	 * Accept a visitor to this node.  The visitor is told to enter, each selected
	 * child told to accept the visitor and then the visitor is told to leave.
	 * @param visitor
	 * @see SyntaxTreeVisitor#enter(SyntaxNode)
	 * @see SyntaxTreeVisitor#leave(SyntaxNode)
	 * @see SyntaxNode#accept(SyntaxTreeVisitor)
	 * @see SyntaxNode#getSelectedChildren()
	 */
	public void accept(SyntaxTreeVisitor visitor) {
		visitor.enter(this);
		List selected = getSelectedChildren();
		for (Iterator z = selected.iterator(); z.hasNext();) {
			SyntaxNode child = (SyntaxNode) z.next();
			child.accept(visitor);
		}
		visitor.leave(this);
	}
	
	/**
	 * Perform any actions required when the Node is entered.  This could include
	 * setting any criteria for selection of children later on.  (hint, hint)
	 * @param context The context on which the action may be performed.
	 */
	public abstract void enter(ScriptContext context);
	
	/**
	 * Perform any actions required when the Node is left.
	 * @param context The context on which the action may be performed.
	 */
	public abstract void leave(ScriptContext context);
	
	/**
	 * Returns the list of selected children.  
	 * The default implementation is to select all children.
	 * Subclasses can override if necessary.
	 * @return the List of selected children.
	 */
	protected List getSelectedChildren() {
		return children;
	}
	
	public final int getLineNumber() {
		return lineNumber;
	}
	
}
