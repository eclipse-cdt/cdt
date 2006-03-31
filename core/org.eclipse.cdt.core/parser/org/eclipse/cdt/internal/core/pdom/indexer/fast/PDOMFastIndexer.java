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


import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMFastIndexer implements IPDOMIndexer {

	private PDOM pdom;

	public void setPDOM(IPDOM pdom) {
		if (pdom instanceof PDOM)
			this.pdom = (PDOM)pdom;
	}
	
	public void handleDelta(ICElementDelta delta) {
		IProgressMonitor group = Platform.getJobManager().createProgressGroup();
		new PDOMFastHandleDelta(pdom, delta, group).schedule();
	}
	
	public void reindex() throws CoreException {
		IProgressMonitor group = Platform.getJobManager().createProgressGroup();
		group.beginTask("Reindexing", 100);
		new PDOMFastReindex(pdom, group).schedule();
	}
	
}
