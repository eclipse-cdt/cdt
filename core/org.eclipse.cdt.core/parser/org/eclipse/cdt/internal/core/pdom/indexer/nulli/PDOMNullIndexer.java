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

package org.eclipse.cdt.internal.core.pdom.indexer.nulli;

import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.model.ElementChangedEvent;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMNullIndexer implements IPDOMIndexer {

	public void setPDOM(IPDOM pdom) {
	}

	public void reindex() {
	}

	public void elementChanged(ElementChangedEvent event) {
	}

}
