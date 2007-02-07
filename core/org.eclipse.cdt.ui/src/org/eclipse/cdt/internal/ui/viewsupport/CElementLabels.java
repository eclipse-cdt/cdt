/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
/*
 * Created on Jun 24, 2003
 */
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.model.IWorkbenchAdapter;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.IInheritance;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITemplate;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.ITypeDef;
import org.eclipse.cdt.core.model.IVariableDeclaration;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.corext.util.CModelUtil;

import org.eclipse.cdt.internal.ui.CUIMessages;

/**
 * @author aniefer
 *
 */
public class CElementLabels {

	/**
	 * Method names contain parameter types.
	 * e.g. <code>foo(int)</code>
	 */
	public final static int M_PARAMETER_TYPES= 1 << 0;

	/**
	 * Method names contain thrown exceptions.
	 * e.g. <code>foo throw( IOException )</code>
	 */
	public final static int M_EXCEPTIONS= 1 << 2;
	
	/**
	 * Method names contain return type (appended)
	 * e.g. <code>foo : int</code>
	 */
	public final static int M_APP_RETURNTYPE= 1 << 3;

	/**
	 * Method names contain return type (appended)
	 * e.g. <code>int foo</code>
	 */
	public final static int M_PRE_RETURNTYPE= 1 << 4;	

	/**
	 * Method names are fully qualified.
	 * e.g. <code>ClassName::size</code>
	 */
	public final static int M_FULLY_QUALIFIED= 1 << 5;

	/**
	 * Method names are post qualified.
	 * e.g. <code>size - ClassName</code>
	 */
	public final static int M_POST_QUALIFIED= 1 << 6;

	/**
	 * Templates are qualified with template parameters.
	 * e.g. <code>ClassName<T></code>
	 */
	public final static int TEMPLATE_PARAMETERS= 1 << 7;

	/**
	 * Field names contain the declared type (appended)
	 * e.g. <code>fHello: int</code>
	 */
	public final static int F_APP_TYPE_SIGNATURE= 1 << 9;

	/**
	 * Field names contain the declared type (prepended)
	 * e.g. <code>int fHello</code>
	 */
	public final static int F_PRE_TYPE_SIGNATURE= 1 << 10;	

	/**
	 * Fields names are fully qualified.
	 * e.g. <code>ClassName::fField</code>
	 */
	public final static int F_FULLY_QUALIFIED= 1 << 11;

	/**
	 * Fields names are post qualified.
	 * e.g. <code>fField - ClassName</code>
	 */
	public final static int F_POST_QUALIFIED= 1 << 12;	

	/**
	 * Type names are fully qualified.
	 * e.g. <code>namespace::ClassName</code>
	 */
	public final static int T_FULLY_QUALIFIED= 1 << 13;

	/**
	 * Append base class specifications to type names.
	 * e.g. <code>MyClass : public BaseClass</code>
	 */
	public final static int T_INHERITANCE= 1 << 16;

	/**
	 * Translation unit names contain the full path.
	 * e.g. <code>/MyProject/src/ClassName.cpp</code>
	 */	
	public final static int TU_QUALIFIED= 1 << 20;

	/**
	 * Translation unit names are post qualified with their path.
	 * e.g. <code>ClassName.cpp - /MyProject/src</code>
	 */	
	public final static int TU_POST_QUALIFIED= 1 << 21;

	/**
	 * Source roots contain the project name (prepended).
	 * e.g. <code>MyProject/src</code>
	 */
	public final static int ROOT_QUALIFIED= 1 << 25;

	/**
	 * Source roots contain the project name (appended).
	 * e.g. <code>src - MyProject</code>
	 */
	public final static int ROOT_POST_QUALIFIED= 1 << 26;	

	/**
	 * Add source root path.
	 * e.g. <code>func() - MyProject/src</code>
	 * Option only applies to getElementLabel
	 */
	public final static int APPEND_ROOT_PATH= 1 << 27;

	/**
	 * Prepend source root path.
	 * e.g. <code>MyProject/src - func()</code>
	 * Option only applies to getElementLabel
	 */
	public final static int PREPEND_ROOT_PATH= 1 << 28;

	/**
	 * Post qualify container project. For example
	 * <code>folder - MyProject</code> if the folder is in project MyProject.
	 */
	public final static int PROJECT_POST_QUALIFIED= 1 << 30; 

	/**
	 * Post qualify symbols with file. 
	 * e.g. func() - /proj/folder/file.cpp 
	 */
	public final static int MF_POST_FILE_QUALIFIED= 1 << 31;

	/**
	 * Qualify all elements
	 */
	public final static int ALL_FULLY_QUALIFIED= F_FULLY_QUALIFIED | M_FULLY_QUALIFIED | T_FULLY_QUALIFIED | TU_QUALIFIED | ROOT_QUALIFIED;

	/**
	 * Post qualify all elements
	 */
	public final static int ALL_POST_QUALIFIED= F_POST_QUALIFIED | M_POST_QUALIFIED  | TU_POST_QUALIFIED | ROOT_POST_QUALIFIED;

	/**
	 *  Default options (M_PARAMETER_TYPES enabled)
	 */
	public final static int ALL_DEFAULT= M_PARAMETER_TYPES;

	/**
	 *  Default qualify options (All except Root)
	 */
	public final static int DEFAULT_QUALIFIED= F_FULLY_QUALIFIED | M_FULLY_QUALIFIED | T_FULLY_QUALIFIED | TU_QUALIFIED;

	/**
	 *  Default post qualify options (All except Root)
	 */
	public final static int DEFAULT_POST_QUALIFIED= F_POST_QUALIFIED | M_POST_QUALIFIED | TU_POST_QUALIFIED;


	public final static String CONCAT_STRING= CUIMessages.getString("CElementLabels.concat_string"); // " - "; //$NON-NLS-1$
	public final static String COMMA_STRING = CUIMessages.getString("CElementLabels.comma_string"); // ", "; //$NON-NLS-1$
	public final static String DECL_STRING  = CUIMessages.getString("CElementLabels.declseparator_string"); // "  "; // use for return type //$NON-NLS-1$

	public static String getTextLabel(Object obj, int flags) {
		if (obj instanceof ICElement) {
			return getElementLabel((ICElement) obj, flags);
		} else if (obj instanceof IAdaptable) {
			IWorkbenchAdapter wbadapter= (IWorkbenchAdapter) ((IAdaptable)obj).getAdapter(IWorkbenchAdapter.class);
			if (wbadapter != null) {
				return wbadapter.getLabel(obj);
			}
		}
		return ""; //$NON-NLS-1$
	}
	
	public static String getElementLabel(ICElement element, int flags) {
		StringBuffer buf= new StringBuffer(60);
		getElementLabel(element, flags, buf);
		return buf.toString();
	}
	
	public static void getElementLabel(ICElement element, int flags, StringBuffer buf) {
		int type= element.getElementType();
		ISourceRoot root= null;
		
		if (type != ICElement.C_MODEL && type != ICElement.C_PROJECT && !(type == ICElement.C_CCONTAINER && element instanceof ISourceRoot))
			root= CModelUtil.getSourceRoot(element);
		if (root != null && getFlag(flags, PREPEND_ROOT_PATH)) {
			getSourceRootLabel(root, ROOT_QUALIFIED, buf);
			buf.append(CONCAT_STRING);
		}		
		switch (type) {
			case ICElement.C_METHOD : 
			case ICElement.C_METHOD_DECLARATION:
			case ICElement.C_TEMPLATE_METHOD:
			case ICElement.C_TEMPLATE_METHOD_DECLARATION:
				getMethodLabel( (IMethodDeclaration) element, flags, buf );
				break;
			case ICElement.C_FUNCTION:
			case ICElement.C_FUNCTION_DECLARATION:
			case ICElement.C_TEMPLATE_FUNCTION:
			case ICElement.C_TEMPLATE_FUNCTION_DECLARATION:
				getFunctionLabel( (IFunctionDeclaration) element, flags, buf);
				break;
			case ICElement.C_FIELD : 
				getFieldLabel( (IField) element, flags, buf );
				break;
			case ICElement.C_VARIABLE:
			case ICElement.C_VARIABLE_DECLARATION:
				getVariableLabel( (IVariableDeclaration) element, flags, buf);
				break;
			case ICElement.C_CLASS:
			case ICElement.C_STRUCT:
			case ICElement.C_UNION:
			case ICElement.C_ENUMERATION:
			case ICElement.C_TEMPLATE_CLASS:
			case ICElement.C_TEMPLATE_STRUCT:
			case ICElement.C_TEMPLATE_UNION:
			case ICElement.C_TEMPLATE_CLASS_DECLARATION:
			case ICElement.C_TEMPLATE_STRUCT_DECLARATION:
			case ICElement.C_TEMPLATE_UNION_DECLARATION:
				getTypeLabel( element, flags, buf );
				break;
			case ICElement.C_TYPEDEF:
				getTypeDefLabel((ITypeDef)element, flags, buf);
				break;
			case ICElement.C_UNIT: 
				getTranslationUnitLabel((ITranslationUnit) element, flags, buf);
				break;	
			case ICElement.C_CCONTAINER:
				ICContainer container = (ICContainer) element;
				if (container instanceof ISourceRoot)
					getSourceRootLabel((ISourceRoot) container, flags, buf);
				else
					getContainerLabel(container, flags, buf);
				break;
			case ICElement.C_PROJECT:
			case ICElement.C_MODEL:
				buf.append(element.getElementName());
				break;
			default:
				buf.append(element.getElementName());
		}
		
		if (root != null && getFlag(flags, APPEND_ROOT_PATH)) {
			buf.append(CONCAT_STRING);
			getSourceRootLabel(root, ROOT_QUALIFIED, buf);
		}
		
		if (element instanceof IBinary) {
			IBinary bin = (IBinary)element;
			buf.append(" - [" + bin.getCPU() + (bin.isLittleEndian() ? "le" : "be") + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

	}
	
	public static void getMethodLabel( IMethodDeclaration method, int flags, StringBuffer buf ) {
		try {
		//return type
		if( getFlag( flags, M_PRE_RETURNTYPE ) && method.exists() && !method.isConstructor() ) {
			buf.append( method.getReturnType() );
			buf.append( ' ' );
		}
		
		//qualification
		if( getFlag( flags, M_FULLY_QUALIFIED ) ){
			ICElement parent = method.getParent();
			if (parent != null && parent.exists() && !(parent instanceof ITranslationUnit)) {
				getTypeLabel( parent, T_FULLY_QUALIFIED, buf );
				buf.append( "::" ); //$NON-NLS-1$
			}
		}
		
		buf.append( method.getElementName() );
		
		//template parameters
		if (method instanceof ITemplate) {
			getTemplateParameters((ITemplate)method, flags, buf);
		}

		//parameters
		if( getFlag( flags, M_PARAMETER_TYPES ) ) {
			buf.append('(');

			String[] types = method.getParameterTypes();
			
			for (int i= 0; i < types.length; i++) {
				if (i > 0) {
					buf.append( COMMA_STRING );
				}
				
				if (types != null) {
					buf.append( types[i] );
				}
			}
			buf.append(')');
		}
		
		//exceptions
		if( getFlag( flags, M_EXCEPTIONS ) && method.exists() ){
			String [] types = method.getExceptions();
			if (types.length > 0) {
				buf.append(" throw( "); //$NON-NLS-1$
				for (int i= 0; i < types.length; i++) {
					if (i > 0) {
						buf.append(COMMA_STRING);
					}
					buf.append( types[i] );
				}
				buf.append( " )" ); //$NON-NLS-1$
			}
		}
		
		if( getFlag( flags, M_APP_RETURNTYPE ) && method.exists() && !method.isConstructor() && !method.isDestructor()) {
			final String typeName= method.getReturnType();
			if (typeName != null && typeName.length() > 0) {
				buf.append( DECL_STRING );
				buf.append(typeName);
			}
		}			
		
		// post qualification
		if( getFlag(flags, M_POST_QUALIFIED)) {
			buf.append( CONCAT_STRING );
			getTypeLabel( method.getParent(), T_FULLY_QUALIFIED, buf );
		}
		if( getFlag(flags, MF_POST_FILE_QUALIFIED)) {
			IPath path= method.getPath();
			if (path != null) {
				buf.append( CONCAT_STRING );
				buf.append(path.toString());
			}
		}
		} catch (CModelException e) {
			CUIPlugin.getDefault().log(e);
		}
	}

	private static void getTemplateParameters(ITemplate template, int flags, StringBuffer buf) {
		if (getFlag(flags, TEMPLATE_PARAMETERS)) {
			String[] types = template.getTemplateParameterTypes();
			buf.append('<');
			if (types != null) {
				for (int i= 0; i < types.length; i++) {
					if (i > 0) {
						buf.append( ',' );
					}
					buf.append( types[i] );
				}
			}
			buf.append('>');
		}
	}

	public static void getFieldLabel(IField field, int flags, StringBuffer buf ) {
		try {
		//return type
		if( getFlag( flags, F_PRE_TYPE_SIGNATURE ) && field.exists()) {
			buf.append( field.getTypeName() );
			buf.append( ' ' );
		}
		
		//qualification
		if( getFlag( flags, F_FULLY_QUALIFIED ) ){
			ICElement parent = field.getParent();
			if (parent != null && parent.exists()) {
				getTypeLabel( parent, T_FULLY_QUALIFIED, buf );
				buf.append( "::" ); //$NON-NLS-1$
			}
		}
		
		buf.append( field.getElementName() );
				
		if( getFlag( flags, F_APP_TYPE_SIGNATURE ) && field.exists()) {
			buf.append( DECL_STRING );
			buf.append( field.getTypeName() );	
		}			
		
		// post qualification
		if( getFlag(flags, F_POST_QUALIFIED)) {
			buf.append( CONCAT_STRING );
			getTypeLabel( field.getParent(), T_FULLY_QUALIFIED, buf );
		}
		if( getFlag(flags, MF_POST_FILE_QUALIFIED)) {
			IPath path= field.getPath();
			if (path != null) {
				buf.append( CONCAT_STRING );
				buf.append(path.toString());
			}
		}
		} catch (CModelException e) {
			CUIPlugin.getDefault().log(e);
		}
	}

	public static void getVariableLabel(IVariableDeclaration var, int flags, StringBuffer buf ) {
		try {
		//return type
		if( getFlag( flags, F_PRE_TYPE_SIGNATURE ) && var.exists()) {
			buf.append( var.getTypeName() );
			buf.append( ' ' );
		}
		
		//qualification
		if( getFlag( flags, F_FULLY_QUALIFIED ) ){
			ICElement parent = var.getParent();
			if (parent != null && parent.exists() && parent.getElementType() == ICElement.C_NAMESPACE) {
				getTypeLabel( parent, T_FULLY_QUALIFIED, buf );
				buf.append( "::" ); //$NON-NLS-1$
			}
		}
		
		buf.append( var.getElementName() );
				
		if( getFlag( flags, F_APP_TYPE_SIGNATURE ) && var.exists()) {
			buf.append( DECL_STRING );
			buf.append( var.getTypeName() );	
		}			
		
		// post qualification
		if( getFlag(flags, F_POST_QUALIFIED)) {
			ICElement parent = var.getParent();
			if (parent != null && parent.exists() && parent.getElementType() == ICElement.C_NAMESPACE) {
				buf.append( CONCAT_STRING );
				getTypeLabel( var.getParent(), T_FULLY_QUALIFIED, buf );
			}
		}
		if( getFlag(flags, MF_POST_FILE_QUALIFIED)) {
			IPath path= var.getPath();
			if (path != null) {
				buf.append( CONCAT_STRING );
				buf.append(path.toString());
			}
		}
		} catch (CModelException e) {
			CUIPlugin.getDefault().log(e);
		}
	}

	public static void getFunctionLabel(IFunctionDeclaration func, int flags, StringBuffer buf) {
		//return type
		if( getFlag( flags, M_PRE_RETURNTYPE ) && func.exists()) {
			buf.append( func.getReturnType() );
			buf.append( ' ' );
		}
		
		//qualification
		if( getFlag( flags, M_FULLY_QUALIFIED ) ){
			ICElement parent = func.getParent();
			if (parent != null && parent.exists() && parent.getElementType() == ICElement.C_NAMESPACE) {
				getTypeLabel( parent, T_FULLY_QUALIFIED, buf );
				buf.append( "::" ); //$NON-NLS-1$
			}
		}
		
		buf.append( func.getElementName() );

		//template parameters
		if (func instanceof ITemplate) {
			getTemplateParameters((ITemplate)func, flags, buf);
		}

		//parameters
		if( getFlag( flags, M_PARAMETER_TYPES ) ) {
			buf.append('(');

			String[] types = func.getParameterTypes();
			
			for (int i= 0; i < types.length; i++) {
				if (i > 0) {
					buf.append( COMMA_STRING );
				}
				
				if (types != null) {
					buf.append( types[i] );
				}
			}
			buf.append(')');
		}
		
		//exceptions
		if( getFlag( flags, M_EXCEPTIONS ) && func.exists() ){
			String [] types = func.getExceptions();
			if (types.length > 0) {
				buf.append(" throw( "); //$NON-NLS-1$
				for (int i= 0; i < types.length; i++) {
					if (i > 0) {
						buf.append(COMMA_STRING);
					}
					buf.append( types[i] );
				}
				buf.append( " )" ); //$NON-NLS-1$
			}
		}
		
		if( getFlag( flags, M_APP_RETURNTYPE ) && func.exists()) {
			String typeName= func.getReturnType();
			if (typeName != null && typeName.length() > 0) {
				buf.append( DECL_STRING );
				buf.append(typeName);
			}
		}			
		
		// post qualification
		if( getFlag(flags, M_POST_QUALIFIED)) {
			ICElement parent = func.getParent();
			if (parent != null && parent.exists() && parent.getElementType() == ICElement.C_NAMESPACE) {
				buf.append( CONCAT_STRING );
				getTypeLabel( func.getParent(), T_FULLY_QUALIFIED, buf );
			}
		}
		if( getFlag(flags, MF_POST_FILE_QUALIFIED)) {
			IPath path= func.getPath();
			if (path != null) {
				buf.append( CONCAT_STRING );
				buf.append(path.toString());
			}
		}
	}

	public static void getTypeDefLabel(ITypeDef typedef, int flags, StringBuffer buf ) {
		// type
		if( getFlag( flags, F_PRE_TYPE_SIGNATURE ) && typedef.exists()) {
			buf.append( typedef.getTypeName() );
			buf.append( ' ' );
		}
		
		//qualification
		if( getFlag( flags, F_FULLY_QUALIFIED ) ){
			ICElement parent = typedef.getParent();
			if (parent != null && parent.exists() && parent.getElementType() == ICElement.C_NAMESPACE) {
				getTypeLabel( parent, T_FULLY_QUALIFIED, buf );
				buf.append( "::" ); //$NON-NLS-1$
			}
		}
		
		buf.append( typedef.getElementName() );
				
		if( getFlag( flags, F_APP_TYPE_SIGNATURE ) && typedef.exists()) {
			String typeName= typedef.getTypeName();
			if (typeName != null && typeName.length() > 0) {
				buf.append( DECL_STRING );
				buf.append(typeName);
			}
		}			
		
		// post qualification
		if( getFlag(flags, F_POST_QUALIFIED)) {
			ICElement parent = typedef.getParent();
			if (parent != null && parent.exists() && parent.getElementType() == ICElement.C_NAMESPACE) {
				buf.append( CONCAT_STRING );
				getTypeLabel( typedef.getParent(), T_FULLY_QUALIFIED, buf );
			}
		}
		if( getFlag(flags, MF_POST_FILE_QUALIFIED)) {
			IPath path= typedef.getPath();
			if (path != null) {
				buf.append( CONCAT_STRING );
				buf.append(path.toString());
			}
		}
	}

	/**
	 * Appends the label for a source root to a StringBuffer. Considers the ROOT_* flags.
	 */	
	public static void getSourceRootLabel(ISourceRoot root, int flags, StringBuffer buf) {
		getFolderLabel(root, flags, buf);
	}
	
	/**
	 * Appends the label for a container to a StringBuffer. Considers the ROOT_* flags.
	 */	
	public static void getContainerLabel(ICContainer container, int flags, StringBuffer buf) {
		getFolderLabel(container, flags, buf);
	}

	private static void getFolderLabel(ICContainer container, int flags, StringBuffer buf) {
		IResource resource= container.getResource();
		boolean rootQualified= getFlag(flags, ROOT_QUALIFIED);
		boolean referencedQualified= getFlag(flags, PROJECT_POST_QUALIFIED)
			&& (container instanceof ISourceRoot && CModelUtil.isReferenced((ISourceRoot)container))
			&& resource != null;
		if (rootQualified) {
			buf.append(container.getPath().makeRelative().toString());
		} else {
			buf.append(container.getElementName());
			if (referencedQualified) {
				buf.append(CONCAT_STRING);
				buf.append(resource.getProject().getName());
			} else if (getFlag(flags, ROOT_POST_QUALIFIED)) {
				buf.append(CONCAT_STRING);
				buf.append(container.getParent().getElementName());
			}
		}
	}

	/**
	 * Appends the label for a translation unit to a StringBuffer. Considers the CU_* flags.
	 */
	public static void getTranslationUnitLabel(ITranslationUnit tu, int flags, StringBuffer buf) {
		IResource r= tu.getResource();
		IPath path;
		if (r != null) {
			path= r.getFullPath().makeRelative();
		}
		else {
			path= tu.getPath();
		}
		
		if (path == null) {
			buf.append(tu.getElementName());
		}
		else {
			if (getFlag(flags, TU_QUALIFIED)) {
				buf.append(path.toString());
			}
			else if (getFlag(flags, TU_POST_QUALIFIED)) {
				buf.append(path.lastSegment());
				buf.append(CONCAT_STRING);
				buf.append(path.removeLastSegments(1));
			}
			else {
				buf.append(path.lastSegment());
			}
		}
	}

	/**
	 * Appends the label for a type to a StringBuffer. Considers the T_* flags.
	 */		
	public static void getTypeLabel(ICElement elem, int flags, StringBuffer buf) {
		if (getFlag(flags, T_FULLY_QUALIFIED)) {
			ICElement parent= elem.getParent();
			boolean isQualifier= true;
			if (parent != null && parent.exists()) {
				switch (parent.getElementType()) {
				case ICElement.C_ARCHIVE:
				case ICElement.C_BINARY:
				case ICElement.C_CCONTAINER:
				case ICElement.C_MODEL:
				case ICElement.C_PROJECT:
				case ICElement.C_UNIT:
				case ICElement.C_VCONTAINER:
					isQualifier= false;
					break;
				}
			}
			// types cannot be qualified in plain c
			if (isQualifier && !isCLanguage(parent)) {
				int qflags= flags & ~MF_POST_FILE_QUALIFIED;
				getTypeLabel(parent, qflags, buf);
				buf.append("::"); //$NON-NLS-1$
			}
		}
		
		String typeName= elem.getElementName();
		if (typeName.length() == 0) { // anonymous
		    typeName = CUIMessages.getString("CElementLabels.anonymous");	//$NON-NLS-1$
		}
		buf.append(typeName);

		if (getFlag(flags, T_INHERITANCE) && elem instanceof IInheritance) {
			IInheritance inheritance= (IInheritance)elem;
			String[] superclassNames= inheritance.getSuperClassesNames();
			if (superclassNames != null && superclassNames.length > 0) {
				buf.append(DECL_STRING);
				for (int i = 0; i < superclassNames.length; i++) {
					if (i> 0) {
						buf.append(COMMA_STRING);
					}
					String superclass = superclassNames[i];
					String visibility = getVisibility(inheritance.getSuperClassAccess(superclass));
					buf.append(visibility).append(' ').append(superclass);
				}
			}
		}

		//template parameters
		if (elem instanceof ITemplate) {
			getTemplateParameters((ITemplate)elem, flags, buf);
		}
		
		if( getFlag(flags, MF_POST_FILE_QUALIFIED)) {
			IPath path= elem.getPath();
			if (path != null) {
				buf.append( CONCAT_STRING );
				buf.append(path.toString());
			}
		}
	}
	
	private static boolean isCLanguage(ICElement elem) {
		while (elem != null) {
			elem= elem.getParent();
			if (elem instanceof ITranslationUnit) {
				 return ((ITranslationUnit) elem).isCLanguage();
			}
		}
		return false;
	}

	/**
	 * Convert an <code>ASTAccessVisibility</code> into its string representation.
	 * 
	 * @param access
	 * @return "public", "protected" or "private"
	 */
	private static String getVisibility(ASTAccessVisibility access) {
		if (access == ASTAccessVisibility.PUBLIC) {
			return "public"; //$NON-NLS-1$
		}
		if (access == ASTAccessVisibility.PROTECTED) {
			return "protected"; //$NON-NLS-1$
		}
		return "private"; //$NON-NLS-1$
	}

	private static boolean getFlag(int flags, int flag) {
		return (flags & flag) != 0;
	}
}
