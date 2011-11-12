/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.callhierarchy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICExternalBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.model.ext.ICElementHandle;
import org.eclipse.cdt.internal.corext.util.CModelUtil;

import org.eclipse.cdt.internal.ui.actions.OpenActionUtil;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.internal.ui.util.StatusLineHandler;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;

public class CallHierarchyUI {
	private static final ICElement[] NO_ELEMENTS = {};
	private static boolean sIsJUnitTest= false;

	/**
	 * List of the Call Hierarchy views in LRU order, where the most recently used view is at index 0.
	 */
	private static List<CHViewPart> fLRUCallHierarchyViews= new ArrayList<CHViewPart>();
	private static int fViewCount = 0;
	
	private static final int MAX_HISTORY_SIZE = 10;
	private static List<ICElement> fHistoryEntries= new ArrayList<ICElement>(MAX_HISTORY_SIZE);
	
	public static void setIsJUnitTest(boolean val) {
		sIsJUnitTest= val;
	}

	public static void open(final IWorkbenchWindow window, final ICElement input) {
        if (input != null) {
        	final Display display= Display.getCurrent();

        	Job job= new Job(CHMessages.CallHierarchyUI_label) {
        		@Override
				protected IStatus run(IProgressMonitor monitor) {
        			final ICElement[] elems= findDefinitions(input);
					if (elems != null && elems.length > 0) {
						display.asyncExec(new Runnable() {
							@Override
							public void run() {
								internalOpen(window, elems);
							}});
					}
					return Status.OK_STATUS;
        		}
        	};
        	job.setUser(true);
        	job.schedule();
        }
    }

    private static CHViewPart internalOpen(IWorkbenchWindow window, ICElement input) {
        IWorkbenchPage page= window.getActivePage();
        try {
        	CHViewPart viewPart = findLRUCallHierarchyViewPart(page); //find the first view which is not pinned
        	String secondaryId = null;
			if (viewPart == null) {
				if (page.findViewReference(CUIPlugin.ID_CALL_HIERARCHY) != null) { //all the current views are pinned, open a new instance
					secondaryId = String.valueOf(++fViewCount);
				}
			} else {
				secondaryId = viewPart.getViewSite().getSecondaryId();
			}
        	
			viewPart = (CHViewPart) page.showView(CUIPlugin.ID_CALL_HIERARCHY, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
			viewPart.setInput(input);
            return viewPart;
        } catch (CoreException e) {
            ExceptionHandler.handle(e, window.getShell(), CHMessages.OpenCallHierarchyAction_label, null);
        }
        return null;
    }

    private static CHViewPart internalOpen(IWorkbenchWindow window, ICElement[] input) {
		ICElement elem = null;
		switch (input.length) {
		case 0:
			break;
		case 1:
			elem = input[0];
			break;
		default:
			if (sIsJUnitTest) {
				throw new RuntimeException("ambiguous input"); //$NON-NLS-1$
			}
			elem = OpenActionUtil.selectCElement(input, window.getShell(),
					CHMessages.CallHierarchyUI_label, CHMessages.CallHierarchyUI_selectMessage,
					CElementLabels.ALL_DEFAULT | CElementLabels.MF_POST_FILE_QUALIFIED, 0);
			break;
		}
		if (elem != null) {
			return internalOpen(window, elem);
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
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							StatusLineHandler.clearStatusLine(editor.getSite());
							final ICElement[] elems= findDefinitions(project, editorInput, sel);
							if (elems.length > 0) {
								display.asyncExec(new Runnable() {
									@Override
									public void run() {
										internalOpen(editor.getSite().getWorkbenchWindow(), elems);
									}});
							} else {
								StatusLineHandler.showStatusLineMessage(editor.getSite(),
										CHMessages.CallHierarchyUI_openFailureMessage);
							}
							return Status.OK_STATUS;
						} catch (CoreException e) {
							return e.getStatus();
						}
					}
					@Override
					public boolean belongsTo(Object family) {
						 return family == CallHierarchyUI.class;
					}
				};
				job.setUser(true);
				job.schedule();
			}
		}
    }

	private static ICElement[] findDefinitions(ICProject project, IEditorInput editorInput, ITextSelection sel)
			throws CoreException {
		try {
			IIndex index= CCorePlugin.getIndexManager().getIndex(project,
					IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);

			index.acquireReadLock();
			try {
				IASTName name= IndexUI.getSelectedName(editorInput, sel);
				if (name != null) {
					IBinding binding= name.resolveBinding();
					if (!CallHierarchyUI.isRelevantForCallHierarchy(binding)) {
						for (IASTNode parent= name; parent != null; parent= parent.getParent()) {
							if (parent.getPropertyInParent() == IASTFunctionCallExpression.FUNCTION_NAME) {
								ICPPASTFunctionCallExpression fcall= (ICPPASTFunctionCallExpression) parent.getParent();
								if (fcall != null) {
									IASTImplicitName[] implicit = fcall.getImplicitNames();
									if (implicit.length > 0)
										binding= implicit[0].resolveBinding();
								}
								break;
							}
						}
					}
					if (CallHierarchyUI.isRelevantForCallHierarchy(binding)) {
						if (name.isDefinition()) {
							ICElement elem= IndexUI.getCElementForName(project, index, name);
							if (elem != null) {
								return new ICElement[] { elem };
							}
							return NO_ELEMENTS;
						}

						ICElement[] elems= IndexUI.findAllDefinitions(index, binding);
						if (elems.length != 0)
							return elems;

						if (name.isDeclaration()) {
							ICElementHandle elem= IndexUI.getCElementForName(project, index, name);
							if (elem != null) {
								return new ICElement[] { elem };
							}
							return NO_ELEMENTS;
						}

						ICElementHandle elem= IndexUI.findAnyDeclaration(index, project, binding);
						if (elem != null) {
							return new ICElement[] { elem };
						}

						if (binding instanceof ICPPSpecialization) {
							return findSpecializationDeclaration(binding, project, index);
						}
						return NO_ELEMENTS;
					}
				}
			} finally {
				index.releaseReadLock();
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return NO_ELEMENTS;
	}

	private static ICElement[] findSpecializationDeclaration(IBinding binding, ICProject project,
			IIndex index) throws CoreException {
		while (binding instanceof ICPPSpecialization) {
			IBinding original= ((ICPPSpecialization) binding).getSpecializedBinding();
			ICElementHandle[] elems= IndexUI.findAllDefinitions(index, original);
			if (elems.length == 0) {
				ICElementHandle elem= IndexUI.findAnyDeclaration(index, project, original);
				if (elem != null) {
					elems= new ICElementHandle[] { elem };
				}
			}
			if (elems.length > 0) {
				return elems;
			}
			binding= original;
		}
		return NO_ELEMENTS;
	}

	public static ICElement[] findDefinitions(ICElement input) {
		try {
			final ITranslationUnit tu= CModelUtil.getTranslationUnit(input);
			if (tu != null) {
				final ICProject project= tu.getCProject();
				final IIndex index= CCorePlugin.getIndexManager().getIndex(project,
						IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);

				index.acquireReadLock();
				try {
					if (needToFindDefinition(input)) {
						IBinding binding= IndexUI.elementToBinding(index, input);
						if (binding != null) {
							ICElement[] result= IndexUI.findAllDefinitions(index, binding);
							if (result.length > 0) {
								return result;
							}
						}
					}
					IIndexName name= IndexUI.elementToName(index, input);
					if (name != null) {
						ICElementHandle handle= IndexUI.getCElementForName(tu, index, name);
						return new ICElement[] { handle };
					}
				} finally {
					index.releaseReadLock();
				}
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return new ICElement[] { input };
	}

	private static boolean needToFindDefinition(ICElement elem) {
		switch (elem.getElementType()) {
		case ICElement.C_FUNCTION_DECLARATION:
		case ICElement.C_METHOD_DECLARATION:
		case ICElement.C_TEMPLATE_FUNCTION_DECLARATION:
		case ICElement.C_TEMPLATE_METHOD_DECLARATION:
			return true;
		}
		return false;
	}

	public static boolean isRelevantForCallHierarchy(IBinding binding) {
		if (binding instanceof ICExternalBinding ||
				binding instanceof IEnumerator ||
				binding instanceof IFunction)
			return true;
		
		if (binding instanceof IVariable) {
			try {
				if (binding.getScope().getKind() == EScopeKind.eLocal)
					return false;
			} catch (DOMException e) {
			}
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
	
		
	/**
	 * Adds the activated view part to the head of the list.
	 * 
	 * @param view the Call Hierarchy view part
	 */
	static void callHierarchyViewActivated(CHViewPart view) {
		fLRUCallHierarchyViews.remove(view);
		fLRUCallHierarchyViews.add(0, view);
	}

	/**
	 * Removes the closed view part from the list.
	 * 
	 * @param view the closed view part
	 */
	static void callHierarchyViewClosed(CHViewPart view) {
		fLRUCallHierarchyViews.remove(view);
	}
	
	/**
	 * Clears the history and updates all the open views.
	 */
	static void clearHistory() {
		setHistoryEntries(new ICElement[0]);
		for (Iterator<CHViewPart> iter= fLRUCallHierarchyViews.iterator(); iter.hasNext();) {
			CHViewPart part= iter.next();
			part.setInput(null);
		}
	}
	
	/**
	 * Finds the first Call Hierarchy view part instance that is not pinned.
	 * 
	 * @param page the active page
	 * @return the Call Hierarchy view part to open or <code>null</code> if none found
	 */
	private static CHViewPart findLRUCallHierarchyViewPart(IWorkbenchPage page) {
		boolean viewFoundInPage= false;
		for (Iterator<CHViewPart> iter= fLRUCallHierarchyViews.iterator(); iter.hasNext();) {
			CHViewPart view= iter.next();
			if (page.equals(view.getSite().getPage())) {
				if (!view.isPinned()) {
					return view;
				}
				viewFoundInPage= true;
			}
		}
		if (!viewFoundInPage) {
			// find unresolved views
			IViewReference[] viewReferences= page.getViewReferences();
			for (int i= 0; i < viewReferences.length; i++) {
				IViewReference curr= viewReferences[i];
				if (CUIPlugin.ID_CALL_HIERARCHY.equals(curr.getId()) && page.equals(curr.getPage())) {
					CHViewPart view= (CHViewPart)curr.getView(true);
					if (view != null && !view.isPinned()) {
						return view;
					}
				}
			}
		}
		return null;
	}
	
	static public ICElement[] getHistoryEntries() {
		return fHistoryEntries.toArray(new ICElement[fHistoryEntries.size()]);
	}
	
	static public void setHistoryEntries(ICElement[] remaining) {
		fHistoryEntries.clear();
		fHistoryEntries.addAll(Arrays.asList(remaining));
	}
	
	static public void updateHistory(ICElement input) {
    	if (input != null) {
    		fHistoryEntries.remove(input);
    		fHistoryEntries.add(0, input);
    		if (fHistoryEntries.size() > MAX_HISTORY_SIZE) {
    			fHistoryEntries.remove(MAX_HISTORY_SIZE-1);
    		}
    	}
	}

}
