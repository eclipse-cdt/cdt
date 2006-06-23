/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ICElement;

/**
 */
public class ArchiveContainerInfo extends OpenableInfo {

	/**
	 * Constructs a new C Model Info 
	 */
	protected ArchiveContainerInfo(CElement element) {
		super(element);
	}

	synchronized void sync() {
		BinaryRunner runner = CModelManager.getDefault().getBinaryRunner(getElement().getCProject(), true);
		if (runner != null) {
			runner.waitIfRunning();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CElementInfo#addChild(org.eclipse.cdt.core.model.ICElement)
	 */
	protected void addChild(ICElement child) {
		if (!includesChild(child)) {
			super.addChild(child);
		}
	}
}
