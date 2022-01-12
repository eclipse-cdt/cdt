/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.workingsets;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistable;

/**
 * The protocol for elements of the working-set configuration model, which can be persisted via
 * {@linkplain IMemento mementos}.
 *
 * @author Christian W. Damus (cdamus)
 *
 * @since 6.0
 *
 */
public interface IWorkingSetConfigurationElement extends IPersistable {
	/**
	 * Loads me from the specified memento.
	 *
	 * @param memento
	 *            a memento in which I am persisted
	 */
	void loadState(IMemento memento);

	//
	// Nested types
	//

	/**
	 * The protocol for mutable working-copies ("snapshots") of working set configuration model elements.
	 *
	 * @author Christian W. Damus (cdamus)
	 *
	 * @since 6.0
	 *
	 * @see WorkspaceSnapshot
	 */
	interface ISnapshot {
		/**
		 * Obtains the workspace snapshot that describes the baseline state of the working-set configuration
		 * editing session of which I am a part.
		 *
		 * @return my base workspace snapshot
		 */
		WorkspaceSnapshot getWorkspaceSnapshot();
	}
}
