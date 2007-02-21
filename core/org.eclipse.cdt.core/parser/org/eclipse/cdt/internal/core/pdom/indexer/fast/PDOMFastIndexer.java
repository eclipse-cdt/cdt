/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
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


import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.indexer.AbstractPDOMIndexer;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMFastIndexer extends AbstractPDOMIndexer {

	// Must match extension id
	public static final String ID = IPDOMManager.ID_FAST_INDEXER;
	
	
	public PDOMFastIndexer() {
	}

	public void handleDelta(ICElementDelta delta) throws CoreException {
		PDOMFastHandleDelta fhd= new PDOMFastHandleDelta(this, delta);
		if (fhd.estimateRemainingSources() > 0) {
			CCoreInternals.getPDOMManager().enqueue(fhd);
		}
	}
	
	public void reindex() throws CoreException {
		CCoreInternals.getPDOMManager().enqueue(new PDOMFastReindex(this));
	}
	
	public String getID() {
		return ID;
	}
}
