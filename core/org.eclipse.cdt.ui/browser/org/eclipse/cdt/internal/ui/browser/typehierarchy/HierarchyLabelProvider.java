/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.typehierarchy;

import org.eclipse.cdt.core.browser.TypeUtil;
import org.eclipse.cdt.core.browser.typehierarchy.ITypeHierarchy;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.internal.ui.viewsupport.StandardCElementLabelProvider;
import org.eclipse.cdt.ui.CElementImageDescriptor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

/**
 * Label provider for the hierarchy viewers. Types in the hierarchy that are not belonging to the
 * input scope are rendered differntly.
  */
public class HierarchyLabelProvider extends StandardCElementLabelProvider // AppearanceAwareLabelProvider {
{
	private static class FocusDescriptor extends CompositeImageDescriptor {
		private ImageDescriptor fBase;
		public FocusDescriptor(ImageDescriptor base) {
			fBase= base;
		}
		protected void drawCompositeImage(int width, int height) {
			drawImage(getImageData(fBase), 0, 0);
			drawImage(getImageData(CPluginImages.DESC_OVR_FOCUS), 0, 0);
		}
		
		private ImageData getImageData(ImageDescriptor descriptor) {
			ImageData data= descriptor.getImageData(); // see bug 51965: getImageData can return null
			if (data == null) {
				data= DEFAULT_IMAGE_DATA;
				CUIPlugin.getDefault().logErrorMessage("Image data not available: " + descriptor.toString()); //$NON-NLS-1$
			}
			return data;
		}
		
		protected Point getSize() {
			return CElementImageProvider.BIG_SIZE;
		}
		public int hashCode() {
			return fBase.hashCode();
		}
		public boolean equals(Object object) {
			return object != null && FocusDescriptor.class.equals(object.getClass()) && ((FocusDescriptor)object).fBase.equals(fBase);
		}		
	}

	private Color fGrayedColor;
	private Color fSpecialColor;

	private ViewerFilter fFilter;
	
	private TypeHierarchyLifeCycle fHierarchy;

	public HierarchyLabelProvider(TypeHierarchyLifeCycle lifeCycle) {
//		super(DEFAULT_TEXTFLAGS, DEFAULT_IMAGEFLAGS);
		super();
		fHierarchy= lifeCycle;
		fFilter= null;
	}
				

	/**
	 * @return Returns the filter.
	 */
	public ViewerFilter getFilter() {
		return fFilter;
	}

	/**
	 * @param filter The filter to set.
	 */
	public void setFilter(ViewerFilter filter) {
		fFilter= filter;
	}

	protected boolean isDifferentScope(ICElement type) {
		if (fFilter != null && !fFilter.select(null, null, type)) {
			return true;
		}
		
		ICElement input= fHierarchy.getInputElement();
		if (input == null || TypeUtil.isClassOrStruct(input)) {
			return false;
		}
			
		ICElement parent= type.getAncestor(input.getElementType());
		if (input.getElementType() == ICElement.C_CCONTAINER) {
			if (parent == null || parent.getElementName().equals(input.getElementName())) {
				return false;
			}
		} else if (input.equals(parent)) {
			return false;
		}
		return true;
	}	
	
	/* (non-Javadoc)
	 * @see ILabelProvider#getImage
	 */ 
	public Image getImage(Object element) {
		Image result= null;
		if (element instanceof ICElement) {
			ImageDescriptor desc= getTypeImageDescriptor((ICElement) element);
			if (desc != null) {
				if (element.equals(fHierarchy.getInputElement())) {
					desc= new FocusDescriptor(desc);
				}
				result= CUIPlugin.getImageDescriptorRegistry().get(desc);
			}
		} else {
			result= fImageLabelProvider.getImageLabel(element, getImageFlags());
		}
		return result;
	}

	private ImageDescriptor getTypeImageDescriptor(ICElement type) {
		ITypeHierarchy hierarchy= fHierarchy.getHierarchy();
		if (hierarchy == null) {
			return new CElementImageDescriptor(CPluginImages.DESC_OBJS_CLASS, 0, CElementImageProvider.BIG_SIZE);
		}
		
		ImageDescriptor desc;
		if (isDifferentScope(type)) {
			desc = CElementImageProvider.getClassImageDescriptor();
		} else {
			desc= fImageLabelProvider.getBaseImageDescriptor(type, 0);
		}

		int adornmentFlags= 0;
		if (type instanceof IMethodDeclaration) {
		    IMethodDeclaration method = (IMethodDeclaration) type;
		    try {
		        if (method.isStatic())
		            adornmentFlags |= CElementImageDescriptor.STATIC;
//		        if (method.isVirtual())
//					adornmentFlags |= CElementImageDescriptor.VIRTUAL;
		    } catch (CModelException e) {
		    }
		}

		if (type instanceof IStructure) {
//		    hierarchy.getSupertypes(type);
//		    TypeCacheManager.getInstance().getCache(type.getCProject().getProject()).getSupertypeAccess();
		}
		
		return new CElementImageDescriptor(desc, adornmentFlags, CElementImageProvider.BIG_SIZE);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		if (element instanceof IMethod) {
			if (fSpecialColor == null) {
				fSpecialColor= Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE);
			}
			return fSpecialColor;
		} else if (element instanceof ICElement && isDifferentScope((ICElement) element)) {
			if (fGrayedColor == null) {
				fGrayedColor= Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
			}
			return fGrayedColor;
		}
		return null;
	}	
	
	

}
