/*******************************************************************************
 * Copyright (c) 2004, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Ericsson             - DSF-GDB version
 * Nokia				- Made generic to DSF
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui; 

import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
 
/**
 * Manages the current evaluation context (stack frame) for evaluation actions.
 * In each page, the selection is tracked in each debug view (if any). When a debug
 * target selection exists, the "debuggerActive" System property is set to true.
 * This property is used to make the "Run to Line", "Resume at Line",
 * "Move to Line" and "Add Watch Expression" actions
 * visible in editors only if there is a running debug session. 
 */
public class EvaluationContextManager implements IWindowListener, IDebugContextListener {

    // Avoid referencing the cdt.debug.ui plugin for this constnat so that the 
    // cdt.debug.ui is not automatically activated
    // Bug 343867.
    private static final String CDT_DEBUG_UI_PLUGIN_ID = "org.eclipse.cdt.debug.ui"; //$NON-NLS-1$
    
	// Must use the same ID than the base CDT uses since we want to enable actions that are registered by base CDT. 
	private final static String DEBUGGER_ACTIVE = CDT_DEBUG_UI_PLUGIN_ID + ".debuggerActive"; //$NON-NLS-1$

	protected static EvaluationContextManager fgManager;

	protected EvaluationContextManager() {
	}

	public static void startup() {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				if ( fgManager == null ) {
					// FindBugs reported that it is unsafe to set s_resources
					// before we finish to initialize the object, because of
					// multi-threading.  This is why we use a temporary variable.
					EvaluationContextManager manager = new EvaluationContextManager();
					IWorkbench workbench = PlatformUI.getWorkbench();
					IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
					for( int i = 0; i < windows.length; i++ ) {
						manager.windowOpened( windows[i] );
					}
					workbench.addWindowListener( manager );
					
					fgManager = manager;
				}
			}
		};
		Display display = Display.getCurrent();
		if ( display == null )
			display = Display.getDefault();
		display.asyncExec( r );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void windowActivated( IWorkbenchWindow window ) {
	    IDebugContextService service = DebugUITools.getDebugContextManager().getContextService(window);
        service.addDebugContextListener(this);
        selectionChanged( service.getActiveContext() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void windowDeactivated( IWorkbenchWindow window ) {
        DebugUITools.getDebugContextManager().getContextService(window).removeDebugContextListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void windowClosed( IWorkbenchWindow window ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void windowOpened( IWorkbenchWindow window ) {
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
	    selectionChanged(event.getContext());
	}
	
	private void selectionChanged(ISelection selection ) {
        if ( selection instanceof IStructuredSelection ) {
            IStructuredSelection ss = (IStructuredSelection)selection;
            if ( ss.size() == 1 ) {
                Object element = ss.getFirstElement();
                if ( element instanceof IDMVMContext ) {
                    setContext( (IDMVMContext)element );
                    return;
                }
            }
        }
        // no context in the given view
        removeContext();
	}

	/**
	 * Sets the evaluation context.
	 */
	private void setContext( IDMVMContext target ) {
		System.setProperty( DEBUGGER_ACTIVE, Boolean.TRUE.toString() );
	}

	/**
	 * Removes an evaluation context.
	 */
	private void removeContext() {
	    System.setProperty( DEBUGGER_ACTIVE, Boolean.FALSE.toString() );
	}
}
