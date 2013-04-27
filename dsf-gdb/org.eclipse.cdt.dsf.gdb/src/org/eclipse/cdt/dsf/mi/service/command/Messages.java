/*******************************************************************************
 * Copyright (c) 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command;

import org.eclipse.osgi.util.NLS;

/**
 * @since 4.2
 */
class Messages extends NLS {
	public static String MIInferiorProcess_ExitValue;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
