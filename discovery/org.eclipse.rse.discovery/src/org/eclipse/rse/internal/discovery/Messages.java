/********************************************************************************
 * Copyright (c) 2006, 2008 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 *   Javier Montalvo Orus (Symbian) - NLS Cleanup
 ********************************************************************************/

package org.eclipse.rse.internal.discovery;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.rse.internal.discovery.messages"; //$NON-NLS-1$

	public static String ServiceDiscoveryWizard_DiscoveryPropertySet;

	public static String ServiceDiscoveryWizard_Port;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

}
