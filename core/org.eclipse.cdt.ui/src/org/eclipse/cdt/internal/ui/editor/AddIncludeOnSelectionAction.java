package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.internal.ui.CCompletionContributorManager;
import org.eclipse.cdt.internal.ui.CFileElementWorkingCopy;
import org.eclipse.cdt.internal.ui.codemanipulation.AddIncludeOperation;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IRequiredInclude;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;


public class AddIncludeOnSelectionAction extends Action implements IUpdate {
		
	private ITextEditor fEditor;
	private IRequiredInclude [] fCachedRequiredIncludes;	
	
	public AddIncludeOnSelectionAction() {
		this(null);
	}
	
	public AddIncludeOnSelectionAction(ITextEditor editor) {	
		super(CEditorMessages.getString("AddIncludeOnSelection.label"));		 //$NON-NLS-1$
		setToolTipText(CEditorMessages.getString("AddIncludeOnSelection.tooltip")); //$NON-NLS-1$
		setDescription(CEditorMessages.getString("AddIncludeOnSelection.description")); //$NON-NLS-1$
		
		fEditor= editor;
		//WorkbenchHelp.setHelp(this,	new Object[] { IJavaHelpContextIds.ADD_IMPORT_ON_SELECTION_ACTION });	
	}
	
	private void addInclude(IRequiredInclude[] inc, CFileElementWorkingCopy tu) {
		AddIncludeOperation op= new AddIncludeOperation(fEditor, tu, inc, false);
		try {
			ProgressMonitorDialog dialog= new ProgressMonitorDialog(getShell());
			dialog.run(false, true, op);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			MessageDialog.openError(getShell(), CEditorMessages.getString("AddIncludeOnSelection.error.message1"), e.getTargetException().getMessage()); //$NON-NLS-1$
		} catch (InterruptedException e) {
			// Do nothing. Operation has been canceled.
		}
	}
	
	private CFileElementWorkingCopy getTranslationUnit () {
		CFileElementWorkingCopy unit = null;
		if(fEditor != null) {
			IEditorInput editorInput= (IEditorInput)fEditor.getEditorInput();
			IDocumentProvider provider= fEditor.getDocumentProvider();
			try {
				if (editorInput instanceof IFileEditorInput)
					unit = new CFileElementWorkingCopy((IFileEditorInput)editorInput, provider);
				else if (editorInput instanceof IStorageEditorInput)
					unit = new CFileElementWorkingCopy((IStorageEditorInput)editorInput, provider);
				else
					throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, 0, "no Editor Input", null));

			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e.getStatus());
			}
		}
		return unit;
	}
	
	protected Shell getShell() {
		return fEditor.getSite().getShell();
	}
	
	private int getNameStart(IDocument doc, int pos) throws BadLocationException {
		if (pos > 0 && doc.getChar(pos - 1) == '.') {
			pos--;
			while (pos > 0) {
				char ch= doc.getChar(pos - 1);
				if (!Character.isJavaIdentifierPart(ch) && ch != '.') {
					return pos;
				}
				pos--;
			}
		}
		return pos;
	}

/*	private void removeQualification(IDocument doc, int nameStart, IType type) throws BadLocationException {
		String packName= type.getPackageFragment().getElementName();
		int packLen= packName.length();
		if (packLen > 0) {
			for (int k= 0; k < packLen; k++) {
				if (doc.getChar(nameStart + k) != packName.charAt(k)) {
					return;
				}
			}
			doc.replace(nameStart, packLen + 1, ""); //$NON-NLS-1$
		}
	} */
	
	/**
	 * @see IAction#actionPerformed
	 */
	public void run() {
		IRequiredInclude [] requiredIncludes;
		if(fCachedRequiredIncludes != null) {
			requiredIncludes = fCachedRequiredIncludes;
		} else {
			requiredIncludes = extractIncludes(fEditor);		
		}

		if(requiredIncludes != null && requiredIncludes.length > 0) {
			CFileElementWorkingCopy tu= getTranslationUnit();
			if(tu != null) {
				addInclude(requiredIncludes, tu);
			}
		} 
	}

	/**
	 * Extract the includes for the given selection.  This can be both used to perform
	 * the work as well as being invoked when there is a change.  The actual results 
	 * can and should be cached as the lookup process could be potentially costly.
	 * 
	 * @return IRequiredInclude [] An array of the required includes, or null if this action is invalid.
	 */
	private IRequiredInclude [] extractIncludes(ITextEditor editor) {
		if(editor == null) {
			return null;
		}
		
		ISelection s= editor.getSelectionProvider().getSelection();
		IDocument doc= editor.getDocumentProvider().getDocument(editor.getEditorInput());

		if (s.isEmpty() || !(s instanceof ITextSelection) || doc == null) {
			return null;
		}
	
		ITextSelection selection= (ITextSelection) s;
		IRequiredInclude [] requiredIncludes = null;
		try {
			int selStart= selection.getOffset();
			int nameStart= getNameStart(doc, selStart);
			int len= selStart - nameStart + selection.getLength();
					
			String name= doc.get(nameStart, len).trim();
					
			//IType[] types= StubUtility.findAllTypes(typeName, cu.getJavaProject(), null);
			//IType chosen= selectResult(types, packName, getShell());
			IFunctionSummary fs = CCompletionContributorManager.getDefault().getFunctionInfo(name);
			if(fs != null) {
				requiredIncludes = fs.getIncludes();
			}
		} catch (BadLocationException e) {
			MessageDialog.openError(getShell(), CEditorMessages.getString("AddIncludeOnSelection.error.message3"), CEditorMessages.getString("AddIncludeOnSelection.error.message4") + e.getMessage()); //$NON-NLS-2$ //$NON-NLS-1$
		}
		
		return requiredIncludes;
	}

/*	private IType selectResult(IType[] results, String packName, Shell shell) {
		int nResults= results.length;
		
		if (nResults == 0) {
			return null;
		} else if (nResults == 1) {
			return results[0];
		}
		
		if (packName.length() != 0) {
			for (int i= 0; i < results.length; i++) {
				IType curr= (IType) results[i];
				if (packName.equals(curr.getPackageFragment().getElementName())) {
					return curr;
				}
			}
		}
		
		JavaPlugin plugin= JavaPlugin.getDefault();
		
		int flags= (JavaElementLabelProvider.SHOW_DEFAULT | JavaElementLabelProvider.SHOW_CONTAINER_QUALIFICATION);
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), new JavaElementLabelProvider(flags), true, false);
		dialog.setTitle(JavaEditorMessages.getString("AddImportOnSelection.dialog.title")); //$NON-NLS-1$
		dialog.setMessage(JavaEditorMessages.getString("AddImportOnSelection.dialog.message")); //$NON-NLS-1$
		if (dialog.open(results) == dialog.OK) {
			return (IType) dialog.getSelectedElement();
		}
		return null;
	} */
	
	public void setContentEditor(ITextEditor editor) {
		fEditor= editor;
	}
	
	public void update() {
		fCachedRequiredIncludes = extractIncludes(fEditor);
		setEnabled(fCachedRequiredIncludes != null);
	}
}


