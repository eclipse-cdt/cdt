/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation
 *    Markus Schorn, Wind River Systems Inc. - ported for rename refactoring impl. 
 *******************************************************************************/
package org.eclipse.cdt.ui.refactoring.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.ui.actions.CdtActionConstants;

import org.eclipse.cdt.internal.ui.editor.ICEditorActionDefinitionIds;

/**
 * Action group that adds refactoring actions (for example Rename..., Move..., etc)
 * to a context menu and the global menu bar.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CRefactoringActionGroup extends ActionGroup implements ISelectionChangedListener {
    /**
     * Pop-up menu: id of the refactor sub menu (value <code>org.eclipse.cdt.ui.refactoring.menu</code>).
     * 
     * @since 2.1
     */
    public static final String MENU_ID = "org.eclipse.cdt.ui.refactoring.menu"; //$NON-NLS-1$

    /**
     * Pop-up menu: id of the reorg group of the refactor sub menu (value
     * <code>reorgGroup</code>).
     * 
     * @since 2.1
     */
    public static final String GROUP_REORG = "reorgGroup"; //$NON-NLS-1$

    /**
     * Pop-up menu: id of the type group of the refactor sub menu (value
     * <code>typeGroup</code>).
     * 
     * @since 2.1
     */
    public static final String GROUP_TYPE = "typeGroup"; //$NON-NLS-1$

    /**
     * Pop-up menu: id of the coding group of the refactor sub menu (value
     * <code>codingGroup</code>).
     * 
     * @since 2.1
     */
    public static final String GROUP_CODING = "codingGroup"; //$NON-NLS-1$
	
	/**
	 * Pop-up menu: id of the coding group 2 of the refactor sub menu (value
	 * <code>codingGroup2</code>).
	 * 
	 * @since 5.0
	 */
	public static final String GROUP_CODING2= "codingGroup2"; //$NON-NLS-1$

	/**
	 * Pop-up menu: id of the reorg group 2 of the refactor sub menu (value
	 * <code>reorgGroup2</code>).
	 * 
	 * @since 5.0
	 */
	public static final String GROUP_REORG2= "reorgGroup2"; //$NON-NLS-1$
	
	/**
	 * Pop-up menu: id of the type group 2 of the refactor sub menu (value
	 * <code>typeGroup2</code>).
	 * 
	 * @since 5.0
	 */
	public static final String GROUP_TYPE2= "typeGroup2"; //$NON-NLS-1$
	
	/**
	 * Pop-up menu: id of the type group 2 of the refactor sub menu (value
	 * <code>typeGroup3</code>).
	 * 
	 * @since 5.0
	 */
	public static final String GROUP_TYPE3= "typeGroup3"; //$NON-NLS-1$

    private String fGroupName= IWorkbenchActionConstants.GROUP_REORGANIZE;
    private CRenameAction fRenameAction;
    private RefactoringAction fExtractConstantAction;
    private RefactoringAction fExtractLocalVariableAction;
    private RefactoringAction fExtractFunctionAction;
	private RefactoringAction fToggleFunctionAction;
    private RefactoringAction fHideMethodAction;
	private IWorkbenchSite fSite;
	private List<RefactoringAction> fAllActions= new ArrayList<RefactoringAction>();

    public CRefactoringActionGroup(IWorkbenchPart part) {
    	this(part, null);
    }

    public CRefactoringActionGroup(Page page) {
        createActions(false);
        setWorkbenchSite(page.getSite());
    }

    public CRefactoringActionGroup(IWorkbenchPart part, String groupName) {
        if (groupName != null && groupName.length() > 0) {
            fGroupName= groupName;
        }
        createActions(part instanceof ITextEditor);

        if (part instanceof ITextEditor) {
        	setEditor((ITextEditor) part);
        }
        else {
        	setWorkbenchSite(part.getSite());
        }
    }

	private void createActions(boolean forEditor) {
		fRenameAction = new CRenameAction();
        fRenameAction.setActionDefinitionId(ICEditorActionDefinitionIds.RENAME_ELEMENT);
        fAllActions.add(fRenameAction);
        
        if (forEditor) {
        	fExtractConstantAction= new ExtractConstantAction();
        	fExtractConstantAction.setActionDefinitionId(ICEditorActionDefinitionIds.EXTRACT_CONSTANT);
        	fAllActions.add(fExtractConstantAction);
        	
        	fExtractLocalVariableAction= new ExtractLocalVariableAction();
        	fExtractLocalVariableAction.setActionDefinitionId(ICEditorActionDefinitionIds.EXTRACT_LOCAL_VARIABLE);
        	fAllActions.add(fExtractLocalVariableAction);

        	fExtractFunctionAction = new ExtractFunctionAction();
			fExtractFunctionAction.setActionDefinitionId(ICEditorActionDefinitionIds.EXTRACT_FUNCTION);
			fAllActions.add(fExtractFunctionAction);

			fToggleFunctionAction = new ToggleFunctionAction();
			fToggleFunctionAction.setActionDefinitionId(ICEditorActionDefinitionIds.TOGGLE_FUNCTION);
			fAllActions.add(fToggleFunctionAction);
        }

        fHideMethodAction = new HideMethodAction();
        fHideMethodAction.setActionDefinitionId(ICEditorActionDefinitionIds.HIDE_METHOD);
        fAllActions.add(fHideMethodAction);

	}

    public void setWorkbenchSite(IWorkbenchSite site) {
		unregisterSite();
        fSite= site;
        
        for (RefactoringAction action : fAllActions) {
			action.setSite(site);
		}        
    	final ISelectionProvider sp = fSite.getSelectionProvider();
        sp.addSelectionChangedListener(this);
        updateActions(sp.getSelection());
    }

	private void unregisterSite() {
		if (fSite != null) {
    		fSite.getSelectionProvider().removeSelectionChangedListener(this);
    		fSite= null;
    	}
	}
    
	public void setEditor(ITextEditor textEditor) {
		unregisterSite();
		
        for (RefactoringAction action : fAllActions) {
			action.setEditor(textEditor);
		}        
    }


	@Override
	public void fillActionBars(IActionBars actionBar) {
		super.fillActionBars(actionBar);
		setActionHandler(actionBar, CdtActionConstants.RENAME, fRenameAction);
		setActionHandler(actionBar, CdtActionConstants.EXTRACT_CONSTANT, fExtractConstantAction);
		setActionHandler(actionBar, CdtActionConstants.EXTRACT_LOCAL_VARIABLE, fExtractLocalVariableAction);
		setActionHandler(actionBar, CdtActionConstants.EXTRACT_METHOD, fExtractFunctionAction);
		setActionHandler(actionBar, CdtActionConstants.TOGGLE_FUNCTION, fToggleFunctionAction);
		setActionHandler(actionBar, CdtActionConstants.HIDE_METHOD, fHideMethodAction);		
	}

	private void setActionHandler(IActionBars actionBar, String id, RefactoringAction action) {
		if (action != null)
			actionBar.setGlobalActionHandler(id, action);
	}

    /* (non-Javadoc)
     * Method declared in ActionGroup
     */
    @Override
	public void fillContextMenu(IMenuManager menu) {
    	updateActionBars();
    	
    	boolean needMenu= false;
        for (RefactoringAction action : fAllActions) {
			if (action.isEnabled()) {
				needMenu= true;
				break;
			}
		}        

		if (needMenu) {
			IMenuManager refactorSubmenu = new MenuManager(Messages.CRefactoringActionGroup_menu, MENU_ID); 
			refactorSubmenu.add(new Separator(GROUP_REORG));
			addAction(refactorSubmenu, fRenameAction);
			refactorSubmenu.add(new Separator(GROUP_CODING));
			addAction(refactorSubmenu, fExtractConstantAction);
			addAction(refactorSubmenu, fExtractLocalVariableAction);
			addAction(refactorSubmenu, fExtractFunctionAction);
			addAction(refactorSubmenu, fToggleFunctionAction);
			addAction(refactorSubmenu, fHideMethodAction);
			refactorSubmenu.add(new Separator(GROUP_REORG2));
			refactorSubmenu.add(new Separator(GROUP_TYPE));
			refactorSubmenu.add(new Separator(GROUP_TYPE2));
			refactorSubmenu.add(new Separator(GROUP_CODING2));
			refactorSubmenu.add(new Separator(GROUP_TYPE3));
        
			menu.appendToGroup(fGroupName, refactorSubmenu);
		}
    }

	private void addAction(IMenuManager refactorSubmenu, RefactoringAction action) {
		if (action != null && action.isEnabled()) {
			refactorSubmenu.add(action);
		}
	}

	private ICElement getCElement(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection) selection;
			if (ss.size() == 1) {
				Object o= ss.getFirstElement();
				if (o instanceof ICElement && o instanceof ISourceReference) {
					return (ICElement) o;
				}
			}
		}
		return null;
	}

	private void updateActions(ISelection selection) {
		ICElement celem= getCElement(selection);
		for (RefactoringAction action : fAllActions) {
			action.updateSelection(celem);
		}
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#dispose()
	 */
	@Override
	public void dispose() {
		unregisterSite();
		fSite= null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		updateActions(event.getSelection());
	}
}
