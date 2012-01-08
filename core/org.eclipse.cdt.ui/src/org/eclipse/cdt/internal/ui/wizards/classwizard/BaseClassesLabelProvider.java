/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.ui.browser.typeinfo.TypeInfoLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;


public final class BaseClassesLabelProvider implements ITableLabelProvider {

	private static final String YES_VALUE = NewClassWizardMessages.BaseClassesLabelProvider_boolean_yes_label; 
	private static final String NO_VALUE = NewClassWizardMessages.BaseClassesLabelProvider_boolean_no_label; 
	private static final String ACCESS_PUBLIC = NewClassWizardMessages.BaseClassesLabelProvider_access_public_label; 
	private static final String ACCESS_PROTECTED = NewClassWizardMessages.BaseClassesLabelProvider_access_protected_label; 
	private static final String ACCESS_PRIVATE = NewClassWizardMessages.BaseClassesLabelProvider_access_private_label; 

    public static final String getYesNoText(boolean value) {
       return value ? YES_VALUE : NO_VALUE;
    }

    public static final String getAccessText(ASTAccessVisibility access) {
        if (access == ASTAccessVisibility.PRIVATE)
            return ACCESS_PRIVATE;
        if (access == ASTAccessVisibility.PROTECTED)
            return ACCESS_PROTECTED;
           return ACCESS_PUBLIC;
    }

    private static TypeInfoLabelProvider fTypeInfoLabelProvider = new TypeInfoLabelProvider(TypeInfoLabelProvider.SHOW_FULLY_QUALIFIED);
    
	/*
	 * @see ITableLabelProvider#getColumnImage(Object, int)
	 */
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex != 0)
			return null;
		
	    IBaseClassInfo info = (IBaseClassInfo) element;
		return fTypeInfoLabelProvider.getImage(info.getType());
	}

	/*
	 * @see ITableLabelProvider#getColumnText(Object, int)
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {
	    IBaseClassInfo info = (IBaseClassInfo) element;
		
		switch (columnIndex) {
			case 0:
			    return fTypeInfoLabelProvider.getText(info.getType());
			case 1:
			    return getAccessText(info.getAccess());
			case 2:
				return getYesNoText(info.isVirtual());
			default:
				return null;
		}
	}

	/*
	 * @see IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	/*
	 * @see IBaseLabelProvider#dispose()
	 */
	@Override
	public void dispose() {
	}

	/*
	 * @see IBaseLabelProvider#isLabelProperty(Object, String)
	 */
	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/*
	 * @see IBaseLabelProvider#removeListener(ILabelProviderListener)
	 */
	@Override
	public void removeListener(ILabelProviderListener listener) {
	}
}
