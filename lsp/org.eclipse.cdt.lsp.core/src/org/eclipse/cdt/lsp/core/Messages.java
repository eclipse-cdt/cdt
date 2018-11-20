/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.lsp.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String PreferencePageDescription;
	public static String ServerChoiceLabel;
	public static String ServerPathLabel;
	public static String ServerOptionsLabel;
	public static String CqueryStateIdle;
	public static String CqueryStateBusy;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
