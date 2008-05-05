/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [188360] renamed from plugin org.eclipse.rse.eclipse.filesystem
 * Remy Chi Jian Suen (IBM) - [202098][efs][nls] Improve error message when creating a remote project with existing name
 *******************************************************************************/
package org.eclipse.rse.internal.efs.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.rse.internal.efs.ui.messages"; //$NON-NLS-1$
	public static String CreateRemoteProjectActionDelegate_CREATING_TITLE;
	public static String CreateRemoteProjectActionDelegate_PROJECT_EXISTS;
	static {
// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
