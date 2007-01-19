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

package org.eclipse.cdt.internal.ui.typehierarchy;

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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.actions.OpenActionUtil;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;
import org.eclipse.cdt.internal.ui.viewsupport.FindNameForSelectionVisitor;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;

public class TypeHierarchyUI {
	private static boolean sIsJUnitTest= false;

	public static void setIsJUnitTest(boolean val) {
		sIsJUnitTest= val;
	}
	
	public static THViewPart open(ICElement input, IWorkbenchWindow window) {
        if (input != null) {
        	return openInViewPart(window, input);
        }
        return null;
    }

    private static THViewPart openInViewPart(IWorkbenchWindow window, ICElement input) {
        IWorkbenchPage page= window.getActivePage();
        try {
            THViewPart result= (THViewPart)page.showView(CUIPlugin.ID_TYPE_HIERARCHY);
            result.setInput(input);
            return result;
        } catch (CoreException e) {
            ExceptionHandler.handle(e, window.getShell(), Messages.TypeHierarchyUI_OpenTypeHierarchy, null); 
        }
        return null;        
    }

    private static THViewPart openInViewPart(IWorkbenchWindow window, ICElement[] input) {
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
					Messages.TypeHierarchyUI_OpenTypeHierarchy, Messages.TypeHierarchyUI_SelectFromList,
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
			
			Job job= new Job(Messages.TypeHierarchyUI_OpenTypeHierarchy) {
				protected IStatus run(IProgressMonitor monitor) {
					try {
						final ICElement[] elems= findDefinitions(project, editorInput, sel);
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
			};
			job.setUser(true);
			job.schedule();
		}
    }
    
	private static ICElement[] findDefinitions(ICProject project, IEditorInput editorInput, ITextSelection sel) throws CoreException {
		try {
			IIndex index= CCorePlugin.getIndexManager().getIndex(project, IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);

			index.acquireReadLock();
			try {
				IASTName name= getSelectedName(index, editorInput, sel);
				if (name != null) {
					IBinding binding= name.resolveBinding();
					if (isValidInput(binding)) {
						if (name.isDefinition()) {
							ICElement elem= IndexUI.getCElementForName(project, index, name);
							if (elem != null) {
								return new ICElement[]{elem};
							}
						}
						else {
							ICElement[] elems= IndexUI.findAllDefinitions(index, binding);
							if (elems.length == 0) {
								elems= IndexUI.findAllDefinitions(index, binding);
								if (elems.length == 0) {
									ICElement elem= IndexUI.findAnyDeclaration(index, project, binding);
									if (elems != null) {
										elems= new ICElement[]{elem};
									}
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

	private static IASTName getSelectedName(IIndex index, IEditorInput editorInput, ITextSelection selection) throws CoreException {
		int selectionStart = selection.getOffset();
		int selectionLength = selection.getLength();

		IWorkingCopy workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editorInput);
		if (workingCopy == null)
			return null;
		
		int options= ITranslationUnit.AST_SKIP_INDEXED_HEADERS;
		IASTTranslationUnit ast = workingCopy.getAST(index, options);
		FindNameForSelectionVisitor finder= new FindNameForSelectionVisitor(ast.getFilePath(), selectionStart, selectionLength);
		ast.accept(finder);
		return finder.getSelectedName();
	}

	public static boolean isValidInput(IBinding binding) {
		if (binding instanceof ICompositeType) {
//			|| binding instanceof ITypedef ||
//				binding instanceof IField || binding instanceof ICPPMethod) {
			return true;
		}
		return false;
	}

	public static boolean isValidInput(ICElement elem) {
		if (elem == null) {
			return false;
		}
		switch (elem.getElementType()) {
		case ICElement.C_CLASS:
		case ICElement.C_STRUCT:
		case ICElement.C_UNION:
		case ICElement.C_CLASS_DECLARATION:
		case ICElement.C_STRUCT_DECLARATION:
		case ICElement.C_UNION_DECLARATION:
//		case ICElement.C_TYPEDEF:
//		case ICElement.C_FIELD:
//		case ICElement.C_METHOD:
//		case ICElement.C_METHOD_DECLARATION:
//		case ICElement.C_TEMPLATE_METHOD:
//		case ICElement.C_TEMPLATE_METHOD_DECLARATION:
			return true;
		}
		return false;
	}
}
