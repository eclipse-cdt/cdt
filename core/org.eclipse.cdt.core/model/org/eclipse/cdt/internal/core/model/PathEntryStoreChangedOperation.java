/*******************************************************************************
 * Copyright (c) 2005 QnX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public boolean isReadOnly() {
		return true;
	}


	protected void executeOperation() throws CModelException {
		PathEntryManager manager = PathEntryManager.getDefault();
		ICProject cproject = (ICProject)getElementToProcess();
		// Clear the old cache entries.
		IPathEntry[] oldResolvedEntries = manager.removeCachedResolvedPathEntries(cproject);
		IPathEntry[] newResolvedEntries = manager.getResolvedPathEntries(cproject);
		ICElementDelta[] deltas = manager.generatePathEntryDeltas(cproject, oldResolvedEntries, newResolvedEntries);
		if (deltas.length > 0) {
			cproject.close();
			for (int i = 0; i < deltas.length; i++) {
				addDelta(deltas[i]);
			}
		}
	}

}
