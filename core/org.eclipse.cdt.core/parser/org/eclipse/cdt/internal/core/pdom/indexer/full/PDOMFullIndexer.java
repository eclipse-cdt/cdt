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

package org.eclipse.cdt.internal.core.pdom.indexer.full;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.indexer.AbstractPDOMIndexer;
import org.eclipse.core.runtime.CoreException;

/**
 * The Full indexer does full parsing in order to gather index information.
 * It has good accuracy but is relatively slow.
 * 
 * @author Doug Schaefer
 *
 */
public class PDOMFullIndexer extends AbstractPDOMIndexer {
	public static final String ID = IPDOMManager.ID_FULL_INDEXER;
	
	public void handleDelta(ICElementDelta delta) throws CoreException {
		PDOMFullHandleDelta task = new PDOMFullHandleDelta(this, delta);
		if (task.estimateRemainingSources() > 0) {
			CCoreInternals.getPDOMManager().enqueue(task);
		}
	}

	public void reindex() throws CoreException {
		CCoreInternals.getPDOMManager().enqueue(new PDOMFullReindex(this));
	}

	public String getID() {
		return ID;
	}
}
