/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.timers;

import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.examples.dsf.DsfExamplesPlugin;
import org.eclipse.cdt.examples.dsf.timers.TimerService.TimerDMContext;
import org.eclipse.cdt.examples.dsf.timers.TimersVMProvider.ViewLayout;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;


/**
 * Example view which displays data from timers and alarms services. It starts 
 * a new DSF session and configures the services for it.  Then it configures 
 * a data model provider to process the service data and display it in a 
 * flexible-hierarchy asynchronous viewer.
 */
@SuppressWarnings("restriction")
public class TimersView extends ViewPart {
    
    /** Timers view ID */
    public static final String ID_VIEW_TIMERS = "org.eclipse.cdt.examples.dsf.TimersView";
    
    /** Asynchronous tree viewer from the platform debug.ui plugin. */
	private TreeModelViewer fViewer;
    
	/** Presentation context of the timers viewer */
	private PresentationContext fPresentationContext;
	
    /** DSF executor to use for a new session with timers and alarms services */
    private DsfExecutor fExecutor;
    
    /** DSF session */
    private DsfSession fSession;
    
    /** DSF services tracker used by actions in the viewer. */
    private DsfServicesTracker fServices;
    
    /** Adapter used to provide view model for flexible-hierarchy viewer */
    private TimersVMAdapter fTimersVMAdapter;
    
    /** Action which toggles the layout in the viewer */
    private Action fToggleLayoutAction;
    
    /** Action that adds a new timer */
	private Action fAddTimerAction;
    
    /** Action that adds a new trigger */
    private Action fAddTriggerAction;
    
    /** Action that removes the selected trigger or timer */
	private Action fRemoveAction;

	public TimersView() {}

	/**
	 * This is a call-back that will allow us to create the viewer and 
     * initialize it.  For this view, it creates the DSF session, along
     * with its services.  Then it creates the viewer model adapter and 
     * registers it with the session.
	 */
	@Override
    public void createPartControl(Composite parent) {
        // Create the Flexible Hierarchy viewer.  Also create a presentation
        // context which will be given to the content/label provider adapters 
        // to distinguish this view from other flexible-hierarchy views. 
        fPresentationContext = new PresentationContext(ID_VIEW_TIMERS); 
		fViewer = new TreeModelViewer(
		    parent, SWT.VIRTUAL | SWT.FULL_SELECTION, fPresentationContext);
        
        // Create the executor, which will be used exclusively with this view, 
        // as well as a session and a services tracker for managing references 
        // to services.
        fExecutor = new DefaultDsfExecutor();
        fSession = DsfSession.startSession(fExecutor, "Timers(DSF Example)"); 
        fServices = new DsfServicesTracker(
            DsfExamplesPlugin.getBundleContext(), fSession.getId());

        // Start the services using a sequence.  The sequence runs in the 
        // session executor thread, therefore the thread calling this method 
        // has to block using Future.get() until the sequence it completes.  
        // The Future.get() will throw an exception if the sequence fails.
        ServicesStartupSequence startupSeq = new ServicesStartupSequence(fSession);
        fSession.getExecutor().execute(startupSeq);
        try {
            startupSeq.get();
        } catch (InterruptedException e) { assert false;
        } catch (ExecutionException e) { assert false;
        }
        
        // Create the flexible hierarchy content/label adapter. Then register 
        // it with the session.
        fTimersVMAdapter = new TimersVMAdapter(fSession, fPresentationContext);
        fSession.registerModelAdapter(IElementContentProvider.class, fTimersVMAdapter);
        fSession.registerModelAdapter(IModelProxyFactory.class, fTimersVMAdapter);
        fSession.registerModelAdapter(IColumnPresentationFactory.class, fTimersVMAdapter);
        
        // Create the input object for the view.  This object needs to return
        // the VM adapter through the IAdaptable interface when queried for the 
        // flexible hierarchy adapters.
        final IAdaptable viewerInputObject = 
            new IAdaptable() {
                /**
                 * The input object provides the viewer access to the viewer model adapter.
                 */
                @SuppressWarnings("unchecked")
                public Object getAdapter(Class adapter) {
                    if ( adapter.isInstance(fTimersVMAdapter) ) {
                        return fTimersVMAdapter;
                    }
                    return null;
                }
                
                @Override
                public String toString() {
                    return "Timers View Root"; //$NON-NLS-1$
                }
            };
        fViewer.setInput(viewerInputObject);
        
		makeActions();
		contributeToActionBars();
	}

    @Override
    public void dispose() {
        try {
            // First dispose the view model, which is the client of services.
            // This operation needs to be performed in the session executor 
            // thread.  Block using Future.get() until this call completes.
            fSession.getExecutor().submit(new Runnable() { 
                public void run() {
                    fSession.unregisterModelAdapter(IElementContentProvider.class);
                    fSession.unregisterModelAdapter(IModelProxyFactory.class);
                    fSession.unregisterModelAdapter(IColumnPresentationFactory.class);
                }}).get();

            // Dispose the VM adapter.
            fTimersVMAdapter.dispose();
            fTimersVMAdapter = null;

            // Next invoke the shutdown sequence for the services.  Sequence
            // class also implements Future.get()...
            ServicesShutdownSequence shutdownSeq = 
                new ServicesShutdownSequence(fSession);
            fSession.getExecutor().execute(shutdownSeq);
            try {
                shutdownSeq.get();
            } catch (InterruptedException e) { assert false;
            } catch (ExecutionException e) { assert false;
            }
            
            // Finally end the session and the executor.
            fSession.getExecutor().submit(new Runnable() { 
                public void run() {
                    DsfSession.endSession(fSession);
                    fSession = null;
                    fExecutor.shutdown();
                    fExecutor = null;
                }}).get();
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        }
        super.dispose();
    }

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(fToggleLayoutAction);
		manager.add(fAddTimerAction);
        manager.add(fAddTriggerAction);
		manager.add(fRemoveAction);
		manager.add(new Separator());
	}

	private void makeActions() {
        fToggleLayoutAction = new Action("Toggle Layout", IAction.AS_CHECK_BOX) { //$NON-NLS-1$
            @Override
            public void run() {
                // Get the toggle state of the action while on UI thread.
                final ViewLayout layout = isChecked() ? ViewLayout.TRIGGERS_AT_TOP : ViewLayout.TIMERS_AT_TOP;
                
                IVMProvider provider = fTimersVMAdapter.getVMProvider(fPresentationContext); 
                ((TimersVMProvider)provider).setViewLayout(layout);
            }
        };
        fToggleLayoutAction.setToolTipText("Toggle Layout"); //$NON-NLS-1$
        fToggleLayoutAction.setImageDescriptor(DsfExamplesPlugin.getDefault().getImageRegistry().getDescriptor(
            DsfExamplesPlugin.IMG_LAYOUT_TOGGLE));
        
        fAddTimerAction = new Action("Add New Timer") { 
			@Override
            public void run() {
                fExecutor.execute(new Runnable() { 
                    public void run() {
                        // Only need to create the new timer, the events will 
                        // cause the view to refresh.
                        fServices.getService(TimerService.class).startTimer();
                    }
                });
			}
		};
		fAddTimerAction.setToolTipText("Add a new timer"); 
		fAddTimerAction.setImageDescriptor(
		    getImage(DsfExamplesPlugin.IMG_TIMER));

        fAddTriggerAction = new Action("Add New Trigger") { 
            @Override
            public void run() {
                // Ask user for the new trigger value.
                InputDialog inputDialog = new InputDialog(
                    getSite().getShell(), 
                    "New Trigger",  
                    "Please enter trigger value", 
                    "", 
                    new IInputValidator() {
                        public String isValid(String input) {
                            try {
                                int i= Integer.parseInt(input);
                                if (i <= 0)
                                    return "Please enter a positive integer";  
    
                            } catch (NumberFormatException x) {
                                return "Please enter a positive integer";  
                            }
                            return null;
                        }                    
                    }
                );
                if (inputDialog.open() != Window.OK) return;
                int tmpTriggerValue = -1;
                try {
                    tmpTriggerValue = Integer.parseInt(inputDialog.getValue());
                } catch (NumberFormatException x) { assert false; }
                final int triggerValue = tmpTriggerValue;
                fExecutor.execute(new Runnable() { 
                    public void run() {
                        // Create the new trigger
                        fServices.getService(AlarmService.class).
                            createTrigger(triggerValue);
                    }
                });
            }
        };
        fAddTriggerAction.setToolTipText("Add a new trigger"); 
        fAddTriggerAction.setImageDescriptor(
            getImage(DsfExamplesPlugin.IMG_ALARM));

		fRemoveAction = new Action("Remove") { 
            @Override
            public void run() {
                final Object selectedElement = 
                    ((IStructuredSelection)fViewer.getSelection()).getFirstElement();
                if (!(selectedElement instanceof IDMVMContext)) return;
                final IDMContext selectedCtx = 
                    ((IDMVMContext)selectedElement).getDMContext();
                // Based on the context from the selection, call the 
                // appropriate service to remove the item.
                if (selectedCtx instanceof TimerDMContext) {
                    fExecutor.execute(new Runnable() { public void run() {
                        fServices.getService(TimerService.class).killTimer(
                            ((TimerDMContext)selectedCtx));
                    }});
                } else if (selectedCtx instanceof AlarmService.TriggerDMContext) {
                    fExecutor.execute(new Runnable() { public void run() {
                        fServices.getService(AlarmService.class).deleteTrigger(
                            (AlarmService.TriggerDMContext)selectedCtx);
                    }});
                }
			}
		};
		fRemoveAction.setToolTipText("Remove selected item"); 
		fRemoveAction.setImageDescriptor( getImage(DsfExamplesPlugin.IMG_REMOVE) );
	}

	private ImageDescriptor getImage(String key) {
        return DsfExamplesPlugin.getDefault().getImageRegistry().getDescriptor(key);
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
    public void setFocus() {
		fViewer.getControl().setFocus();
	}
}