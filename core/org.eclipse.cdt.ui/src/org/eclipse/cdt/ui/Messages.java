/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui;

import org.eclipse.osgi.util.NLS;

class Messages extends NLS {
	public static String CDTSharedImages_MissingImage;
	public static String CElementGrouping_includeGroupingLabel;
	public static String CElementGrouping_macroGroupingLabel;
	public static String CUIPlugin_jobStartMakeUI;

	static {
		// Initialize resource bundle
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
