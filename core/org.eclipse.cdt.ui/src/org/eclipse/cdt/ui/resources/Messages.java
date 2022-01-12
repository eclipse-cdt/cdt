/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.resources;

import org.eclipse.osgi.util.NLS;

/**
 * @since 5.3
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.ui.resources.messages"; //$NON-NLS-1$
	public static String RefreshPolicyExceptionDialog_exceptionTypeResources;
	public static String RefreshPolicyExceptionDialog_addButtonLabel;
	public static String RefreshPolicyExceptionDialog_SelectResourceDialogMessage;
	public static String RefreshPolicyExceptionDialog_SelectResourceDialogTitle;
	public static String RefreshPolicyExceptionDialog_deleteButtonLabel;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
