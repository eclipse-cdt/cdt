/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.modules; 

import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.elements.adapters.AsynchronousDebugLabelAdapter;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.internal.ui.viewers.IAsynchronousLabelAdapter;
import org.eclipse.debug.internal.ui.viewers.IAsynchronousRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.IAsynchronousTreeContentAdapter;
import org.eclipse.debug.internal.ui.viewers.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.IPresentationContext;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.progress.UIJob;
 
/**
 * Asynchronous viewer used by the Modules view.
 */
public class ModulesViewer extends AsynchronousTreeViewer {
	
	static class ModuleProxyFactory implements IModelProxyFactory {

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.IModelProxyFactory#createModelProxy(java.lang.Object, org.eclipse.debug.internal.ui.viewers.IPresentationContext)
		 */
		public IModelProxy createModelProxy( Object element, IPresentationContext context ) {
			IWorkbenchPart part = context.getPart();
			if ( part != null ) {
				String id = part.getSite().getId();
				if ( ICDebugUIConstants.ID_MODULES_VIEW.equals( id ) ) {
					if ( element instanceof IAdaptable ) {
						ICDebugTarget target = (ICDebugTarget)((IAdaptable)element).getAdapter( ICDebugTarget.class );
						if ( target != null )
							return new ModulesViewModelProxy();
					}
				}
			}
			return null;
		}
	}

	private static IAsynchronousLabelAdapter fgModuleLabelAdapter = new AsynchronousDebugLabelAdapter();
	private static IAsynchronousTreeContentAdapter fgModuleTreeContentAdapter = new ModuleTreeContentAdapter();
	private static IModelProxyFactory fgModuleProxyFactory = new ModuleProxyFactory();

	protected ModulesView fView;

	private UIJob fRestoreJob = new UIJob( "restore viewer state" ) { //$NON-NLS-1$

		public IStatus runInUIThread( IProgressMonitor monitor ) {
			fView.restoreState();
			return Status.OK_STATUS;
		}
	};

	/** 
	 * Constructor for ModulesViewer. 
	 */
	public ModulesViewer( Composite parent, int style, ModulesView view ) {
		super( parent, style );
		fView = view;
		fRestoreJob.setSystem( true );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousViewer#updateComplete(org.eclipse.debug.internal.ui.viewers.IAsynchronousRequestMonitor)
	 */
	protected void updateComplete( IAsynchronousRequestMonitor update ) {
		super.updateComplete( update );
		if ( fView != null ) {
			fRestoreJob.schedule( 100 );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousViewer#handlePresentationFailure(org.eclipse.debug.internal.ui.viewers.IAsynchronousRequestMonitor, org.eclipse.core.runtime.IStatus)
	 */
	protected void handlePresentationFailure( IAsynchronousRequestMonitor update, IStatus status ) {
		fView.showMessage( status.getMessage() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer#getTreeContentAdapter(java.lang.Object)
	 */
	protected IAsynchronousTreeContentAdapter getTreeContentAdapter( Object element ) {
		return fgModuleTreeContentAdapter;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousViewer#getLabelAdapter(java.lang.Object)
	 */
	protected IAsynchronousLabelAdapter getLabelAdapter( Object element ) {
		return fgModuleLabelAdapter;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousViewer#getModelProxyFactoryAdapter(java.lang.Object)
	 */
	protected IModelProxyFactory getModelProxyFactoryAdapter( Object element ) {
		return fgModuleProxyFactory;
	}
}
