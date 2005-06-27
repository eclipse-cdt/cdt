/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring.base;


/**
 * Listener to monitor changes made to an <code>UndoManager</code>
 */
public interface IUndoManagerListener {
	
	/**
	 * This method is called by the undo manager if an undo change has been 
	 * added to it.
	 */
	public void undoStackChanged(IUndoManager manager);
	
	/**
	 * This method is called by the undo manager if a redo change has been 
	 * added to it.
	 */
	public void redoStackChanged(IUndoManager manager);	
}
