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

import org.eclipse.cdt.core.browser.TypeUtil;
import org.eclipse.cdt.core.browser.typehierarchy.ITypeHierarchy;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMember;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.cdt.internal.ui.StandardCElementLabelProvider;
import org.eclipse.cdt.internal.ui.browser.cbrowsing.AppearanceAwareLabelProvider;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * Label provider for the hierarchy method viewers. 
 */
public class MethodsLabelProvider extends StandardCElementLabelProvider //extends AppearanceAwareLabelProvider
{
	private Color fResolvedBackground;
	
	private boolean fShowDefiningType;
	private TypeHierarchyLifeCycle fHierarchy;
	private MethodsViewer fMethodsViewer;

	public MethodsLabelProvider(TypeHierarchyLifeCycle lifeCycle, MethodsViewer methodsViewer) {
//		super(DEFAULT_TEXTFLAGS, DEFAULT_IMAGEFLAGS);
	    super();
		fHierarchy= lifeCycle;
		fShowDefiningType= false;
		fMethodsViewer= methodsViewer;
		fResolvedBackground= null;
	}
	
	public void setShowDefiningType(boolean showDefiningType) {
		fShowDefiningType= showDefiningType;
	}
	
	public boolean isShowDefiningType() {
		return fShowDefiningType;
	}	
			

	private ICElement getDefiningType(Object element) throws CModelException {
	    if (!(element instanceof ICElement))
	        return null;   

	    ICElement elem = (ICElement) element;
		int kind= elem.getElementType();
		if (kind != ICElement.C_METHOD_DECLARATION && kind != ICElement.C_FIELD) {
			return null;
		}
		ICElement declaringType= TypeUtil.getDeclaringType(elem);
		if (kind != ICElement.C_METHOD_DECLARATION) {
			return declaringType;
		}
		ITypeHierarchy hierarchy= fHierarchy.getHierarchy();
		if (hierarchy == null) {
			return declaringType;
		}
		IMethodDeclaration method= (IMethodDeclaration) element;
		if ((method.getVisibility() == ASTAccessVisibility.PRIVATE) || method.isStatic() || method.isConstructor() || method.isDestructor()) {
			return declaringType;
		}
		IMethodDeclaration res= TypeUtil.findMethodDeclarationInHierarchy(hierarchy, declaringType, method.getElementName(), method.getParameterTypes(), false, false);
		if (res == null || method.equals(res)) {
			return declaringType;
		}
		return TypeUtil.getDeclaringType(res);
	}

	/* (non-Javadoc)
	 * @see ILabelProvider#getText
	 */ 	
	public String getText(Object element) {
		String text= super.getText(element);
		if ((getTextFlags() & CElementLabels.M_POST_QUALIFIED) != 0) {
			if (element instanceof ICElement) {
			    ICElement parent = ((ICElement)element).getParent();
			    if (parent != null) {
			        StringBuffer name = new StringBuffer();
			        name.append(text);
			        name.append(CElementLabels.CONCAT_STRING);
			        name.append(TypeUtil.getFullyQualifiedName(parent).toString());
			        text = name.toString();
			    }
			}
		}
		
		if (fShowDefiningType) {
			try {
			    ICElement type= getDefiningType(element);
				if (type != null) {
					StringBuffer buf= new StringBuffer(super.getText(type));
					buf.append(CElementLabels.CONCAT_STRING);
					buf.append(text);
					return buf.toString();			
				}
			} catch (CModelException e) {
			}
		}
		return text;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		if (fMethodsViewer.isShowInheritedMethods() && element instanceof IMethod) {
			IMethod curr= (IMethod) element;
			ICElement declaringType= TypeUtil.getDeclaringType(curr);
			
			if (declaringType.equals(fMethodsViewer.getInput())) {
				if (fResolvedBackground == null) {
					Display display= Display.getCurrent();
					fResolvedBackground= display.getSystemColor(SWT.COLOR_DARK_BLUE);
				}
				return fResolvedBackground;
			}
		}
		return null;
	}
	
}
