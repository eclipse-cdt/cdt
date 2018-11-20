/*******************************************************************************
 * Copyright (c) 2005, 2008 QnX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Qnx Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;

public class PathEntryStoreChangedOperation extends CModelOperation {

	public PathEntryStoreChangedOperation(ICProject cproject) {
		super(cproject);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CModelOperation#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	protected void executeOperation() throws CModelException {
		PathEntryManager manager = PathEntryManager.getDefault();
		ICProject cproject = (ICProject) getElementToProcess();
		// Clear the old cache entries.
		IPathEntry[] oldResolvedEntries = manager.removeCachedResolvedPathEntries(cproject);
		IPathEntry[] newResolvedEntries = manager.getResolvedPathEntries(cproject);
		//		if(needDelta(cproject.getProject())){
		ICElementDelta[] deltas = manager.generatePathEntryDeltas(cproject, oldResolvedEntries, newResolvedEntries);
		if (deltas.length > 0) {
			cproject.close();
			for (int i = 0; i < deltas.length; i++) {
				addDelta(deltas[i]);
			}
		}
		//		}
	}
}
