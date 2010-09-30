/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *     Markus Schorn (Wind River Systems)
 *     Patrick Hofer [bug 325799]
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.ui.model.IWorkbenchAdapter;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IEnumerator;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.IMacro;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.ITypeDef;
import org.eclipse.cdt.core.model.IVariableDeclaration;

import org.eclipse.cdt.internal.core.model.CElement;
import org.eclipse.cdt.internal.core.model.CoreModelMessages;
import org.eclipse.cdt.internal.corext.util.Strings;

/*
 * This class is basically a clone of org.eclipse.cdt.core.model.util.CElementLabels 
 * (refer to bug 325799 for the reasoning)
 */

/**
 * Computes labels for objects of type {@link CElement}.
 */
public class CElementLabels {
	/**
	 * Method names contain parameter types.
	 * e.g. <code>foo(int)</code>
	 */
	public final static long M_PARAMETER_TYPES= 1L << 0;

	/**
	 * Method definition names without qualifier.
	 * e.g. <code>foo(int)</code>
	 */
	public final static long M_SIMPLE_NAME= 1L << 1;

	/**
	 * Method names contain thrown exceptions.
	 * e.g. <code>foo throw( IOException )</code>
	 */
	public final static long M_EXCEPTIONS= 1L << 2;
	
	/**
	 * Method names contain return type (appended)
	 * e.g. <code>foo : int</code>
	 */
	public final static long M_APP_RETURNTYPE= 1L << 3;

	/**
	 * Method names contain return type (appended)
	 * e.g. <code>int foo</code>
	 */
	public final static long M_PRE_RETURNTYPE= 1L << 4;	

	/**
	 * Method names are fully qualified.
	 * e.g. <code>ClassName::size</code>
	 */
	public final static long M_FULLY_QUALIFIED= 1L << 5;

	/**
	 * Method names are post qualified.
	 * e.g. <code>size - ClassName</code>
	 */
	public final static long M_POST_QUALIFIED= 1L << 6;

	/**
	 * Templates are qualified with template parameters.
	 * e.g. <code>ClassName<T></code>
	 */
	public final static long TEMPLATE_PARAMETERS= 1L << 7;

	/**
	 * Static field names without qualifier.
	 * e.g. <code>fHello</code>
	 */
	public final static long F_SIMPLE_NAME= 1L << 8;

	/**
	 * Field names contain the declared type (appended)
	 * e.g. <code>fHello: int</code>
	 */
	public final static long F_APP_TYPE_SIGNATURE= 1L << 9;

	/**
	 * Field names contain the declared type (prepended)
	 * e.g. <code>int fHello</code>
	 */
	public final static long F_PRE_TYPE_SIGNATURE= 1L << 10;	

	/**
	 * Fields names are fully qualified.
	 * e.g. <code>ClassName::fField</code>
	 */
	public final static long F_FULLY_QUALIFIED= 1L << 11;

	/**
	 * Fields names are post qualified.
	 * e.g. <code>fField - ClassName</code>
	 */
	public final static long F_POST_QUALIFIED= 1L << 12;	

	/**
	 * Type names are fully qualified.
	 * e.g. <code>namespace::ClassName</code>
	 */
	public final static long T_FULLY_QUALIFIED= 1L << 13;

	/**
	 * Instances and specializations are qualified with arguments, templates with template parameter names.
	 * The flag overrides {@link #TEMPLATE_PARAMETERS}.
	 */
	public final static long TEMPLATE_ARGUMENTS= 1L << 14;

	/**
	 * Append base class specifications to type names.
	 * e.g. <code>MyClass : public BaseClass</code>
	 */
	public final static long T_INHERITANCE= 1L << 16;

	/**
	 * Translation unit names contain the full path.
	 * e.g. <code>/MyProject/src/ClassName.cpp</code>
	 */	
	public final static long TU_QUALIFIED= 1L << 20;

	/**
	 * Translation unit names are post qualified with their path.
	 * e.g. <code>ClassName.cpp - /MyProject/src</code>
	 */	
	public final static long TU_POST_QUALIFIED= 1L << 21;

	/**
	 * Source roots contain the project name (prepended).
	 * e.g. <code>MyProject/src</code>
	 */
	public final static long ROOT_QUALIFIED= 1L << 25;

	/**
	 * Source roots contain the project name (appended).
	 * e.g. <code>src - MyProject</code>
	 */
	public final static long ROOT_POST_QUALIFIED= 1L << 26;	

	/**
	 * Add source root path.
	 * e.g. <code>func() - MyProject/src</code>
	 * Option only applies to getElementLabel
	 */
	public final static long APPEND_ROOT_PATH= 1L << 27;

	/**
	 * Prepend source root path.
	 * e.g. <code>MyProject/src - func()</code>
	 * Option only applies to getElementLabel
	 */
	public final static long PREPEND_ROOT_PATH= 1L << 28;

	/**
	 * Post qualify container project. For example
	 * <code>folder - MyProject</code> if the folder is in project MyProject.
	 */
	public final static long PROJECT_POST_QUALIFIED= 1L << 30; 

	/**
	 * Post qualify symbols with file. 
	 * e.g. <code>func() - /proj/folder/file.cpp</code> 
	 */
	public final static long MF_POST_FILE_QUALIFIED= 1L << 31;

	/**
	 * Specifies to apply color styles to labels. This flag only applies to methods taking or returning a {@link StyledString}.
	 */
	public final static long COLORIZE= 1L << 32;
	
	/**
	 * Qualify all elements
	 */
	public final static long ALL_FULLY_QUALIFIED= F_FULLY_QUALIFIED | M_FULLY_QUALIFIED | T_FULLY_QUALIFIED | TU_QUALIFIED | ROOT_QUALIFIED;

	/**
	 * Post qualify all elements
	 */
	public final static long ALL_POST_QUALIFIED= F_POST_QUALIFIED | M_POST_QUALIFIED  | TU_POST_QUALIFIED | ROOT_POST_QUALIFIED;

	/**
	 *  Default options (M_PARAMETER_TYPES enabled)
	 */
	public final static long ALL_DEFAULT= M_PARAMETER_TYPES;

	/**
	 *  Default qualify options (All except Root)
	 */
	public final static long DEFAULT_QUALIFIED= F_FULLY_QUALIFIED | M_FULLY_QUALIFIED | T_FULLY_QUALIFIED | TU_QUALIFIED;

	/**
	 *  Default post qualify options (All except Root)
	 */
	public final static long DEFAULT_POST_QUALIFIED= F_POST_QUALIFIED | M_POST_QUALIFIED | TU_POST_QUALIFIED;

	/**
	 * Separator for appending qualifiers
	 */
	public final static String CONCAT_STRING= CoreModelMessages.getString("CElementLabels.concat_string"); // " - "; //$NON-NLS-1$
	
	/**
	 * Separator for parameters, base classes, exceptions, etc.
	 */
	public final static String COMMA_STRING = CoreModelMessages.getString("CElementLabels.comma_string"); // ", "; //$NON-NLS-1$
	
	/**
	 * Separator for appending (return) type
	 */
	public final static String DECL_STRING  = CoreModelMessages.getString("CElementLabels.declseparator_string"); // "  "; // use for return type //$NON-NLS-1$

	
	
	//====================
	
	
	private CElementLabels() {
	}

	/**
	 * Returns the label of the given object. The object must be of type {@link ICElement} or adapt to {@link IWorkbenchAdapter}.
	 * If the element type is not known, the empty string is returned.
	 * The returned label is BiDi-processed with {@link TextProcessor#process(String, String)}.
	 *
	 * @param obj object to get the label for
	 * @param flags the rendering flags
	 * @return the label or the empty string if the object type is not supported
	 */
	public static String getTextLabel(Object obj, long flags) {
		if (obj instanceof ICElement) {
			return getElementLabel((ICElement) obj, flags);

		} else if (obj instanceof IResource) {
			return BasicElementLabels.getResourceName((IResource) obj);

		} else if (obj instanceof IStorage) {
			return BasicElementLabels.getResourceName(((IStorage) obj).getName());

		} else if (obj instanceof IAdaptable) {
			IWorkbenchAdapter wbadapter= (IWorkbenchAdapter) ((IAdaptable)obj).getAdapter(IWorkbenchAdapter.class);
			if (wbadapter != null) {
				return Strings.markLTR(wbadapter.getLabel(obj));
			}
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns the styled label of the given object. The object must be of type {@link ICElement} or adapt to {@link IWorkbenchAdapter}.
	 * If the element type is not known, the empty string is returned.
	 * The returned label is BiDi-processed with {@link TextProcessor#process(String, String)}.
	 *
	 * @param obj object to get the label for
	 * @param flags the rendering flags
	 * @return the label or the empty string if the object type is not supported
	 */
	public static StyledString getStyledTextLabel(Object obj, long flags) {
		if (obj instanceof ICElement) {
			return getStyledElementLabel((ICElement) obj, flags);

		} else if (obj instanceof IResource) {
			return getStyledResourceLabel((IResource) obj);

		} else if (obj instanceof IStorage) {
			return getStyledStorageLabel((IStorage) obj);

		} else if (obj instanceof IAdaptable) {
			IWorkbenchAdapter wbadapter= (IWorkbenchAdapter) ((IAdaptable)obj).getAdapter(IWorkbenchAdapter.class);
			if (wbadapter != null) {
				return Strings.markLTR(new StyledString(wbadapter.getLabel(obj)));
			}
		}
		return new StyledString();
	}

	/**
	 * Returns the styled string for the given resource.
	 * The returned label is BiDi-processed with {@link TextProcessor#process(String, String)}.
	 *
	 * @param resource the resource
	 * @return the styled string
	 */
	private static StyledString getStyledResourceLabel(IResource resource) {
		StyledString result= new StyledString(resource.getName());
		return Strings.markLTR(result);
	}

	/**
	 * Returns the styled string for the given storage.
	 * The returned label is BiDi-processed with {@link TextProcessor#process(String, String)}.
	 *
	 * @param storage the storage
	 * @return the styled string
	 */
	private static StyledString getStyledStorageLabel(IStorage storage) {
		StyledString result= new StyledString(storage.getName());
		return Strings.markLTR(result);
	}


	/**
	 * Returns the label for a Java element with the flags as defined by this class.
	 *
	 * @param element the element to render
	 * @param flags the rendering flags
	 * @return the label of the Java element
	 */
	public static String getElementLabel(ICElement element, long flags) {
		StringBuffer result= new StringBuffer();
		getElementLabel(element, flags, result);
		return Strings.markCElementLabelLTR(result.toString());
	}

	/**
	 * Returns the styled label for a Java element with the flags as defined by this class.
	 *
	 * @param element the element to render
	 * @param flags the rendering flags
	 * @return the label of the Java element
	 *
	 */
	public static StyledString getStyledElementLabel(ICElement element, long flags) {
		StyledString result= new StyledString();
		getElementLabel(element, flags, result);
		return Strings.markCElementLabelLTR(result);
	}

	/**
	 * Returns the label for a Java element with the flags as defined by this class.
	 *
	 * @param element the element to render
	 * @param flags the rendering flags
	 * @param buf the buffer to append the resulting label to
	 */
	public static void getElementLabel(ICElement element, long flags, StringBuffer buf) {
		new CElementLabelComposer(buf).appendElementLabel(element, flags);
	}

	/**
	 * Returns the styled label for a Java element with the flags as defined by this class.
	 *
	 * @param element the element to render
	 * @param flags the rendering flags
	 * @param result the buffer to append the resulting label to
	 */
	public static void getElementLabel(ICElement element, long flags, StyledString result) {
		new CElementLabelComposer(result).appendElementLabel(element, flags);
	}
	
	/**
	 * Appends the label for a macro definition to a StringBuffer.
	 * @param macro a macro definition
	 * @param flags {@link #MF_POST_FILE_QUALIFIED}, or 0.
	 * @param buf the buffer to append the label to.
	 */
	public static void getMacroLabel(IMacro macro, int flags, StringBuffer buf) {
		new CElementLabelComposer(buf).appendMacroLabel(macro, flags);
	}

	/**
	 * Appends the label for a macro to a {@link StyledString}. Considers the M_* flags.
	 *
	 * @param macro the element to render
	 * @param flags the rendering flags. Flags with names starting with 'M_' are considered.
	 * @param result the buffer to append the resulting label to
	 */
	public static void getMethodLabel(IMacro macro, long flags, StyledString result) {
		new CElementLabelComposer(result).appendMacroLabel(macro, flags);
	}
	
	/**
	 * Appends the label for a method declaration to a StringBuffer.
	 * @param method a method declaration
	 * @param flags any of the M_* flags, and MF_POST_FILE_QUALIFIED
	 * @param buf the buffer to append the label
	 */
	public static void getMethodLabel( IMethodDeclaration method, int flags, StringBuffer buf ) {
		new CElementLabelComposer(buf).appendMethodLabel(method, flags);
	}

	/**
	 * Appends the label for a macro to a {@link StyledString}. Considers the M_* flags.
	 *
	 * @param method the element to render
	 * @param flags the rendering flags. Flags with names starting with 'M_' are considered.
	 * @param result the buffer to append the resulting label to
	 */
	public static void getMethodLabel(IMethodDeclaration method, long flags, StyledString result) {
		new CElementLabelComposer(result).appendMethodLabel(method, flags);
	}
	
	/**
	 * Appends the label for a field to a StringBuffer.
	 * @param field a field
	 * @param flags any of the F_* flags, and MF_POST_FILE_QUALIFIED
	 * @param buf the buffer to append the label
	 */
	public static void getFieldLabel(IField field, int flags, StringBuffer buf ) {
		new CElementLabelComposer(buf).appendFieldLabel(field, flags);	
	}

	/**
	 * Appends the label for a field to a {@link StyledString}.
	 * @param field a field
	 * @param flags any of the F_* flags, and MF_POST_FILE_QUALIFIED
	 * @param result the buffer to append the label
	 */
	public static void getFieldLabel(IField field, int flags, StyledString result ) {
		new CElementLabelComposer(result).appendFieldLabel(field, flags);	
	}

	/**
	 * Appends the label for a variable declaration to a StringBuffer.
	 * @param var a variable declaration
	 * @param flags any of the F_* flags, and MF_POST_FILE_QUALIFIED
	 * @param buf the buffer to append the label
	 */
	public static void getVariableLabel(IVariableDeclaration var, int flags, StringBuffer buf ) {
		new CElementLabelComposer(buf).appendVariableLabel(var, flags);	
	}

	/**
	 * Appends the label for a variable declaration to a {@link StyledString}.
	 * @param var a variable declaration
	 * @param flags any of the F_* flags, and MF_POST_FILE_QUALIFIED
	 * @param result the buffer to append the label
	 */
	public static void getVariableLabel(IVariableDeclaration var, int flags, StyledString result ) {
		new CElementLabelComposer(result).appendVariableLabel(var, flags);	
	}

	/**
	 * Appends the label for an enumerator to a StringBuffer.
	 * @param var an enumerator
	 * @param flags any of the F_* flags, and MF_POST_FILE_QUALIFIED
	 * @param buf the buffer to append the label
	 */
	public static void getEnumeratorLabel(IEnumerator var, int flags, StringBuffer buf ) {
		new CElementLabelComposer(buf).appendEnumeratorLabel(var, flags);	
	}

	/**
	 * Appends the label for an enumerator to a {@link StyledString}.
	 * @param var an enumerator
	 * @param flags any of the F_* flags, and MF_POST_FILE_QUALIFIED
	 * @param result the buffer to append the label
	 */
	public static void getEnumeratorLabel(IEnumerator var, int flags, StyledString result ) {
		new CElementLabelComposer(result).appendEnumeratorLabel(var, flags);	
	}

	/**
	 * Appends the label for a function declaration to a StringBuffer.
	 * @param func a function declaration
	 * @param flags any of the M_* flags, and MF_POST_FILE_QUALIFIED
	 * @param buf the buffer to append the label
	 */
	public static void getFunctionLabel(IFunctionDeclaration func, int flags, StringBuffer buf) {
		new CElementLabelComposer(buf).appendFunctionLabel(func, flags);	
	}

	/**
	 * Appends the label for a function declaration to a {@link StyledString}.
	 * @param func a function declaration
	 * @param flags any of the M_* flags, and MF_POST_FILE_QUALIFIED
	 * @param result the buffer to append the label
	 */
	public static void getFunctionLabel(IFunctionDeclaration func, int flags, StyledString result) {
		new CElementLabelComposer(result).appendFunctionLabel(func, flags);	
	}

	/**
	 * Appends the label for a type definition to a StringBuffer.
	 * @param typedef a type definition
	 * @param flags any of the F_* flags, and MF_POST_FILE_QUALIFIED
	 * @param buf the buffer to append the label
	 */
	public static void getTypeDefLabel(ITypeDef typedef, int flags, StringBuffer buf ) {
		new CElementLabelComposer(buf).appendTypeDefLabel(typedef, flags);		
	}

	/**
	 * Appends the label for a type definition to a {@link StyledString}.
	 * @param typedef a type definition
	 * @param flags any of the F_* flags, and MF_POST_FILE_QUALIFIED
	 * @param result the buffer to append the label
	 */
	public static void getTypeDefLabel(ITypeDef typedef, int flags, StyledString result ) {
		new CElementLabelComposer(result).appendTypeDefLabel(typedef, flags);		
	}
	
}
