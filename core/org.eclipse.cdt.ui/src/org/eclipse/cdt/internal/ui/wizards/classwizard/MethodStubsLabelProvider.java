/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;


public final class MethodStubsLabelProvider implements ITableLabelProvider {

	/*
	 * @see ITableLabelProvider#getColumnImage(Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex != 0)
			return null;
		
	    IMethodStub stub = (IMethodStub) element;
		ImageDescriptor descriptor = CElementImageProvider.getMethodImageDescriptor(stub.getAccess());
		if (descriptor != null) {
			return CUIPlugin.getImageDescriptorRegistry().get(descriptor);
		}
		return CPluginImages.get(CPluginImages.IMG_OBJS_PUBLIC_METHOD);
	}

	/*
	 * @see ITableLabelProvider#getColumnText(Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
	    IMethodStub stub = (IMethodStub) element;
		
		switch (columnIndex) {
			case 0:
				return stub.getName();
			case 1:
				return BaseClassesLabelProvider.getAccessText(stub.getAccess());
			case 2:
				return BaseClassesLabelProvider.getYesNoText(stub.isVirtual());
			case 3:
				return BaseClassesLabelProvider.getYesNoText(stub.isInline());
			default:
				return null;
		}
	}

	/*
	 * @see IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/*
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/*
	 * @see IBaseLabelProvider#isLabelProperty(Object, String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/*
	 * @see IBaseLabelProvider#removeListener(ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}

}