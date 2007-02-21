/********************************************************************************
 * Copyright (c) 2006, 2007 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 ********************************************************************************/

package org.eclipse.tm.internal.discovery.view;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.tm.internal.discovery.view.messages"; //$NON-NLS-1$

	public static String ServiceDiscoveryView_ClearActionText;

	public static String ServiceDiscoveryView_ClearActionToolTipText;

	public static String ServiceDiscoveryView_DiscoveryActionText;

	public static String ServiceDiscoveryView_DiscoveryActionToolTipText;

	public static String ServiceDiscoveryView_KeyColumnLabel;

	public static String ServiceDiscoveryView_PropertiesTableTitle;

	public static String ServiceDiscoveryView_ProtocolErrorDialogMessage;

	public static String ServiceDiscoveryView_ProtocolErrorDialogTitle;

	public static String ServiceDiscoveryView_RefreshActionText;

	public static String ServiceDiscoveryView_RefreshActionToolTipText;

	public static String ServiceDiscoveryView_ServicesTreeTitle;

	public static String ServiceDiscoveryView_TransportErrorDialogMessage;

	public static String ServiceDiscoveryView_TransportErrorDialogTitle;

	public static String ServiceDiscoveryView_TransportNoAddressFoundDialogTitle;

	public static String ServiceDiscoveryView_TransportNoAddressFoundDialogTransport;

	public static String ServiceDiscoveryView_ValueColumnLabel;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
