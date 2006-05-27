/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui;

import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNamedNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Common label provider for index based viewers.
 * 
 * @author Doug Schaefer
 */
public class IndexLabelProvider extends LabelProvider {
	public String getText(Object element) {
		if (element == null) {
			return "null :(";
		} else if (element instanceof PDOMNode) {
			try {
				return ((PDOMNamedNode)element).getDBName().getString();
			} catch (CoreException e) {
				return e.getMessage();
			}
		} else
			return super.getText(element);
	}
	
	public Image getImage(Object element) {
		ImageDescriptor desc = null;
	
		if (element instanceof IVariable)
			desc = CElementImageProvider.getVariableImageDescriptor();
		else if (element instanceof IFunction)
			desc = CElementImageProvider.getFunctionImageDescriptor();
		else if (element instanceof ICPPClassType) {
			try {
				switch (((ICPPClassType)element).getKey()) {
				case ICPPClassType.k_class:
					desc = CElementImageProvider.getClassImageDescriptor();
					break;
				case ICPPClassType.k_struct:
					desc = CElementImageProvider.getStructImageDescriptor();
					break;
				case ICPPClassType.k_union:
					desc = CElementImageProvider.getUnionImageDescriptor();
					break;
				}
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
			}
		}
		else if (element instanceof ICompositeType)
			desc = CElementImageProvider.getStructImageDescriptor();
		else if (element instanceof ICPPNamespace)
			desc = CElementImageProvider.getNamespaceImageDescriptor();
		else if (element instanceof ICProject)
			desc = CPluginImages.DESC_OBJS_SEARCHHIERPROJECT;
		else if (element instanceof ICContainer)
			desc = CPluginImages.DESC_OBJS_SEARCHHIERFODLER;
		else if (element instanceof ITranslationUnit) {
			ITranslationUnit tu = (ITranslationUnit)element;
			desc = tu.isHeaderUnit()
				? CPluginImages.DESC_OBJS_TUNIT_HEADER
				: CPluginImages.DESC_OBJS_TUNIT;
		}
		
		if (desc != null)
			return CUIPlugin.getImageDescriptorRegistry().get(desc);
		else if (element instanceof PDOMLinkage)
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		else
			return super.getImage(element);
	}
	
}