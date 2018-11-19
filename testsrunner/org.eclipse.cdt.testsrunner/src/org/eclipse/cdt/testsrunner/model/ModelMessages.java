/*******************************************************************************
 * Copyright (c) 2011, 2012 Anton Gorenkov.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.model;

import org.eclipse.osgi.util.NLS;

public class ModelMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.testsrunner.model.ModelMessages"; //$NON-NLS-1$
	public static String MessageLevel_error;
	public static String MessageLevel_exception;
	public static String MessageLevel_fatal_error;
	public static String MessageLevel_info;
	public static String MessageLevel_message;
	public static String MessageLevel_warning;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ModelMessages.class);
	}

	private ModelMessages() {
	}
}
