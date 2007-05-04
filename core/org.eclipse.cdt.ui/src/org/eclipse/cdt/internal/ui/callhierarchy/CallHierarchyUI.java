/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
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
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICExternalBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.util.CElementBaseLabels;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.actions.OpenActionUtil;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.internal.ui.util.StatusLineHandler;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;

public class CallHierarchyUI {
	private static boolean sIsJUnitTest= false;

	public static void setIsJUnitTest(boolean val) {
		sIsJUnitTest= val;
	}
	
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

    private static CHViewPart openInViewPart(IWorkbenchSite site, ICElement[] input) {
    	IWorkbenchWindow window = site.getWorkbenchWindow();
    	StatusLineHandler.clearStatusLine(site);
		ICElement elem = null;
		switch (input.length) {
		case 0:
			break;
		case 1:
			elem = input[0];
			break;
		default:
			if (sIsJUnitTest) {
				throw new RuntimeException("ambigous input"); //$NON-NLS-1$
			}
			elem = OpenActionUtil.selectCElement(input, window.getShell(),
					CHMessages.CallHierarchyUI_label, CHMessages.CallHierarchyUI_selectMessage,
					CElementBaseLabels.ALL_DEFAULT | CElementBaseLabels.MF_POST_FILE_QUALIFIED, 0);
			break;
		}
		if (elem != null) {
			return openInViewPart(window, elem);
		} else {
			StatusLineHandler.showStatusLineMessage(site, 
					CHMessages.CallHierarchyUI_openFailureMessage);
		}
		return null;
	}

    public static void open(final ITextEditor editor, final ITextSelection sel) {
		if (editor != null) {
			ICElement inputCElement = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput());
			if (inputCElement != null) {
				final ICProject project= inputCElement.getCProject();
				final IEditorInput editorInput = editor.getEditorInput();
				final Display display= Display.getCurrent();

				Job job= new Job(CHMessages.CallHierarchyUI_label) {
					protected IStatus run(IProgressMonitor monitor) {
						try {
							StatusLineHandler.clearStatusLine(editor.getSite());
							final ICElement[] elems= findDefinitions(project, editorInput, sel);
							if (elems != null && elems.length > 0) {
								display.asyncExec(new Runnable() {
									public void run() {
										openInViewPart(editor.getSite(), elems);
									}});
							} else {
								StatusLineHandler.showStatusLineMessage(editor.getSite(), 
										CHMessages.CallHierarchyUI_openFailureMessage);
							}
							return Status.OK_STATUS;
						} 
						catch (CoreException e) {
							return e.getStatus();
						}
					}
				};
				job.setUser(true);
				job.schedule();
			}
		}
    }
    
	private static ICElement[] findDefinitions(ICProject project, IEditorInput editorInput, ITextSelection sel) throws CoreException {
		try {
			IIndex index= CCorePlugin.getIndexManager().getIndex(project, IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);

			index.acquireReadLock();
			try {
				IASTName name= IndexUI.getSelectedName(index, editorInput, sel);
				if (name != null) {
					IBinding binding= name.resolveBinding();
					if (CallHierarchyUI.isRelevantForCallHierarchy(binding)) {
						if (name.isDefinition()) {
							ICElement elem= IndexUI.getCElementForName(project, index, name);
							if (elem != null) {
								return new ICElement[]{elem};
							}
						}
						else {
							ICElement[] elems= IndexUI.findAllDefinitions(index, binding);
							if (elems.length == 0) {
								ICElement elem= IndexUI.findAnyDeclaration(index, project, binding);
								if (elems != null) {
									elems= new ICElement[]{elem};
								}
							}
							return elems;
						}
					}
				}
			}
			finally {
				if (index != null) {
					index.releaseReadLock();
				}
			}
		}
		catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		} 
		catch (DOMException e) {
			CUIPlugin.getDefault().log(e);
		} 
		catch (InterruptedException e) {
		}
		return null;
	}

	public static boolean isRelevantForCallHierarchy(IBinding binding) {
		if (binding instanceof ICExternalBinding ||
				binding instanceof IEnumerator ||
				binding instanceof IFunction ||
				binding instanceof IVariable) {
			return true;
		}
		return false;
	}

	public static boolean isRelevantForCallHierarchy(ICElement elem) {
		if (elem == null) {
			return false;
		}
		switch (elem.getElementType()) {
		case ICElement.C_ENUMERATOR:
		case ICElement.C_FIELD:
		case ICElement.C_FUNCTION:
		case ICElement.C_FUNCTION_DECLARATION:
		case ICElement.C_METHOD:
		case ICElement.C_METHOD_DECLARATION:
		case ICElement.C_TEMPLATE_FUNCTION:
		case ICElement.C_TEMPLATE_FUNCTION_DECLARATION:
		case ICElement.C_TEMPLATE_METHOD:
		case ICElement.C_TEMPLATE_METHOD_DECLARATION:
		case ICElement.C_TEMPLATE_VARIABLE:
		case ICElement.C_VARIABLE:
		case ICElement.C_VARIABLE_DECLARATION:
			return true;
		}
		return false;
	}
}
