/*******************************************************************************
 * Copyright (c) 2009, 2012 Alena Laskavaia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.param;

import org.eclipse.osgi.util.NLS;

/**
 * Core Messages
 */
class Messages extends NLS {
	public static String FileScopeProblemPreference_Label;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	// Do not instantiate
	private Messages() {
	}
}
