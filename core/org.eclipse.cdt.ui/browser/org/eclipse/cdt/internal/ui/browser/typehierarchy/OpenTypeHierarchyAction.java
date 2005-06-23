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
package org.eclipse.cdt.internal.ui.browser.typehierarchy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeSearchScope;
import org.eclipse.cdt.core.browser.QualifiedTypeName;
import org.eclipse.cdt.core.browser.TypeSearchScope;
import org.eclipse.cdt.core.browser.TypeUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.ICStatusConstants;
import org.eclipse.cdt.internal.ui.actions.ActionMessages;
import org.eclipse.cdt.internal.ui.actions.ActionUtil;
import org.eclipse.cdt.internal.ui.actions.SelectionConverter;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.actions.SelectionDispatchAction;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * This action opens a type hierarchy on the selected type.
 * <p>
 * The action is applicable to selections containing elements of type
 * <code>IType</code>.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.0
 */
public class OpenTypeHierarchyAction extends SelectionDispatchAction {
	
	private CEditor fEditor;
	
	/**
	 * Creates a new <code>OpenTypeHierarchyAction</code>. The action requires
	 * that the selection provided by the site's selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
	 * 
	 * @param site the site providing context information for this action
	 */
	public OpenTypeHierarchyAction(IWorkbenchSite site) {
		super(site);
		setText(ActionMessages.getString("OpenTypeHierarchyAction.label")); //$NON-NLS-1$
		setToolTipText(ActionMessages.getString("OpenTypeHierarchyAction.tooltip")); //$NON-NLS-1$
		setDescription(ActionMessages.getString("OpenTypeHierarchyAction.description")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, ICHelpContextIds.OPEN_TYPE_HIERARCHY_ACTION);
	}
	
	/**
	 * Note: This constructor is for internal use only. Clients should not call this constructor.
	 */
	public OpenTypeHierarchyAction(CEditor editor) {
		this(editor.getEditorSite());
		fEditor= editor;
		setEnabled(SelectionConverter.canOperateOn(fEditor));
	}

	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	public void selectionChanged(ITextSelection selection) {
/*		boolean enable = true;
		ICElement selectedElement = null;
		try {
			ICElement[] elements = SelectionConverter.getElementsAtOffset(fEditor);
			if (elements == null || elements.length == 0) {
			    setEnabled(false);
			    return;
			}
			// find class or struct
			for (int i = 0; i < elements.length; ++i) {
			    if (TypeUtil.isClassOrStruct(elements[i])) {
			        selectedElement = elements[i];
			        break;
			    }
			}

		    if (selectedElement == null) {
			    setEnabled(false);
			    return;
			}
		} catch (CModelException e) {
			setEnabled(false);
			return;
		}

		ITextSelection textSelection= (ITextSelection)fEditor.getSelectionProvider().getSelection();
		
		if (textSelection == null) {
			setEnabled(false);
			return;
		}
		
		if( (((CElement)selectedElement).getIdStartPos() != textSelection.getOffset()) 
		|| (((CElement)selectedElement).getIdLength() != textSelection.getLength())) {
			enable = false;
		}
		setEnabled(enable);
*/	}

	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(isEnabled(selection));
	}
	
	private boolean isEnabled(IStructuredSelection selection) {
		if (selection.size() != 1)
			return false;
		Object input= selection.getFirstElement();
		if (input instanceof ICElement) {
		    ICElement elem = (ICElement)input;
		    return TypeUtil.isClassOrStruct(elem);
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	public void run(ITextSelection selection) {
		ICElement input= SelectionConverter.getInput(fEditor);
		if (!ActionUtil.isProcessable(getShell(), input))
			return;		
		
		ICElement[] elements= SelectionConverter.codeResolveOrInputHandled(fEditor, getShell(), getDialogTitle());
		if (elements == null)
			return;
		List candidates= new ArrayList(elements.length);
		for (int i= 0; i < elements.length; i++) {
		    ICElement elem = elements[i];
		    if (elem instanceof ITranslationUnit) {
		        ICElement[] realElems = findTypeDeclarations(selection.getText(), (ITranslationUnit)elem, selection.getOffset(), selection.getLength(), new NullProgressMonitor());
		        if (realElems != null) {
		            for (int j = 0; j < realElems.length; ++j) {
					    ICElement[] resolvedElements= OpenTypeHierarchyUtil.getCandidates(realElems[j]);
						if (resolvedElements != null)	
							candidates.addAll(Arrays.asList(resolvedElements));
		            }
		        }
		    } else {
			    ICElement[] resolvedElements= OpenTypeHierarchyUtil.getCandidates(elem);
				if (resolvedElements != null)	
					candidates.addAll(Arrays.asList(resolvedElements));
		    }
		}
		run((ICElement[])candidates.toArray(new ICElement[candidates.size()]));
	}
	
	private ICElement[] findTypeDeclarations(String name, ITranslationUnit unit, int offset, int length, IProgressMonitor monitor) {
		final ITypeSearchScope wsScope = new TypeSearchScope(true);
		if (!AllTypesCache.isCacheUpToDate(wsScope)) {
			AllTypesCache.updateCache(wsScope, monitor);
		}
        ITypeSearchScope projectScope = new TypeSearchScope();
        projectScope.add(unit.getCProject());
        int[] kinds = {ICElement.C_CLASS, ICElement.C_STRUCT};
        ITypeInfo[] types = AllTypesCache.getTypes(projectScope, new QualifiedTypeName(name), kinds, true);
        if (types != null) {
            List elements = new ArrayList(types.length);
            for (int i = 0; i < types.length; ++i) {
                ICElement e = AllTypesCache.getElementForType(types[i], true, true, monitor);
                if (e != null && !elements.contains(e))
                    elements.add(e);
            }
            if (!elements.isEmpty())
                return (ICElement[])elements.toArray(new ICElement[elements.size()]);
        }
        return null;
	}

        /* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	public void run(IStructuredSelection selection) {
		if (selection.size() != 1)
			return;
		Object input= selection.getFirstElement();

		if (!(input instanceof ICElement)) {
			IStatus status= createStatus(ActionMessages.getString("OpenTypeHierarchyAction.messages.no_c_element")); //$NON-NLS-1$
			ErrorDialog.openError(getShell(), getDialogTitle(), ActionMessages.getString("OpenTypeHierarchyAction.messages.title"), status); //$NON-NLS-1$
			return;
		}
		ICElement element= (ICElement) input;
		if (!ActionUtil.isProcessable(getShell(), element))
			return;

		List result= new ArrayList(1);
		IStatus status= compileCandidates(result, element);
		if (status.isOK()) {
			run((ICElement[]) result.toArray(new ICElement[result.size()]));
		} else {
			ErrorDialog.openError(getShell(), getDialogTitle(), ActionMessages.getString("OpenTypeHierarchyAction.messages.title"), status); //$NON-NLS-1$
		}
	}
	
	public void run(ICElement[] elements) {
		if (elements.length == 0) {
			getShell().getDisplay().beep();
			return;
		}
		OpenTypeHierarchyUtil.open(elements, getSite().getWorkbenchWindow());
	}
	
	private static String getDialogTitle() {
		return ActionMessages.getString("OpenTypeHierarchyAction.dialog.title"); //$NON-NLS-1$
	}
	
	private static IStatus compileCandidates(List result, ICElement elem) {
		IStatus ok = new Status(IStatus.OK, CUIPlugin.getPluginId(), 0, "", null); //$NON-NLS-1$		
		switch (elem.getElementType()) {
			case ICElement.C_CLASS:
			case ICElement.C_STRUCT:
			case ICElement.C_PROJECT:
			    result.add(elem);
			return ok;
		}
		return createStatus(ActionMessages.getString("OpenTypeHierarchyAction.messages.no_valid_c_element")); //$NON-NLS-1$
	}
	
	private static IStatus createStatus(String message) {
		return new Status(IStatus.INFO, CUIPlugin.getPluginId(), ICStatusConstants.INTERNAL_ERROR, message, null);
	}			
}
