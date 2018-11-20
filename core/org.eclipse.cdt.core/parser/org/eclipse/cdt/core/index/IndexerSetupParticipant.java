/*******************************************************************************
 * Copyright (c) 2008, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.index;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;

/**
 * Abstract base class for indexer setup participants. A participant can delay the
 * setup of the indexer when a project is added to the workspace.
 */
public abstract class IndexerSetupParticipant {
	/**
	 * The method will be called before an indexer is set up for a project. If you
	 * return <code>true</code> the setup will be postponed. You need to call
	 * {@link #notifyIndexerSetup(ICProject)} as soon as this participant no longer
	 * needs to block the indexer setup.
	 * <p>
	 * This method may be called multiple times for the same project.
	 * @param project the project for which the indexer is supposed to be initialized.
	 * @return whether or not to proceed with the indexer setup.
	 */
	public boolean postponeIndexerSetup(ICProject project) {
		return false;
	}

	/**
	 * Informs the index manager that this participant no longer needs to postpone the
	 * indexer setup for the given project. Depending on the state of other participants
	 * this may trigger the indexer setup.
	 * @param project the project for which the setup no longer needs to be postponed
	 */
	public final void notifyIndexerSetup(ICProject project) {
		CCoreInternals.getPDOMManager().notifyIndexerSetup(this, project);
	}

	/**
	 * Call-back that tells the implementor that a project has passed all setup participants
	 * and therefore it is actually initialized.
	 */
	public void onIndexerSetup(ICProject project) {
	}
}
