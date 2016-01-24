/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors;

import org.eclipse.cdt.internal.autotools.ui.editors.automake.IReconcilingParticipant;
import org.eclipse.ui.texteditor.ITextEditor;


public interface IAutotoolsEditor extends ITextEditor {
	
	/**
	 * Adds the given listener.
	 * Has no effect if an identical listener was not already registered.
	 * 
	 * @param listener	The reconcile listener to be added
	 */
	void addReconcilingParticipant(IReconcilingParticipant listener);
}
