/*******************************************************************************
 * Copyright (c) 2014, 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.gdb;

import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbAdapterFactory;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbSessionAdapters;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunch;

@SuppressWarnings("restriction")
@ThreadSafe
public class GdbExtendedAdapterFactory extends GdbAdapterFactory {
	@Override
	protected GdbSessionAdapters createGdbSessionAdapters(ILaunch launch, DsfSession session) {
		return new GdbExtendedSessionAdapters(launch, session, getAdapterList());
	}
}
