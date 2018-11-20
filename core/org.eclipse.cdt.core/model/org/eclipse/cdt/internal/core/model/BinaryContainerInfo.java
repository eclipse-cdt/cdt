/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ICElement;

/**
 */
public class BinaryContainerInfo extends OpenableInfo {

	/**
	 * Constructs a new C Model Info
	 */
	protected BinaryContainerInfo(CElement element) {
		super(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CElementInfo#addChild(org.eclipse.cdt.core.model.ICElement)
	 */
	@Override
	protected void addChild(ICElement child) {
		if (!includesChild(child)) {
			super.addChild(child);
		}
	}
}
