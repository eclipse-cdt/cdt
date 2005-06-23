/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.cbrowsing;

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.ui.browser.typeinfo.TypeInfoLabelProvider;
import org.eclipse.swt.graphics.Image;

public class NamespacesViewLabelProvider extends CBrowsingLabelProvider {

	protected static final TypeInfoLabelProvider fTypeInfoLabelProvider = new TypeInfoLabelProvider(TypeInfoLabelProvider.SHOW_FULLY_QUALIFIED);

    public NamespacesViewLabelProvider() {
        super();
    }
    
    public Image getImage(Object element) {
    	if (element instanceof ITypeInfo)
    		return fTypeInfoLabelProvider.getImage(element);
    	return super.getImage(element);
    }

    public String getText(Object element) {
    	if (element instanceof ITypeInfo)
    		return fTypeInfoLabelProvider.getText(element);
    	return super.getText(element);
    }
}
