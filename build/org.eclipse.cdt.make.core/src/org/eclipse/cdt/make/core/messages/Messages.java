/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.messages;

import org.eclipse.osgi.util.NLS;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.make.core.messages.messages"; //$NON-NLS-1$
	public static String SCMarkerGenerator_Add_Markers;
	public static String SCMarkerGenerator_Error_Adding_Markers;
	public static String SCMarkerGenerator_Discovery_Options_Page;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
