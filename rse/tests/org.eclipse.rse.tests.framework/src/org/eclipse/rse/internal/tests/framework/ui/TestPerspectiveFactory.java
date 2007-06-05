/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 *******************************************************************************/
package org.eclipse.rse.internal.tests.framework.ui;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * Creates the test perspective.
 */
public class TestPerspectiveFactory implements IPerspectiveFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		layout.addView("org.eclipse.rse.tests.framework.ConsoleView", IPageLayout.RIGHT, 0.30f, null); //$NON-NLS-1$
		layout.addView("org.eclipse.rse.tests.framework.ImageView", IPageLayout.LEFT, 0.70f, "org.eclipse.rse.tests.framework.ConsoleView"); //$NON-NLS-1$ //$NON-NLS-2$
		layout.addView("org.eclipse.rse.tests.framework.HolderView", IPageLayout.BOTTOM, 0.70f, "org.eclipse.rse.tests.framework.ImageView"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
