/*******************************************************************************
 * Copyright (c) 2006, 2014 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.ui;

import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.pda.launch.PDALaunch;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.ui.contexts.ISuspendTrigger;

/**
 * The adapter factory is the central point of control of view model and other
 * UI adapters of a DSF-based debugger.  As new launches are created and
 * old ones removed, this factory manages the life cycle of the associated
 * UI adapters.
 * <p>
 * As a platform adapter factory, this factory only  provides adapters for
 * the launch object.  Adapters for all other objects in the DSF model hierarchy
 * are registered with the DSF session.
 * </p>
 */
@ThreadSafe
@SuppressWarnings({ "restriction" })
public class PDAAdapterFactory implements IAdapterFactory {
	// This IAdapterFactory method returns adapters for the PDA launch object only.
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (!(adaptableObject instanceof PDALaunch))
			return null;

		PDALaunch launch = (PDALaunch) adaptableObject;

		// Check for valid session.
		// Note: even if the session is no longer active, the adapter set
		// should still be returned.  This is because the view model may still
		// need to show elements representing a terminated process/thread/etc.
		DsfSession session = launch.getSession();
		if (session == null)
			return null;

		SessionAdapterSet adapterSet = PDAUIPlugin.getDefault().getAdapterSet(launch);
		if (adapterSet == null)
			return null;

		// Returns the adapter type for the launch object.
		if (adapterType.equals(IElementContentProvider.class))
			return adapterType.cast(adapterSet.fViewModelAdapter);
		else if (adapterType.equals(IModelProxyFactory.class))
			return adapterType.cast(adapterSet.fViewModelAdapter);
		else if (adapterType.equals(ISuspendTrigger.class))
			return adapterType.cast(adapterSet.fSuspendTrigger);
		else
			return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { IElementContentProvider.class, IModelProxyFactory.class, ISuspendTrigger.class };
	}

}
