/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on Jun 24, 2003
 */
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CElementLabels {

	/**
	 * Method names contain parameter types.
	 * e.g. <code>foo(int)</code>
	 */
	public final static int M_PARAMETER_TYPES= 1 << 0;

	/**
	 * Method names contain parameter names.
	 * e.g. <code>foo(index)</code>
	 */
	public final static int M_PARAMETER_NAMES= 1 << 1;	

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
	 * e.g. <code>java.util.Vector.size</code>
	 */
	public final static int M_FULLY_QUALIFIED= 1 << 5;

	/**
	 * Method names are post qualified.
	 * e.g. <code>size - java.util.Vector</code>
	 */
	public final static int M_POST_QUALIFIED= 1 << 6;

	/**
	 * Initializer names are fully qualified.
	 * e.g. <code>java.util.Vector.{ ... }</code>
	 */
	public final static int I_FULLY_QUALIFIED= 1 << 7;

	/**
	 * Type names are post qualified.
	 * e.g. <code>{ ... } - java.util.Map</code>
	 */
	public final static int I_POST_QUALIFIED= 1 << 8;		

	/**
	 * Field names contain the declared type (appended)
	 * e.g. <code>int fHello</code>
	 */
	public final static int F_APP_TYPE_SIGNATURE= 1 << 9;

	/**
	 * Field names contain the declared type (prepended)
	 * e.g. <code>fHello : int</code>
	 */
	public final static int F_PRE_TYPE_SIGNATURE= 1 << 10;	

	/**
	 * Fields names are fully qualified.
	 * e.g. <code>java.lang.System.out</code>
	 */
	public final static int F_FULLY_QUALIFIED= 1 << 11;

	/**
	 * Fields names are post qualified.
	 * e.g. <code>out - java.lang.System</code>
	 */
	public final static int F_POST_QUALIFIED= 1 << 12;	

	/**
	 * Type names are fully qualified.
	 * e.g. <code>java.util.Map.MapEntry</code>
	 */
	public final static int T_FULLY_QUALIFIED= 1 << 13;

	/**
	 * Type names are type container qualified.
	 * e.g. <code>Map.MapEntry</code>
	 */
	public final static int T_CONTAINER_QUALIFIED= 1 << 14;

	/**
	 * Type names are post qualified.
	 * e.g. <code>MapEntry - java.util.Map</code>
	 */
	public final static int T_POST_QUALIFIED= 1 << 15;

	/**
	 * Declarations (import container / declarartion, package declarartion) are qualified.
	 * e.g. <code>java.util.Vector.class/import container</code>
	 */	
	public final static int D_QUALIFIED= 1 << 16;

	/**
	 * Declarations (import container / declarartion, package declarartion) are post qualified.
	 * e.g. <code>import container - java.util.Vector.class</code>
	 */	
	public final static int D_POST_QUALIFIED= 1 << 17;	

	/**
	 * Class file names are fully qualified.
	 * e.g. <code>java.util.Vector.class</code>
	 */	
	public final static int CF_QUALIFIED= 1 << 18;

	/**
	 * Class file names are post qualified.
	 * e.g. <code>Vector.class - java.util</code>
	 */	
	public final static int CF_POST_QUALIFIED= 1 << 19;

	/**
	 * Compilation unit names are fully qualified.
	 * e.g. <code>java.util.Vector.java</code>
	 */	
	public final static int CU_QUALIFIED= 1 << 20;

	/**
	 * Compilation unit names are post  qualified.
	 * e.g. <code>Vector.java - java.util</code>
	 */	
	public final static int CU_POST_QUALIFIED= 1 << 21;

	/**
	 * Package names are qualified.
	 * e.g. <code>MyProject/src/java.util</code>
	 */	
	public final static int P_QUALIFIED= 1 << 22;

	/**
	 * Package names are post qualified.
	 * e.g. <code>java.util - MyProject/src</code>
	 */	
	public final static int P_POST_QUALIFIED= 1 << 23;

	/**
	 * Package Fragment Roots contain variable name if from a variable.
	 * e.g. <code>JRE_LIB - c:\java\lib\rt.jar</code>
	 */
	public final static int ROOT_VARIABLE= 1 << 24;

	/**
	 * Package Fragment Roots contain the project name if not an archive (prepended).
	 * e.g. <code>MyProject/src</code>
	 */
	public final static int ROOT_QUALIFIED= 1 << 25;

	/**
	 * Package Fragment Roots contain the project name if not an archive (appended).
	 * e.g. <code>src - MyProject</code>
	 */
	public final static int ROOT_POST_QUALIFIED= 1 << 26;	

	/**
	 * Add root path to all elements except Package Fragment Roots and Java projects.
	 * e.g. <code>java.lang.Vector - c:\java\lib\rt.jar</code>
	 * Option only applies to getElementLabel
	 */
	public final static int APPEND_ROOT_PATH= 1 << 27;

	/**
	 * Add root path to all elements except Package Fragment Roots and Java projects.
	 * e.g. <code>java.lang.Vector - c:\java\lib\rt.jar</code>
	 * Option only applies to getElementLabel
	 */
	public final static int PREPEND_ROOT_PATH= 1 << 28;

	/**
	 * Package names are compressed.
	 * e.g. <code>o*.e*.search</code>
	 */	
	public final static int P_COMPRESSED= 1 << 29;

	/**
	 * Post qualify referenced package fragement roots. For example
	 * <code>jdt.jar - org.eclipse.jdt.ui</code> if the jar is referenced
	 * from another project.
	 */
	public final static int REFERENCED_ROOT_POST_QUALIFIED= 1 << 30; 

	/**
	 * Qualify all elements
	 */
	public final static int ALL_FULLY_QUALIFIED= F_FULLY_QUALIFIED | M_FULLY_QUALIFIED | I_FULLY_QUALIFIED | T_FULLY_QUALIFIED | D_QUALIFIED | CF_QUALIFIED | CU_QUALIFIED | P_QUALIFIED | ROOT_QUALIFIED;

	/**
	 * Post qualify all elements
	 */
	public final static int ALL_POST_QUALIFIED= F_POST_QUALIFIED | M_POST_QUALIFIED | I_POST_QUALIFIED | T_POST_QUALIFIED | D_POST_QUALIFIED | CF_POST_QUALIFIED | CU_POST_QUALIFIED | P_POST_QUALIFIED | ROOT_POST_QUALIFIED;

	/**
	 *  Default options (M_PARAMETER_TYPES enabled)
	 */
	public final static int ALL_DEFAULT= M_PARAMETER_TYPES;

	/**
	 *  Default qualify options (All except Root and Package)
	 */
	public final static int DEFAULT_QUALIFIED= F_FULLY_QUALIFIED | M_FULLY_QUALIFIED | I_FULLY_QUALIFIED | T_FULLY_QUALIFIED | D_QUALIFIED | CF_QUALIFIED | CU_QUALIFIED;

	/**
	 *  Default post qualify options (All except Root and Package)
	 */
	public final static int DEFAULT_POST_QUALIFIED= F_POST_QUALIFIED | M_POST_QUALIFIED | I_POST_QUALIFIED | T_POST_QUALIFIED | D_POST_QUALIFIED | CF_POST_QUALIFIED | CU_POST_QUALIFIED;


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
				getMethodLabel( (IMethod) element, flags, buf );
				break;
			case ICElement.C_CLASS:
			case ICElement.C_STRUCT:
			case ICElement.C_UNION:
			case ICElement.C_ENUMERATION:
				getTypeLabel( element, flags, buf );
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
	}
	
	public static void getMethodLabel( IMethod method, int flags, StringBuffer buf ) {
		try {
		//return type
		if( getFlag( flags, M_PRE_RETURNTYPE ) && method.exists() && !method.isConstructor() ) {
			buf.append( method.getReturnType() );
			buf.append( ' ' );
		}
		
		//qualification
		if( getFlag( flags, M_FULLY_QUALIFIED ) ){
			ICElement parent = method.getParent();
			if (parent != null && parent.exists()) {
				getTypeLabel( parent, T_FULLY_QUALIFIED | (flags & P_COMPRESSED), buf );
				buf.append( "::" ); //$NON-NLS-1$
			}
		}
		
		buf.append( method.getElementName() );
		
		//parameters
		if( getFlag( flags, M_PARAMETER_TYPES | M_PARAMETER_NAMES ) ) {
			buf.append('(');

			String[] types = getFlag(flags, M_PARAMETER_TYPES) ? method.getParameterTypes() : null;
			String[] names = null;//(getFlag(flags, M_PARAMETER_NAMES) && method.exists()) ? method.getParameterNames() : null;
			
			int nParams = ( types != null ) ? types.length : names.length;

			for (int i= 0; i < nParams; i++) {
				if (i > 0) {
					buf.append( COMMA_STRING ); //$NON-NLS-1$
				}
				
				if (types != null) {
					buf.append( types[i] );
				}
				
				if (names != null) {
					if (types != null) {
						buf.append(' ');
					}
					buf.append( names[i] );
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
		
		if( getFlag( flags, M_APP_RETURNTYPE ) && method.exists() && !method.isConstructor()) {
			buf.append( DECL_STRING );
			buf.append( method.getReturnType() );	
		}			
		
		// post qualification
		if( getFlag(flags, M_POST_QUALIFIED)) {
			buf.append( CONCAT_STRING );
			getTypeLabel( method.getParent(), T_FULLY_QUALIFIED | (flags & P_COMPRESSED), buf );
		}
		} catch (CModelException e) {
			CUIPlugin.getDefault().log(e);
		}
	}
	
	/**
	 * Appends the label for a source root to a StringBuffer. Considers the ROOT_* flags.
	 */	
	public static void getSourceRootLabel(ISourceRoot root, int flags, StringBuffer buf) {
//		if (root.isArchive())
//			getArchiveLabel(root, flags, buf);
//		else
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
		boolean referencedQualified= getFlag(flags, REFERENCED_ROOT_POST_QUALIFIED)
			&& (container instanceof ISourceRoot && CModelUtil.isReferenced((ISourceRoot)container))
			&& resource != null;
		if (rootQualified) {
			buf.append(container.getPath().makeRelative().toString());
		} else {
			if (resource != null)
				buf.append(resource.getProjectRelativePath().toString());
			else
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
		if (getFlag(flags, CU_QUALIFIED)) {
			ISourceRoot root= CModelUtil.getSourceRoot(tu);
//			if (!pack.isDefaultPackage()) {
				buf.append(root.getElementName());
				buf.append('.');
//			}
		}
		buf.append(tu.getElementName());
		
		if (getFlag(flags, CU_POST_QUALIFIED)) {
			buf.append(CONCAT_STRING);
			getSourceRootLabel((ISourceRoot) tu.getParent(), 0, buf);
		}		
	}

	/**
	 * Appends the label for a type to a StringBuffer. Considers the T_* flags.
	 */		
	public static void getTypeLabel(ICElement elem, int flags, StringBuffer buf) {
		if (getFlag(flags, T_FULLY_QUALIFIED)) {
			ISourceRoot root= CModelUtil.getSourceRoot(elem);
			if (root != null) {
				getSourceRootLabel(root, (flags & P_COMPRESSED), buf);
				buf.append(root.getElementName());
				buf.append('.');
			}
		}
		
		String typeName= elem.getElementName();
		if (typeName.length() == 0) { // anonymous
		    typeName = CUIMessages.getString("CElementLabels.anonymous");	//$NON-NLS-1$
		}
		buf.append(typeName);
		
//		// post qualification
//		if (getFlag(flags, T_POST_QUALIFIED)) {
//			buf.append(CONCAT_STRING);
//			IType declaringType= type.getDeclaringType();
//			if (declaringType != null) {
//				getTypeLabel(declaringType, T_FULLY_QUALIFIED | (flags & P_COMPRESSED), buf);
//				int parentType= type.getParent().getElementType();
//				if (parentType == ICElement.METHOD || parentType == ICElement.FIELD || parentType == ICElement.INITIALIZER) { // anonymous or local
//					buf.append('.');
//					getElementLabel(type.getParent(), 0, buf);
//				}
//			} else {
//				getPackageFragmentLabel(type.getPackageFragment(), (flags & P_COMPRESSED), buf);
//			}
//		}
	}
	
	private static boolean getFlag(int flags, int flag) {
		return (flags & flag) != 0;
	}
}
