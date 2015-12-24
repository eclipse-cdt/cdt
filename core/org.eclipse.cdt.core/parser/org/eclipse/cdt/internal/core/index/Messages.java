/*******************************************************************************
 * Copyright (c) 2006, 2014 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String CIndex_FindBindingsTask_label;
	public static String IndexFactory_errorNoSuchPDOM0;

	static {
		// Initialize resource bundle.
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
