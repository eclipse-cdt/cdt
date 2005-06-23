/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring.base;

/**
 * An adapter implementation for <code>IUndoManagerListener</code>.
 */
public class UndoManagerAdapter implements IUndoManagerListener {

	/* (non-Javadoc)
	 * Method declared in IUndoManagerListener
	 */
	public void undoStackChanged(IUndoManager manager) {
	}
	
	/* (non-Javadoc)
	 * Method declared in IUndoManagerListener
	 */
	public void redoStackChanged(IUndoManager manager) {
	}
}

