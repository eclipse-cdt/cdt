/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.ui.viewmodel.datamodel;

import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMAdapter;

/** 
 * Base implementation for DSF-based view model adapters.
 */
@ThreadSafe
abstract public class AbstractDMVMAdapter extends AbstractVMAdapter
{
    private final DsfSession fSession;

    /**
     * Constructor for the View Model session.  It is tempting to have the 
     * adapter register itself here with the session as the model adapter, but
     * that would mean that the adapter might get accessed on another thread
     * even before the deriving class is fully constructed.  So it it better
     * to have the owner of this object register it with the session.
     * @param session
     */
    public AbstractDMVMAdapter(DsfSession session) {
        super();
        fSession = session;
    }    

    /**
     * Returns the DSF session that this adapter is associated with.
     * @return
     */
    protected DsfSession getSession() { return fSession; }
    
}
