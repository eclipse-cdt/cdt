/*****************************************************************
 * Copyright (c) 2011 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - 
 *     	Update CDT ToggleBreakpointTargetFactory enablement (340177)
 *****************************************************************/
package org.eclipse.cdt.debug.internal.ui.breakpoints;

import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IDeclaration;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.IVariableDeclaration;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.asm.AsmTextEditor;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;

/**
 * Toggle breakpoint factor enablement tester for editors and IDeclaration.
 * 
 * @since 7.1
 */
public class ToggleCBreakpointTester extends PropertyTester {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		// test for CEditor
		if ("isCEditorSupportsCBreakpoint".equals(property) && (receiver instanceof CEditor)) { //$NON-NLS-1$
			if (!CDebugUtils.isCustomToggleBreakpointFactory())
				return true;
			
			CEditor editor = (CEditor) receiver;
			ICElement cElement = editor.getInputCElement();
			if (cElement != null) {
				ICProject cproject = cElement.getCProject();
				if (cproject != null) {
					// Handles the case for external file, check to see whether the file exist.
					// This is to workaround the EditorUtility wrongly assign the project for
					// external file.
					IResource resource = cElement.getResource();
					if (resource == null || !resource.exists())
						return true;
					
					if (CDebugUtils.isStandardCBreakpointFactory(cproject.getProject()))
						return true;
					
				} else {
					return true; // can't figure the associated project, enable it by default.
				}
			}
			
		// test for AsmEditor
		} else if ("isAsmEditorSupportsCBreakpoint".equals(property) && (receiver instanceof AsmTextEditor)) { //$NON-NLS-1$
			if (!CDebugUtils.isCustomToggleBreakpointFactory())
				return true;
			
			AsmTextEditor editor = (AsmTextEditor) receiver;
			ICElement cElement = editor.getInputCElement();
			if (cElement != null) {
				// Handles the case for external file, check to see whether the file exist.
				// This is to workaround the EditorUtility wrongly assign the project for
				// external file.
				IResource resource = cElement.getResource();
				if (resource == null || !resource.exists())
					return true;
				
				ICProject cproject = cElement.getCProject();
				if (cproject != null) {
					if (CDebugUtils.isStandardCBreakpointFactory(cproject.getProject()))
						return true;
					
				} else {
					return true; // can't figure the associated project, enable it by default.
				}
			}
			
		// test for IVariableDeclaration, IFunctionDeclaration, IMethodDeclaration
		} else if ("isCDeclarationSupportsCBreakpoint".equals(property) && (receiver instanceof List<?>)) { //$NON-NLS-1$
			if (!CDebugUtils.isCustomToggleBreakpointFactory())
				return true;
			
			List<?> list = (List<?>) receiver;
			if (list.size() == 1) {
				Object element = list.get(0);
				if ((element instanceof IDeclaration) && 
					(element instanceof IVariableDeclaration || 
					 element instanceof IFunctionDeclaration || 
					 element instanceof IMethodDeclaration)) {
					
					IDeclaration cElement = (IDeclaration) element;
					
					// Handles the case for external file, check to see whether the file exist.
					// This is to workaround the EditorUtility wrongly assign the project for
					// external file.
					IResource resource = cElement.getResource();
					if (resource == null || !resource.exists())
						return true;
					
					ICProject cproject = cElement.getCProject();
					if (cproject != null) {
						if (CDebugUtils.isStandardCBreakpointFactory(cproject.getProject()))
							return true;
						
					} else {
						return true; // can't figure the associated project, enable it by default.
					}
				}
			}
		}
		
		return false;
	}
}
