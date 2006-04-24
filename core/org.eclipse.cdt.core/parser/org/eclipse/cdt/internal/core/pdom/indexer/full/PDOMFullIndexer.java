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

package org.eclipse.cdt.internal.core.pdom.indexer.full;

import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.CoreException;

/**
 * The Full indexer does full parsing in order to gather index information.
 * It has good accuracy but is relatively slow.
 * 
 * @author Doug Schaefer
 *
 */
public class PDOMFullIndexer implements IPDOMIndexer {

	private IPDOM pdom;
	
	public void handleDelta(ICElementDelta delta) {
		new PDOMFullHandleDelta((PDOM)pdom, delta).schedule();
	}

	public void reindex() throws CoreException {
		new PDOMFullReindex((PDOM)pdom).schedule();
	}

	public void setPDOM(IPDOM pdom) {
		this.pdom = pdom;
	}

}
