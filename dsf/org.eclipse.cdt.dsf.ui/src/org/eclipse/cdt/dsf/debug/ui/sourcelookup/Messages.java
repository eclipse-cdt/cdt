/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.sourcelookup;

import org.eclipse.osgi.util.NLS;

/**
 * @since 2.1
 */
public class Messages extends NLS {

	public static String IPAnnotation_primary;
	public static String IPAnnotation_secondary;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
