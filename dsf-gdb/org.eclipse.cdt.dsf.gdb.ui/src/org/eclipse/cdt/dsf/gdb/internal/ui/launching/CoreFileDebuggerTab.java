/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import org.eclipse.cdt.dsf.gdb.service.SessionType;

/**
 * @since 2.0
 */
public class CoreFileDebuggerTab extends CDebuggerTab {
	public CoreFileDebuggerTab() {
		super(SessionType.CORE, false);
	}
}
