/*******************************************************************************
 * Copyright (c) 2000,2003,2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - adapted for use in CDT
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.opentype;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;


public class TypeSearchMatchLabelProvider extends LabelProvider {

	public static final int SHOW_FULLYQUALIFIED=	0x01;
	public static final int SHOW_FILENAME_POSTFIX=	0x02;
	public static final int SHOW_FILENAME_ONLY=		0x04;
	public static final int SHOW_ROOT_POSTFIX=		0x08;
	public static final int SHOW_TYPE_ONLY=			0x10;
	public static final int SHOW_TYPE_CONTAINER_ONLY=	0x20;

	private static final Image HEADER_ICON= CPluginImages.get(CPluginImages.IMG_OBJS_TUNIT_HEADER);
	private static final Image SOURCE_ICON= CPluginImages.get(CPluginImages.IMG_OBJS_TUNIT);
	private static final Image NAMESPACE_ICON= CPluginImages.get(CPluginImages.IMG_OBJS_CONTAINER);
	private static final Image TEMPLATE_ICON= CPluginImages.get(CPluginImages.IMG_OBJS_TEMPLATE);
	private static final Image CLASS_ICON= CPluginImages.get(CPluginImages.IMG_OBJS_CLASS);
	private static final Image STRUCT_ICON= CPluginImages.get(CPluginImages.IMG_OBJS_STRUCT);
	private static final Image TYPEDEF_ICON= CPluginImages.get(CPluginImages.IMG_OBJS_TYPEDEF);
	private static final Image UNION_ICON= CPluginImages.get(CPluginImages.IMG_OBJS_UNION);
	private static final Image ENUM_ICON= CPluginImages.get(CPluginImages.IMG_OBJS_ENUMERATION);

	private int fFlags;
	
	public TypeSearchMatchLabelProvider(int flags) {
		fFlags= flags;
	}	
	
	private boolean isSet(int flag) {
		return (fFlags & flag) != 0;
	}

	/* non java-doc
	 * @see ILabelProvider#getText
	 */
	public String getText(Object element) {
		if (! (element instanceof TypeSearchMatch)) 
			return super.getText(element);
		
		TypeSearchMatch typeRef= (TypeSearchMatch) element;
		
		StringBuffer buf= new StringBuffer();
		if (isSet(SHOW_TYPE_ONLY)) {
			buf.append(typeRef.getName());
		} else if (isSet(SHOW_TYPE_CONTAINER_ONLY)) {
			buf.append(typeRef.getQualifiedParentName());
		} else if (isSet(SHOW_FILENAME_ONLY)) {
			buf.append(typeRef.getFileName());
		} else {
			if (isSet(SHOW_FULLYQUALIFIED))
				buf.append(typeRef.getFullyQualifiedName());
			else
				buf.append(typeRef.getParentName());
		
			if (isSet(SHOW_FILENAME_POSTFIX)) {
				buf.append(OpenTypeMessages.getString("TypeInfoLabelProvider.dash")); //$NON-NLS-1$
				buf.append(typeRef.getFileName());
			}
		}

		if (isSet(SHOW_ROOT_POSTFIX)) {
			buf.append(OpenTypeMessages.getString("TypeInfoLabelProvider.dash"));//$NON-NLS-1$
			buf.append(typeRef.getFilePath());
		}
		return buf.toString();				
	}
	
	private Image getFileIcon(TypeSearchMatch typeRef)
	{
		String ext = typeRef.getFileExtension();
		if (ext != null) {	
			String[] exts = CoreModel.getDefault().getHeaderExtensions();
			for (int i = 0; i < exts.length; i++) {
				if (exts[i].equalsIgnoreCase(ext)) {
					return HEADER_ICON;
				}
			}
		}
		return SOURCE_ICON;
	}
	
	private Image getIcon(TypeSearchMatch typeRef)
	{
		switch (typeRef.getType())
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

	/* non java-doc
	 * @see ILabelProvider#getImage
	 */	
	public Image getImage(Object element) {
		if (!(element instanceof TypeSearchMatch)) 
			return super.getImage(element);	

		TypeSearchMatch typeRef= (TypeSearchMatch) element;
		if (isSet(SHOW_TYPE_CONTAINER_ONLY)) {
			return getFileIcon(typeRef);
		} else if (isSet(SHOW_FILENAME_ONLY)) {
			return getFileIcon(typeRef);
		} else {
			return getIcon(typeRef);
		}
	}
}
