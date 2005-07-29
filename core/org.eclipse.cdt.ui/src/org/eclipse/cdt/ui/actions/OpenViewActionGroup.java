/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.actions;

import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.IContextMenuConstants;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.part.Page;

/**
 * Action group that adds actions to open a new JDT view part or an external 
 * viewer to a context menu and the global menu bar.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.0
 */
public class OpenViewActionGroup extends ActionGroup {

    private boolean fEditorIsOwner;
//	private boolean fIsTypeHiararchyViewerOwner;
//    private boolean fIsCallHiararchyViewerOwner;
	private IWorkbenchSite fSite;

//	private OpenSuperImplementationAction fOpenSuperImplementation;
//	private OpenExternalJavadocAction fOpenExternalJavadoc;
//	private OpenTypeHierarchyAction fOpenTypeHierarchy;
//    private OpenCallHierarchyAction fOpenCallHierarchy;
	private PropertyDialogAction fOpenPropertiesDialog;

	/**
	 * Creates a new <code>OpenActionGroup</code>. The group requires
	 * that the selection provided by the page's selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
	 * 
	 * @param page the page that owns this action group
	 */
	public OpenViewActionGroup(Page page) {
		createSiteActions(page.getSite());
	}
	
	/**
	 * Creates a new <code>OpenActionGroup</code>. The group requires
	 * that the selection provided by the part's selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
	 * 
	 * @param part the view part that owns this action group
	 */
	public OpenViewActionGroup(IViewPart part) {
		createSiteActions(part.getSite());
//		fIsTypeHiararchyViewerOwner= part instanceof TypeHierarchyViewPart;
//        fIsCallHiararchyViewerOwner= part instanceof ICallHierarchyViewPart;
	}
	
	/**
	 * Note: This constructor is for internal use only. Clients should not call this constructor.
	 */
	public OpenViewActionGroup(CEditor part) {
		fEditorIsOwner= true;

//		fOpenSuperImplementation= new OpenSuperImplementationAction(part);
//		fOpenSuperImplementation.setActionDefinitionId(IJavaEditorActionDefinitionIds.OPEN_SUPER_IMPLEMENTATION);
//		part.setAction("OpenSuperImplementation", fOpenSuperImplementation); //$NON-NLS-1$

//		fOpenExternalJavadoc= new OpenExternalJavadocAction(part);
//		fOpenExternalJavadoc.setActionDefinitionId(IJavaEditorActionDefinitionIds.OPEN_EXTERNAL_JAVADOC);
//		part.setAction("OpenExternalJavadoc", fOpenExternalJavadoc); //$NON-NLS-1$

//		fOpenTypeHierarchy= new OpenTypeHierarchyAction(part);
//		fOpenTypeHierarchy.setActionDefinitionId(ICEditorActionDefinitionIds.OPEN_TYPE_HIERARCHY);
//		part.setAction("OpenTypeHierarchy", fOpenTypeHierarchy); //$NON-NLS-1$

//        fOpenCallHierarchy= new OpenCallHierarchyAction(part);
//        fOpenCallHierarchy.setActionDefinitionId(IJavaEditorActionDefinitionIds.OPEN_CALL_HIERARCHY);
//        part.setAction("OpenCallHierarchy", fOpenCallHierarchy); //$NON-NLS-1$

		initialize(part.getEditorSite());
	}

	private void createSiteActions(IWorkbenchSite site) {
//		fOpenSuperImplementation= new OpenSuperImplementationAction(site);
//		fOpenSuperImplementation.setActionDefinitionId(IJavaEditorActionDefinitionIds.OPEN_SUPER_IMPLEMENTATION);
//
//		fOpenExternalJavadoc= new OpenExternalJavadocAction(site);
//		fOpenExternalJavadoc.setActionDefinitionId(IJavaEditorActionDefinitionIds.OPEN_EXTERNAL_JAVADOC);

//		fOpenTypeHierarchy= new OpenTypeHierarchyAction(site);
//		fOpenTypeHierarchy.setActionDefinitionId(ICEditorActionDefinitionIds.OPEN_TYPE_HIERARCHY);

//		fOpenCallHierarchy= new OpenCallHierarchyAction(site);
//        fOpenCallHierarchy.setActionDefinitionId(IJavaEditorActionDefinitionIds.OPEN_CALL_HIERARCHY);

        fOpenPropertiesDialog= new PropertyDialogAction(site, site.getSelectionProvider());
        fOpenPropertiesDialog.setActionDefinitionId("org.eclipse.ui.file.properties"); //$NON-NLS-1$
		
        initialize(site);
	}
	
	private void initialize(IWorkbenchSite site) {
		fSite= site;
		ISelectionProvider provider= fSite.getSelectionProvider();
		ISelection selection= provider.getSelection();
//		fOpenSuperImplementation.update(selection);
//		fOpenExternalJavadoc.update(selection);
//		fOpenTypeHierarchy.update(selection);
//        fOpenCallHierarchy.update(selection);
		if (!fEditorIsOwner) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss= (IStructuredSelection)selection;
				fOpenPropertiesDialog.selectionChanged(ss);
			} else {
				fOpenPropertiesDialog.selectionChanged(selection);
			}
//			provider.addSelectionChangedListener(fOpenSuperImplementation);
//			provider.addSelectionChangedListener(fOpenExternalJavadoc);
//			provider.addSelectionChangedListener(fOpenTypeHierarchy);
//           provider.addSelectionChangedListener(fOpenCallHierarchy);
			// no need to register the open properties dialog action since it registers itself
		}
	}

	/* (non-Javadoc)
	 * Method declared in ActionGroup
	 */
	public void fillActionBars(IActionBars actionBar) {
		super.fillActionBars(actionBar);
		setGlobalActionHandlers(actionBar);
	}
	
	/* (non-Javadoc)
	 * Method declared in ActionGroup
	 */
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
//		if (!fIsTypeHiararchyViewerOwner)
//			appendToGroup(menu, fOpenTypeHierarchy);
//      if (!fIsCallHiararchyViewerOwner)
//      	appendToGroup(menu, fOpenCallHierarchy);
//		appendToGroup(menu, fOpenSuperImplementation);
		IStructuredSelection selection= getStructuredSelection();
		if (fOpenPropertiesDialog != null && fOpenPropertiesDialog.isEnabled() && selection != null &&fOpenPropertiesDialog.isApplicableForSelection(selection))
			menu.appendToGroup(IContextMenuConstants.GROUP_PROPERTIES, fOpenPropertiesDialog);
	}

	/*
	 * @see ActionGroup#dispose()
	 */
	public void dispose() {
		ISelectionProvider provider= fSite.getSelectionProvider();
//		provider.removeSelectionChangedListener(fOpenSuperImplementation);
//		provider.removeSelectionChangedListener(fOpenExternalJavadoc);
//		provider.removeSelectionChangedListener(fOpenTypeHierarchy);
//        provider.removeSelectionChangedListener(fOpenCallHierarchy);
		super.dispose();
	}
	
	private void setGlobalActionHandlers(IActionBars actionBars) {
//		actionBars.setGlobalActionHandler(JdtActionConstants.OPEN_SUPER_IMPLEMENTATION, fOpenSuperImplementation);
//		actionBars.setGlobalActionHandler(JdtActionConstants.OPEN_EXTERNAL_JAVA_DOC, fOpenExternalJavadoc);
//		actionBars.setGlobalActionHandler(CdtActionConstants.OPEN_TYPE_HIERARCHY, fOpenTypeHierarchy);
//        actionBars.setGlobalActionHandler(JdtActionConstants.OPEN_CALL_HIERARCHY, fOpenCallHierarchy);
		actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(), fOpenPropertiesDialog);		
	}
	
	private void appendToGroup(IMenuManager menu, IAction action) {
		if (action.isEnabled())
			menu.appendToGroup(IContextMenuConstants.GROUP_OPEN, action);
	}
	
	private IStructuredSelection getStructuredSelection() {
		ISelection selection= getContext().getSelection();
		if (selection instanceof IStructuredSelection)
			return (IStructuredSelection)selection;
		return null;
	}

	public static boolean canActionBeAdded(ISelection selection) {
		if(selection instanceof ITextSelection) {
			return (((ITextSelection)selection).getLength() > 0);
		}
		return getElement(selection) != null;
	}
	
	private static ICElement getElement(ISelection sel) {
		if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
			List list= ((IStructuredSelection)sel).toList();
			if (list.size() == 1) {
				Object element= list.get(0);
				if (element instanceof ICElement) {
					return (ICElement)element;
				}
			}
		}
		return null;
	}
}
