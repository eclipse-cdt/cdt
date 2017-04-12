/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     cartu38 opendev (STMicroelectronics) - [514385] Build setting validity support
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.ui.newui.AbstractPage;


/**
 * The class have the same functionality as superclass.
 * The only need to create it is distinguishing tabs.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class Page_BuildSettings extends AbstractPage {
	@Override
	protected boolean isSingle() {	return false; }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractPage#isValid()
	 */

	@Override
	public boolean isValid() {
		for (InternalTab itab : itabs) {
			if (!itab.tab.isValid()) {
				return false;
			}
		}
		return super.isValid();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractPage#performDefaults()
	 */
	@Override
	public void performDefaults() {
		super.performDefaults();
		updateApplyButton();
	}
}
