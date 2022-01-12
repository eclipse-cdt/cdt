/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.actions;

import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.internal.ui.IContextMenuConstants;
import org.eclipse.cdt.internal.ui.callhierarchy.OpenCallHierarchyAction;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.ICEditorActionDefinitionIds;
import org.eclipse.cdt.internal.ui.includebrowser.OpenIncludeBrowserAction;
import org.eclipse.cdt.internal.ui.search.actions.OpenDeclarationsAction;
import org.eclipse.cdt.internal.ui.typehierarchy.OpenTypeHierarchyAction;
import org.eclipse.cdt.ui.ICModelBasedEditor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Action group that adds actions to open a new CDT view part or an external
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
	private boolean fSuppressTypeHierarchy;
	private boolean fSuppressCallHierarchy;
	private boolean fSuppressProperties;
	private boolean fEnableIncludeBrowser;

	private IWorkbenchSite fSite;
	private String fGroupName = IContextMenuConstants.GROUP_OPEN;

	//	private OpenSuperImplementationAction fOpenSuperImplementation;
	//	private OpenExternalJavadocAction fOpenExternalJavadoc;
	private OpenTypeHierarchyAction fOpenTypeHierarchy;
	private PropertyDialogAction fOpenPropertiesDialog;
	private OpenCallHierarchyAction fOpenCallHierarchy;
	private OpenIncludeBrowserAction fOpenIncludeBrowser;
	private OpenDeclarationsAction fOpenDeclaration;

	/**
	 * Creates a new <code>OpenActionGroup</code>. The group requires
	 * that the selection provided by the page's selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
	 *
	 * @param page the page that owns this action group
	 */
	public OpenViewActionGroup(Page page) {
		createSiteActions(page.getSite(), null);
	}

	/**
	 * Creates a new <code>OpenActionGroup</code>. The group requires
	 * that the selection provided by the page's selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
	 *
	 * @param page the page that owns this action group
	 *
	 * @noreference This constructor is not intended to be referenced by clients.
	 */
	public OpenViewActionGroup(Page page, CEditor editor) {
		createSiteActions(page.getSite(), editor);
	}

	/**
	 * Creates a new <code>OpenActionGroup</code>. The group requires
	 * that the selection provided by the part's selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
	 *
	 * @param part the view part that owns this action group
	 */
	public OpenViewActionGroup(IWorkbenchPart part) {
		createSiteActions(part.getSite(), null);
	}

	/**
	 * Note: This constructor is for internal use only. Clients should not call this constructor.
	 */
	public OpenViewActionGroup(ITextEditor part) {
		fEditorIsOwner = true;

		//		fOpenSuperImplementation= new OpenSuperImplementationAction(part);
		//		fOpenSuperImplementation.setActionDefinitionId(IJavaEditorActionDefinitionIds.OPEN_SUPER_IMPLEMENTATION);
		//		part.setAction("OpenSuperImplementation", fOpenSuperImplementation); //$NON-NLS-1$

		//		fOpenExternalJavadoc= new OpenExternalJavadocAction(part);
		//		fOpenExternalJavadoc.setActionDefinitionId(IJavaEditorActionDefinitionIds.OPEN_EXTERNAL_JAVADOC);
		//		part.setAction("OpenExternalJavadoc", fOpenExternalJavadoc); //$NON-NLS-1$

		fOpenTypeHierarchy = new OpenTypeHierarchyAction(part);
		fOpenTypeHierarchy.setActionDefinitionId(ICEditorActionDefinitionIds.OPEN_TYPE_HIERARCHY);
		part.setAction("OpenTypeHierarchy", fOpenTypeHierarchy); //$NON-NLS-1$

		fOpenCallHierarchy = new OpenCallHierarchyAction(part);
		fOpenCallHierarchy.setActionDefinitionId(ICEditorActionDefinitionIds.OPEN_CALL_HIERARCHY);
		part.setAction("OpenCallHierarchy", fOpenCallHierarchy); //$NON-NLS-1$

		fOpenIncludeBrowser = new OpenIncludeBrowserAction(part);
		fOpenIncludeBrowser.setActionDefinitionId(ICEditorActionDefinitionIds.OPEN_INCLUDE_BROWSER);
		part.setAction("OpenIncludeBrowser", fOpenIncludeBrowser); //$NON-NLS-1$

		if (part instanceof ICModelBasedEditor) {
			fOpenDeclaration = new OpenDeclarationsAction((ICModelBasedEditor) part);
			fOpenDeclaration.setActionDefinitionId(ICEditorActionDefinitionIds.OPEN_DECL);
			part.setAction("OpenDeclarations", fOpenDeclaration); //$NON-NLS-1$
		}

		initialize(part.getEditorSite());
	}

	private void createSiteActions(IWorkbenchSite site, CEditor editor) {
		//		fOpenSuperImplementation= new OpenSuperImplementationAction(site);
		//		fOpenSuperImplementation.setActionDefinitionId(IJavaEditorActionDefinitionIds.OPEN_SUPER_IMPLEMENTATION);
		//
		//		fOpenExternalJavadoc= new OpenExternalJavadocAction(site);
		//		fOpenExternalJavadoc.setActionDefinitionId(IJavaEditorActionDefinitionIds.OPEN_EXTERNAL_JAVADOC);

		fOpenTypeHierarchy = new OpenTypeHierarchyAction(site);
		fOpenTypeHierarchy.setActionDefinitionId(ICEditorActionDefinitionIds.OPEN_TYPE_HIERARCHY);

		fOpenCallHierarchy = new OpenCallHierarchyAction(site);
		fOpenCallHierarchy.setActionDefinitionId(ICEditorActionDefinitionIds.OPEN_CALL_HIERARCHY);

		fOpenIncludeBrowser = new OpenIncludeBrowserAction(site);
		fOpenIncludeBrowser.setActionDefinitionId(ICEditorActionDefinitionIds.OPEN_INCLUDE_BROWSER);

		if (editor != null) {
			fOpenDeclaration = new OpenDeclarationsAction(editor);
			fOpenDeclaration.setActionDefinitionId(ICEditorActionDefinitionIds.OPEN_DECL);
		}

		fOpenPropertiesDialog = new PropertyDialogAction(site, site.getSelectionProvider());
		fOpenPropertiesDialog.setActionDefinitionId("org.eclipse.ui.file.properties"); //$NON-NLS-1$

		initialize(site);
	}

	private void initialize(IWorkbenchSite site) {
		fSite = site;
		ISelectionProvider provider = fSite.getSelectionProvider();
		ISelection selection = provider.getSelection();
		//		fOpenSuperImplementation.update(selection);
		//		fOpenExternalJavadoc.update(selection);
		fOpenTypeHierarchy.update(selection);
		fOpenCallHierarchy.update(selection);
		fOpenIncludeBrowser.update(selection);
		if (!fEditorIsOwner) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				fOpenPropertiesDialog.selectionChanged(ss);
			} else {
				fOpenPropertiesDialog.selectionChanged(selection);
			}
			//			provider.addSelectionChangedListener(fOpenSuperImplementation);
			//			provider.addSelectionChangedListener(fOpenExternalJavadoc);
			provider.addSelectionChangedListener(fOpenTypeHierarchy);
			provider.addSelectionChangedListener(fOpenCallHierarchy);
			provider.addSelectionChangedListener(fOpenIncludeBrowser);
			// no need to register the open properties dialog action since it registers itself
		}
	}

	/* (non-Javadoc)
	 * Method declared in ActionGroup
	 */
	@Override
	public void fillActionBars(IActionBars actionBar) {
		super.fillActionBars(actionBar);
		setGlobalActionHandlers(actionBar);
	}

	/* (non-Javadoc)
	 * Method declared in ActionGroup
	 */
	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		IStructuredSelection selection = getStructuredSelection();
		if (!fEditorIsOwner) {
			if (fOpenDeclaration != null && fOpenDeclaration.isEnabled()) {
				if (selection != null) {
					Object elem = selection.getFirstElement();
					if (elem instanceof ICElement && elem instanceof IInclude == false) {
						menu.appendToGroup(fGroupName, fOpenDeclaration);
					}
				}
			}
			if (!fSuppressTypeHierarchy && fOpenTypeHierarchy.isEnabled()) {
				menu.appendToGroup(fGroupName, fOpenTypeHierarchy);
			}
			if (!fSuppressCallHierarchy && fOpenCallHierarchy.isEnabled()) {
				menu.appendToGroup(fGroupName, fOpenCallHierarchy);
			}
			if (fEnableIncludeBrowser && fOpenIncludeBrowser.isEnabled()) {
				menu.appendToGroup(fGroupName, fOpenIncludeBrowser);
			}
		}
		//		appendToGroup(menu, fOpenSuperImplementation);
		if (!fSuppressProperties) {
			if (fOpenPropertiesDialog != null && fOpenPropertiesDialog.isEnabled() && selection != null
					&& fOpenPropertiesDialog.isApplicableForSelection(selection)) {
				menu.appendToGroup(IContextMenuConstants.GROUP_PROPERTIES, fOpenPropertiesDialog);
			}
		}
	}

	/*
	 * @see ActionGroup#dispose()
	 */
	@Override
	public void dispose() {
		ISelectionProvider provider = fSite.getSelectionProvider();
		//		provider.removeSelectionChangedListener(fOpenSuperImplementation);
		//		provider.removeSelectionChangedListener(fOpenExternalJavadoc);
		provider.removeSelectionChangedListener(fOpenTypeHierarchy);
		provider.removeSelectionChangedListener(fOpenCallHierarchy);
		provider.removeSelectionChangedListener(fOpenIncludeBrowser);
		if (fOpenPropertiesDialog != null) {
			fOpenPropertiesDialog.dispose();
		}
		super.dispose();
	}

	private void setGlobalActionHandlers(IActionBars actionBars) {
		//		actionBars.setGlobalActionHandler(JdtActionConstants.OPEN_SUPER_IMPLEMENTATION, fOpenSuperImplementation);
		//		actionBars.setGlobalActionHandler(JdtActionConstants.OPEN_EXTERNAL_JAVA_DOC, fOpenExternalJavadoc);
		actionBars.setGlobalActionHandler(CdtActionConstants.OPEN_TYPE_HIERARCHY, fOpenTypeHierarchy);
		actionBars.setGlobalActionHandler(CdtActionConstants.OPEN_CALL_HIERARCHY, fOpenCallHierarchy);
		actionBars.setGlobalActionHandler(CdtActionConstants.OPEN_INCLUDE_BROWSER, fOpenIncludeBrowser);
		if (fOpenDeclaration != null) {
			actionBars.setGlobalActionHandler(CdtActionConstants.OPEN_DECLARATION, fOpenDeclaration);
		}
		if (fOpenPropertiesDialog != null) {
			actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(), fOpenPropertiesDialog);
		}
	}

	private IStructuredSelection getStructuredSelection() {
		if (fSite != null) {
			ISelection selection = fSite.getSelectionProvider().getSelection();
			if (selection instanceof IStructuredSelection) {
				return (IStructuredSelection) selection;
			}
		}
		return null;
	}

	public static boolean canActionBeAdded(ISelection selection) {
		if (selection instanceof ITextSelection) {
			return true;
		}
		return getElement(selection) != null;
	}

	private static ICElement getElement(ISelection sel) {
		if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
			List<?> list = ((IStructuredSelection) sel).toList();
			if (list.size() == 1) {
				Object element = list.get(0);
				if (element instanceof ICElement) {
					return (ICElement) element;
				}
			}
		}
		return null;
	}

	public void setAppendToGroup(String groupName) {
		fGroupName = groupName;
	}

	public void setSuppressTypeHierarchy(boolean suppressTypeHierarchy) {
		fSuppressTypeHierarchy = suppressTypeHierarchy;
	}

	public void setSuppressCallHierarchy(boolean suppressCallHierarchy) {
		fSuppressCallHierarchy = suppressCallHierarchy;
	}

	public void setSuppressProperties(boolean suppressProperties) {
		fSuppressProperties = suppressProperties;
	}

	public void setEnableIncludeBrowser(boolean enableIncludeBrowser) {
		fEnableIncludeBrowser = enableIncludeBrowser;
	}
}
