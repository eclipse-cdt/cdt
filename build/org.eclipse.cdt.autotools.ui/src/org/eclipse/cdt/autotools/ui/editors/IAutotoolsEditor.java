/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
