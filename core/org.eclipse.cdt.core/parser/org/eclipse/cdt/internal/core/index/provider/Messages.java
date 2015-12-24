/*******************************************************************************
 * Copyright (c) 2007, 2014 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.provider;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String IndexProviderManager_InvalidIndexProvider;
	public static String IndexProviderManager_NoCompatibleFragmentsAvailable;

	static {
		// Initialize resource bundle.
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
