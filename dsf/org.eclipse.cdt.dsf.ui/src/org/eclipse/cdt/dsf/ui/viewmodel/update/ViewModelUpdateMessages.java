/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.update;

import org.eclipse.osgi.util.NLS;

public class ViewModelUpdateMessages extends NLS {
	public static String AutomaticUpdatePolicy_name;
	public static String ManualUpdatePolicy_InitialDataElement__label;
	public static String ManualUpdatePolicy_name;
	/**
	 * @since 1.1
	 */
	public static String AllUpdateScope_name;
	/**
	 * @since 1.1
	 */
	public static String VisibleUpdateScope_name;

	static {
		// load message values from bundle file
		NLS.initializeMessages(ViewModelUpdateMessages.class.getName(), ViewModelUpdateMessages.class);
	}

	private ViewModelUpdateMessages() {
	}
}
