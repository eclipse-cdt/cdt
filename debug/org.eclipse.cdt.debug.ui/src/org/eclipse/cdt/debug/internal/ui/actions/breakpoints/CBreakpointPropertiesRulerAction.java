/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Anton Leherbauer (Wind River Systems) - bug 183397
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions.breakpoints;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.cdt.debug.internal.ui.actions.ActionMessages;
import org.eclipse.cdt.debug.ui.breakpoints.CBreakpointPropertyDialogAction;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextProvider;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Opens a custom properties dialog to configure the attibutes of a C/C++ breakpoint 
 * from the ruler popup menu.
 */
public class CBreakpointPropertiesRulerAction extends AbstractBreakpointRulerAction {

    private ICBreakpoint fBreakpoint;

	/**
	 * Creates the action to modify the breakpoint properties.
	 */
	public CBreakpointPropertiesRulerAction( IWorkbenchPart part, IVerticalRulerInfo info ) {
		super( part, info );
		setText( ActionMessages.getString( "CBreakpointPropertiesRulerAction.Breakpoint_Properties" ) ); //$NON-NLS-1$
		part.getSite().getWorkbenchWindow().getWorkbench().getHelpSystem().setHelp( this, ICDebugHelpContextIds.BREAKPOINT_PROPERTIES_ACTION );
		setId( IInternalCDebugUIConstants.ACTION_BREAKPOINT_PROPERTIES );
	}

	/* (non-Javadoc)
	 * @see Action#run()
	 */
	@Override
	public void run() {
        if ( fBreakpoint != null ) {
            final ISelection debugContext = DebugUITools.getDebugContextForPart(getTargetPart());
            CBreakpointPropertyDialogAction propertiesAction = new CBreakpointPropertyDialogAction(
                getTargetPart().getSite(), 
                new ISelectionProvider() {
                    @Override
                    public ISelection getSelection() {
                        return new StructuredSelection( fBreakpoint );
                    }
                    @Override public void addSelectionChangedListener( ISelectionChangedListener listener ) {}
                    @Override public void removeSelectionChangedListener( ISelectionChangedListener listener ) {}
                    @Override public void setSelection( ISelection selection ) {}
                }, 
                new IDebugContextProvider() {
                    @Override
                    public ISelection getActiveContext() {
                        return debugContext;
                    }
                    @Override public void addDebugContextListener(IDebugContextListener listener) {}
                    @Override public void removeDebugContextListener(IDebugContextListener listener) {}
                    @Override public IWorkbenchPart getPart() { return null; }
                    
                }
                );
            propertiesAction.run();
            propertiesAction.dispose();
        }
	}

	/* (non-Javadoc)
	 * @see IUpdate#update()
	 */
	@Override
	public void update() {
        IBreakpoint breakpoint= getBreakpoint();
        
        if (breakpoint instanceof ICBreakpoint) {
            fBreakpoint = (ICBreakpoint)breakpoint;
        } else {
            fBreakpoint = null;
        }
        setEnabled( fBreakpoint != null );
	}
}
