/*******************************************************************************
 * Copyright (c) 2008 ARM Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Limited - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.elements.adapters;

import org.eclipse.cdt.debug.core.model.IDisassemblyLine;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.core.model.DisassemblyRetrieval;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.jface.viewers.Viewer;

/**
 * org.eclipse.cdt.debug.internal.ui.elements.adapters.DisassemblyElementProxy: 
 * //TODO Add description.
 */
public class DisassemblyElementProxy extends AbstractModelProxy implements IDebugEventSetListener {

    private Object fElement;

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
     */
    public DisassemblyElementProxy( Object element ) {
        super();
        fElement = element;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy#installed(org.eclipse.jface.viewers.Viewer)
     */
    @Override
    public void installed( Viewer viewer ) {
        super.installed( viewer );
        DebugPlugin.getDefault().addDebugEventListener( this );
        fireModelChanged( new ModelDelta( fElement, IModelDelta.CONTENT ) );
    }

    @Override
	public void handleDebugEvents( DebugEvent[] events ) {
        for ( DebugEvent event : events ) {
            Object source = event.getSource();
            int kind = event.getKind();
            int detail = event.getDetail();
            if ( source instanceof IDisassemblyLine ) {
                if ( kind == DebugEvent.CHANGE && detail == DebugEvent.STATE ) {
                    IDisassemblyLine line = (IDisassemblyLine)source;
                    DisassemblyRetrieval dr = ((CDebugTarget)line.getDebugTarget()).getDisassemblyRetrieval();
                    if ( getElement().equals( dr ) ) {
                        fireModelChanged( createDelta( line ) );
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy#dispose()
     */
    @Override
    public synchronized void dispose() {
        DebugPlugin.getDefault().removeDebugEventListener( this );
        super.dispose();
    }

    protected Object getElement() {
        return fElement;
    }

    protected IModelDelta createDelta( IDisassemblyLine line ) {
        DisassemblyRetrieval dr = ((CDebugTarget)line.getDebugTarget()).getDisassemblyRetrieval();
        ModelDelta delta = new ModelDelta( dr, IModelDelta.NO_CHANGE );
        delta.addNode( line, IModelDelta.STATE );
        return delta;
    }
}
