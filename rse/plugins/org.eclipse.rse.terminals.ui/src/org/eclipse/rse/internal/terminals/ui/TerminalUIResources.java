/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [235626] initial API and implementation
 * Anna Dushistova  (MontaVista) - [238257] Request a help text when no tab is open in "Remote Shell", "Remote Monitor" and "Terminals" views
 * Zhou Renjian     (Kortide)    - [282256] "null:..." status message for launched terminal
 *******************************************************************************/

package org.eclipse.rse.internal.terminals.ui;

import org.eclipse.osgi.util.NLS;

public class TerminalUIResources extends NLS {
	private static String BUNDLE_NAME = "org.eclipse.rse.internal.terminals.ui.TerminalUIResources"; //$NON-NLS-1$

	public static String RemoveTerminalAction_label;
	public static String RemoveTerminalAction_tooltip;
	public static String ShowInTerminalViewAction_label;
	public static String ShowInTerminalViewAction_tooltip;

	public static String TerminalsUI_cannotOpenView_error;

	public static String TerminalViewer_text;

	public static String TerminalViewElementAdapter_type;

	static {
		NLS.initializeMessages(BUNDLE_NAME, TerminalUIResources.class);
		// FIXME Workaround for NLS added in TM 3.1.1 where some translations
		// may no longer be possible. Fallback to hardcoded text in case the NLS
		// can not be found.
		// May be removed in TM 3.2 when a new NLS translation cycle starts.
		if (TerminalViewElementAdapter_type.startsWith("NLS missing message: ")) { //$NON-NLS-1$
			TerminalViewElementAdapter_type = "Terminal"; //$NON-NLS-1$
		}
	}

}
