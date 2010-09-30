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
 *     Gerhard Schaber (Wind River Systems)
 *     Patrick Hofer [bug 325799]
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IEnumerator;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.IInheritance;
import org.eclipse.cdt.core.model.IMacro;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITemplate;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.ITypeDef;
import org.eclipse.cdt.core.model.IVariableDeclaration;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.model.CoreModelMessages;


// Most parts of this file were previously located in CElementLabels.
// FlexibleBuffer and sub-types are taken from JDTs JavaElementLabelComposer.

/**
 * Creates labels for ICElement objects.
 */
public class CElementLabelComposer {

	/**
	 * An adapter for buffer supported by the label composer.
	 */
	public static abstract class FlexibleBuffer {

		/**
		 * Appends the string representation of the given character to the buffer.
		 *
		 * @param ch the character to append
		 * @return a reference to this object
		 */
		public abstract FlexibleBuffer append(char ch);

		/**
		 * Appends the given string to the buffer.
		 *
		 * @param string the string to append
		 * @return a reference to this object
		 */
		public abstract FlexibleBuffer append(String string);

		/**
		 * Returns the length of the the buffer.
		 *
		 * @return the length of the current string
		 */
		public abstract int length();

		/**
		 * Sets a styler to use for the given source range. The range must be subrange of actual
		 * string of this buffer. Stylers previously set for that range will be overwritten.
		 *
		 * @param offset the start offset of the range
		 * @param length the length of the range
		 * @param styler the styler to set
		 *
		 * @throws StringIndexOutOfBoundsException if <code>start</code> is less than zero, or if
		 *             offset plus length is greater than the length of this object.
		 */
		public abstract void setStyle(int offset, int length, Styler styler);
	}

	public static class FlexibleStringBuffer extends FlexibleBuffer {
		private final StringBuffer fStringBuffer;

		public FlexibleStringBuffer(StringBuffer stringBuffer) {
			fStringBuffer= stringBuffer;
		}

		@Override
		public FlexibleBuffer append(char ch) {
			fStringBuffer.append(ch);
			return this;
		}

		@Override
		public FlexibleBuffer append(String string) {
			fStringBuffer.append(string);
			return this;
		}

		@Override
		public int length() {
			return fStringBuffer.length();
		}

		@Override
		public void setStyle(int offset, int length, Styler styler) {
			// no style
		}

		@Override
		public String toString() {
			return fStringBuffer.toString();
		}
	}

	public static class FlexibleStyledString extends FlexibleBuffer {
		private final StyledString fStyledString;

		public FlexibleStyledString(StyledString stringBuffer) {
			fStyledString= stringBuffer;
		}

		@Override
		public FlexibleBuffer append(char ch) {
			fStyledString.append(ch);
			return this;
		}

		@Override
		public FlexibleBuffer append(String string) {
			fStyledString.append(string);
			return this;
		}

		@Override
		public int length() {
			return fStyledString.length();
		}

		@Override
		public void setStyle(int offset, int length, Styler styler) {
			fStyledString.setStyle(offset, length, styler);
		}

		@Override
		public String toString() {
			return fStyledString.toString();
		}
	}
	
	
	private static final Styler QUALIFIER_STYLE= StyledString.QUALIFIER_STYLER;
	//private static final Styler COUNTER_STYLE= StyledString.COUNTER_STYLER;
	private static final Styler DECORATIONS_STYLE= StyledString.DECORATIONS_STYLER;

	
	private final FlexibleBuffer fBuffer;

	/**
	 * Creates a new java element composer based on the given buffer.
	 *
	 * @param buffer the buffer
	 */
	public CElementLabelComposer(FlexibleBuffer buffer) {
		fBuffer= buffer;
	}

	/**
	 * Creates a new java element composer based on the given buffer.
	 *
	 * @param buffer the buffer
	 */
	public CElementLabelComposer(StyledString buffer) {
		this(new FlexibleStyledString(buffer));
	}

	/**
	 * Creates a new java element composer based on the given buffer.
	 *
	 * @param buffer the buffer
	 */
	public CElementLabelComposer(StringBuffer buffer) {
		this(new FlexibleStringBuffer(buffer));
	}
	
	
	/**
	 * Appends the label for an element to a StringBuffer.
	 * @param element any element (IMethodDeclaration, IField, ITypeDef, IVariableDeclaration, etc.)
	 * @param flags any of the flags (M_*, F_*, ROOT_*, etc.) defined in this class
	 */
	public void appendElementLabel(ICElement element, long flags) {
		int type= element.getElementType();
		ISourceRoot root= null;
		
		if (type != ICElement.C_MODEL && type != ICElement.C_PROJECT && !(type == ICElement.C_CCONTAINER && element instanceof ISourceRoot))
			root= getSourceRoot(element);
		if (root != null && getFlag(flags, CElementLabels.PREPEND_ROOT_PATH)) {
			getSourceRootLabel(root, CElementLabels.ROOT_QUALIFIED);
			fBuffer.append(CElementLabels.CONCAT_STRING);
		}		
		switch (type) {
			case ICElement.C_MACRO:
				appendMacroLabel((IMacro) element, flags);
				break;
			case ICElement.C_METHOD : 
			case ICElement.C_METHOD_DECLARATION:
			case ICElement.C_TEMPLATE_METHOD:
			case ICElement.C_TEMPLATE_METHOD_DECLARATION:
				appendMethodLabel( (IMethodDeclaration) element, flags);
				break;
			case ICElement.C_FUNCTION:
			case ICElement.C_FUNCTION_DECLARATION:
			case ICElement.C_TEMPLATE_FUNCTION:
			case ICElement.C_TEMPLATE_FUNCTION_DECLARATION:
				appendFunctionLabel( (IFunctionDeclaration) element, flags);
				break;
			case ICElement.C_FIELD : 
				appendFieldLabel( (IField) element, flags);
				break;
			case ICElement.C_VARIABLE:
			case ICElement.C_VARIABLE_DECLARATION:
				appendVariableLabel( (IVariableDeclaration) element, flags);
				break;
			case ICElement.C_ENUMERATOR:
				appendEnumeratorLabel((IEnumerator) element, flags);
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
			case ICElement.C_NAMESPACE:
				appendTypeLabel( element, flags);
				break;
			case ICElement.C_TYPEDEF:
				appendTypeDefLabel((ITypeDef)element, flags);
				break;
			case ICElement.C_UNIT: 
				appendTranslationUnitLabel((ITranslationUnit) element, flags);
				break;	
			case ICElement.C_CCONTAINER:
				ICContainer container = (ICContainer) element;
				if (container instanceof ISourceRoot)
					getSourceRootLabel((ISourceRoot) container, flags);
				else
					appendContainerLabel(container, flags);
				break;
			case ICElement.C_PROJECT:
			case ICElement.C_MODEL:
				fBuffer.append(element.getElementName());
				break;
			default:
				fBuffer.append(element.getElementName());
		}
		
		if (root != null && getFlag(flags, CElementLabels.APPEND_ROOT_PATH)) {
			int offset= fBuffer.length();
			fBuffer.append(CElementLabels.CONCAT_STRING);
			getSourceRootLabel(root, CElementLabels.ROOT_QUALIFIED);
			if (getFlag(flags, CElementLabels.COLORIZE)) {
				fBuffer.setStyle(offset, fBuffer.length() - offset, QUALIFIER_STYLE);
			}
		}
		
		if (element instanceof IBinary) {
			IBinary bin = (IBinary)element;
			fBuffer.append(" - [" + bin.getCPU() + "/" + (bin.isLittleEndian() ? "le" : "be") + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
	}
	
	
	/**
	 * Appends the label for a macro definition to a StringBuffer.
	 * @param macro a macro definition
	 * @param flags {@link CElementLabels#MF_POST_FILE_QUALIFIED}, or 0.
	 */
	public void appendMacroLabel(IMacro macro, long flags) {
		fBuffer.append(macro.getElementName());
		if( getFlag(flags, CElementLabels.MF_POST_FILE_QUALIFIED)) {
			IPath path= macro.getPath();
			if (path != null) {
				int offset= fBuffer.length();
				fBuffer.append( CElementLabels.CONCAT_STRING );
				fBuffer.append(path.toString());
				if (getFlag(flags, CElementLabels.COLORIZE)) {
					fBuffer.setStyle(offset, fBuffer.length() - offset, QUALIFIER_STYLE);
				}
			}
		}
	}

	/**
	 * Appends the label for a method declaration to a StringBuffer.
	 * @param method a method declaration
	 * @param flags any of the M_* flags, and {@link CElementLabels#MF_POST_FILE_QUALIFIED}
	 */
	public void appendMethodLabel( IMethodDeclaration method, long flags ) {
		try {
		//return type
		if( getFlag( flags, CElementLabels.M_PRE_RETURNTYPE ) && method.exists() && !method.isConstructor() ) {
			fBuffer.append( method.getReturnType() );
			fBuffer.append( ' ' );
		}
		
		//qualification
		if( getFlag( flags, CElementLabels.M_FULLY_QUALIFIED ) ){
			ICElement parent = method.getParent();
			if (parent != null && parent.exists() && !(parent instanceof ITranslationUnit)) {
				appendTypeLabel( parent, CElementLabels.T_FULLY_QUALIFIED | (flags & CElementLabels.TEMPLATE_ARGUMENTS));
				fBuffer.append( "::" ); //$NON-NLS-1$
			}
		}
		
		if (getFlag(flags, CElementLabels.M_SIMPLE_NAME)) {
			fBuffer.append(getSimpleName(method.getElementName()));
		} else {
			fBuffer.append(method.getElementName());
		}
		
		//template parameters
		if (method instanceof ITemplate) {
			appendTemplateParameters((ITemplate)method, flags);
		}

		//parameters
		if( getFlag( flags, CElementLabels.M_PARAMETER_TYPES ) ) {
			fBuffer.append('(');

			String[] types = method.getParameterTypes();
			
			if (types != null) {
				for (int i= 0; i < types.length; i++) {
					if (i > 0) {
						fBuffer.append( CElementLabels.COMMA_STRING );
					}
					fBuffer.append( types[i] );
				}
			}
			fBuffer.append(')');
		}
		
		//exceptions
		if( getFlag( flags, CElementLabels.M_EXCEPTIONS ) && method.exists() ){
			String [] types = method.getExceptions();
			if (types.length > 0) {
				fBuffer.append(" throw( "); //$NON-NLS-1$
				for (int i= 0; i < types.length; i++) {
					if (i > 0) {
						fBuffer.append(CElementLabels.COMMA_STRING);
					}
					fBuffer.append( types[i] );
				}
				fBuffer.append( " )" ); //$NON-NLS-1$
			}
		}
		
		if( getFlag( flags, CElementLabels.M_APP_RETURNTYPE ) && method.exists() && !method.isConstructor() && !method.isDestructor()) {
			final String typeName= method.getReturnType();
			if (typeName != null && typeName.length() > 0) {
				int offset= fBuffer.length();
				fBuffer.append( CElementLabels.DECL_STRING );
				fBuffer.append(typeName);
				if (getFlag(flags, CElementLabels.COLORIZE)) {
					fBuffer.setStyle(offset, fBuffer.length() - offset, DECORATIONS_STYLE);
				}
			}
		}			
		
		// post qualification
		if( getFlag(flags, CElementLabels.M_POST_QUALIFIED)) {
			int offset= fBuffer.length();
			fBuffer.append( CElementLabels.CONCAT_STRING );
			appendTypeLabel( method.getParent(), CElementLabels.T_FULLY_QUALIFIED | (flags & CElementLabels.TEMPLATE_ARGUMENTS));
			if (getFlag(flags, CElementLabels.COLORIZE)) {
				fBuffer.setStyle(offset, fBuffer.length() - offset, QUALIFIER_STYLE);
			}
		}
		if( getFlag(flags, CElementLabels.MF_POST_FILE_QUALIFIED)) {
			
			IPath path= method.getPath();
			if (path != null) {
				int offset= fBuffer.length();
				fBuffer.append( CElementLabels.CONCAT_STRING );
				fBuffer.append(path.toString());
				if (getFlag(flags, CElementLabels.COLORIZE)) {
					fBuffer.setStyle(offset, fBuffer.length() - offset, QUALIFIER_STYLE);
				}
			}
			
		}
		} catch (CModelException e) {
			CUIPlugin.log(e);
		}
	}

	/**
	 * Strip any qualifier from the given name.
	 * 
	 * @param elementName
	 * @return a "simple" name
	 */
	private static String getSimpleName(String elementName) {
		int idx = elementName.lastIndexOf("::"); //$NON-NLS-1$
		if (idx >= 0) {
			return elementName.substring(idx+2);
		}
		return elementName;
	}

	private void appendTemplateParameters(ITemplate template, long flags) {
		String[] args= null;
		if (getFlag(flags, CElementLabels.TEMPLATE_ARGUMENTS)) {
			args = template.getTemplateArguments();
		} else if (getFlag(flags, CElementLabels.TEMPLATE_PARAMETERS)) {
			args= template.getTemplateParameterTypes();
		} else {
			return;
		}
		
		fBuffer.append('<');
		if (args != null) {
			for (int i= 0; i < args.length; i++) {
				if (i > 0) {
					fBuffer.append( ',' );
				}
				fBuffer.append( args[i] );
			}
		}
		fBuffer.append('>');
	}

	/**
	 * Appends the label for a field to a StringBuffer.
	 * @param field a field
	 * @param flags any of the F_* flags, and {@link CElementLabels#MF_POST_FILE_QUALIFIED}
	 */
	public void appendFieldLabel(IField field, long flags ) {
		try {
		//return type
		if( getFlag( flags, CElementLabels.F_PRE_TYPE_SIGNATURE ) && field.exists()) {
			fBuffer.append( field.getTypeName() );
			fBuffer.append( ' ' );
		}
		
		//qualification
		if( getFlag( flags, CElementLabels.F_FULLY_QUALIFIED ) ){
			ICElement parent = field.getParent();
			if (parent != null && parent.exists()) {
				appendTypeLabel( parent, CElementLabels.T_FULLY_QUALIFIED | (flags & CElementLabels.TEMPLATE_PARAMETERS));
				fBuffer.append( "::" ); //$NON-NLS-1$
			}
		}
		
		if (getFlag(flags, CElementLabels.F_SIMPLE_NAME)) {
			fBuffer.append(getSimpleName(field.getElementName()));
		} else {
			fBuffer.append(field.getElementName());
		}
			
		if( getFlag( flags, CElementLabels.F_APP_TYPE_SIGNATURE ) && field.exists()) {
			int offset= fBuffer.length();
			fBuffer.append( CElementLabels.DECL_STRING );
			fBuffer.append( field.getTypeName() );	
			if (getFlag(flags, CElementLabels.COLORIZE)) {
				fBuffer.setStyle(offset, fBuffer.length() - offset, DECORATIONS_STYLE);
			}
		}	
		
		// post qualification
		if( getFlag(flags, CElementLabels.F_POST_QUALIFIED)) {
			int offset= fBuffer.length();
			fBuffer.append( CElementLabels.CONCAT_STRING );
			appendTypeLabel( field.getParent(), CElementLabels.T_FULLY_QUALIFIED | (flags & CElementLabels.TEMPLATE_PARAMETERS));
			if (getFlag(flags, CElementLabels.COLORIZE)) {
				fBuffer.setStyle(offset, fBuffer.length() - offset, QUALIFIER_STYLE);
			}
		}
		if( getFlag(flags, CElementLabels.MF_POST_FILE_QUALIFIED)) {
			IPath path= field.getPath();
			if (path != null) {
				int offset= fBuffer.length();
				fBuffer.append( CElementLabels.CONCAT_STRING );
				fBuffer.append(path.toString());
				if (getFlag(flags, CElementLabels.COLORIZE)) {
					fBuffer.setStyle(offset, fBuffer.length() - offset, QUALIFIER_STYLE);
				}
			}

		}
		} catch (CModelException e) {
			CUIPlugin.log(e);
		}
	}

	/**
	 * Appends the label for a variable declaration to a StringBuffer.
	 * @param var a variable declaration
	 * @param flags any of the F_* flags, and {@link CElementLabels#MF_POST_FILE_QUALIFIED}
	 */
	public void appendVariableLabel(IVariableDeclaration var, long flags ) {
		try {
		//return type
		if( getFlag( flags, CElementLabels.F_PRE_TYPE_SIGNATURE ) && var.exists()) {
			fBuffer.append( var.getTypeName() );
			fBuffer.append( ' ' );
		}
		
		//qualification
		if( getFlag( flags, CElementLabels.F_FULLY_QUALIFIED ) ){
			ICElement parent = var.getParent();
			if (parent != null && parent.exists() && parent.getElementType() == ICElement.C_NAMESPACE) {
				appendTypeLabel( parent, CElementLabels.T_FULLY_QUALIFIED);
				fBuffer.append( "::" ); //$NON-NLS-1$
			}
		}
		
		fBuffer.append( var.getElementName() );
		
		if( getFlag( flags, CElementLabels.F_APP_TYPE_SIGNATURE ) && var.exists()) {
			int offset= fBuffer.length();
			fBuffer.append( CElementLabels.DECL_STRING );
			fBuffer.append( var.getTypeName() );	
			if (getFlag(flags, CElementLabels.COLORIZE)) {
				fBuffer.setStyle(offset, fBuffer.length() - offset, DECORATIONS_STYLE);
			}
		}			
		
		// post qualification
		if( getFlag(flags, CElementLabels.F_POST_QUALIFIED)) {
			ICElement parent = var.getParent();
			if (parent != null && parent.exists() && parent.getElementType() == ICElement.C_NAMESPACE) {
				int offset= fBuffer.length();
				fBuffer.append( CElementLabels.CONCAT_STRING );
				appendTypeLabel( var.getParent(), CElementLabels.T_FULLY_QUALIFIED);
				if (getFlag(flags, CElementLabels.COLORIZE)) {
					fBuffer.setStyle(offset, fBuffer.length() - offset, DECORATIONS_STYLE);
				}
			}
		}
		if( getFlag(flags, CElementLabels.MF_POST_FILE_QUALIFIED)) {
			int offset= fBuffer.length();
			IPath path= var.getPath();
			if (path != null) {
				fBuffer.append( CElementLabels.CONCAT_STRING );
				fBuffer.append(path.toString());
			}
			if (getFlag(flags, CElementLabels.COLORIZE)) {
				fBuffer.setStyle(offset, fBuffer.length() - offset, QUALIFIER_STYLE);
			}
		}
		} catch (CModelException e) {
			CUIPlugin.log(e);
		}
	}

	/**
	 * Appends the label for an enumerator to a StringBuffer.
	 * @param var an enumerator
	 * @param flags any of the F_* flags, and {@link CElementLabels#MF_POST_FILE_QUALIFIED}
	 */
	public void appendEnumeratorLabel(IEnumerator var, long flags ) {
		//qualification
		if( getFlag( flags, CElementLabels.F_FULLY_QUALIFIED ) ){
			ICElement parent = var.getParent();
			if (parent != null && parent.exists() && parent.getElementType() == ICElement.C_NAMESPACE) {
				appendTypeLabel( parent, CElementLabels.T_FULLY_QUALIFIED);
				fBuffer.append( "::" ); //$NON-NLS-1$
			}
		}
		
		fBuffer.append( var.getElementName() );
						
		// post qualification
		if( getFlag(flags, CElementLabels.F_POST_QUALIFIED)) {
			ICElement parent = var.getParent();
			if (parent != null && parent.exists() && parent.getElementType() == ICElement.C_NAMESPACE) {
				fBuffer.append( CElementLabels.CONCAT_STRING );
				appendTypeLabel( var.getParent(), CElementLabels.T_FULLY_QUALIFIED);
			}
		}
		if( getFlag(flags, CElementLabels.MF_POST_FILE_QUALIFIED)) {
			int offset= fBuffer.length();
			IPath path= var.getPath();
			if (path != null) {
				fBuffer.append( CElementLabels.CONCAT_STRING );
				fBuffer.append(path.toString());
			}
			if (getFlag(flags, CElementLabels.COLORIZE)) {
				fBuffer.setStyle(offset, fBuffer.length() - offset, QUALIFIER_STYLE);
			}
		}
	}

	/**
	 * Appends the label for a function declaration to a StringBuffer.
	 * @param func a function declaration
	 * @param flags any of the M_* flags, and {@link CElementLabels#MF_POST_FILE_QUALIFIED}
	 */
	public void appendFunctionLabel(IFunctionDeclaration func, long flags) {
		//return type
		if( getFlag( flags, CElementLabels.M_PRE_RETURNTYPE ) && func.exists()) {
			fBuffer.append( func.getReturnType() );
			fBuffer.append( ' ' );
		}
		
		//qualification
		if( getFlag( flags, CElementLabels.M_FULLY_QUALIFIED ) ){
			ICElement parent = func.getParent();
			if (parent != null && parent.exists() && parent.getElementType() == ICElement.C_NAMESPACE) {
				appendTypeLabel( parent, CElementLabels.T_FULLY_QUALIFIED);
				fBuffer.append( "::" ); //$NON-NLS-1$
			}
		}
		
		fBuffer.append( func.getElementName() );

		//template parameters
		if (func instanceof ITemplate) {
			appendTemplateParameters((ITemplate)func, flags);
		}

		//parameters
		if( getFlag( flags, CElementLabels.M_PARAMETER_TYPES ) ) {
			fBuffer.append('(');

			String[] types = func.getParameterTypes();
			
			if (types != null) {
				for (int i= 0; i < types.length; i++) {
					if (i > 0) {
						fBuffer.append( CElementLabels.COMMA_STRING );
					}
					fBuffer.append( types[i] );
				}
			}
			fBuffer.append(')');
		}
		
		//exceptions
		if( getFlag( flags, CElementLabels.M_EXCEPTIONS ) && func.exists() ){
			String [] types = func.getExceptions();
			if (types.length > 0) {
				fBuffer.append(" throw( "); //$NON-NLS-1$
				for (int i= 0; i < types.length; i++) {
					if (i > 0) {
						fBuffer.append(CElementLabels.COMMA_STRING);
					}
					fBuffer.append( types[i] );
				}
				fBuffer.append( " )" ); //$NON-NLS-1$
			}
		}
		
		if( getFlag( flags, CElementLabels.M_APP_RETURNTYPE ) && func.exists()) {
			final String typeName= func.getReturnType();
			if (typeName != null && typeName.length() > 0) {
				int offset= fBuffer.length();
				fBuffer.append( CElementLabels.DECL_STRING );
				fBuffer.append(typeName);
				if (getFlag(flags, CElementLabels.COLORIZE)) {
					fBuffer.setStyle(offset, fBuffer.length() - offset, DECORATIONS_STYLE);
				}
			}
		}			
		
		// post qualification
		if( getFlag(flags, CElementLabels.M_POST_QUALIFIED)) {
			ICElement parent = func.getParent();
			if (parent != null && parent.exists() && parent.getElementType() == ICElement.C_NAMESPACE) {
				fBuffer.append( CElementLabels.CONCAT_STRING );
				appendTypeLabel( func.getParent(), CElementLabels.T_FULLY_QUALIFIED);
			}
		}
		if( getFlag(flags, CElementLabels.MF_POST_FILE_QUALIFIED)) {
			IPath path= func.getPath();
			if (path != null) {
				int offset= fBuffer.length();
				fBuffer.append( CElementLabels.CONCAT_STRING );
				fBuffer.append(path.toString());
				if (getFlag(flags, CElementLabels.COLORIZE)) {
					fBuffer.setStyle(offset, fBuffer.length() - offset, QUALIFIER_STYLE);
				}
			}
		}
	}

	/**
	 * Appends the label for a type definition to a StringBuffer.
	 * @param typedef a type definition
	 * @param flags any of the F_* flags, and CElementLabels.MF_POST_FILE_QUALIFIED
	 */
	public void appendTypeDefLabel(ITypeDef typedef, long flags ) {
		// type
		if( getFlag( flags, CElementLabels.F_PRE_TYPE_SIGNATURE ) && typedef.exists()) {
			fBuffer.append( typedef.getTypeName() );
			fBuffer.append( ' ' );
		}
		
		//qualification
		if( getFlag( flags, CElementLabels.F_FULLY_QUALIFIED ) ){
			ICElement parent = typedef.getParent();
			if (parent != null && parent.exists() && parent.getElementType() == ICElement.C_NAMESPACE) {
				appendTypeLabel( parent, CElementLabels.T_FULLY_QUALIFIED);
				fBuffer.append( "::" ); //$NON-NLS-1$
			}
		}
		
		fBuffer.append( typedef.getElementName() );
				
		if( getFlag( flags, CElementLabels.F_APP_TYPE_SIGNATURE ) && typedef.exists()) {
			String typeName= typedef.getTypeName();
			if (typeName != null && typeName.length() > 0) {
				int offset= fBuffer.length();
				fBuffer.append( CElementLabels.DECL_STRING );
				fBuffer.append(typeName);
				if (getFlag(flags, CElementLabels.COLORIZE)) {
					fBuffer.setStyle(offset, fBuffer.length() - offset, DECORATIONS_STYLE);
				}
			}
		}			
		
		// post qualification
		if( getFlag(flags, CElementLabels.F_POST_QUALIFIED)) {
			ICElement parent = typedef.getParent();
			if (parent != null && parent.exists() && parent.getElementType() == ICElement.C_NAMESPACE) {
				fBuffer.append( CElementLabels.CONCAT_STRING );
				appendTypeLabel( typedef.getParent(), CElementLabels.T_FULLY_QUALIFIED);
			}
		}
		if( getFlag(flags, CElementLabels.MF_POST_FILE_QUALIFIED)) {
			IPath path= typedef.getPath();
			if (path != null) {
				int offset= fBuffer.length();
				fBuffer.append( CElementLabels.CONCAT_STRING );
				fBuffer.append(path.toString());
				if (getFlag(flags, CElementLabels.COLORIZE)) {
					fBuffer.setStyle(offset, fBuffer.length() - offset, QUALIFIER_STYLE);
				}
			}
		}
	}

	/**
	 * Appends the label for a source root to a StringBuffer.
	 * @param root a source root
	 * @param flags any of the ROOT_* flags, and PROJECT_POST_QUALIFIED
	 */
	public void getSourceRootLabel(ISourceRoot root, long flags) {
		appendFolderLabel(root, flags);
	}
	
	/**
	 * Appends the label for a container to a StringBuffer.
	 * @param container a container
	 * @param flags any of the ROOT_* flags, and PROJECT_POST_QUALIFIED
	 */
	public void appendContainerLabel(ICContainer container, long flags) {
		appendFolderLabel(container, flags);
	}

	private void appendFolderLabel(ICContainer container, long flags) {
		IResource resource= container.getResource();
		boolean rootQualified= getFlag(flags, CElementLabels.ROOT_QUALIFIED);
		if (rootQualified) {
			fBuffer.append(container.getPath().makeRelative().toString());
		} else {
			if (CCorePlugin.showSourceRootsAtTopOfProject()) {
				fBuffer.append(container.getElementName());
			}
			else {
				String elementName = container.getElementName();
				IPath path = new Path(elementName);
				fBuffer.append(path.lastSegment());
			}
			if (getFlag(flags, CElementLabels.ROOT_QUALIFIED)) {
				if (resource != null && container instanceof ISourceRoot && isReferenced((ISourceRoot)container)) {
					fBuffer.append(CElementLabels.CONCAT_STRING);
					fBuffer.append(resource.getProject().getName());
				} else {
					fBuffer.append(CElementLabels.CONCAT_STRING);
					fBuffer.append(container.getParent().getElementName());
				}
			}
		}
	}

	/**
	 * Appends the label for a translation unit to a StringBuffer.
	 * @param tu a translation unit
	 * @param flags any of the TU_* flags
	 */
	public  void appendTranslationUnitLabel(ITranslationUnit tu, long flags) {
		IResource r= tu.getResource();
		IPath path;
		if (r != null) {
			path= r.getFullPath().makeRelative();
		}
		else {
			path= tu.getPath();
		}
		
		if (path == null) {
			fBuffer.append(tu.getElementName());
		}
		else {
			if (getFlag(flags, CElementLabels.TU_QUALIFIED)) {
				fBuffer.append(path.toString());
			}
			else if (getFlag(flags, CElementLabels.TU_POST_QUALIFIED)) {
				fBuffer.append(path.lastSegment());
				fBuffer.append(CElementLabels.CONCAT_STRING);
				fBuffer.append(path.removeLastSegments(1).toString());
			}
			else {
				fBuffer.append(path.lastSegment());
			}
		}
	}

	/**
	 * Appends the label for a type to a StringBuffer.
	 * @param elem a type
	 * @param flags any of the T_* flags, and {@link CElementLabels#MF_POST_FILE_QUALIFIED}
	 */
	private void appendTypeLabel(ICElement elem, long flags) {
		if (getFlag(flags, CElementLabels.T_FULLY_QUALIFIED)) {
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
				long qflags= flags & ~CElementLabels.MF_POST_FILE_QUALIFIED;
				appendTypeLabel(parent, qflags);
				fBuffer.append("::"); //$NON-NLS-1$
			}
		}
		
		String typeName= elem.getElementName();
		if (typeName.length() == 0) { // anonymous
		    typeName = CoreModelMessages.getString("CElementLabels.anonymous");	//$NON-NLS-1$
		}
		fBuffer.append(typeName);

		if (getFlag(flags, CElementLabels.T_INHERITANCE) && elem instanceof IInheritance) {
			IInheritance inheritance= (IInheritance)elem;
			String[] superclassNames= inheritance.getSuperClassesNames();
			if (superclassNames != null && superclassNames.length > 0) {
				fBuffer.append(CElementLabels.DECL_STRING);
				for (int i = 0; i < superclassNames.length; i++) {
					if (i> 0) {
						fBuffer.append(CElementLabels.COMMA_STRING);
					}
					String superclass = superclassNames[i];
					String visibility = getVisibility(inheritance.getSuperClassAccess(superclass));
					fBuffer.append(visibility).append(' ').append(superclass);
				}
			}
		}

		//template parameters
		if (elem instanceof ITemplate) {
			appendTemplateParameters((ITemplate)elem, flags);
		}
		
		if( getFlag(flags, CElementLabels.MF_POST_FILE_QUALIFIED)) {
			IPath path= elem.getPath();
			if (path != null) {
				int offset= fBuffer.length();
				fBuffer.append( CElementLabels.CONCAT_STRING );
				fBuffer.append(path.toString());
				if (getFlag(flags, CElementLabels.COLORIZE)) {
					fBuffer.setStyle(offset, fBuffer.length() - offset, QUALIFIER_STYLE);
				}
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

	private static boolean getFlag(long flags, long flag) {
		return (flags & flag) != 0;
	}


	/**
	 * Returns the source root of <code>ICElement</code>. If the given
	 * element is already a source root, the element itself is returned.
	 */
	public static ISourceRoot getSourceRoot(ICElement element) {
		ICElement root = element;
		while (root != null) {
			if (root instanceof ISourceRoot)
				return (ISourceRoot)root;
			ICElement parent = root.getAncestor(ICElement.C_CCONTAINER);
			if (parent == root)
				return null;
			root = parent;
		}
		return null;
	}
	
	/**
	 * Returns <code>true</code> if the given source root is
	 * referenced. This means it is own by a different project but is referenced
	 * by the root's parent. Returns <code>false</code> if the given root
	 * doesn't have an underlying resource.
	 */
	public static boolean isReferenced(ISourceRoot root) {
		IResource resource= root.getResource();
		if (resource != null) {
			IProject project= resource.getProject();
			IProject container= root.getCProject().getProject();
			return !container.equals(project);
		}
		return false;
	}
}
