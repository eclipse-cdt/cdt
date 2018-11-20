/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;

/**
 *
 *
 */
public class MultiFileDescription extends MultiResourceDescription implements ICFileDescription {

	public MultiFileDescription(ICFileDescription[] res) {
		super(res);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICFileDescription#getLanguageSetting()
	 */
	@Override
	public ICLanguageSetting getLanguageSetting() {
		if (DEBUG)
			System.out.println("Limited multi access: MultiFileDescription.getLanguageSetting()"); //$NON-NLS-1$
		return ((ICFileDescription) fRess[0]).getLanguageSetting();
	}

}
