/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.typehierarchy;

import java.security.Signature;

import org.eclipse.cdt.core.browser.TypeUtil;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMember;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.internal.ui.actions.OpenActionUtil;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.WorkbenchException;

public class OpenTypeHierarchyUtil {
	
	private OpenTypeHierarchyUtil() {
	}

	public static TypeHierarchyViewPart open(ICElement element, IWorkbenchWindow window) {
		ICElement[] candidates= getCandidates(element);
		if (candidates != null) {
			return open(candidates, window);
		}
		return null;
	}	
	
	public static TypeHierarchyViewPart open(ICElement[] candidates, IWorkbenchWindow window) {
		Assert.isTrue(candidates != null && candidates.length != 0);
			
		ICElement input= null;
		if (candidates.length > 1) {
			String title= CUIMessages.getString("OpenTypeHierarchyUtil.selectionDialog.title");  //$NON-NLS-1$
			String message= CUIMessages.getString("OpenTypeHierarchyUtil.selectionDialog.message"); //$NON-NLS-1$
			input= OpenActionUtil.selectCElement(candidates, window.getShell(), title, message);			
		} else {
			input= candidates[0];
		}
		if (input == null)
			return null;
			
		try {
			if (PreferenceConstants.OPEN_TYPE_HIERARCHY_IN_PERSPECTIVE.equals(PreferenceConstants.getPreferenceStore().getString(PreferenceConstants.OPEN_TYPE_HIERARCHY))) {
				return openInPerspective(window, input);
			} else {
				return openInViewPart(window, input);
			}
				
		} catch (WorkbenchException e) {
			ExceptionHandler.handle(e, window.getShell(),
				CUIMessages.getString("OpenTypeHierarchyUtil.error.open_perspective"), //$NON-NLS-1$
				e.getMessage());
		} catch (CModelException e) {
			ExceptionHandler.handle(e, window.getShell(),
				CUIMessages.getString("OpenTypeHierarchyUtil.error.open_editor"), //$NON-NLS-1$
				e.getMessage());
		}
		return null;
	}

	private static TypeHierarchyViewPart openInViewPart(IWorkbenchWindow window, ICElement input) {
		IWorkbenchPage page= window.getActivePage();
		try {
			TypeHierarchyViewPart result= (TypeHierarchyViewPart) page.findView(CUIPlugin.ID_TYPE_HIERARCHY);
			if (result != null) {
				result.clearNeededRefresh(); // avoid refresh of old hierarchy on 'becomes visible'
			}
			result= (TypeHierarchyViewPart) page.showView(CUIPlugin.ID_TYPE_HIERARCHY);
			result.setInputElement(input);
			
			if (input instanceof IMember) {
				result.selectMember((IMember) input);
			}	
			return result;
		} catch (CoreException e) {
			ExceptionHandler.handle(e, window.getShell(), 
				CUIMessages.getString("OpenTypeHierarchyUtil.error.open_view"), e.getMessage()); //$NON-NLS-1$
		}
		return null;		
	}
	
	private static TypeHierarchyViewPart openInPerspective(IWorkbenchWindow window, ICElement input) throws WorkbenchException, CModelException {
		IWorkbench workbench= CUIPlugin.getDefault().getWorkbench();
		// The problem is that the input element can be a working copy. So we first convert it to the original element if
		// it exists.
		ICElement perspectiveInput= input;
		
		if (input instanceof IMember) {
//			if (input.getElementType() != ITypeElement.TYPE) {
		    if (TypeUtil.isClassOrStruct(input)) {
//				perspectiveInput= ((IMember)input).getDeclaringType();
		        perspectiveInput= TypeUtil.getDeclaringType(input);
			} else {
				perspectiveInput= input;
			}
		}
		IWorkbenchPage page= workbench.showPerspective(CUIPlugin.ID_CHIERARCHY_PERSPECTIVE, window, perspectiveInput);
		
		TypeHierarchyViewPart part= (TypeHierarchyViewPart) page.findView(CUIPlugin.ID_TYPE_HIERARCHY);
		if (part != null) {
			part.clearNeededRefresh(); // avoid refresh of old hierarchy on 'becomes visible'
		}		
		part= (TypeHierarchyViewPart) page.showView(CUIPlugin.ID_TYPE_HIERARCHY);
		part.setInputElement(perspectiveInput);
		if (input instanceof IMember) {
			part.selectMember((IMember) input);
			
			if (page.getEditorReferences().length == 0) {
				openEditor(input, false); // only open when the perspecive has been created
			}
		}
		return part;
	}

	private static void openEditor(Object input, boolean activate) throws PartInitException, CModelException {
		IEditorPart part= EditorUtility.openInEditor(input, activate);
		if (input instanceof ICElement)
			EditorUtility.revealInEditor(part, (ICElement) input);
	}
	
	/**
	 * Converts the input to a possible input candidates
	 */	
	public static ICElement[] getCandidates(Object input) {
		if (!(input instanceof ICElement)) {
			return null;
		}
//		try {
			ICElement elem= (ICElement) input;
			switch (elem.getElementType()) {
				case ICElement.C_METHOD:
				case ICElement.C_METHOD_DECLARATION:
				case ICElement.C_FIELD:
				case ICElement.C_UNION:
				case ICElement.C_ENUMERATION:
				case ICElement.C_TYPEDEF:
				    return new ICElement[] { TypeUtil.getDeclaringType(elem) };
				case ICElement.C_CLASS:
				case ICElement.C_STRUCT:
					return new ICElement[] { elem };
				case ICElement.C_NAMESPACE:
				    return TypeUtil.getTypes(elem);
				case ICElement.C_UNIT: {
					ITranslationUnit cu= (ITranslationUnit) elem.getAncestor(ICElement.C_UNIT);
					if (cu != null) {
					    return TypeUtil.getTypes(cu);
					}
					break;
				}					
				case ICElement.C_PROJECT:
				default:
			}
//		} catch (CModelException e) {
//			CUIPlugin.getDefault().log(e);
//		}
		return null;	
	}
}
