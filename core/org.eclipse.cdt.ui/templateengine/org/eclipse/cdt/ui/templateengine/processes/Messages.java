/*******************************************************************************
 * Copyright (c) 2010 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine.processes;

import org.eclipse.osgi.util.NLS;

/**
 * @since 5.2
 */
public class Messages {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.ui.templateengine.processes.messages"; //$NON-NLS-1$

	private Messages() {
	}
	
	public static String OpenFiles_CannotOpen_error;
	public static String OpenFiles_FileNotExist_error;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
