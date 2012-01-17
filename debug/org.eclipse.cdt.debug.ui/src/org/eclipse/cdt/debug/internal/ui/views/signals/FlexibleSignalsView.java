/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.signals;

import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputRequestor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ViewerInputService;
import org.eclipse.debug.internal.ui.views.DebugModelPresentationContext;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

/**
 * A Signals view based on flexible hierarchy.
 */
public class FlexibleSignalsView extends AbstractDebugView implements IViewerUpdateListener, IDebugContextListener, IModelChangedListener {

	private PresentationContext fPresentationContext;

	private DelegatingModelPresentation fModelPresentation;

	/**
	 * Viewer input requester used to update the viewer once the viewer input has been
	 * resolved.
	 */
	private IViewerInputRequestor fRequester = new IViewerInputRequestor() {
		@Override
		public void viewerInputComplete(IViewerInputUpdate update) {
			if (!update.isCanceled()) {
			    viewerInputUpdateComplete(update);
			}
		}
	};

	private ViewerInputService fInputService;

	@Override
	protected Viewer createViewer(Composite parent) {
		getModelPresentation();
		TreeModelViewer signalsViewer = createTreeViewer(parent);
		fInputService = new ViewerInputService(signalsViewer, fRequester);
		getSite().setSelectionProvider(signalsViewer);
		signalsViewer.addModelChangedListener(this);
		signalsViewer.addViewerUpdateListener(this);
		return signalsViewer;
	}

	/**
	 * @return the model presentation to be used for this view
	 */
	protected IDebugModelPresentation getModelPresentation() {
		if (fModelPresentation == null) {
			fModelPresentation = new DelegatingModelPresentation();
		}
		return fModelPresentation;
	}
	
	protected TreeModelViewer createTreeViewer(Composite parent) {
		int style = getViewerStyle();
		fPresentationContext = new DebugModelPresentationContext(getPresentationContextId(), this, fModelPresentation); 
		final TreeModelViewer variablesViewer = new TreeModelViewer(parent, style, fPresentationContext);
		
		variablesViewer.getPresentationContext().addPropertyChangeListener(
				new IPropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent event) {
						if (IPresentationContext.PROPERTY_COLUMNS.equals(event.getProperty())) {
							IAction action = getAction("ShowTypeNames"); //$NON-NLS-1$
							if (action != null) {
								action.setEnabled(event.getNewValue() == null);
							}
						}
					}
				});
		
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).addDebugContextListener(this);
		return variablesViewer;
	}

	protected void viewerInputUpdateComplete(IViewerInputUpdate update) {
	    setViewerInput(update.getInputElement());
	}

	/**
	 * Sets the input to the viewer
	 * @param context the object context
	 */
	protected void setViewerInput(Object context) {
        Object current = getViewer().getInput();
        
        if (current == null && context == null) {
            return;
        }

        if (current != null && current.equals(context)) {
            return;
        }
        
        showViewer();
        getViewer().setInput(context);		
	}

	/**
	 * Returns the presentation context id for this view.
	 * 
	 * @return context id
	 */
	protected String getPresentationContextId() {
		return ICDebugUIConstants.ID_SIGNALS_VIEW;
	}
	
	/**
	 * Returns the style bits for the viewer.
	 * 
	 * @return SWT style
	 */
	protected int getViewerStyle() {
		return SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL | SWT.FULL_SELECTION;
	}
	
	@Override
	protected void createActions() {
	}

	@Override
	protected String getHelpContextId() {
		return ICDebugHelpContextIds.SIGNALS_VIEW;
	}

	@Override
	protected void fillContextMenu(IMenuManager menu) {
		menu.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
		updateObjects();
	}

	@Override
	protected void configureToolBar(IToolBarManager tbm) {
	}

	/*
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener#viewerUpdatesBegin()
	 */
	@Override
	public void viewerUpdatesBegin() {
        IWorkbenchSiteProgressService progressService = 
            (IWorkbenchSiteProgressService)getSite().getAdapter(IWorkbenchSiteProgressService.class);
        if (progressService != null) {
            progressService.incrementBusy();
        }
	}

	/*
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener#viewerUpdatesComplete()
	 */
	@Override
	public void viewerUpdatesComplete() {
        IWorkbenchSiteProgressService progressService = 
            (IWorkbenchSiteProgressService)getSite().getAdapter(IWorkbenchSiteProgressService.class);
        if (progressService != null) {
            progressService.decrementBusy();
        }       
	}

	/*
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener#updateStarted(org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
	 */
	@Override
	public void updateStarted(IViewerUpdate update) {
	}

	/*
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener#updateComplete(org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
	 */
	@Override
	public void updateComplete(IViewerUpdate update) {
		IStatus status = update.getStatus();
		if (!update.isCanceled()) {
			if (status != null && !status.isOK()) {
				showMessage(status.getMessage());
			} else {
				showViewer();
			}
		}
	}

	/*
	 * @see org.eclipse.debug.ui.contexts.IDebugContextListener#debugContextChanged(org.eclipse.debug.ui.contexts.DebugContextEvent)
	 */
	@Override
	public void debugContextChanged(DebugContextEvent event) {
		if ((event.getFlags() & DebugContextEvent.ACTIVATED) > 0) {
			contextActivated(event.getContext());
		}
	}

	/**
	 * Updates actions and sets the viewer input when a context is activated.
	 * @param selection
	 */
	protected void contextActivated(ISelection selection) {
		if (!isAvailable() || !isVisible()) {
			return;
		}
		if (selection instanceof IStructuredSelection) {
			Object source = ((IStructuredSelection)selection).getFirstElement();
			fInputService.resolveViewerInput(source);
		}
	}
		
	/*
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener#modelChanged(org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta, org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy)
	 */
	@Override
	public void modelChanged(IModelDelta delta, IModelProxy proxy) {
	}

}
