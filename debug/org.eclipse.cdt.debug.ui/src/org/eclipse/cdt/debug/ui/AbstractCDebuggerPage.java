/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui; 

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
 
/**
 * Common function for debugger pages.
 * @since 3.1
 */
abstract public class AbstractCDebuggerPage extends AbstractLaunchConfigurationTab 
    implements ICDebuggerPage, ICDebuggerPageExtension {

	private String fDebuggerID = null;
	private ListenerList fContentListeners;

    public AbstractCDebuggerPage() {
        super();
        fContentListeners = new ListenerList();
    }

    /* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.ui.ICDebuggerPage#init(java.lang.String)
	 */
	@Override
	public void init(String debuggerID) {
		fDebuggerID = debuggerID;
	}

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#dispose()
     */
    @Override
    public void dispose() {
        fContentListeners.clear();
        super.dispose();
    }

    /* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.ui.ICDebuggerPage#getDebuggerIdentifier()
	 */
	@Override
	public String getDebuggerIdentifier() {
		return fDebuggerID;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.ui.ICDebuggerPageExtension#addContentChangeListener(org.eclipse.cdt.debug.ui.ICDebuggerPageExtension.IContentChangeListener)
     */
    /** @since 7.0 */
    @Override
	public void addContentChangeListener(IContentChangeListener listener) {
        fContentListeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.ui.ICDebuggerPageExtension#removeContentChangeListener(org.eclipse.cdt.debug.ui.ICDebuggerPageExtension.IContentChangeListener)
     */
    /** @since 7.0 */
    @Override
	public void removeContentChangeListener(IContentChangeListener listener) {
        fContentListeners.remove(listener);
    }

    /**
     * Notifies the registered listeners that the page's content has changed.
     * 
     * @since 7.0
     */
    protected void contentChanged() {
        for (Object listener : fContentListeners.getListeners())
            ((IContentChangeListener) listener).contentChanged();
    }
}
