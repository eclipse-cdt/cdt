/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer.fast;


import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMFastIndexer implements IPDOMIndexer {

	// Must match extension id
	public static final String ID = "org.eclipse.cdt.core.fastIndexer";
	
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
		CCorePlugin.getPDOMManager().enqueue(
				new PDOMFastHandleDelta(this, delta));
	}
	
	public void reindex() throws CoreException {
		CCorePlugin.getPDOMManager().enqueue(
				new PDOMFastReindex(this));
	}
	
}
