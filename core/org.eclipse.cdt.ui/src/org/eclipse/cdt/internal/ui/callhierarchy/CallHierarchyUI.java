/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.callhierarchy;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.actions.OpenActionUtil;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.missingapi.CIndexQueries;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;

public class CallHierarchyUI {

	public static CHViewPart open(ICElement input, IWorkbenchWindow window) {
        if (input != null) {
        	return openInViewPart(window, input);
        }
        return null;
    }

    private static CHViewPart openInViewPart(IWorkbenchWindow window, ICElement input) {
        IWorkbenchPage page= window.getActivePage();
        try {
            CHViewPart result= (CHViewPart)page.showView(CUIPlugin.ID_CALL_HIERARCHY);
            result.setInput(input);
            return result;
        } catch (CoreException e) {
            ExceptionHandler.handle(e, window.getShell(), CHMessages.OpenCallHierarchyAction_label, null); 
        }
        return null;        
    }

    private static CHViewPart openInViewPart(IWorkbenchWindow window, ICElement[] input) {
		ICElement elem = null;
		switch (input.length) {
		case 0:
			break;
		case 1:
			elem = input[0];
			break;
		default:
			elem = OpenActionUtil.selectCElement(input, window.getShell(),
					CHMessages.CallHierarchyUI_label, CHMessages.CallHierarchyUI_selectMessage,
					CElementLabels.ALL_DEFAULT | CElementLabels.MF_POST_FILE_QUALIFIED, 0);
			break;
		}
		if (elem != null) {
			return openInViewPart(window, elem);
		}
		return null;
	}

    public static void open(final CEditor editor, final ITextSelection sel) {
		if (editor != null) {
			final ICProject project= editor.getInputCElement().getCProject();
			final IEditorInput editorInput = editor.getEditorInput();
			final Display display= Display.getCurrent();
			
			Job job= new Job(CHMessages.CallHierarchyUI_label) {
				protected IStatus run(IProgressMonitor monitor) {
					try {
						final ICElement[] elems= findDefinitions();
						if (elems != null && elems.length > 0) {
							display.asyncExec(new Runnable() {
								public void run() {
									openInViewPart(editor.getSite().getWorkbenchWindow(), elems);
								}});
						}
						return Status.OK_STATUS;
					} 
					catch (CoreException e) {
						return e.getStatus();
					}
				}

				private ICElement[] findDefinitions() throws CoreException {
					CIndexQueries index= CIndexQueries.getInstance();
					IASTName name= getSelectedName(editorInput, sel);
					if (name != null) {
						if (name.isDefinition()) {
							ICElement elem= index.findDefinition(project, name);
							if (elem != null) {
								return new ICElement[]{elem};
							}
						}
						else {
							ICElement[] elems= index.findAllDefinitions(project, name);
							if (elems.length == 0) {
								ICProject[] allProjects= CoreModel.getDefault().getCModel().getCProjects();
								elems= index.findAllDefinitions(allProjects, name);
							}
							return elems;
						}
					}
					return null;
				}
			};
			job.setUser(true);
			job.schedule();
		}
    }

	private static IASTName getSelectedName(IEditorInput editorInput, ITextSelection selection) throws CoreException {
		int selectionStart = selection.getOffset();
		int selectionLength = selection.getLength();

		IWorkingCopy workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editorInput);
		if (workingCopy == null)
			return null;
		
		int options= ILanguage.AST_SKIP_ALL_HEADERS | ILanguage.AST_USE_INDEX;
		IASTTranslationUnit ast = workingCopy.getLanguage().getASTTranslationUnit(workingCopy, options);
		IASTName[] selectedNames = workingCopy.getLanguage().getSelectedNames(ast, selectionStart, selectionLength);
		
		if (selectedNames.length > 0 && selectedNames[0] != null) { // just right, only one name selected
			return selectedNames[0];
		}
		return null;
	}
}
