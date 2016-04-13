/*******************************************************************************
 * Copyright (c) 2014, 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.gdb;

import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbAdapterFactory;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbSessionAdapters;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.gdb.launch.GdbExtendedLaunch;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;

@SuppressWarnings("restriction")
@ThreadSafe
public class GdbExtendedAdapterFactory extends GdbAdapterFactory {

	public GdbExtendedAdapterFactory() {
		/*
		 * Insulate against future types being added to #getAdapterList()
		 * without being added to the extenders plugin.xml
		 */
		Platform.getAdapterManager().registerAdapters(this, GdbExtendedLaunch.class);
	}

	@Override
	protected GdbSessionAdapters createGdbSessionAdapters(ILaunch launch, DsfSession session) {
		return new GdbExtendedSessionAdapters(launch, session, getAdapterList());
	}
}
