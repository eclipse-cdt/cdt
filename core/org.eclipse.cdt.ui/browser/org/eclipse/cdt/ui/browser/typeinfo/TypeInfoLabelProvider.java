/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - adapted for use in CDT
 *******************************************************************************/
package org.eclipse.cdt.ui.browser.typeinfo;

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class TypeInfoLabelProvider extends LabelProvider {

	public static final int SHOW_FULLYQUALIFIED=	0x01;
	public static final int SHOW_FILENAME_POSTFIX=	0x02;
	public static final int SHOW_FILENAME_ONLY=		0x04;
	public static final int SHOW_ROOT_POSTFIX=		0x08;
	public static final int SHOW_TYPE_ONLY=			0x10;
	public static final int SHOW_TYPE_CONTAINER_ONLY=	0x20;

	private static final Image HEADER_ICON= CPluginImages.get(CPluginImages.IMG_OBJS_TUNIT_HEADER);
	private static final Image SOURCE_ICON= CPluginImages.get(CPluginImages.IMG_OBJS_TUNIT);
	private static final Image NAMESPACE_ICON= CPluginImages.get(CPluginImages.IMG_OBJS_NAMESPACE);
	private static final Image TEMPLATE_ICON= CPluginImages.get(CPluginImages.IMG_OBJS_TEMPLATE);
	private static final Image CLASS_ICON= CPluginImages.get(CPluginImages.IMG_OBJS_CLASS);
	private static final Image STRUCT_ICON= CPluginImages.get(CPluginImages.IMG_OBJS_STRUCT);
	private static final Image TYPEDEF_ICON= CPluginImages.get(CPluginImages.IMG_OBJS_TYPEDEF);
	private static final Image UNION_ICON= CPluginImages.get(CPluginImages.IMG_OBJS_UNION);
	private static final Image ENUM_ICON= CPluginImages.get(CPluginImages.IMG_OBJS_ENUMERATION);

	private int fFlags;
	
	public TypeInfoLabelProvider(int flags) {
		fFlags= flags;
	}	
	
	private boolean isSet(int flag) {
		return (fFlags & flag) != 0;
	}

	/* non java-doc
	 * @see ILabelProvider#getText
	 */
	public String getText(Object element) {
		if (! (element instanceof ITypeInfo)) 
			return super.getText(element);
		
		ITypeInfo typeRef= (ITypeInfo) element;
		
		StringBuffer buf= new StringBuffer();
		if (isSet(SHOW_TYPE_ONLY)) {
			String name= typeRef.getName();
			if (name != null && name.length() > 0)
				buf.append(name);
		} else if (isSet(SHOW_TYPE_CONTAINER_ONLY)) {
			String name= typeRef.getQualifiedParentName();
			if (name != null && name.length() > 0)
				buf.append(name);
		} else if (isSet(SHOW_FILENAME_ONLY)) {
			String name= typeRef.getFileName();
			if (name != null && name.length() > 0)
				buf.append(name);
		} else {
			if (isSet(SHOW_FULLYQUALIFIED)) {
				String name= typeRef.getFullyQualifiedName();
				if (name != null && name.length() > 0)
					buf.append(name);
			}
			else {
				String name= typeRef.getParentName();
				if (name != null && name.length() > 0)
					buf.append(name);
			}
		
			if (isSet(SHOW_FILENAME_POSTFIX)) {
				String name= typeRef.getFileName();
				if (name != null && name.length() > 0) {
					buf.append(TypeInfoMessages.getString("TypeInfoLabelProvider.dash")); //$NON-NLS-1$
					buf.append(name);
				}
			}
		}

		if (isSet(SHOW_ROOT_POSTFIX)) {
			IPath path= typeRef.getPath();
			if (path != null) {
				buf.append(TypeInfoMessages.getString("TypeInfoLabelProvider.dash"));//$NON-NLS-1$
				buf.append(path.toString());
			}
		}
		return buf.toString();				
	}
	
	/* non java-doc
	 * @see ILabelProvider#getImage
	 */	
	public Image getImage(Object element) {
		if (!(element instanceof ITypeInfo)) 
			return super.getImage(element);	

		ITypeInfo typeRef= (ITypeInfo) element;
		if (isSet(SHOW_TYPE_CONTAINER_ONLY)) {
			return getContainerIcon(typeRef);
		} else if (isSet(SHOW_FILENAME_ONLY)) {
			return getFileIcon(typeRef.getPath());
		} else {
			return getTypeIcon(typeRef.getType());
		}
	}

	public static Image getContainerIcon(ITypeInfo typeRef)
	{
		//TODO get enclosing types and parent type icon
		return getFileIcon(typeRef.getPath());
	}

	public static Image getFileIcon(IPath path)
	{
		if (path != null) {
			if (CoreModel.isValidHeaderUnitName(path.lastSegment())) {
				return HEADER_ICON;
			}
		}
		return SOURCE_ICON;
	}
	
	public static Image getTypeIcon(int type)
	{
		switch (type)
		{
		case ICElement.C_NAMESPACE:
			return NAMESPACE_ICON;
			
		case ICElement.C_TEMPLATE_CLASS:
			return TEMPLATE_ICON;

		case ICElement.C_CLASS:
			return CLASS_ICON;

		case ICElement.C_STRUCT:
			return STRUCT_ICON;

		case ICElement.C_UNION:
			return UNION_ICON;

		case ICElement.C_ENUMERATION:
			return ENUM_ICON;

		case ICElement.C_TYPEDEF:
			return TYPEDEF_ICON;

		default:
			return CLASS_ICON;
		}
	}
}
