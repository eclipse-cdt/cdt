/*******************************************************************************
 * Copyright (c) 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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