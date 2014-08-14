/*******************************************************************************
 * Copyright (c) 2014 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.internal.model.cfg;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	public static String ControlFlowGraphBuilder_unsupported_statement_type;
	
	private Messages() {
		// Do not instantiate
	}

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}