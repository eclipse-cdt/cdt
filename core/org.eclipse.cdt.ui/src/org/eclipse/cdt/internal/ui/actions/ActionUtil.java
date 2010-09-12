/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import org.eclipse.cdt.core.model.ICElement;

import org.eclipse.cdt.internal.corext.util.Messages;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.viewsupport.BasicElementLabels;

/*
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=19104
 */
public class ActionUtil {
	
	private ActionUtil(){
	}

	//bug 31998	we will have to disable renaming of linked packages (and cus)
	public static boolean mustDisableCModelAction(Shell shell, Object element) {
	    return false;
//		if (!(element instanceof IPackageFragment) && !(element instanceof IPackageFragmentRoot))
//			return false;
//		
//		IResource resource= ResourceUtil.getResource(element);
//		if ((resource == null) || (! (resource instanceof IFolder)) || (! resource.isLinked()))
//			return false;
//			
//		MessageDialog.openInformation(shell, ActionMessages.ActionUtil.not_possible"), ActionMessages.ActionUtil.no_linked")); //$NON-NLS-1$ //$NON-NLS-2$
//		return true;
	}
	
	public static boolean isProcessable(CEditor editor) {
		if (editor == null)
			return true;
		Shell shell = editor.getSite().getShell();
		ICElement input= SelectionConverter.getInput(editor);
		// if a C/C++ editor doesn't have an input of type C element
		// then it is for sure not on the build path
		if (input == null) {
			MessageDialog.openInformation(shell, 
					ActionMessages.ActionUtil_notOnBuildPath_title,
					ActionMessages.ActionUtil_notOnBuildPath_message);
			return false;
		}
		return isProcessable(shell, input);
	}
	
	public static boolean isProcessable(Shell shell, Object element) {
		if (!(element instanceof ICElement))
			return true;
			
		if (isOnBuildPath((ICElement) element))
			return true;
		MessageDialog.openInformation(shell, 
				ActionMessages.ActionUtil_notOnBuildPath_title,
				ActionMessages.ActionUtil_notOnBuildPath_message);
		return false;
	}

	public static boolean isOnBuildPath(ICElement element) {	
        //fix for bug http://dev.eclipse.org/bugs/show_bug.cgi?id=20051
        if (element.getElementType() == ICElement.C_PROJECT)
            return true;
//		ICProject project= element.getCProject();
//		if (!project.isOnSourceRoot(element.getResource()))
//			return false;
		return true;
	}

	/**
	 * Check whether <code>editor</code> and <code>element</code> are
	 * processable and editable. If the editor edits the element, the validation
	 * is only performed once. If necessary, ask the user whether the file(s)
	 * should be edited.
	 *
	 * @param editor an editor, or <code>null</code> iff the action was not
	 *        executed from an editor
	 * @param shell a shell to serve as parent for a dialog
	 * @param element the element to check, cannot be <code>null</code>
	 * @return <code>true</code> if the element can be edited,
	 *         <code>false</code> otherwise
	 */
	public static boolean isEditable(CEditor editor, Shell shell, ICElement element) {
		if (editor != null) {
			ICElement input= SelectionConverter.getInput(editor);
			if (input != null && input.equals(element.getAncestor(ICElement.C_UNIT)))
				return isEditable(editor);
			else
				return isEditable(editor) && isEditable(shell, element);
		}
		return isEditable(shell, element);
	}

	public static boolean isEditable(CEditor editor) {
		if (!isProcessable(editor))
			return false;

		return editor.validateEditorInputState();
	}
	
	public static boolean isEditable(Shell shell, ICElement element) {
		if (!isProcessable(shell, element))
			return false;

		ICElement cu= element.getAncestor(ICElement.C_UNIT);
		if (cu != null) {
			IResource resource= cu.getResource();
			if (resource != null && resource.isDerived()) {
				// see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#validateEditorInputState()
				final String warnKey= AbstractDecoratedTextEditorPreferenceConstants.EDITOR_WARN_IF_INPUT_DERIVED;
				IPreferenceStore store= EditorsUI.getPreferenceStore();
				if (!store.getBoolean(warnKey))
					return true;

				MessageDialogWithToggle toggleDialog= MessageDialogWithToggle.openYesNoQuestion(
						shell,
						ActionMessages.ActionUtil_warning_derived_title,
						Messages.format(ActionMessages.ActionUtil_warning_derived_message, BasicElementLabels.getPathLabel(resource.getFullPath(), false)),
						ActionMessages.ActionUtil_warning_derived_dontShowAgain,
						false,
						null,
						null);

				EditorsUI.getPreferenceStore().setValue(warnKey, !toggleDialog.getToggleState());
				return toggleDialog.getReturnCode() == IDialogConstants.YES_ID;
			}
		}
		return true;
	}
}

