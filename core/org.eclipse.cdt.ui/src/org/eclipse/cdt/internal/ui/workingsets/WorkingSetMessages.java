/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.workingsets;

import org.eclipse.osgi.util.NLS;

public final class WorkingSetMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.workingsets.WorkingSetMessages";//$NON-NLS-1$

	private WorkingSetMessages() {
		// Do not instantiate
	}

	public static String CElementWorkingSetPage_title;
	public static String CElementWorkingSetPage_name;
	public static String CElementWorkingSetPage_description;
	public static String CElementWorkingSetPage_content;
	public static String CElementWorkingSetPage_warning_nameMustNotBeEmpty;
	public static String CElementWorkingSetPage_warning_workingSetExists;
	public static String CElementWorkingSetPage_warning_resourceMustBeChecked;

	static {
		NLS.initializeMessages(BUNDLE_NAME, WorkingSetMessages.class);
	}
}