/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer.fast;


import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMFastIndexer implements IPDOMIndexer {

	// Must match extension id
	public static final String ID = IPDOMManager.ID_FAST_INDEXER;
	
	protected ICProject project;
	
	public PDOMFastIndexer() {
	}

	public ICProject getProject() {
		return project;
	}
	
	public void setProject(ICProject project) {
		this.project = project;
	}
	
	public void handleDelta(ICElementDelta delta) throws CoreException {
		PDOMFastHandleDelta fhd= new PDOMFastHandleDelta(this, delta);
		if (fhd.getFilesToIndexCount() > 0) {
			CCoreInternals.getPDOMManager().enqueue(fhd);
		}
	}
	
	public void reindex() throws CoreException {
		CCoreInternals.getPDOMManager().enqueue(new PDOMFastReindex(this));
	}
	
}
