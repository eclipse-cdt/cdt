/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
 
/**
 * Manages the current evaluation context (stack frame) for evaluation actions.
 * In each page, the selection is tracked in each debug view (if any). When a debug
 * target selection exists, the "debuggerActive" System property is set to true.
 * This property is used to make the "Run To Line", "Resume At Line",
 * "Move To Line" and "Add Watch Expression" actions
 * visible in editors only if there is a running debug session. 
 */
public class EvaluationContextManager implements IWindowListener, IPageListener, ISelectionListener, IPartListener2 {

    // Avoid referencing the cdt.debug.ui plugin for this constnat so that the 
    // cdt.debug.ui is not automatically activated
    // Bug 343867.
    private static final String CDT_DEBUG_UI_PLUGIN_ID = "org.eclipse.cdt.debug.ui"; //$NON-NLS-1$
    
	// Must use the same ID than the base CDT uses since we want to enable actions that are registered by base CDT. 
	private final static String DEBUGGER_ACTIVE = CDT_DEBUG_UI_PLUGIN_ID + ".debuggerActive"; //$NON-NLS-1$

	protected static EvaluationContextManager fgManager;

	private Map<IWorkbenchPage,IDMVMContext> fContextsByPage = null;

	protected EvaluationContextManager() {
	}

	public static void startup() {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				if ( fgManager == null ) {
					fgManager = new EvaluationContextManager();
					IWorkbench workbench = PlatformUI.getWorkbench();
					IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
					for( int i = 0; i < windows.length; i++ ) {
						fgManager.windowOpened( windows[i] );
					}
					workbench.addWindowListener( fgManager );
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
		windowOpened( window );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void windowDeactivated( IWorkbenchWindow window ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void windowClosed( IWorkbenchWindow window ) {
		window.removePageListener( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void windowOpened( IWorkbenchWindow window ) {
		IWorkbenchPage[] pages = window.getPages();
		for( int i = 0; i < pages.length; i++ ) {
			window.addPageListener( this );
			pageOpened( pages[i] );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageListener#pageActivated(org.eclipse.ui.IWorkbenchPage)
	 */
	@Override
	public void pageActivated( IWorkbenchPage page ) {
		pageOpened( page );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageListener#pageClosed(org.eclipse.ui.IWorkbenchPage)
	 */
	@Override
	public void pageClosed( IWorkbenchPage page ) {
		page.removeSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		page.removePartListener( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageListener#pageOpened(org.eclipse.ui.IWorkbenchPage)
	 */
	@Override
	public void pageOpened( IWorkbenchPage page ) {
		page.addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		page.addPartListener( this );
		IWorkbenchPartReference ref = page.getActivePartReference();
		if ( ref != null ) {
			partActivated( ref );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged( IWorkbenchPart part, ISelection selection ) {
		IWorkbenchPage page = part.getSite().getPage();
		if ( selection instanceof IStructuredSelection ) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			if ( ss.size() == 1 ) {
				Object element = ss.getFirstElement();
				if ( element instanceof IDMVMContext ) {
					setContext( page, (IDMVMContext)element );
					return;
				}
			}
		}
		// no context in the given view
		removeContext( page );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partActivated( IWorkbenchPartReference partRef ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partBroughtToTop( IWorkbenchPartReference partRef ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partClosed( IWorkbenchPartReference partRef ) {
		if ( IDebugUIConstants.ID_DEBUG_VIEW.equals( partRef.getId() ) ) {
			removeContext( partRef.getPage() );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partDeactivated( IWorkbenchPartReference partRef ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partOpened( IWorkbenchPartReference partRef ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partHidden( IWorkbenchPartReference partRef ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partVisible( IWorkbenchPartReference partRef ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partInputChanged( IWorkbenchPartReference partRef ) {
	}

	/**
	 * Sets the evaluation context for the given page, and notes that
	 * a valid execution context exists.
	 * 
	 * @param page
	 * @param frame
	 */
	private void setContext( IWorkbenchPage page, IDMVMContext target ) {
		if ( fContextsByPage == null ) {
			fContextsByPage = new HashMap<IWorkbenchPage,IDMVMContext>();
		}
		fContextsByPage.put( page, target );
		System.setProperty( DEBUGGER_ACTIVE, Boolean.TRUE.toString() );
	}

	/**
	 * Removes an evaluation context for the given page, and determines if
	 * any valid execution context remain.
	 * 
	 * @param page
	 */
	private void removeContext( IWorkbenchPage page ) {
		if ( fContextsByPage != null ) {
			fContextsByPage.remove( page );
			if ( fContextsByPage.isEmpty() ) {
				System.setProperty( DEBUGGER_ACTIVE, Boolean.FALSE.toString() );
			}
		}
	}
}
