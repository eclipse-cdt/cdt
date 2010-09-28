/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.sourcelookup;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceNotFoundElement;
import org.eclipse.cdt.debug.internal.ui.sourcelookup.CSourceNotFoundEditorInput;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.cdt.dsf.debug.sourcelookup.DsfSourceLookupParticipant;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.SteppingController;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.SteppingController.ISteppingControlParticipant;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.SteppingController.SteppingTimedOutEvent;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ISourcePresentation;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
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
 * </p>
 * <p>
 * The the lookup jobs can run in parallel with the display or the clearing job, 
 * but the clearing job and the display job must not run at the same time.
 * Hence there is some involved logic which ensures that the jobs are run in 
 * proper order.  To avoid race conditions, this logic uses the session's 
 * dispatch thread to synchronize access to the state data of the running jobs.
 * </p>
 * <p>
 * Debuggers can override the default source editor used by the source display
 * adapter by registering their own ISourcePresentation adapter. 
 * </p>
 * 
 * @see ISourcePresentation
 * 
 * @since 1.0
 */
@ThreadSafe
public class DsfSourceDisplayAdapter implements ISourceDisplay, ISteppingControlParticipant
{
    private static final class FrameData {
		IFrameDMContext fDmc;
        int fLine;
        int fLevel;
		String fFile;
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FrameData other = (FrameData) obj;
			if (!fDmc.equals(other.fDmc))
				return false;
			if (fFile == null) {
				if (other.fFile != null)
					return false;
			} else if (!fFile.equals(other.fFile))
				return false;
			return true;
		}

		/**
		 * Test whether the given frame data instance refers to the very same location.
		 * 
		 * @param frameData
		 * @return <code>true</code> if the frame data refers to the same location
		 */
		public boolean isIdentical(FrameData frameData) {
			return equals(frameData) && fLine == frameData.fLine;
		}
    }
    
    /**
	 * A job to perform source lookup on the given DMC.
	 */
	class LookupJob extends Job {
		
		private final IWorkbenchPage fPage;
		private final FrameData fFrameData;
		private final boolean fEventTriggered;

		/**
		 * Constructs a new source lookup job.
		 */
		public LookupJob(FrameData frameData, IWorkbenchPage page, boolean eventTriggered) {
			super("DSF Source Lookup");  //$NON-NLS-1$
			setPriority(Job.INTERACTIVE);
			setSystem(true);
			fFrameData = frameData;
			fPage = page;
			fEventTriggered = eventTriggered;
		}

        IDMContext getDmc() { return fFrameData.fDmc; }
        
		@Override
        protected IStatus run(final IProgressMonitor monitor) {
			if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            
			final SourceLookupResult result = performLookup();
            executeFromJob(new DsfRunnable() { public void run() {
                if (!monitor.isCanceled()) { 
                    fPrevResult = result;
                    fPrevFrameData = fFrameData;
                    fRunningLookupJob = null;
                    startDisplayJob(fPrevResult, fFrameData, fPage, fEventTriggered);
                }
            }});
			return Status.OK_STATUS;
		}
        
		private SourceLookupResult performLookup() {
            IDMContext dmc = fFrameData.fDmc;
			SourceLookupResult result = new SourceLookupResult(dmc , null, null, null);
            String editorId = null;
            IEditorInput editorInput = null;
            Object sourceElement = fSourceLookup.getSourceElement(dmc);

            if (sourceElement == null) {
				editorInput = new CSourceNotFoundEditorInput(new CSourceNotFoundElement(dmc, fSourceLookup.getLaunchConfiguration(), fFrameData.fFile));
				editorId = ICDebugUIConstants.CSOURCENOTFOUND_EDITOR_ID;
            } else {
				ISourcePresentation presentation= null;
				if (fSourceLookup instanceof ISourcePresentation) {
					presentation = (ISourcePresentation) fSourceLookup;
				} else {
				    if (dmc != null) {
				        presentation = (ISourcePresentation) dmc.getAdapter(ISourcePresentation.class);
				    }
				}
	            if (presentation != null) {
	            	editorInput = presentation.getEditorInput(sourceElement);
	            	if (editorInput != null) {
	            		editorId = presentation.getEditorId(editorInput, sourceElement);
	            	}
	            } else if (sourceElement instanceof IFile) {
	                editorId = getEditorIdForFilename(((IFile)sourceElement).getName());
	                editorInput = new FileEditorInput((IFile)sourceElement);
	            } else if (sourceElement instanceof ITranslationUnit) {
	            	try {
	                	URI uriLocation = ((ITranslationUnit)sourceElement).getLocationURI();
	            		IFileStore fileStore = EFS.getStore(uriLocation);
	            		editorInput = new FileStoreEditorInput(fileStore);
	            		editorId = getEditorIdForFilename(fileStore.getName());
	            	} catch (CoreException e) {
						editorInput = new CSourceNotFoundEditorInput(new CSourceNotFoundElement(dmc, fSourceLookup.getLaunchConfiguration(), fFrameData.fFile));
						editorId = ICDebugUIConstants.CSOURCENOTFOUND_EDITOR_ID;
	            	}
	            } else if (sourceElement instanceof LocalFileStorage) {
	            	File file = ((LocalFileStorage)sourceElement).getFile();
	            	IFileStore fileStore = EFS.getLocalFileSystem().fromLocalFile(file);
	        		editorInput = new FileStoreEditorInput(fileStore);
	        		editorId = getEditorIdForFilename(file.getName());
	            }
            }
            result.setEditorInput(editorInput);
            result.setEditorId(editorId);
            result.setSourceElement(sourceElement);

            return result;
        }
        
        private String getEditorIdForFilename(String filename) {
			try {
	            IEditorDescriptor descriptor= IDE.getEditorDescriptor(filename);
	            return descriptor.getId();
			} catch (PartInitException exc) {
				DsfUIPlugin.log(exc);
			}
			return "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
        }
	}

    /**
     * Job that positions the editor and paints the IP Annotation for given DMC. 
     */
	class DisplayJob extends UIJob {
		private final SourceLookupResult fResult;
		private final IWorkbenchPage fPage;
		private final FrameData fFrameData;

		private final DsfRunnable fDisplayJobFinishedRunnable = new DsfRunnable() { 
            public void run() {
                // If the current display job does not match up with "this", it means that this job got canceled
                // after it already completed and after this runnable was queued into the dispatch thread.
                if (fRunningDisplayJob == DisplayJob.this) {
                    fRunningDisplayJob = null;
                    if (fEventTriggered && !fDoneStepping.getAndSet(true)) {
                        doneStepping(fResult.getDmc());
                    }
                    serviceDisplayAndClearingJobs();
                }
            }
        }; 

        private final AtomicBoolean fDoneStepping = new AtomicBoolean(false);
        private IRegion fRegion;
        private ITextViewer fTextViewer;
		private final boolean fEventTriggered;
        
        IDMContext getDmc() { return fResult.getDmc(); }
        
		/**
		 * Constructs a new source display job
		 */
		public DisplayJob(SourceLookupResult result, FrameData frameData, IWorkbenchPage page, boolean eventTriggered) {
			super("Debug Source Display");  //$NON-NLS-1$
			setSystem(true);
			setPriority(Job.INTERACTIVE);
			fResult = result;
			fFrameData = frameData;
			fPage = page;
			fEventTriggered = eventTriggered;
		}

		@Override
        public IStatus runInUIThread(final IProgressMonitor monitor) {
            
            if (monitor.isCanceled()) {
                executeFromJob(fDisplayJobFinishedRunnable);
                return Status.CANCEL_STATUS;
            }
            
            if (fRegion != null && fTextViewer != null) {
            	if (fRunningDisplayJob == this) {
            		if (!shouldCancelSelectionChange()) {
	        			enableLineBackgroundPainter();
		                fTextViewer.setSelectedRange(fRegion.getOffset(), 0);
            		}
	                executeFromJob(fDisplayJobFinishedRunnable);
            	}
            } else {
                IEditorPart editor = openEditor(fResult, fPage);
                if (editor == null) {
                    executeFromJob(fDisplayJobFinishedRunnable);
                    return Status.OK_STATUS;
                }
    
                ITextEditor textEditor = null;
                if (editor instanceof ITextEditor) {                    
                    textEditor = (ITextEditor)editor;
                } else {
                    textEditor = (ITextEditor) editor.getAdapter(ITextEditor.class);
                }
                if (textEditor != null) {
                    if (positionEditor(textEditor, fFrameData)) {
                        return Status.OK_STATUS;
                    }
                }
                executeFromJob(fDisplayJobFinishedRunnable);    
            }
			return Status.OK_STATUS;
		}
		
		private boolean shouldCancelSelectionChange() {
            Query<Boolean> delaySelectionChangeQuery = new Query<Boolean>() {
                @Override
                protected void execute(DataRequestMonitor<Boolean> rm) {
                    IExecutionDMContext execCtx = DMContexts.getAncestorOfType(fFrameData.fDmc,
                        IExecutionDMContext.class);
                    
                    IRunControl runControl = fServicesTracker.getService(IRunControl.class);
                    rm.setData(runControl != null && execCtx != null 
                    		&& (fController != null && fController.getPendingStepCount(execCtx) != 0
                    				|| runControl.isStepping(execCtx)));
                    rm.done();
                }
            };

            try {
                fExecutor.execute(delaySelectionChangeQuery);
            } catch (RejectedExecutionException e) {
                return false;
            }

            try {
                return delaySelectionChangeQuery.get();
            } catch (InterruptedException e) {
                return false;
            } catch (ExecutionException e) {
                return false;
            }
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
                        	if (input instanceof CSourceNotFoundEditorInput)
                        	{ 	// Don't open additional source not found editors if
                        		// there is one to reuse.
                                editor[0] = page.openEditor(input, id, false, IWorkbenchPage.MATCH_ID);
                                if (editor[0] instanceof IReusableEditor) {
                                	IReusableEditor re = (IReusableEditor)editor[0];
                                	if (! input.equals(re.getEditorInput()))
                                		re.setInput(input);
                                }
                            }
                        	else
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
        private boolean positionEditor(ITextEditor editor, final FrameData frameData) {
            // Position and annotate the editor.
            fRegion= getLineInformation(editor, frameData.fLine);
            if (fRegion != null) {
                // add annotation
                fIPManager.addAnnotation(
                        editor, frameData.fDmc, new Position(fRegion.getOffset(), fRegion.getLength()), 
                        frameData.fLevel == 0);

                // this is a dirty trick to get access to the ITextViewer of the editor
            	Object tot = editor.getAdapter(ITextOperationTarget.class);
            	if (tot instanceof ITextViewer) {
                    fTextViewer = (ITextViewer)tot;
            		int widgetLine = frameData.fLine;
            		if (tot instanceof ITextViewerExtension5) {
            			ITextViewerExtension5 ext5 = (ITextViewerExtension5) tot;
            			// expand region if collapsed
            			ext5.exposeModelRange(fRegion);
            			widgetLine = ext5.modelLine2WidgetLine(widgetLine);
            		}
            		revealLine(fTextViewer, widgetLine);
            		
            		if (fStepCount > 0 && fSelectionChangeDelay > 0) {
            			disableLineBackgroundPainter();
            			// reschedule for selection change
                		schedule(fSelectionChangeDelay);
                		if (!fDoneStepping.getAndSet(true)) {
                		    doneStepping(getDmc());
                		}
                		return true;
            		} else {
            			enableLineBackgroundPainter();
            			fTextViewer.setSelectedRange(fRegion.getOffset(), 0);
            		}
            	} else {
            		editor.selectAndReveal(fRegion.getOffset(), 0);
            	}
            }
            return false;
        }

    	/**
    	 * Scroll the given line into the visible area if it is not yet visible.
    	 * @param focusLine
    	 * @see org.eclipse.jface.text.TextViewer#revealRange(int, int)
    	 */
    	private void revealLine(ITextViewer viewer, int focusLine) {
    		StyledText textWidget = viewer.getTextWidget();
			int top = textWidget.getTopIndex();
			if (top > -1) {

				// scroll vertically
				int lines = getEstimatedVisibleLinesInViewport(textWidget);
				int bottom = top + lines;

				int bottomBuffer = Math.max(1, lines / 3);
				
				if (focusLine >= top && focusLine <= bottom - bottomBuffer) {
					// do not scroll at all as it is already visible
				} else {
					if (focusLine > bottom - bottomBuffer && focusLine <= bottom) {
						// focusLine is already in bottom bufferZone
						// scroll to top of bottom bufferzone - for smooth down-scrolling
						int scrollDelta = focusLine - (bottom - bottomBuffer);
						textWidget.setTopIndex(top + scrollDelta);
					} else {
						// scroll to top of visible area minus buffer zone
						int topBuffer = lines / 3;
						textWidget.setTopIndex(Math.max(0, focusLine - topBuffer));
					}
				}
			}
    	}

    	/**
    	 * @return the number of visible lines in the view port assuming a constant
    	 *         line height.
    	 */
    	private int getEstimatedVisibleLinesInViewport(StyledText textWidget) {
    		if (textWidget != null) {
    			Rectangle clArea= textWidget.getClientArea();
    			if (!clArea.isEmpty())
    				return clArea.height / textWidget.getLineHeight();
    		}
    		return -1;
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
        Set<IRunControl.IExecutionDMContext> fDmcsToClear;
        
        public ClearingJob(Set<IRunControl.IExecutionDMContext> dmcs) {
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
            
            enableLineBackgroundPainter();
            
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

	private static final boolean DEBUG = false;

    private DsfSession fSession;
    private DsfExecutor fExecutor;
    private DsfServicesTracker fServicesTracker;
    private FrameData fPrevFrameData;
    private SourceLookupResult fPrevResult;
    private ISourceLookupDirector fSourceLookup;
    private DsfSourceLookupParticipant fSourceLookupParticipant;
    private InstructionPointerManager fIPManager;
    
    private LookupJob fRunningLookupJob;
    private DisplayJob fRunningDisplayJob;
    private DisplayJob fPendingDisplayJob;
    private ClearingJob fRunningClearingJob;
    private Set<IRunControl.IExecutionDMContext> fPendingExecDmcsToClear = new HashSet<IRunControl.IExecutionDMContext>();
	private SteppingController fController;

	/** Delay (in milliseconds) before the selection is changed to the IP location */
	private int fSelectionChangeDelay = 150;
	
    private long fStepStartTime = 0;
    private long fLastStepTime = 0;
    private long fStepCount;

    private boolean fEnableLineBackgroundPainter;

    public DsfSourceDisplayAdapter(DsfSession session, ISourceLookupDirector sourceLocator) {
    	this(session, sourceLocator, null);
    }

    /**
	 * @since 1.1
	 */
    public DsfSourceDisplayAdapter(DsfSession session, ISourceLookupDirector sourceLocator, SteppingController controller) {
        fSession = session;
        fExecutor = session.getExecutor();
        fServicesTracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), session.getId());
        fSourceLookup = sourceLocator;
        fSourceLookupParticipant = new DsfSourceLookupParticipant(session); 
        fSourceLookup.addParticipants(new ISourceLookupParticipant[] {fSourceLookupParticipant} );

        final IInstructionPointerPresentation ipPresentation = (IInstructionPointerPresentation) session.getModelAdapter(IInstructionPointerPresentation.class);
		fIPManager = new InstructionPointerManager(ipPresentation);
        
        fExecutor.execute(new DsfRunnable() { public void run() {
        	fSession.addServiceEventListener(DsfSourceDisplayAdapter.this, null);
        }});

        fController = controller;
		if (fController != null) {
			fController.addSteppingControlParticipant(this);
		}
    }

	/**
	 * Configure the delay (in milliseconds) before the selection in the editor
	 * is changed to the IP location.
	 * 
	 * @param delay  the delay in milliseconds, a non-negative integer
	 * 
	 * @since 1.1
	 */
    public void setSelectionChangeDelay(int delay) {
    	fSelectionChangeDelay = delay;
    }

    public void dispose() {
		if (fController != null) {
			fController.removeSteppingControlParticipant(this);
			fController = null;
		}
		
		try {
			fExecutor.execute(new DsfRunnable() { public void run() {
				fSession.removeServiceEventListener(DsfSourceDisplayAdapter.this);
			}});
		} catch (RejectedExecutionException e) {
            // Session is shut down.
		}
		
        fServicesTracker.dispose();
        fSourceLookup.removeParticipants(new ISourceLookupParticipant[] {fSourceLookupParticipant});
        
        // fSourceLookupParticipant is disposed by the source lookup director
        
        // Need to remove annotations in UI thread.
        Display display = Display.getDefault();
        if (!display.isDisposed()) {
        	display.asyncExec(new Runnable() {
				public void run() {
					enableLineBackgroundPainter();
			        fIPManager.removeAllAnnotations();
				}});
        }
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.ISourceDisplayAdapter#displaySource(java.lang.Object, org.eclipse.ui.IWorkbenchPage, boolean)
	 */
	public void displaySource(Object context, final IWorkbenchPage page,
			final boolean force) {
		fStepCount = 0;

		IFrameDMContext displayFrame = null;
		if (context instanceof IDMVMContext) {
			IDMContext dmc = ((IDMVMContext) context).getDMContext();
			if (dmc instanceof IFrameDMContext)
				displayFrame = (IFrameDMContext) dmc;
		} else if (context instanceof IFrameDMContext)
			displayFrame = (IFrameDMContext) context;

		// Quick test. DMC is checked again in source lookup participant, but
		// it's much quicker to test here.
		if (displayFrame != null)
			doDisplaySource(displayFrame, page, force, false);
	}

	private void doDisplaySource(final IFrameDMContext context, final IWorkbenchPage page, final boolean force, final boolean eventTriggered) {
	    if (DEBUG) System.out.println("[DsfSourceDisplayAdapter] doDisplaySource ctx="+context+" eventTriggered="+eventTriggered); //$NON-NLS-1$ //$NON-NLS-2$
    	if (context.getLevel() < 0) {
    		return;
    	}
        // Re-dispatch to executor thread before accessing job lists.
        fExecutor.execute(new DsfRunnable() { public void run() {
            // We need to retrieve the frame level and line number from the service.  
        	IStack stackService = fServicesTracker.getService(IStack.class); 
            if (stackService == null) {
                return;
            }
        	stackService.getFrameData(
                context, 
                new DataRequestMonitor<IFrameDMData>(fExecutor, null) { 
					@Override
					public void handleSuccess() {
						FrameData frameData = new FrameData();
						frameData.fDmc = context;
						frameData.fLevel = context.getLevel();
						// Document line numbers are 0-based. While debugger line numbers are 1-based.
						IFrameDMData data = getData();
						frameData.fLine = data.getLine() - 1;
						frameData.fFile = data.getFile();
						if (!force && frameData.equals(fPrevFrameData)) {
							fPrevResult.updateArtifact(context);
							startDisplayJob(fPrevResult, frameData, page, eventTriggered);
						} else {
							startLookupJob(frameData, page, eventTriggered);
						}
					}
					@Override
					protected void handleFailure() {
					    doneStepping(context);
					}
                    
                    @Override
                    protected void handleRejectedExecutionException() {
                        doneStepping(context);
                    }
    			});
        }});
	}

    private void executeFromJob(Runnable runnable) {
        try {
            fExecutor.execute(runnable);
        } catch (RejectedExecutionException e) {
            // Session disposed, ignore
        }
    }
    
	private void startLookupJob(final FrameData frameData, final IWorkbenchPage page, boolean eventTriggered) {
        // If there is a previous lookup job running, cancel it.
        if (fRunningLookupJob != null) {
            fRunningLookupJob.cancel();
        }
        
        fRunningLookupJob = new LookupJob(frameData, page, eventTriggered);
        fRunningLookupJob.schedule();
    }

	// To be called only on dispatch thread. 
    private void startDisplayJob(SourceLookupResult lookupResult, FrameData frameData, IWorkbenchPage page, boolean eventTriggered) {
    	DisplayJob nextDisplayJob = new DisplayJob(lookupResult, frameData, page, eventTriggered);
    	if (fRunningDisplayJob != null) {
    		fPendingDisplayJob = null;
    		IExecutionDMContext[] execCtxs = DMContexts.getAllAncestorsOfType(frameData.fDmc, IExecutionDMContext.class);
    		fPendingExecDmcsToClear.removeAll(Arrays.asList(execCtxs));

    		if (!eventTriggered && frameData.isIdentical(fRunningDisplayJob.fFrameData)) {
    			// identical location - we are done
    			return;
    		}
    		// cancel running display job
    		fRunningDisplayJob.cancel();
            // make sure doneStepping() is called even if the job never ran - bug 325394
    		if (fRunningDisplayJob.fEventTriggered && !fRunningDisplayJob.fDoneStepping.getAndSet(true)) {
    		    // ... but not if this request is event-triggered for the same context (duplicate suspended event)
    		    if (!eventTriggered || !fRunningDisplayJob.getDmc().equals(lookupResult.getDmc())) {
    		        doneStepping(fRunningDisplayJob.getDmc());
    		    }
    		}
    	}
    	if (fRunningClearingJob != null) {
            // Wait for the clearing job to finish, instead, set the 
            // display job as pending.
            fPendingDisplayJob = nextDisplayJob;
        } else {
            fRunningDisplayJob = nextDisplayJob;
            fRunningDisplayJob.schedule();        
        }
    }

	private void doneStepping(IDMContext context) {
		if (fController != null) {
			// indicate completion of step
        	final IExecutionDMContext dmc = DMContexts.getAncestorOfType(context, IExecutionDMContext.class);
        	if (dmc != null) {
        		try {
        			fController.getExecutor().execute(new DsfRunnable() {
        				public void run() {
        					fController.doneStepping(dmc, DsfSourceDisplayAdapter.this);
        				};
        			});
        		} catch (RejectedExecutionException e) {
        			// Session is shutdown
        		}
        	}
        }
	}
    
    private void serviceDisplayAndClearingJobs() {
        if (!fPendingExecDmcsToClear.isEmpty()) {
            // There are annotations to be cleared, run the job first
            fRunningClearingJob = new ClearingJob(fPendingExecDmcsToClear);
            fRunningClearingJob.schedule();
            fPendingExecDmcsToClear = new HashSet<IRunControl.IExecutionDMContext>();            
        } else if (fPendingDisplayJob != null) {
            fRunningDisplayJob = fPendingDisplayJob;
            fRunningDisplayJob.schedule();
            fPendingDisplayJob = null;
        }
    }
    
    private void startAnnotationClearingJob(IRunControl.IExecutionDMContext execDmc) {
        // Make sure to add the context to the list.
        fPendingExecDmcsToClear.add(execDmc);
        
        // If lookup job is running, check it against the execution context,
        // and cancel it if matches.
        if (fRunningLookupJob != null) {
            if (DMContexts.isAncestorOf(fRunningLookupJob.getDmc(), execDmc)) {
                fRunningLookupJob.cancel();
                fRunningLookupJob = null;
            }
        }
        // If there is a pending display job, make sure it doesn't get 
        // preempted by this event.  If so, just cancel the pending
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
            fPendingExecDmcsToClear = new HashSet<IRunControl.IExecutionDMContext>();
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
        startAnnotationClearingJob(e.getDMContext());
    }

    /**
     * @since 1.1
     */
    @DsfServiceEventHandler
    public void eventDispatched(SteppingTimedOutEvent e) {
        startAnnotationClearingJob(e.getDMContext());        
    }
    
    @DsfServiceEventHandler
    public void eventDispatched(final IRunControl.ISuspendedDMEvent e) {
		updateStepTiming();
    	if (e.getReason() == StateChangeReason.STEP || e.getReason() == StateChangeReason.BREAKPOINT) {
    	    if (DEBUG) System.out.println("[DsfSourceDisplayAdapter] eventDispatched e="+e); //$NON-NLS-1$
	        // trigger source display immediately (should be optional?)
	        Display.getDefault().asyncExec(new Runnable() {
				public void run() {
			        Object context = DebugUITools.getDebugContext();
			        if (context instanceof IDMVMContext) {
				        final IDMContext dmc = ((IDMVMContext)context).getDMContext();
				        if (dmc instanceof IFrameDMContext && DMContexts.isAncestorOf(dmc, e.getDMContext())) {
				        	IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
							doDisplaySource((IFrameDMContext) dmc, page, false, true);
							return;
				        }
			        }
		    		doneStepping(e.getDMContext());
				}});
    	} else {
    		doneStepping(e.getDMContext());
    	}
    }

	private void updateStepTiming() {
		long now = System.currentTimeMillis();
		if (now - fLastStepTime > Math.max(fSelectionChangeDelay, 200)) {
			fStepCount = 0;
			fStepStartTime = fLastStepTime = now;
			return;
		}
		fLastStepTime = now;
		++fStepCount;
		if (DEBUG) {
			long delta = now - fStepStartTime;
			float meanTime = delta/(float)fStepCount/1000;
			System.out.println("[DsfSourceDisplayAdapter] step speed = " + 1/meanTime); //$NON-NLS-1$
		}
	}

	/**
	 * Disable editor line background painter if it is enabled.
	 * <p>
	 * <strong>Must be called on display thread.</strong>
	 * </p>
	 */
	private void disableLineBackgroundPainter() {
		if (!fEnableLineBackgroundPainter) {
			fEnableLineBackgroundPainter = EditorsUI.getPreferenceStore().getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE);
			if (fEnableLineBackgroundPainter) {
				EditorsUI.getPreferenceStore().setValue(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE, false);
			}
		}
	}

	/**
	 * Enable the editor line background painter if it was enabled before.
	 * <p>
	 * <strong>Must be called on display thread.</strong>
	 * </p>
	 */
	private void enableLineBackgroundPainter() {
		if (fEnableLineBackgroundPainter) {
			fEnableLineBackgroundPainter = false;
			EditorsUI.getPreferenceStore().setValue(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE, true);
		}
	}

}
