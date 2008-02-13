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
package org.eclipse.dd.examples.dsf.timers;

import java.util.concurrent.ExecutionException;

import org.eclipse.dd.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.dd.examples.dsf.DsfExamplesPlugin;
import org.eclipse.dd.examples.dsf.timers.TimerService.TimerDMC;
import org.eclipse.dd.examples.dsf.timers.TimersVMProvider.ViewLayout;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;


/**
 * Example view which displays data from timers and alarms DSF services.  This 
 * starts a new DSF session and configures the services for it.  Then it 
 * configures a data model provider to process the service data and display it
 * in a flexible-hierarchy asynchronous viewer.
 */
@SuppressWarnings("restriction")
public class TimersView extends ViewPart {
    
    /** Asynchronous tree viewer from the platform debug.ui plugin. */
	private TreeModelViewer fViewer;
    
    /** DSF executor to use for a new session with timers and alarms services */
    private DsfExecutor fExecutor;
    
    /** DSF session */
    private DsfSession fSession;
    
    /** DSF services tracker used by actions in the viewer. */
    private DsfServicesTracker fServices;
    
    /** Adapter used to provide view model for flexible-hierarchy viewer */
    private TimersModelAdapter fTimersModelAdapter;
    
    /** Action which toggles the layout in the viewer */
    private Action fToggleLayoutAction;
    
    /** Action that adds a new timer */
	private Action fAddTimerAction;
    
    /** Action that adds a new alarm */
    private Action fAddAlarmAction;
    
    /** Action that removes the selected alarm or timer */
	private Action fRemoveAction;

	public TimersView() {}

	/**
	 * This is a callback that will allow us to create the viewer and 
     * initialize it.  For this view, it creates the DSF session, along
     * with its services.  Then it creates the viewer model adapter and 
     * registers it with the session.
	 */
	@Override
    public void createPartControl(Composite parent) {
        /*
         * Create the Flexible Hierarchy viewer.  Also create a presentation
         * context which will be given to the content/label provider adapters 
         * to distinguish this view from other flex-hierarchy views. 
         */
        final IPresentationContext presentationContext = new PresentationContext("org.eclipse.dd.examples.dsf.timers"); //$NON-NLS-1$
		fViewer = new TreeModelViewer(parent, SWT.VIRTUAL | SWT.FULL_SELECTION, presentationContext);
        
        /*
         * Create the executor, which will be used exclusively with this view, 
         * as well as a session and a services tracker for managing references 
         * to services.
         */
        fExecutor = new DefaultDsfExecutor();
        fSession = DsfSession.startSession(fExecutor, "org.eclipse.dd.examples.dsf.timers"); //$NON-NLS-1$
        fServices = new DsfServicesTracker(DsfExamplesPlugin.getBundleContext(), fSession.getId());

        /*
         * Start the services using a sequence.  The sequence runs in the 
         * dispatch thread, so we have to block this thread using Future.get() 
         * until it completes.  The Future.get() will throw an exception if 
         * the sequence fails.
         */
        ServicesStartupSequence startupSeq = new ServicesStartupSequence(fSession);
        fSession.getExecutor().execute(startupSeq);
        try {
            startupSeq.get();
        } catch (InterruptedException e) { assert false;
        } catch (ExecutionException e) { assert false;
        }
        
        /*
         * Create the flexible hierarchy content/label adapter. Then register 
         * it with the session. 
         */
        fTimersModelAdapter = new TimersModelAdapter(fSession, presentationContext);
        fSession.registerModelAdapter(IElementContentProvider.class, fTimersModelAdapter);
        fSession.registerModelAdapter(IModelProxyFactory.class, fTimersModelAdapter);
        fSession.registerModelAdapter(IColumnPresentationFactory.class, fTimersModelAdapter);
        
        /*
         * Set the root element for the timers tree viewer.  The root element 
         * comes from the content provider.
         */
        fViewer.setInput(fTimersModelAdapter.getTimersVMProvider().getViewerInputObject());
        
		makeActions();
		contributeToActionBars();
	}

    @Override
    public void dispose() {
        try {
            /*
             * First dispose the view model, which is the client of services.
             * We are not in the dispatch thread
             */
            fSession.getExecutor().submit(new Runnable() { 
                public void run() {
                    fSession.unregisterModelAdapter(IElementContentProvider.class);
                    fSession.unregisterModelAdapter(IModelProxyFactory.class);
                    fSession.unregisterModelAdapter(IColumnPresentationFactory.class);
                    fTimersModelAdapter.dispose();
                    fTimersModelAdapter = null;
                }}).get();
            
            // Then invoke the shutdown sequence for the services.
            ServicesShutdownSequence shutdownSeq = new ServicesShutdownSequence(fSession);
            fSession.getExecutor().execute(shutdownSeq);
            try {
                shutdownSeq.get();
            } catch (InterruptedException e) { assert false;
            } catch (ExecutionException e) { assert false;
            }
            
            // Finally end the session and the executor:
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
        //fViewer.dispose();
        super.dispose();
    }

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(fToggleLayoutAction);
		manager.add(fAddTimerAction);
        manager.add(fAddAlarmAction);
		manager.add(fRemoveAction);
		manager.add(new Separator());
	}

	private void makeActions() {
        fToggleLayoutAction = new Action("Toggle Layout", IAction.AS_CHECK_BOX) { //$NON-NLS-1$
            @Override
            public void run() {
                // Get the toggle state of the action while on UI thread.
                final ViewLayout layout = isChecked() ? ViewLayout.ALARMS_AT_TOP : ViewLayout.TIMERS_AT_TOP;
                
                // Switch to executor thread to perform the change in layout.
                fExecutor.submit(new Runnable() { public void run() {
                    fTimersModelAdapter.getTimersVMProvider().setViewLayout(layout);
                }});
            }
        };
        fToggleLayoutAction.setToolTipText("Toggle Layout"); //$NON-NLS-1$
        fToggleLayoutAction.setImageDescriptor(DsfExamplesPlugin.getDefault().getImageRegistry().getDescriptor(
            DsfExamplesPlugin.IMG_LAYOUT_TOGGLE));
        
        fAddTimerAction = new Action("Add New Timer") { //$NON-NLS-1$
			@Override
            public void run() {
                fExecutor.submit(new Runnable() { public void run() {
                    // Only need to create the new timer, the events will cause 
                    // the view to refresh.
                    fServices.getService(TimerService.class).startTimer();
                }});
			}
		};
		fAddTimerAction.setToolTipText("Add new timer"); //$NON-NLS-1$
		fAddTimerAction.setImageDescriptor(DsfExamplesPlugin.getDefault().getImageRegistry().getDescriptor(
            DsfExamplesPlugin.IMG_TIMER));

        fAddAlarmAction = new Action("Add New Alarm") { //$NON-NLS-1$
            @Override
            public void run() {
                // Ask user for the new alarm value.
                InputDialog inputDialog = new InputDialog(
                    fViewer.getControl().getShell(), 
                    "New Alarm",  //$NON-NLS-1$
                    "Please enter alarm time", //$NON-NLS-1$
                    "", //$NON-NLS-1$
                    new IInputValidator() {
                        public String isValid(String input) {
                            try {
                                int i= Integer.parseInt(input);
                                if (i <= 0)
                                    return "Please enter a positive integer";  //$NON-NLS-1$
    
                            } catch (NumberFormatException x) {
                                return "Please enter a positive integer";  //$NON-NLS-1$
                            }
                            return null;
                        }                    
                    }
                );
                if (inputDialog.open() != Window.OK) return;
                int tmpAlarmValue = -1;
                try {
                    tmpAlarmValue = Integer.parseInt(inputDialog.getValue());
                } catch (NumberFormatException x) { assert false; }
                final int alarmValue = tmpAlarmValue;
                fExecutor.submit(new Runnable() { public void run() {
                    // Create the new alarm.
                    fServices.getService(AlarmService.class).createAlarm(alarmValue);
                }});
            }
        };
        fAddAlarmAction.setToolTipText("Add new alarm"); //$NON-NLS-1$
        fAddAlarmAction.setImageDescriptor(DsfExamplesPlugin.getDefault().getImageRegistry().getDescriptor(
            DsfExamplesPlugin.IMG_ALARM));

		fRemoveAction = new Action("Remove") { //$NON-NLS-1$
            @Override
            public void run() {
                final Object selectedElement = ((IStructuredSelection)fViewer.getSelection()).getFirstElement();
                if (!(selectedElement instanceof IDMVMContext)) return;
                final IDMContext selectedDmc = ((IDMVMContext)selectedElement).getDMContext();
                // Based on the DMC from the selection, call the appropriate service to 
                // remove the item.
                if (selectedDmc instanceof TimerDMC) {
                    fExecutor.submit(new Runnable() { public void run() {
                        fServices.getService(TimerService.class).killTimer(
                            ((TimerDMC)selectedDmc));
                    }});
                } else if (selectedDmc instanceof AlarmService.AlarmDMC) {
                    fExecutor.submit(new Runnable() { public void run() {
                        fServices.getService(AlarmService.class).deleteAlarm(
                            (AlarmService.AlarmDMC)selectedDmc);
                    }});
                }
			}
		};
		fRemoveAction.setToolTipText("Remove selected item"); //$NON-NLS-1$
		fRemoveAction.setImageDescriptor(DsfExamplesPlugin.getDefault().getImageRegistry().getDescriptor(
            DsfExamplesPlugin.IMG_REMOVE));
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
    public void setFocus() {
		fViewer.getControl().setFocus();
	}
}