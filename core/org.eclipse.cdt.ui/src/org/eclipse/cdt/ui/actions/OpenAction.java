/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IEditorStatusLine;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.ICStatusConstants;
import org.eclipse.cdt.internal.ui.actions.ActionMessages;
import org.eclipse.cdt.internal.ui.actions.ActionUtil;
import org.eclipse.cdt.internal.ui.actions.OpenActionUtil;
import org.eclipse.cdt.internal.ui.actions.SelectionConverter;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;

/**
 * This action opens a Java editor on a Java element or file.
 * <p>
 * The action is applicable to selections containing elements of
 * type <code>ICompilationUnit</code>, <code>IMember</code>
 * or <code>IFile</code>.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p> 
 * 
 * @since 2.0
 */
public class OpenAction extends SelectionDispatchAction {
	private CEditor fEditor;
	
	/**
	 * Creates a new <code>OpenAction</code>. The action requires
	 * that the selection provided by the site's selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
	 * 
	 * @param site the site providing context information for this action
	 */
	public OpenAction(IWorkbenchSite site) {
		super(site);
		setText(ActionMessages.OpenAction_label);
		setToolTipText(ActionMessages.OpenAction_tooltip);
		setDescription(ActionMessages.OpenAction_description);		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.OPEN_ACTION);
	}
	
	/**
	 * Note: This constructor is for internal use only. Clients should not call this constructor.
	 * 
	 * @noreference This constructor is not intended to be referenced by clients.
	 */
	public OpenAction(CEditor editor) {
		this(editor.getEditorSite());
		fEditor= editor;
		setText(ActionMessages.OpenAction_declaration_label);
		setEnabled(SelectionConverter.canOperateOn(fEditor));
	}
	
	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	@Override
	public void selectionChanged(ITextSelection selection) {
	}

	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	@Override
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(checkEnabled(selection));
	}
	
	private boolean checkEnabled(IStructuredSelection selection) {
		if (selection.isEmpty())
			return false;
		for (Iterator<?> iter= selection.iterator(); iter.hasNext();) {
			Object element= iter.next();
			if (element instanceof ISourceReference)
				continue;
			if (element instanceof IFile)
				continue;
			if (element instanceof IStorage)
				continue;
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	@Override
	public void run(ITextSelection selection) {
		if (!ActionUtil.isProcessable(getShell(), fEditor))
			return;
		try {
			ICElement element= SelectionConverter.codeResolve(fEditor, getShell(), getDialogTitle(), 
				ActionMessages.OpenAction_select_element);
			if (element == null) {
				IEditorStatusLine statusLine= (IEditorStatusLine) fEditor.getAdapter(IEditorStatusLine.class);
				if (statusLine != null)
					statusLine.setMessage(true, ActionMessages.OpenAction_error_messageBadSelection, null);
				getShell().getDisplay().beep();
				return;
			}
			ICElement input= SelectionConverter.getInput(fEditor);
			int type= element.getElementType();
			if (type == ICElement.C_PROJECT || type == ICElement.C_CCONTAINER)
				element= input;
			run(new Object[] {element} );
		} catch (CModelException e) {
			showError(e);
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	@Override
	public void run(IStructuredSelection selection) {
		if (!checkEnabled(selection))
			return;
		run(selection.toArray());
	}
	
	/**
	 * Note: this method is for internal use only. Clients should not call this method.
	 */
	public void run(Object[] elements) {
		if (elements == null)
			return;
		for (Object element : elements) {
			try {
				element= getElementToOpen(element);
				boolean activateOnOpen= fEditor != null ? true : OpenStrategy.activateOnOpen();
				OpenActionUtil.open(element, activateOnOpen);
			} catch (CModelException e) {
				CUIPlugin.log(new Status(IStatus.ERROR, CUIPlugin.getPluginId(),
						ICStatusConstants.INTERNAL_ERROR, ActionMessages.OpenAction_error_message, e));
				ErrorDialog.openError(getShell(), getDialogTitle(),
						ActionMessages.OpenAction_error_messageProblems, e.getStatus());
			} catch (PartInitException x) {
				String name= null;
				
				if (element instanceof ICElement) {
					name= ((ICElement) element).getElementName();
				} else if (element instanceof IStorage) {
					name= ((IStorage) element).getName();
				} else if (element instanceof IResource) {
					name= ((IResource) element).getName();
				}
				
				if (name != null) {
					MessageDialog.openError(getShell(),
						ActionMessages.OpenAction_error_messageProblems,
						NLS.bind(ActionMessages.OpenAction_error_messageArgs, name, x.getMessage()));			
				}
			}		
		}
	}
	
	/**
	 * Note: this method is for internal use only. Clients should not call this method.
	 */
	public Object getElementToOpen(Object object) throws CModelException {
		return object;
	}	
	
	private String getDialogTitle() {
		return ActionMessages.OpenAction_error_title;
	}
	
	private void showError(CoreException e) {
		ExceptionHandler.handle(e, getShell(), getDialogTitle(), ActionMessages.OpenAction_error_message);
	}
}
