/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.console.actions;

import org.eclipse.osgi.util.NLS;

public class ActionMessages extends NLS {

	static {
		NLS.initializeMessages(ActionMessages.class.getName(), ActionMessages.class);
	}

	public static String CONNECT;
	public static String DISCONNECT;
	public static String SCROLL_LOCK;

}
