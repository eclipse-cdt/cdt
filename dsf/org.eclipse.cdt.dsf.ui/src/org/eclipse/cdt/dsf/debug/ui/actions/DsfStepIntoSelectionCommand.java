/*******************************************************************************
 * Copyright (c) 2013 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson AB - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.actions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.cdt.dsf.commands.IStepIntoSelectionHandler;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.internal.ui.sourcelookup.DsfSelectionParse;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl3;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.commands.AbstractDebugCommand;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * @author Alvaro Sanchez-Leon
 * @since 2.4
 */
public class DsfStepIntoSelectionCommand extends AbstractDebugCommand implements IStepIntoSelectionHandler {
	private final DsfServicesTracker fTracker;
	private final DsfSession fSession;

	public DsfStepIntoSelectionCommand(DsfSession session, DsfSteppingModeTarget steppingMode) {
		fSession = session;
		fTracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), session.getId());
	}

	private class LineLocation {
		private String fileName;
		private int lineNumber;

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public int getLineNumber() {
			return lineNumber;
		}

		public void setLineNumber(int lineNumber) {
			this.lineNumber = lineNumber;
		}
	}

	class StepIntoSelectionRunnable implements Runnable {
		private TextEditor fEditorPage = null;
		private LineLocation fLineLocation;
		private IFunctionDeclaration fFunction;
		private boolean fSuccessful = false;

		@Override
		public void run() {
			fEditorPage = resolveEditor();
			// IEditorPart editor = HandlerUtil.getActiveEditor(event);
			if (fEditorPage != null) {

				ISelection selection = fEditorPage.getEditorSite().getSelectionProvider().getSelection();
				if (selection instanceof ITextSelection) {
					DsfSelectionParse parser = new DsfSelectionParse((CEditor) fEditorPage);
					IFunctionDeclaration[] selectedFunctions = parser.resolveSelectedFunction();

					IFunctionDeclaration selFunction = null;
					if (selectedFunctions == null || selectedFunctions.length != 1 || selectedFunctions[0] == null) {
						DsfUIPlugin.debug("Unable to resolve selection to a method"); //$NON-NLS-1$
						return;
					} else {
						// Continue as expected
						selFunction = selectedFunctions[0];
					}

					LineLocation selectedLine = resolveSelection();
					if (selectedLine == null) {
						// Unable to resolve the selected line
						return;
					}

					fLineLocation = selectedLine;
					fFunction = selFunction;
					fSuccessful = true;
				}
			}
		}

		private TextEditor resolveEditor() {
			final IWorkbench wb = DsfUIPlugin.getDefault().getWorkbench();
			// Run in UI thread to access UI resources
			ResolveEditorRunnable reditorRunnable = new ResolveEditorRunnable() {
				TextEditor result = null;

				@Override
				public void run() {
					IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
					if (win == null || win.getActivePage() == null || win.getActivePage().getActiveEditor() == null) {
						result = null;
					} else {
						IEditorPart editorPart = win.getActivePage().getActiveEditor();
						if (editorPart instanceof CEditor) {
							result = (TextEditor) win.getActivePage().getActiveEditor();
						}
					}
				}

				@Override
				public TextEditor getEditor() {
					return result;
				}
			};

			Display.getDefault().syncExec(reditorRunnable);
			return reditorRunnable.getEditor();
		}		
		
		private LineLocation resolveSelection() {
			String errorMessage = ""; //$NON-NLS-1$
			IEditorInput input = fEditorPage.getEditorInput();
			if (input == null) {
				errorMessage = "Invalid Editor input on selection"; //$NON-NLS-1$				
			} else {
				IDocument document = fEditorPage.getDocumentProvider().getDocument(input);
				if (document == null) {
					errorMessage = "Invalid Editor Document input on selection"; //$NON-NLS-1$
				} else {
					ISelection selection = fEditorPage.getEditorSite().getSelectionProvider().getSelection();
					if (!(selection instanceof ITextSelection)) {
						errorMessage = "Only textual selections are supported"; //$NON-NLS-1$
					} else {
						String fileName = null;
						try {
							fileName = CDebugUIUtils.getEditorFilePath(input);
						} catch (CoreException e) {
							// unable to resolve the path
							DsfUIPlugin.log(e);
							return null;
						}

						if (fileName == null) {
							errorMessage = "Unable to resolve fileName from selection"; //$NON-NLS-1$
							DsfUIPlugin.logErrorMessage(errorMessage);
						} else {
							// Resolve the values
							ITextSelection textSelection = (ITextSelection) selection;
							LineLocation lineLocation = new LineLocation();

							lineLocation.setFileName(fileName);
							lineLocation.setLineNumber(textSelection.getStartLine() + 1);
							return lineLocation;
						}
					}
				}
			}

			DsfUIPlugin.logErrorMessage(errorMessage);
			return null;
		}
		
		public LineLocation getLineLocation() {
			return fLineLocation;
		}

		public IFunctionDeclaration getFunction() {
			return fFunction;
		}

		public boolean isSuccessful() {
			return fSuccessful;
		}	
	}
	
	private interface ResolveEditorRunnable extends Runnable {
		TextEditor getEditor();
	}
	
	public void dispose() {
		fTracker.dispose();
	}

	@Override
	protected void doExecute(Object[] targets, IProgressMonitor monitor, IRequest request) throws CoreException {

		final IExecutionDMContext dmc = DMContexts.getAncestorOfType(((IDMVMContext) targets[0]).getDMContext(), IExecutionDMContext.class);
		if (dmc == null) {
			return;
		}

		StepIntoSelectionRunnable resolveSelection = new StepIntoSelectionRunnable();
		// Resolve UI selection from the the UI thread
		Display.getDefault().syncExec(resolveSelection);
		if (resolveSelection.isSuccessful()) {
			runToSelection(resolveSelection.getLineLocation(), resolveSelection.getFunction(), dmc);
		} else {
			String message = "DSfStepIntoSelectionCommand: Unable to resolve a selected function"; //$NON-NLS-1$
			DsfUIPlugin.debug(message);
		}
	}
	
	private void runToSelection(final LineLocation linelocation, final IFunctionDeclaration selectedFunction, final IExecutionDMContext context) {
		if (fSession != null && fSession.isActive()) {
			Throwable exception = null;
			try {
				Query<Object> query = new Query<Object>() {
					@Override
					protected void execute(final DataRequestMonitor<Object> rm) {
						DsfServicesTracker tracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), fSession.getId());

						boolean skipBreakpoints = DebugUITools.getPreferenceStore().getBoolean(IDebugUIConstants.PREF_SKIP_BREAKPOINTS_DURING_RUN_TO_LINE);

						IRunControl3 runControl = tracker.getService(IRunControl3.class);
						if (runControl != null) {
							runControl.stepIntoSelection(context, linelocation.getFileName(), linelocation.getLineNumber(), skipBreakpoints, rm, selectedFunction);
						} else {
							rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "IRunControl3 service not available", null)); //$NON-NLS-1$
							rm.done();
						}
						tracker.dispose();
					}
				};
				fSession.getExecutor().execute(query);
				query.get();
			} catch (RejectedExecutionException e) {
				exception = e;
			} catch (InterruptedException e) {
				exception = e;
			} catch (ExecutionException e) {
				exception = e;
			}
			if (exception != null) {
				DsfUIPlugin.log(new DebugException(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Failed executing Step into Selection", exception)));//$NON-NLS-1$
			}
		} else {
			DsfUIPlugin.log(new DebugException(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Debug session is not active", null))); //$NON-NLS-1$            
		}
	}

	@Override
	protected boolean isExecutable(Object[] targets, IProgressMonitor monitor, IEnabledStateRequest request) throws CoreException {
		final IExecutionDMContext dmc = DMContexts.getAncestorOfType(((IDMVMContext) targets[0]).getDMContext(), IExecutionDMContext.class);
		if (dmc == null) {
			return false;
		}

		DsfSession session = DsfSession.getSession(dmc.getSessionId());
		if (session != null && session.isActive()) {
			try {
				Query<Boolean> query = new Query<Boolean>() {
					@Override
					protected void execute(DataRequestMonitor<Boolean> rm) {
						DsfServicesTracker tracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), dmc.getSessionId());

						IRunControl3 runControl = tracker.getService(IRunControl3.class);
						if (runControl != null) {
							runControl.canStepIntoSelection(dmc, rm);
						} else {
							rm.setData(false);
							rm.done();
						}
						tracker.dispose();
					}
				};
				session.getExecutor().execute(query);
				return query.get();
			} catch (RejectedExecutionException e) {
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
			}
		}
		
		return false;
	}
	
	@Override
	protected Object getTarget(Object element) {
		// receiving AbstractDMVMNode$DMVMContex containing MIStack$MIFrameDMC + StackFramesVMNode
		if (element instanceof IDMVMContext) {
			return element;
		}
		return null;
	}

	@Override
	protected boolean isRemainEnabled(IDebugCommandRequest request) {
		return true;
	}
}
