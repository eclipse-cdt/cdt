/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;


import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.CHelpProviderManager;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.actions.WorkbenchRunnableAdapter;
import org.eclipse.cdt.internal.ui.codemanipulation.AddIncludesOperation;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IRequiredInclude;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;

// TODO this is a big TODO. 
public class AddIncludeOnSelectionAction extends Action implements IUpdate {
		
	private ITextEditor fEditor;
	private IRequiredInclude[] fRequiredIncludes;
	private String[] fUsings;

	class RequiredIncludes implements IRequiredInclude {
		String name;
		boolean isStandard;

		RequiredIncludes(String n) {
			name = n;
			isStandard = true;
		}

		RequiredIncludes(String n, boolean isStandard) {
			name = n;
			this.isStandard = isStandard;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.ui.IRequiredInclude#getIncludeName()
		 */
		public String getIncludeName() {
			return name;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.ui.IRequiredInclude#isStandard()
		 */
		public boolean isStandard() {
			return isStandard;
		}

	}

	public AddIncludeOnSelectionAction(ITextEditor editor) {	
		super(CEditorMessages.getString("AddIncludeOnSelection.label"));		 //$NON-NLS-1$
		setToolTipText(CEditorMessages.getString("AddIncludeOnSelection.tooltip")); //$NON-NLS-1$
		setDescription(CEditorMessages.getString("AddIncludeOnSelection.description")); //$NON-NLS-1$
		
		fEditor= editor;
		CUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.ADD_INCLUDE_ON_SELECTION_ACTION);
	}
	
	private void addInclude(ITranslationUnit tu) {
		AddIncludesOperation op= new AddIncludesOperation(tu, fRequiredIncludes, fUsings, false);
		try {
			PlatformUI.getWorkbench().getProgressService().runInUI(
				PlatformUI.getWorkbench().getProgressService(),
				new WorkbenchRunnableAdapter(op), op.getScheduleRule());
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, getShell(), CEditorMessages.getString("AddIncludeOnSelection.error.message1"), null); //$NON-NLS-1$
		} catch (InterruptedException e) {
			// Do nothing. Operation has been canceled.
		}

	}
	
	protected ITranslationUnit getTranslationUnit () {
		ITranslationUnit unit = null;
		if (fEditor != null) {
			IEditorInput editorInput= fEditor.getEditorInput();
			unit = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editorInput);
		}
		return unit;
	}
	
	private Shell getShell() {
		return fEditor.getSite().getShell();
	}

	public void run() {
		ITranslationUnit tu= getTranslationUnit();
		if (tu != null) {
			extractIncludes(fEditor);
			addInclude(tu);
		}
		fUsings = null;
		fRequiredIncludes = null;
	}

	/**
	 * Extract the includes for the given selection.  This can be both used to perform
	 * the work as well as being invoked when there is a change.  The actual results 
	 * can and should be cached as the lookup process could be potentially costly.
	 * 
	 * @return IRequiredInclude [] An array of the required includes, or null if this action is invalid.
	 */
	private void extractIncludes(ITextEditor editor) {
		if (editor == null) {
			return;
		}
		
		ISelection s= editor.getSelectionProvider().getSelection();
		IDocument doc= editor.getDocumentProvider().getDocument(editor.getEditorInput());

		if (s.isEmpty() || !(s instanceof ITextSelection) || doc == null) {
			return;
		}
	
		ITextSelection selection= (ITextSelection) s;
		try {
			IRegion region = CWordFinder.findWord(doc, selection.getOffset());
			if (region == null || region.getLength() == 0) {
				return;
			}
			String name = doc.get(region.getOffset(), region.getLength());
			if (name.length() == 0) {
				return;
			}

			// Try contribution from plugins.
			IFunctionSummary fs = findContribution(name);
			if (fs != null) {
				fRequiredIncludes = fs.getIncludes();
				String ns = fs.getNamespace();
				if (ns != null && ns.length() > 0) {
					fUsings = new String[] {fs.getNamespace()};
				}
			}

			// Try the type caching.
			if (fRequiredIncludes == null && fUsings == null) {
			}

			// Do a full search
			if (fRequiredIncludes == null && fUsings == null) {
			}
		} catch (BadLocationException e) {
			MessageDialog.openError(getShell(), CEditorMessages.getString("AddIncludeOnSelection.error.message3"), CEditorMessages.getString("AddIncludeOnSelection.error.message4") + e.getMessage()); //$NON-NLS-2$ //$NON-NLS-1$
		}
		
	}

	private IFunctionSummary findContribution (final String name) {
		final IFunctionSummary[] fs = new IFunctionSummary[1];
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				ICHelpInvocationContext context = new ICHelpInvocationContext() {

					public IProject getProject() {
						ITranslationUnit u = getTranslationUnit();
						if (u != null) {
							return u.getCProject().getProject();
						}
						return null;
					}

					public ITranslationUnit getTranslationUnit() {
						return AddIncludeOnSelectionAction.this.getTranslationUnit();
					}	
				};

				fs[0] = CHelpProviderManager.getDefault().getFunctionInfo(context, name);
			}
		};
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(op);
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, getShell(), CEditorMessages.getString("AddIncludeOnSelection.error.message1"), null); //$NON-NLS-1$
		} catch (InterruptedException e) {
			// Do nothing. Operation has been canceled.
		}
		return fs[0];
	}

	public void setContentEditor(ITextEditor editor) {
		fEditor= editor;
	}
	
	public void update() {
		setEnabled(true);
	}
}


