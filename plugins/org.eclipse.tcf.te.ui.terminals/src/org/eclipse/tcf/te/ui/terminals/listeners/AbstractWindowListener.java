/*******************************************************************************
 * Copyright (c) 2013, 2014 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.listeners;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Abstract window listener implementation.
 */
public abstract class AbstractWindowListener implements IWindowListener {
	// The part listener instance
	protected final IPartListener2 partListener;
	// The perspective listener instance
	protected final IPerspectiveListener perspectiveListener;

	// Flag to remember if the initialization is done or not
	private boolean initialized = false;

	/**
     * Constructor
     */
    public AbstractWindowListener() {
    	// Create the part listener instance
    	partListener = createPartListener();
    	// Create the perspective listener instance
    	perspectiveListener = createPerspectiveListener();
    }

    /**
     * Creates a new part listener instance.
     * <p>
     * <b>Note:</b> The default implementation returns <code>null</code>.
     *
     * @return The part listener instance or <code>null</code>.
     */
    protected IPartListener2 createPartListener() {
    	return null;
    }

    /**
     * Creates a new perspective listener instance.
     * <p>
     * <b>Note:</b> The default implementation returns <code>null</code>.
     *
     * @return The perspective listener instance or <code>null</code>.
     */
    protected IPerspectiveListener createPerspectiveListener() {
    	return null;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void windowActivated(IWorkbenchWindow window) {
		if (!initialized && window != null) {
			windowOpened(window);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void windowDeactivated(IWorkbenchWindow window) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void windowClosed(IWorkbenchWindow window) {
		// On close, remove the listeners from the window
		if (window != null) {
			if (window.getPartService() != null && partListener != null) {
				window.getPartService().removePartListener(partListener);
			}

			if (perspectiveListener != null) window.removePerspectiveListener(perspectiveListener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void windowOpened(IWorkbenchWindow window) {
		if (window != null) {
			// On open, register the part listener to the window
			if (window.getPartService() != null && partListener != null) {
				// Get the part service
				IPartService service = window.getPartService();
				// Unregister the part listener, just in case
				service.removePartListener(partListener);
				// Register the part listener
				service.addPartListener(partListener);
				// Signal the active part to the part listener after registration
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					IWorkbenchPartReference partRef = page.getActivePartReference();
					if (partRef != null) partListener.partActivated(partRef);
				}
			}

			// Register the perspective listener
			if (perspectiveListener != null) {
				window.addPerspectiveListener(perspectiveListener);
				// Signal the active perspective to the perspective listener after registration
				if (window.getActivePage() != null) {
					perspectiveListener.perspectiveActivated(window.getActivePage(), window.getActivePage().getPerspective());
				}
			}

			initialized = true;
		}
	}
}
