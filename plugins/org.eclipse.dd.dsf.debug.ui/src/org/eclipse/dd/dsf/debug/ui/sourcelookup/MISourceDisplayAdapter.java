/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.ui.sourcelookup;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.Query;
import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.internal.ui.DsfDebugUIPlugin;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IStack;
import org.eclipse.dd.dsf.debug.service.StepQueueManager;
import org.eclipse.dd.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.dd.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.dd.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.dd.dsf.debug.sourcelookup.DsfMISourceLookupParticipant;
import org.eclipse.dd.dsf.service.DsfServiceEventHandler;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.sourcelookup.CommonSourceNotFoundEditorInput;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Source display adapter that performs the source lookup, opens the editor, 
 * and paints the IP for the given object.  
 * <p>
 * The implementation relies on three types of jobs to perform the operations.<br>
 * - The first kind, "lookup job" performs the source lookup operation. <br>  
 * - The second "display job" positions and annotates the editor. <br>  
 * - The third clears the old IP annotations when a thread or process has resumed
 * or exited.
 * <p>
 * The the lookup jobs can run in parallel with the display or the clearing job, 
 * but the clearing job and the display job must not run at the same time.
 * Hence there is some involved logic which ensures that the jobs are run in 
 * proper order.  To avoid race conditions, this logic uses the session's 
 * dispatch thread to synchronize access to the state data of the running jobs.
 */
@ThreadSafe
public class MISourceDisplayAdapter implements ISourceDisplay 
{
    /**
	 * A job to perform source lookup on the given DMC.
	 */
	class LookupJob extends Job {
		
		private IDMContext fDmc;
		private IWorkbenchPage fPage;

		/**
		 * Constructs a new source lookup job.
		 */
		public LookupJob(IDMContext dmc, IWorkbenchPage page) {
			super("DSF Source Lookup");  //$NON-NLS-1$
			setPriority(Job.INTERACTIVE);
			setSystem(true);
			fDmc = dmc;
			fPage = page;
		}

        IDMContext getDmc() { return fDmc; }
        
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
        protected IStatus run(final IProgressMonitor monitor) {
			if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            
			final SourceLookupResult result = performLookup();
            executeFromJob(new DsfRunnable() { public void run() {
                if (!monitor.isCanceled()) { 
                    fPrevResult = result;
                    fPrevModelContext = fDmc;
                    fRunningLookupJob = null;
                    startDisplayJob(fPrevResult, fPage);
                }
            }});
			return Status.OK_STATUS;
		}
        
        private SourceLookupResult performLookup() {
            SourceLookupResult result = new SourceLookupResult(fDmc, null, null, null);
            String editorId = null;
            IEditorInput editorInput = null;
            Object sourceElement = fSourceLookup.getSourceElement(fDmc);

            if (sourceElement == null) {
                editorInput = new CommonSourceNotFoundEditorInput(fDmc);
                editorId = IDebugUIConstants.ID_COMMON_SOURCE_NOT_FOUND_EDITOR;
            } else if (sourceElement instanceof IFile) {
                editorId = getEditorIdForFilename(((IFile)sourceElement).getName());
                editorInput = new FileEditorInput((IFile)sourceElement);
            }
            result.setEditorInput(editorInput);
            result.setEditorId(editorId);
            result.setSourceElement(sourceElement);

            return result;
        }
        
        private String getEditorIdForFilename(String filename) {
            IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
            IEditorDescriptor descriptor = registry.getDefaultEditor(filename);
            if (descriptor == null) {
                return "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
            } 
                
            return descriptor.getId();
        }
	}

    /**
     * Job that positions the editor and paints the IP Annotation for given DMC. 
     */
	class DisplayJob extends UIJob {
		private SourceLookupResult fResult;
		private IWorkbenchPage fPage;
        
        IDMContext getDmc() { return fResult.getDmc(); }
        
		/**
		 * Constructs a new source display job
		 */
		public DisplayJob(SourceLookupResult result, IWorkbenchPage page) {
			super("Debug Source Display");  //$NON-NLS-1$
			setSystem(true);
			setPriority(Job.INTERACTIVE);
			fResult = result;
			fPage = page;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
        public IStatus runInUIThread(final IProgressMonitor monitor) {
            DsfRunnable displayJobFinishedRunnable = new DsfRunnable() { 
                public void run() {
                    // If the current display job does not match up with "this", it means that this job got cancelled
                    // after it already completed and after this runnable was queued into the dispatch thread.
                    if (fRunningDisplayJob == DisplayJob.this) {
                        fRunningDisplayJob = null;
                        serviceDisplayAndClearingJobs();
                    }
                }
            }; 
            
            if (monitor.isCanceled()) {
                executeFromJob(displayJobFinishedRunnable);
                return Status.CANCEL_STATUS;
            }
            
            IEditorPart editor = openEditor(fResult, fPage);
            if (editor == null) {
                executeFromJob(displayJobFinishedRunnable);
                return Status.OK_STATUS;
            }

            ITextEditor textEditor = null;
            if (editor instanceof ITextEditor) {                    
                textEditor = (ITextEditor)editor;
            } else {
                textEditor = (ITextEditor) editor.getAdapter(ITextEditor.class);
            }
            if (textEditor != null) {
                positionEditor(textEditor, fResult.getDmc());
            }

            executeFromJob(displayJobFinishedRunnable);

			return Status.OK_STATUS;
		}
		
        /**
         * Opens the editor used to display the source for an element selected in
         * this view and returns the editor that was opened or <code>null</code> if
         * no editor could be opened.
         */
        private IEditorPart openEditor(SourceLookupResult result, IWorkbenchPage page) {
            IEditorInput input= result.getEditorInput();
            String id= result.getEditorId();
            if (input == null || id == null) {
                return null;
            }
            
            return openEditor(page, input, id);
        }   

        /**
         * Opens an editor in the workbench and returns the editor that was opened
         * or <code>null</code> if an error occurred while attempting to open the
         * editor.
         */
        private IEditorPart openEditor(final IWorkbenchPage page, final IEditorInput input, final String id) {
            final IEditorPart[] editor = new IEditorPart[] {null};
            Runnable r = new Runnable() {
                public void run() {
                    if (!page.getWorkbenchWindow().getWorkbench().isClosing()) {
                        try {
                            editor[0] = page.openEditor(input, id, false);
                        } catch (PartInitException e) {}
                    }
                }
            }; 
            BusyIndicator.showWhile(Display.getDefault(), r);
            return editor[0];
        }   

        /**
         * Positions the text editor for the given stack frame
         */
        private void positionEditor(ITextEditor editor, final IDMContext dmc) {
            if (!(dmc instanceof IFrameDMContext)) return;
            final IFrameDMContext frameDmc = (IFrameDMContext)dmc;
            
            // We need to retrieve the frame level and line number from the service.  
            // Normally we could just get the needed information from IFrameDMData, but
            // IFrameDMData, which derives from IModelData can only be accessed on the 
            // dispatch thread, so we need to copy over relevant information from 
            // IFrameDMData into this structure so we can read it in the job thread.
            class FramePositioningData {
                int fLine;
                int fLevel;
            }
            
            // Query the service for frame data.  We are calling from a job thread, 
            // so we use the Query.get() method, which will block until the 
            // query is completed.
            Query<FramePositioningData> query = new Query<FramePositioningData>() {
                @Override
                protected void execute(final DataRequestMonitor<FramePositioningData> rm) {
                    IStack stackService = fServicesTracker.getService(IStack.class); 
                    if (stackService == null) {
                        doneException(new CoreException(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, -1, "Stack data not available", null))); //$NON-NLS-1$
                        return;
                    }
                	stackService.getFrameData(
                        frameDmc, 
                        new DataRequestMonitor<IFrameDMData>(fExecutor, rm) { 
                            @Override
                            public void handleOK() {
                                FramePositioningData clientData = new FramePositioningData();
                                clientData.fLevel = frameDmc.getLevel();
                                // Document line numbers are 0-based. While debugger line numbers are 1-based.
                                clientData.fLine = getData().getLine() - 1;
                                rm.setData(clientData);
                                rm.done();
                    		}
            			});
                }
            };
            try {
                fExecutor.execute(query);
                FramePositioningData framePositioningData = query.get();
                // If the frame data is not available, or the line number is not 
                // known, give up.
                if (framePositioningData == null || framePositioningData.fLevel < 0) {
                    return;
                }
                
                // Position and annotate the editor.
                IRegion region= getLineInformation(editor, framePositioningData.fLine);
                if (region != null) {
                    editor.selectAndReveal(region.getOffset(), 0);
                    fIPManager.addAnnotation(
                        editor, frameDmc, new Position(region.getOffset(), region.getLength()), 
                        framePositioningData.fLevel == 0);
                }
            } catch (InterruptedException e) { assert false : "Interrupted exception in DSF thread"; //$NON-NLS-1$
            } catch (ExecutionException e) { // Ignore 
            }
            
            
        }
    
        /**
         * Returns the line information for the given line in the given editor
         */
        private IRegion getLineInformation(ITextEditor editor, int lineNumber) {
            IDocumentProvider provider= editor.getDocumentProvider();
            IEditorInput input= editor.getEditorInput();
            try {
                provider.connect(input);
            } catch (CoreException e) {
                return null;
            }
            try {
                IDocument document= provider.getDocument(input);
                if (document != null)
                    return document.getLineInformation(lineNumber);
            } catch (BadLocationException e) {
            } finally {
                provider.disconnect(input);
            }
            return null;
        }   

    }	
    
    /**
     * Job that removes the old IP Annotations associated with given execution 
     * context.
     */
    class ClearingJob extends UIJob {
        List<IRunControl.IExecutionDMContext> fDmcsToClear;
        
        public ClearingJob(List<IRunControl.IExecutionDMContext> dmcs) {
            super("Debug Source Display");  //$NON-NLS-1$
            setSystem(true);
            setPriority(Job.INTERACTIVE);
            fDmcsToClear = dmcs;
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
            DsfRunnable clearingJobFinishedRunnable = new DsfRunnable() { public void run() {
                assert fRunningClearingJob == ClearingJob.this;
                fRunningClearingJob = null;
                serviceDisplayAndClearingJobs();
            }}; 
            
            if (monitor.isCanceled()) {
                executeFromJob(clearingJobFinishedRunnable);
                return Status.CANCEL_STATUS;
            }
            
            for (IRunControl.IExecutionDMContext dmc : fDmcsToClear) {
                fIPManager.removeAnnotations(dmc);
            }
            
            executeFromJob(clearingJobFinishedRunnable);
            return Status.OK_STATUS;
        }
    }

    private DsfSession fSession;
    private DsfExecutor fExecutor;
    private DsfServicesTracker fServicesTracker;
    private IDMContext fPrevModelContext;
    private SourceLookupResult fPrevResult;
    private ISourceLookupDirector fSourceLookup;
    private DsfMISourceLookupParticipant fSourceLookupParticipant;
    private InstructionPointerManager fIPManager;
    
    private LookupJob fRunningLookupJob;
    private DisplayJob fRunningDisplayJob;
    private DisplayJob fPendingDisplayJob;
    private ClearingJob fRunningClearingJob;
    private List<IRunControl.IExecutionDMContext> fPendingExecDmcsToClear = new LinkedList<IRunControl.IExecutionDMContext>();
    
    public MISourceDisplayAdapter(DsfSession session, ISourceLookupDirector sourceLocator) {
        fSession = session;
        fExecutor = session.getExecutor();
        fServicesTracker = new DsfServicesTracker(DsfDebugUIPlugin.getBundleContext(), session.getId());
        fSourceLookup = sourceLocator;
        fSourceLookupParticipant = new DsfMISourceLookupParticipant(session); 
        fSourceLookup.addParticipants(new ISourceLookupParticipant[] {fSourceLookupParticipant} );

        fIPManager = new InstructionPointerManager();
        
        fSession.addServiceEventListener(this, null);
    }
    
    public void dispose() {
        fSession.removeServiceEventListener(this);
        fServicesTracker.dispose();
        fSourceLookup.removeParticipants(new ISourceLookupParticipant[] {fSourceLookupParticipant});
        
        // fSourceLookupParticipant is disposed by the source lookup director
        
        // Need to remove annotations in UI thread.
        //fIPManager.removeAllAnnotations();
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.ISourceDisplayAdapter#displaySource(java.lang.Object, org.eclipse.ui.IWorkbenchPage, boolean)
	 */
    public void displaySource(Object context, final IWorkbenchPage page, final boolean force) {
        if (!(context instanceof IDMVMContext)) return;
        final IDMContext dmc = ((IDMVMContext)context).getDMContext();

        // Quick test.  DMC is checked again in source lookup participant, but 
        // it's much quicker to test here. 
        if (!(dmc instanceof IFrameDMContext)) return;

        // Re-dispatch to executor thread before accessing job lists.
        fExecutor.execute(new DsfRunnable() { public void run() {
            if (!force && dmc.equals(fPrevModelContext)) {
                fPrevResult.updateArtifact(dmc);
                startDisplayJob(fPrevResult, page);
            } else {
                startLookupJob(dmc, page);
            }
        }});
	}
    
    private void executeFromJob(Runnable runnable) {
        try {
            fExecutor.execute(runnable);
        } catch (RejectedExecutionException e) {
            // Session disposed, ignore
        }
    }
    
    private void startLookupJob(final IDMContext dmc, final IWorkbenchPage page) {
        // If there is a previous lookup job running, cancel it.
        if (fRunningLookupJob != null) {
            fRunningLookupJob.cancel();
        }
        
        fRunningLookupJob = new LookupJob(dmc, page);
        fRunningLookupJob.schedule();
    }
    
    // To be called only on dispatch thread. 
    private void startDisplayJob(SourceLookupResult lookupResult, IWorkbenchPage page) {
        DisplayJob nextDisplayJob = new DisplayJob(lookupResult, page);
        if (fRunningDisplayJob != null) {
            // There is a display job currently running.  Cancel it, and set 
            // the next display job to be run.
            if (false && fRunningDisplayJob.cancel()) {
                fPendingDisplayJob = nextDisplayJob;
                fRunningDisplayJob = null;
                serviceDisplayAndClearingJobs();
            } else {
                // The job already started, so we need to wait until 
                // serviceDisplayAndClearingJobs() is called by the job itself.
                fPendingDisplayJob = nextDisplayJob;
            }
        } else if (fRunningClearingJob != null) {
            // Wait for the clearing job to finish, instead, set the 
            // display job as pending.
            fPendingDisplayJob = nextDisplayJob;
        } else {
            fRunningDisplayJob = nextDisplayJob;
            fRunningDisplayJob.schedule();        
        }
    }

    
    private void serviceDisplayAndClearingJobs() {
        if (!fPendingExecDmcsToClear.isEmpty()) {
            // There are annotations to be cleared, run the job first
            fRunningClearingJob = new ClearingJob(fPendingExecDmcsToClear);
            fRunningClearingJob.schedule();
            fPendingExecDmcsToClear = new LinkedList<IRunControl.IExecutionDMContext>();            
        } else if (fPendingDisplayJob != null) {
            fRunningDisplayJob = fPendingDisplayJob;
            fRunningDisplayJob.schedule();
            fPendingDisplayJob = null;
        }
    }
    
    private void startAnnotationClearingJob(IRunControl.IExecutionDMContext execDmc) {
        // Make sure to add the dmc to the list.
        fPendingExecDmcsToClear.add(execDmc);
        
        // If lookup job is running, check it agains the exec context,
        // and cancel it if matches.
        if (fRunningLookupJob != null) {
            if (DMContexts.isAncestorOf(fRunningLookupJob.getDmc(), execDmc)) {
                fRunningLookupJob.cancel();
                fRunningLookupJob = null;
            }
        }
        // If there is a pending displahy job, make sure it doesn't get 
        // pre-empted by this event.  If so, just cancel the pending
        // display job.
        if (fPendingDisplayJob != null) {
            if (DMContexts.isAncestorOf(fPendingDisplayJob.getDmc(), execDmc)) {
                fPendingDisplayJob = null;
            }
        }
        
        // If no display or clearing jobs are running, schedule the clearing job.
        if (fRunningClearingJob == null && fRunningDisplayJob == null) {
            fRunningClearingJob = new ClearingJob(fPendingExecDmcsToClear);
            fRunningClearingJob.schedule();
            fPendingExecDmcsToClear = new LinkedList<IRunControl.IExecutionDMContext>();
        }
    }
    
    @DsfServiceEventHandler
    public void eventDispatched(IRunControl.IResumedDMEvent e) {
        if (e.getReason() != StateChangeReason.STEP) {
            startAnnotationClearingJob(e.getDMContext());
        }
    }

    @DsfServiceEventHandler
    public void eventDispatched(IRunControl.IExitedDMEvent e) {
        startAnnotationClearingJob(e.getExecutionContext());        
    }

    @DsfServiceEventHandler
    public void eventDispatched(StepQueueManager.ISteppingTimedOutEvent e) {
        startAnnotationClearingJob(e.getDMContext());        
    }    
    
    @DsfServiceEventHandler
    public void eventDispatched(IRunControl.ISuspendedDMEvent e) {
        fPrevModelContext = null;
    }
}
