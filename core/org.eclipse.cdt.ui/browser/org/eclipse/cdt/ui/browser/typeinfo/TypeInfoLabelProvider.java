/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - adapted for use in CDT
 *     Andrew Ferguson (Symbian)
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.browser.typeinfo;

import org.eclipse.cdt.core.browser.IFunctionInfo;
import org.eclipse.cdt.core.browser.IQualifiedTypeName;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.browser.IndexTypeInfo;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IVariableDeclaration;
import org.eclipse.cdt.internal.core.model.FunctionDeclaration;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class TypeInfoLabelProvider extends LabelProvider {

	public static final int SHOW_NAME_ONLY = 0x01;
	public static final int SHOW_ENCLOSING_TYPE_ONLY = 0x02;
	public static final int SHOW_FULLY_QUALIFIED = 0x04;
	public static final int SHOW_PATH = 0x08;
	public static final int SHOW_PARAMETERS = 0x10;
	public static final int SHOW_RETURN_TYPE = 0x20;

	private static final Image HEADER_ICON = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_TUNIT_HEADER);
	private static final Image SOURCE_ICON = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_TUNIT);
	private static final Image NAMESPACE_ICON = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_NAMESPACE);
	private static final Image TEMPLATE_ICON = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_TEMPLATE);
	private static final Image CLASS_ICON = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_CLASS);
	private static final Image STRUCT_ICON = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_STRUCT);
	private static final Image TYPEDEF_ICON = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_TYPEDEF);
	private static final Image UNION_ICON = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_UNION);
	private static final Image ENUM_ICON = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_ENUMERATION);
	private static final Image FUNCTION_ICON = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_FUNCTION);
	private static final Image VARIABLE_ICON = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_VARIABLE);
	private static final Image VARIABLE_LOCAL_ICON = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_LOCAL_VARIABLE);
	private static final Image ENUMERATOR_ICON = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_ENUMERATOR);
	private static final Image MACRO_ICON = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_MACRO);
	private static final Image UNKNOWN_TYPE_ICON = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_UNKNOWN_TYPE);

	private int fFlags;

	public TypeInfoLabelProvider(int flags) {
		fFlags = flags;
	}

	private boolean isSet(int flag) {
		return (fFlags & flag) != 0;
	}

	/* non java-doc
	 * @see ILabelProvider#getText
	 */
	@Override
	public String getText(Object element) {
		if (!(element instanceof ITypeInfo))
			return super.getText(element);

		ITypeInfo typeInfo = (ITypeInfo) element;
		IQualifiedTypeName qualifiedName = typeInfo.getQualifiedTypeName();

		StringBuilder buf = new StringBuilder();
		if (isSet(SHOW_NAME_ONLY)) {
			String name = typeInfo.getName();
			if (name != null && name.length() > 0)
				buf.append(name);
		} else if (isSet(SHOW_ENCLOSING_TYPE_ONLY)) {
			IQualifiedTypeName parentName = qualifiedName.getEnclosingTypeName();
			if (parentName != null) {
				buf.append(parentName.getFullyQualifiedName());
			} else {
				buf.append(TypeInfoMessages.TypeInfoLabelProvider_globalScope);
			}
		} else if (isSet(SHOW_FULLY_QUALIFIED)) {
			final int elemType = typeInfo.getCElementType();
			if (elemType != ICElement.C_VARIABLE_LOCAL && qualifiedName.isGlobal()) {
				if ((elemType != ICElement.C_FUNCTION && elemType != ICElement.C_VARIABLE)
						|| !(typeInfo instanceof IndexTypeInfo && ((IndexTypeInfo) typeInfo).isFileLocal())) {
					buf.append(TypeInfoMessages.TypeInfoLabelProvider_globalScope);
					buf.append(' ');
				}
			}
			buf.append(qualifiedName.getFullyQualifiedName());
		}
		if (isSet(SHOW_PARAMETERS)) {
			final int elementType = typeInfo.getCElementType();
			if (elementType == ICElement.C_FUNCTION || elementType == ICElement.C_MACRO) {
				if (typeInfo instanceof IFunctionInfo) {
					IFunctionInfo functionInfo = (IFunctionInfo) typeInfo;
					String[] params = functionInfo.getParameters();
					if (params != null) {
						buf.append(FunctionDeclaration.getParameterClause(params));
					}
				}
			}
		}
		if (isSet(SHOW_RETURN_TYPE)) {
			switch (typeInfo.getCElementType()) {
			case ICElement.C_FUNCTION:
				if (typeInfo instanceof IFunctionInfo) {
					IFunctionInfo functionInfo = (IFunctionInfo) typeInfo;
					String returnType = functionInfo.getReturnType();
					if (returnType != null && returnType.length() > 0) {
						buf.append(TypeInfoMessages.TypeInfoLabelProvider_colon);
						buf.append(returnType);
					}
				}
				break;
			case ICElement.C_VARIABLE:
				ITypeReference ref = typeInfo.getResolvedReference();
				if (ref != null) {
					ICElement[] cElements = ref.getCElements();
					if (cElements != null && cElements.length > 0) {
						String returnType = null;
						if (cElements[0] instanceof IVariableDeclaration) {
							try {
								returnType = ((IVariableDeclaration) cElements[0]).getTypeName();
							} catch (CModelException exc) {
							}
						}
						if (returnType != null && returnType.length() > 0) {
							buf.append(TypeInfoMessages.TypeInfoLabelProvider_colon);
							buf.append(returnType);
						}
					}
				}
				break;
			}
		}
		if (isSet(SHOW_PATH)) {
			IPath path = null;
			ITypeReference ref = typeInfo.getResolvedReference();
			if (ref != null) {
				path = ref.getPath();
			} else {
				ICProject project = typeInfo.getEnclosingProject();
				if (project != null) {
					path = project.getProject().getFullPath();
				}
			}
			if (path != null) {
				buf.append(TypeInfoMessages.TypeInfoLabelProvider_dash);
				buf.append(path.toString());
			}
		}
		return buf.toString();
	}

	/* non java-doc
	 * @see ILabelProvider#getImage
	 */
	@Override
	public Image getImage(Object element) {
		if (!(element instanceof ITypeInfo))
			return super.getImage(element);

		ITypeInfo typeRef = (ITypeInfo) element;
		if (isSet(SHOW_ENCLOSING_TYPE_ONLY)) {
			IPath path = null;
			ITypeReference ref = typeRef.getResolvedReference();
			if (ref != null) {
				path = ref.getPath();

				// IndexTypeInfo may not have an enclosing project
				ICProject cproject = typeRef.getEnclosingProject();
				IProject project = cproject == null ? null : cproject.getProject();

				if (CoreModel.isValidHeaderUnitName(project, path.lastSegment())) {
					return HEADER_ICON;
				}
			}
			return SOURCE_ICON;
		}

		return getTypeIcon(typeRef.getCElementType());
	}

	public static Image getTypeIcon(int type) {
		switch (type) {
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

		case ICElement.C_FUNCTION:
			return FUNCTION_ICON;

		case ICElement.C_VARIABLE:
			return VARIABLE_ICON;

		case ICElement.C_ENUMERATOR:
			return ENUMERATOR_ICON;

		case ICElement.C_MACRO:
			return MACRO_ICON;

		case ICElement.C_VARIABLE_LOCAL:
			return VARIABLE_LOCAL_ICON;

		default:
			return UNKNOWN_TYPE_ICON;
		}
	}
}
