/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Navid Mehregani (TI) - Bug 289526 - Migrate the Restart feature to the new one, as supported by the platform
 *     Patrick Chuong (Texas Instruments) - Add support for icon overlay in the debug view (Bug 334566)
 *     Alvaro Sanchez-Leon (Ericsson AB) - Support for Step into selection (bug 244865)
 *     Marc Khouzam (Ericsson) - Extracted GdbSessionAdapters to allow overriding
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.cdt.debug.core.model.IConnectHandler;
import org.eclipse.cdt.debug.core.model.IDebugNewExecutableHandler;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.commands.IDisconnectHandler;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.ui.contexts.ISuspendTrigger;

/**
 * This implementation of platform adapter factory only retrieves the adapters
 * for the launch object.  But it also manages the creation and destruction
 * of the session-based adapters which are returned by the
 * IDMContext.getAdapter() methods.
 */
@ThreadSafe
public class GdbAdapterFactory
    implements IAdapterFactory, ILaunchesListener2
{
    /**
     * Active adapter sets.  They are accessed using the launch instance 
     * which owns the debug services session. 
     */
    private static Map<GdbLaunch, GdbSessionAdapters> fgLaunchAdapterSets =
        Collections.synchronizedMap(new HashMap<GdbLaunch, GdbSessionAdapters>());

    /**
     * Map of launches for which adapter sets have already been disposed.
     * This map (used as a set) is maintained in order to avoid re-creating an 
     * adapter set after the launch was removed from the launch manager, but 
     * while the launch is still being held by other classes which may 
     * request its adapters.  A weak map is used to avoid leaking 
     * memory once the launches are no longer referenced.
     * <p>
     * Access to this map is synchronized using the fgLaunchAdapterSets 
     * instance.
     * </p>
     */
    private static Map<ILaunch, GdbSessionAdapters> fgDisposedLaunchAdapterSets = new WeakHashMap<>();

    static void disposeAdapterSet(ILaunch launch) {
	synchronized(fgLaunchAdapterSets) {
	    if ( fgLaunchAdapterSets.containsKey(launch) ) {
		fgLaunchAdapterSets.remove(launch).dispose();
		fgDisposedLaunchAdapterSets.put(launch, null);
	    }
	}
    }

    public GdbAdapterFactory() {
        DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
    }

    /**
     * This method only actually returns adapters for the launch object.
     */
    @Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (!(adaptableObject instanceof GdbLaunch)) return null;

        GdbLaunch launch = (GdbLaunch)adaptableObject;

        // Check for valid session.  
        // Note: even if the session is no longer active, the adapter set 
        // should still be returned.  This is because the view model may still
        // need to show elements representing a terminated process/thread/etc.
        DsfSession session = launch.getSession();
        if (session == null) return null;

        // Find the correct set of adapters based on the launch session-ID.  If not found
        // it means that we have a new launch and new session, and we have to create a
        // new set of adapters.

        GdbSessionAdapters adapterSet;
        synchronized(fgLaunchAdapterSets) {
            // The adapter set for the given launch was already disposed.  
            // Return a null adapter.
            if (fgDisposedLaunchAdapterSets.containsKey(launch)) {
                return null;
            }
            adapterSet = fgLaunchAdapterSets.get(launch);
            if (adapterSet == null) {
            	// If the first time we attempt to create an adapterSet is once the session is
            	// already inactive, we should not create it and return null.
            	// This can happen, for example, when we run JUnit tests and we don't actually
            	// have a need for any adapters until the launch is actually being removed.
            	// Note that we must do this here because fgDisposedLaunchAdapterSets
            	// may not already know that the launch has been removed because of a race
            	// condition with the caller which is also processing a launchRemoved method.
            	// Bug 334687 
            	if (session.isActive() == false) {
            		return null;
            	}
                adapterSet = createGdbSessionAdapters(launch, session);
                fgLaunchAdapterSets.put(launch, adapterSet);
            }
        }
        
        // Returns the adapter type for the launch object.
        return adapterSet.getLaunchAdapter(adapterType);
    }

    @Override
    public Class<?>[] getAdapterList() {
        return new Class<?>[] {
            IElementContentProvider.class, 
            IModelProxyFactory.class, 
            ISuspendTrigger.class,
            IColumnPresentationFactory.class,
        	ITerminateHandler.class,
        	IConnectHandler.class,
        	IDisconnectHandler.class,
        	IDebugNewExecutableHandler.class,
        };
    }

    @Override
    public void launchesRemoved(ILaunch[] launches) {
        // Dispose the set of adapters for a launch only after the launch is
        // removed.
        for (ILaunch launch : launches) {
            if (launch instanceof GdbLaunch) {
                disposeAdapterSet(launch);
            }
        }
    }

    @Override
    public void launchesTerminated(ILaunch[] launches) {
    }

    @Override
    public void launchesAdded(ILaunch[] launches) {
    }
    
    @Override
    public void launchesChanged(ILaunch[] launches) {
    }
    
    protected GdbSessionAdapters createGdbSessionAdapters(ILaunch launch, DsfSession session) {
    	return new GdbSessionAdapters(launch, session, getAdapterList());
    }
}
