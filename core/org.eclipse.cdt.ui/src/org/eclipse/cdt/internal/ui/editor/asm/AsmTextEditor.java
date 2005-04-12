package org.eclipse.cdt.internal.ui.editor.asm;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;





/**
 * Assembly text editor
 */
public class AsmTextEditor extends TextEditor {
//public class AsmTextEditor extends StatusTextEditor {
// FIXME: Should this editor have a different preference store ?
// For now we are sharing with the CEditor and any changes will in the
// setting of the CEditor will be reflected in this editor.

	/**
	 * Creates a new text editor.
	 */
	public AsmTextEditor() {
		super();
		initializeEditor();
	}
	/**
	 * Initializes this editor.
	 */
	protected void initializeEditor() {
		AsmTextTools textTools= CUIPlugin.getDefault().getAsmTextTools();
		setSourceViewerConfiguration(new AsmSourceViewerConfiguration(textTools, this));
		setDocumentProvider(CUIPlugin.getDefault().getDocumentProvider());
		setRangeIndicator(new DefaultRangeIndicator());
		// FIXME: Should this editor have a different preference store ?
		// For now we are sharing with the CEditor and any changes will in the
		// setting of the CEditor will be reflected in this editor.
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
		setEditorContextMenuId("#ASMEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#ASMEditorRulerContext"); //$NON-NLS-1$
		//setOutlinerContextMenuId("#CEditorOutlinerContext"); //$NON-NLS-1$
	}

	/**
	 * The <code>TextEditor</code> implementation of this 
	 * <code>IEditorPart</code> method returns <code>true</code>.
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}
	/**
	 * The <code>TextEditor</code> implementation of this 
	 * <code>AbstractTextEditor</code> method asks the user for the workspace path
	 * of a file resource and saves the document there.
	 */
	protected void performSaveAs(IProgressMonitor progressMonitor) {
		/*
		 * 1GEUSSR: ITPUI:ALL - User should never loose changes made in the editors.
		 * Changed Behavior to make sure that if called inside a regular save (because
		 * of deletion of input element) there is a way to report back to the caller.
		 */
				 		
		Shell shell= getSite().getShell();
		
		SaveAsDialog dialog= new SaveAsDialog(shell);
		dialog.open();
		IPath path= dialog.getResult();
		
		if (path == null) {
			if (progressMonitor != null)
				progressMonitor.setCanceled(true);
			return;
		}
			
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IFile file= workspace.getRoot().getFile(path);
		final IEditorInput newInput= new FileEditorInput(file);
		
		WorkspaceModifyOperation op= new WorkspaceModifyOperation() {
			public void execute(final IProgressMonitor monitor) throws CoreException {
				/* 
				 * 1GF5YOX: ITPJUI:ALL - Save of delete file claims it's still there
				 * Changed false to true.
				 */
				getDocumentProvider().saveDocument(monitor, newInput, getDocumentProvider().getDocument(getEditorInput()), true);
			}
		};
		
		boolean success= false;
		try {
			
			getDocumentProvider().aboutToChange(newInput);
			new ProgressMonitorDialog(shell).run(false, true, op);
			success= true;
			
		} catch (InterruptedException x) {
		} catch (InvocationTargetException x) {
			// Shared with C editor
			String title= CEditorMessages.getString("CEditor.error.saving.title"); //$NON-NLS-1$
			String msg= MessageFormat.format(CEditorMessages.getString("CEditor.error.saving.message"), new Object[] { x.getTargetException().getMessage() }); //$NON-NLS-1$
			MessageDialog.openError(shell, title, msg);
		} finally {
			getDocumentProvider().changed(newInput);
			if (success)
				setInput(newInput);
		}
		
		if (progressMonitor != null)
			progressMonitor.setCanceled(!success);
	}
	
	/*
	 * @see AbstractTextEditor#affectsTextPresentation(PropertyChangeEvent)
	 * Pulled in from 2.0
	 */
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		// String p= event.getProperty();
		
		boolean affects= false;
		AsmTextTools textTools= CUIPlugin.getDefault().getAsmTextTools();
		affects |= textTools.affectsBehavior(event);
									
		return affects ? affects : super.affectsTextPresentation(event);
	}
	
		/*
	 * @see AbstractTextEditor#createActions()
	 * @since 2.0
	 */
	protected void createActions() {
		super.createActions();

	}
	

	
	/*
	 * @see AbstractTextEditor#editorContextMenuAboutToShow(IMenuManager)
	 * @since 2.0
	 */
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, ITextEditorActionConstants.SHIFT_RIGHT);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, ITextEditorActionConstants.SHIFT_LEFT);
	}
}
