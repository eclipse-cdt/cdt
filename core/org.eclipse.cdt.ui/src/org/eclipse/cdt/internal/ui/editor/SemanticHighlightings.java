/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - Adapted for CDT
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

/**
 * Semantic highlightings.
 * Cloned from JDT.
 * 
 * @since 4.0
 */
public class SemanticHighlightings {

	private static final RGB RGB_BLACK = new RGB(0, 0, 0);

	/**
	 * A named preference part that controls the highlighting of static const fields.
	 */
	public static final String STATIC_CONST_FIELD="staticConstField"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of static fields.
	 */
	public static final String STATIC_FIELD="staticField"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of fields.
	 */
	public static final String FIELD="field"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of method declarations.
	 */
	public static final String METHOD_DECLARATION="methodDeclaration"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of static method invocations.
	 */
	public static final String STATIC_METHOD_INVOCATION="staticMethodInvocation"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of inherited method invocations.
	 */
	public static final String INHERITED_METHOD_INVOCATION="inheritedMethodInvocation"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of virtual method invocations.
	 */
	public static final String VIRTUAL_METHOD_INVOCATION="virtualMethodInvocation"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of function declarations.
	 */
	public static final String FUNCTION_DECLARATION="functionDeclaration"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of static method invocations.
	 */
	public static final String FUNCTION_INVOCATION="functionInvocation"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of local variables.
	 */
	public static final String LOCAL_VARIABLE_DECLARATION="localVariableDeclaration"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of local variables.
	 */
	public static final String LOCAL_VARIABLE="localVariable"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of global variables.
	 */
	public static final String GLOBAL_VARIABLE="globalVariable"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of parameter variables.
	 */
	public static final String PARAMETER_VARIABLE="parameterVariable"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of template parameters.
	 */
	public static final String TEMPLATE_PARAMETER="templateParameter"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of method invocations.
	 */
	public static final String METHOD_INVOCATION="method"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of classes.
	 */
	public static final String CLASS="class"; //$NON-NLS-1$
	
	/**
	 * A named preference part that controls the highlighting of enums.
	 */
	public static final String ENUM="enum"; //$NON-NLS-1$
	
	/**
	 * A named preference part that controls the highlighting of template arguments.
	 */
	public static final String TEMPLATE_ARGUMENT="templateArgument"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of macro substitutions
	 * (=references).
	 */
	public static final String MACRO_SUBSTITUTION="macroSubstitution"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of macro definitions.
	 */
	public static final String MACRO_DEFINITION="macroDefinition"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of typedefs.
	 */
	public static final String TYPEDEF="typedef"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of namespaces.
	 */
	public static final String NAMESPACE="namespace"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of labels.
	 */
	public static final String LABEL="label"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of enumerators.
	 */
	public static final String ENUMERATOR="enumerator"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of problems.
	 */
	public static final String PROBLEM="problem"; //$NON-NLS-1$

	/** Init debugging mode */
	private static final boolean DEBUG= "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.cdt.ui/debug/SemanticHighlighting"));  //$NON-NLS-1$//$NON-NLS-2$
	
	/**
	 * Semantic highlightings
	 */
	private static SemanticHighlighting[] fgSemanticHighlightings;

	/**
	 * Semantic highlighting for static const fields.
	 */
//	private static final class StaticConstFieldHighlighting extends SemanticHighlighting {
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
//		 */
//		public String getPreferenceKey() {
//			return STATIC_CONST_FIELD;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
//		 */
//		public RGB getDefaultTextColor() {
//			return RGB_BLACK;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
//		 */
//		public boolean isBoldByDefault() {
//			return false;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
//		 */
//		public boolean isItalicByDefault() {
//			return false;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
//		 */
//		public boolean isEnabledByDefault() {
//			return false;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
//		 */
//		public String getDisplayName() {
//			return CEditorMessages.getString("SemanticHighlighting_staticConstField"); //$NON-NLS-1$
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
//		 */
//		public boolean consumes(SemanticToken token) {
//			IBinding binding= token.getBinding();
//			if (binding instanceof ICPPField && !(binding instanceof IProblemBinding)) {
//				ICPPField field= (ICPPField)binding;
//				try {
//					// TLETODO [semanticHighlighting] need access to const storage class
//					return field.isStatic() /* && field.isConst() */;
//				} catch (DOMException exc) {
//					CUIPlugin.getDefault().log(exc.getStatus());
//				} catch (Error e) /* PDOMNotImplementedError */ {
//					// ignore
//				}
//			}
//			return false;
//		}
//	}

	/**
	 * Semantic highlighting for static fields.
	 */
	private static final class StaticFieldHighlighting extends SemanticHighlighting {

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
		 */
		public String getPreferenceKey() {
			return STATIC_FIELD;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
		 */
		public RGB getDefaultTextColor() {
			return new RGB(0, 0, 192);
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
		 */
		public boolean isBoldByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
		 */
		public boolean isItalicByDefault() {
			return true;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
		 */
		public boolean isEnabledByDefault() {
			return true;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
		 */
		public String getDisplayName() {
			return CEditorMessages.getString("SemanticHighlighting_staticField"); //$NON-NLS-1$
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		public boolean consumes(SemanticToken token) {
			IBinding binding= token.getBinding();
			if (binding instanceof IField && !(binding instanceof IProblemBinding)) {
				try {
					return ((IField)binding).isStatic();
				} catch (DOMException exc) {
					CUIPlugin.getDefault().log(exc.getStatus());
				} catch (Error e) /* PDOMNotImplementedError */ {
					// ignore
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for fields.
	 */
	private static final class FieldHighlighting extends SemanticHighlighting {

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
		 */
		public String getPreferenceKey() {
			return FIELD;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
		 */
		public RGB getDefaultTextColor() {
			return new RGB(0, 0, 192);
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
		 */
		public boolean isBoldByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
		 */
		public boolean isItalicByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
		 */
		public boolean isEnabledByDefault() {
			return true;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
		 */
		public String getDisplayName() {
			return CEditorMessages.getString("SemanticHighlighting_field"); //$NON-NLS-1$
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		public boolean consumes(SemanticToken token) {
			IBinding binding= token.getBinding();
			if (binding instanceof IField && !(binding instanceof IProblemBinding)) {
				try {
					return !((IField)binding).isStatic();
				} catch (DOMException exc) {
					CUIPlugin.getDefault().log(exc.getStatus());
				} catch (Error e) /* PDOMNotImplementedError */ {
					// ignore
				}
				return true;
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for method declarations.
	 */
	private static final class MethodDeclarationHighlighting extends SemanticHighlighting {

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
		 */
		public String getPreferenceKey() {
			return METHOD_DECLARATION;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
		 */
		public RGB getDefaultTextColor() {
			return RGB_BLACK;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
		 */
		public boolean isBoldByDefault() {
			return true;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
		 */
		public boolean isItalicByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
		 */
		public boolean isEnabledByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
		 */
		public String getDisplayName() {
			return CEditorMessages.getString("SemanticHighlighting_methodDeclaration"); //$NON-NLS-1$
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName)node;
				if (name.isDeclaration() || name.isDefinition()) {
					IBinding binding= token.getBinding();
					if (binding instanceof ICPPMethod && !(binding instanceof IProblemBinding)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for static method invocations.
	 */
//	private static final class StaticMethodInvocationHighlighting extends SemanticHighlighting {
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
//		 */
//		public String getPreferenceKey() {
//			return STATIC_METHOD_INVOCATION;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
//		 */
//		public RGB getDefaultTextColor() {
//			return RGB_BLACK;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
//		 */
//		public boolean isBoldByDefault() {
//			return false;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
//		 */
//		public boolean isItalicByDefault() {
//			return true;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
//		 */
//		public boolean isEnabledByDefault() {
//			return true;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
//		 */
//		public String getDisplayName() {
//			return CEditorMessages.getString("SemanticHighlighting_staticMethodInvocation"); //$NON-NLS-1$
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
//		 */
//		public boolean consumes(SemanticToken token) {
//			IASTNode node= token.getNode();
//			if (node instanceof IASTName) {
//				IASTName name= (IASTName)node;
//				if (name.isReference()) {
//					IBinding binding= token.getBinding();
//					if (binding instanceof ICPPMethod) {
//						try {
//							return ((ICPPMethod)binding).isStatic();
//						} catch (DOMException exc) {
//							CUIPlugin.getDefault().log(exc.getStatus());
//						} catch (Error e) /* PDOMNotImplementedError */ {
//							// ignore
//						}
//					}
//				}
//			}
//			return false;
//		}
//	}

	/**
	 * Semantic highlighting for virtual method invocations.
	 */
//	private static final class VirtualMethodInvocationHighlighting extends SemanticHighlighting {
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
//		 */
//		public String getPreferenceKey() {
//			return VIRTUAL_METHOD_INVOCATION;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
//		 */
//		public RGB getDefaultTextColor() {
//			return RGB_BLACK;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
//		 */
//		public boolean isBoldByDefault() {
//			return false;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
//		 */
//		public boolean isItalicByDefault() {
//			return false;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
//		 */
//		public boolean isEnabledByDefault() {
//			return false;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
//		 */
//		public String getDisplayName() {
//			return CEditorMessages.getString("SemanticHighlighting_virtualMethodInvocation"); //$NON-NLS-1$
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
//		 */
//		public boolean consumes(SemanticToken token) {
//			IASTName node= token.getNode();
//			if (node.isReference()) {
//				IBinding binding= token.getBinding();
//				if (binding instanceof ICPPMethod) {
//					try {
//						// TLETODO [semanticHighlighting] need proper check for virtual method
//						return ((ICPPMethod)binding).isVirtual();
//					} catch (DOMException exc) {
//						CUIPlugin.getDefault().log(exc.getStatus());
//					} catch (Error e) /* PDOMNotImplementedError */ {
//						// ignore
//					}
//				}
//			}
//			return false;
//		}
//	}

	/**
	 * Semantic highlighting for inherited method invocations.
	 */
//	private static final class InheritedMethodInvocationHighlighting extends SemanticHighlighting {
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
//		 */
//		public String getPreferenceKey() {
//			return INHERITED_METHOD_INVOCATION;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
//		 */
//		public RGB getDefaultTextColor() {
//			return RGB_BLACK;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
//		 */
//		public boolean isBoldByDefault() {
//			return false;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
//		 */
//		public boolean isItalicByDefault() {
//			return false;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
//		 */
//		public boolean isEnabledByDefault() {
//			return false;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
//		 */
//		public String getDisplayName() {
//			return CEditorMessages.getString("SemanticHighlighting_inheritedMethodInvocation"); //$NON-NLS-1$
//		}
//
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
//		 */
//		public boolean consumes(SemanticToken token) {
//			// TLETODO [semanticHighlighting] inherited method invocation
//			return false;
//		}
//	}

	/**
	 * Semantic highlighting for method invocations.
	 */
	private static final class MethodInvocationHighlighting extends SemanticHighlighting {

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
		 */
		public String getPreferenceKey() {
			return METHOD_INVOCATION;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
		 */
		public RGB getDefaultTextColor() {
			return RGB_BLACK;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
		 */
		public boolean isBoldByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
		 */
		public boolean isItalicByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
		 */
		public boolean isEnabledByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
		 */
		public String getDisplayName() {
			return CEditorMessages.getString("SemanticHighlighting_method"); //$NON-NLS-1$
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName)node;
				if (name.isReference()) {
					IBinding binding= token.getBinding();
					if (binding instanceof ICPPMethod && !(binding instanceof IProblemBinding)) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * Extracts the method binding from the token's simple name. The method
		 * binding is either the token's binding (if the parent of token is a
		 * method call or declaration) or the constructor binding of a class
		 * instance creation if the node is the type name of a class instance
		 * creation.
		 *
		 * @param token the token to extract the method binding from
		 * @return the corresponding method binding, or <code>null</code>
		 */
//		private IBinding getMethodBinding(SemanticToken token) {
//			IBinding binding= null;
//			// work around: https://bugs.eclipse.org/bugs/show_bug.cgi?id=62605
//			IASTNode node= token.getNode();
//			IASTNode parent= node.getParent();
//			while (isTypePath(node, parent)) {
//				node= parent;
//				parent= parent.getParent();
//			}
//
//			if (parent != null && node.getLocationInParent() == ClassInstanceCreation.TYPE_PROPERTY)
//				binding= ((ClassInstanceCreation) parent).resolveConstructorBinding();
//			else
//				binding= token.getBinding();
//			return binding;
//		}

		/**
		 * Returns <code>true</code> if the given child/parent nodes are valid
		 * sub nodes of a <code>Type</code> IASTNode.
		 * @param child the child node
		 * @param parent the parent node
		 * @return <code>true</code> if the nodes may be the sub nodes of a type node, false otherwise
		 */
//		private boolean isTypePath(IASTNode child, IASTNode parent) {
//			if (parent instanceof Type) {
//				StructuralPropertyDescriptor location= child.getLocationInParent();
//				return location == ParameterizedType.TYPE_PROPERTY || location == SimpleType.NAME_PROPERTY;
//			} else if (parent instanceof QualifiedName) {
//				StructuralPropertyDescriptor location= child.getLocationInParent();
//				return location == QualifiedName.NAME_PROPERTY;
//			}
//			return false;
//		}
	}

	/**
	 * Semantic highlighting for function declarations.
	 */
	private static final class FunctionDeclarationHighlighting extends SemanticHighlighting {

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
		 */
		public String getPreferenceKey() {
			return FUNCTION_DECLARATION;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
		 */
		public RGB getDefaultTextColor() {
			return RGB_BLACK;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
		 */
		public boolean isBoldByDefault() {
			return true;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
		 */
		public boolean isItalicByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
		 */
		public boolean isEnabledByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
		 */
		public String getDisplayName() {
			return CEditorMessages.getString("SemanticHighlighting_functionDeclaration"); //$NON-NLS-1$
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName)node;
				if (name.isDeclaration()) {
					IBinding binding= token.getBinding();
					if (binding instanceof IFunction 
							&& !(binding instanceof ICPPMethod)
							&& !(binding instanceof IProblemBinding)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for function invocations.
	 */
	private static final class FunctionInvocationHighlighting extends SemanticHighlighting {

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
		 */
		public String getPreferenceKey() {
			return FUNCTION_INVOCATION;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
		 */
		public RGB getDefaultTextColor() {
			return RGB_BLACK;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
		 */
		public boolean isBoldByDefault() {
			return true;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
		 */
		public boolean isItalicByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
		 */
		public boolean isEnabledByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
		 */
		public String getDisplayName() {
			return CEditorMessages.getString("SemanticHighlighting_functionInvocation"); //$NON-NLS-1$
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName)node;
				if (name.isReference()) {
					IBinding binding= token.getBinding();
					if (binding instanceof IFunction 
							&& !(binding instanceof ICPPMethod)
							&& !(binding instanceof IProblemBinding)) {
						return true;
					}
				}
			}
			return false;
		}
	}
	
	/**
	 * Semantic highlighting for local variable declarations.
	 */
	private static final class LocalVariableDeclarationHighlighting extends SemanticHighlighting {

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
		 */
		public String getPreferenceKey() {
			return LOCAL_VARIABLE_DECLARATION;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
		 */
		public RGB getDefaultTextColor() {
			return new RGB(128, 0, 0);
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
		 */
		public boolean isBoldByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
		 */
		public boolean isItalicByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
		 */
		public boolean isEnabledByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
		 */
		public String getDisplayName() {
			return CEditorMessages.getString("SemanticHighlighting_localVariableDeclaration"); //$NON-NLS-1$
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName)node;
				if (name.isDeclaration()) {
					IBinding binding= token.getBinding();
					if (binding instanceof IVariable
							&& !(binding instanceof IField)
							&& !(binding instanceof IParameter)
							&& !(binding instanceof IProblemBinding)) {
						try {
							IScope scope= binding.getScope();
							if (LocalVariableHighlighting.isLocalScope(scope)) {
								return true;
							}
						} catch (DOMException exc) {
							CUIPlugin.getDefault().log(exc.getStatus());
						} catch (Error e) /* PDOMNotImplementedError */ {
							// ignore
						}
					}
				}
			}
			return false;
		}

}

	/**
	 * Semantic highlighting for local variables.
	 */
	private static final class LocalVariableHighlighting extends SemanticHighlighting {

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
		 */
		public String getPreferenceKey() {
			return LOCAL_VARIABLE;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
		 */
		public RGB getDefaultTextColor() {
			return RGB_BLACK;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
		 */
		public boolean isBoldByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
		 */
		public boolean isItalicByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
		 */
		public boolean isEnabledByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
		 */
		public String getDisplayName() {
			return CEditorMessages.getString("SemanticHighlighting_localVariable"); //$NON-NLS-1$
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName)node;
				if (name.isReference()) {
					IBinding binding= token.getBinding();
					if (binding instanceof IVariable
							&& !(binding instanceof IField)
							&& !(binding instanceof IParameter)
							&& !(binding instanceof IProblemBinding)) {
						try {
							IScope scope= binding.getScope();
							if (isLocalScope(scope)) {
								return true;
							}
						} catch (DOMException exc) {
							CUIPlugin.getDefault().log(exc.getStatus());
						} catch (Error e) /* PDOMNotImplementedError */ {
							// ignore
						}
					}
				}
			}
			return false;
		}

	    public static boolean isLocalScope(IScope scope) {
	        while (scope != null) {
	            if (scope instanceof ICPPFunctionScope ||
	                    scope instanceof ICPPBlockScope ||
	                    scope instanceof ICFunctionScope) {
	                return true;
	            }
	            try {
	                scope= scope.getParent();
	            } catch (DOMException e) {
	                scope= null;
	            }
	        }
	        return false;
	    }
}

	/**
	 * Semantic highlighting for global variables.
	 */
	private static final class GlobalVariableHighlighting extends SemanticHighlighting {

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
		 */
		public String getPreferenceKey() {
			return GLOBAL_VARIABLE;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
		 */
		public RGB getDefaultTextColor() {
			return RGB_BLACK;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
		 */
		public boolean isBoldByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
		 */
		public boolean isItalicByDefault() {
			return true;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
		 */
		public boolean isEnabledByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
		 */
		public String getDisplayName() {
			return CEditorMessages.getString("SemanticHighlighting_globalVariable"); //$NON-NLS-1$
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		public boolean consumes(SemanticToken token) {
			IBinding binding= token.getBinding();
			if (binding instanceof IVariable
					&& !(binding instanceof IField)
					&& !(binding instanceof IParameter)
					&& !(binding instanceof IProblemBinding)) {
				try {
					IScope scope= binding.getScope();
					if (!LocalVariableHighlighting.isLocalScope(scope)) {
						return true;
					}
				} catch (DOMException exc) {
					CUIPlugin.getDefault().log(exc.getStatus());
				} catch (Error e) /* PDOMNotImplementedError */ {
					// ignore
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for parameter variables.
	 */
	private static final class ParameterVariableHighlighting extends SemanticHighlighting {

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
		 */
		public String getPreferenceKey() {
			return PARAMETER_VARIABLE;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
		 */
		public RGB getDefaultTextColor() {
			return RGB_BLACK;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
		 */
		public boolean isBoldByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
		 */
		public boolean isItalicByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
		 */
		public boolean isEnabledByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
		 */
		public String getDisplayName() {
			return CEditorMessages.getString("SemanticHighlighting_parameterVariable"); //$NON-NLS-1$
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		public boolean consumes(SemanticToken token) {
			IBinding binding= token.getBinding();
			if (binding instanceof IParameter && !(binding instanceof IProblemBinding)) {
				return true;
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for template parameters.
	 */
	private static final class TemplateParameterHighlighting extends SemanticHighlighting {

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
		 */
		public String getPreferenceKey() {
			return TEMPLATE_PARAMETER;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
		 */
		public RGB getDefaultTextColor() {
			return new RGB(100, 70, 50);
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
		 */
		public boolean isBoldByDefault() {
			return true;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
		 */
		public boolean isItalicByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
		 */
		public boolean isEnabledByDefault() {
			return true;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
		 */
		public String getDisplayName() {
			return CEditorMessages.getString("SemanticHighlighting_templateParameter"); //$NON-NLS-1$
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IBinding binding= token.getBinding();
				if (binding instanceof ICPPTemplateParameter && !(binding instanceof IProblemBinding)) {
					return true;
				}
				// template parameters are resolved as problems??
//				if (node.getParent() instanceof ICPPASTNamedTypeSpecifier) {
//					if (binding instanceof IProblemBinding) {
//						return true;
//					}
//				}
			}
			return false;
		}
	}
	
	/**
	 * Semantic highlighting for classes.
	 */
	private static final class ClassHighlighting extends SemanticHighlighting {

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
		 */
		public String getPreferenceKey() {
			return CLASS;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
		 */
		public RGB getDefaultTextColor() {
			return new RGB(0, 80, 50);
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
		 */
		public boolean isBoldByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
		 */
		public boolean isItalicByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
		 */
		public boolean isEnabledByDefault() {
			return false;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
		 */
		public String getDisplayName() {
			return CEditorMessages.getString("SemanticHighlighting_classes"); //$NON-NLS-1$
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IBinding binding= token.getBinding();
				if (binding instanceof ICPPClassType && !(binding instanceof IProblemBinding)) {
					IASTName name= (IASTName)node;
					if (name.isReference()) {
						if (node.getParent() instanceof ICPPASTQualifiedName) {
							ICPPASTQualifiedName qName= (ICPPASTQualifiedName)node.getParent();
							if (qName.getLastName() == name) {
								return true;
							}
						} else {
							return true;
						}
					} else {
						return true;
					}
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for enums.
	 */
	private static final class EnumHighlighting extends SemanticHighlighting {
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
		 */
		public String getPreferenceKey() {
			return ENUM;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
		 */
		public RGB getDefaultTextColor() {
			return new RGB(100, 70, 50);
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
		 */
		public boolean isBoldByDefault() {
			return false;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
		 */
		public boolean isItalicByDefault() {
			return false;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
		 */
		public boolean isEnabledByDefault() {
			return false;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
		 */
		public String getDisplayName() {
			return CEditorMessages.getString("SemanticHighlighting_enums"); //$NON-NLS-1$
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		public boolean consumes(SemanticToken token) {
			IBinding binding= token.getBinding();
			if (binding instanceof IEnumeration && !(binding instanceof IProblemBinding)) {
				return true;
			}
			return false;
		}
	}
	
	/**
	 * Semantic highlighting for template arguments.
	 */
//	private static final class TemplateArgumentHighlighting extends SemanticHighlighting {
//		
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
//		 */
//		public String getPreferenceKey() {
//			return TEMPLATE_ARGUMENT;
//		}
//		
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
//		 */
//		public RGB getDefaultTextColor() {
//			return new RGB(13, 100, 0);
//		}
//		
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
//		 */
//		public boolean isBoldByDefault() {
//			return false;
//		}
//		
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
//		 */
//		public boolean isItalicByDefault() {
//			return false;
//		}
//		
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
//		 */
//		public boolean isEnabledByDefault() {
//			return false;
//		}
//		
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
//		 */
//		public String getDisplayName() {
//			return CEditorMessages.getString("SemanticHighlighting_templateArguments"); //$NON-NLS-1$
//		}
//		
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
//		 */
//		public boolean consumes(SemanticToken token) {
//			IBinding binding= token.getBinding();
//			if (binding instanceof ICPPTemplateParameter) {
//				return true;
//			}
//			return false;
//		}
//	}
	
	/**
	 * Semantic highlighting for macro substitutions (references).
	 */
	private static final class MacroSubstitutionHighlighting extends SemanticHighlighting {
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
		 */
		public String getPreferenceKey() {
			return MACRO_SUBSTITUTION;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
		 */
		public RGB getDefaultTextColor() {
			return RGB_BLACK;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
		 */
		public boolean isBoldByDefault() {
			return false;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
		 */
		public boolean isItalicByDefault() {
			return false;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
		 */
		public boolean isEnabledByDefault() {
			return false;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
		 */
		public String getDisplayName() {
			return CEditorMessages.getString("SemanticHighlighting_macroSubstitution"); //$NON-NLS-1$
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (!(node instanceof IASTName)) {
				IASTNodeLocation[] nodeLocations= node.getNodeLocations();
				if (nodeLocations.length == 1 && nodeLocations[0] instanceof IASTMacroExpansion) {
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * Semantic highlighting for macro definitions.
	 */
//	private static final class MacroDefinitionHighlighting extends SemanticHighlighting {
//		
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
//		 */
//		public String getPreferenceKey() {
//			return MACRO_DEFINITION;
//		}
//		
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
//		 */
//		public RGB getDefaultTextColor() {
//			return RGB_BLACK;
//		}
//		
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
//		 */
//		public boolean isBoldByDefault() {
//			return false;
//		}
//		
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
//		 */
//		public boolean isItalicByDefault() {
//			return false;
//		}
//		
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
//		 */
//		public boolean isEnabledByDefault() {
//			return false;
//		}
//		
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
//		 */
//		public String getDisplayName() {
//			return CEditorMessages.getString("SemanticHighlighting_macroDefinition"); //$NON-NLS-1$
//		}
//		
//		/*
//		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
//		 */
//		public boolean consumes(SemanticToken token) {
//			IASTNode node= token.getNode();
//			if (node instanceof IASTName && node.getParent() instanceof IASTPreprocessorMacroDefinition) {
//				return true;
//			}
//			return false;
//		}
//	}
	
	/**
	 * Semantic highlighting for typedefs.
	 */
	private static final class TypedefHighlighting extends SemanticHighlighting {
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
		 */
		public String getPreferenceKey() {
			return TYPEDEF;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
		 */
		public RGB getDefaultTextColor() {
			return RGB_BLACK;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
		 */
		public boolean isBoldByDefault() {
			return false;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
		 */
		public boolean isItalicByDefault() {
			return false;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
		 */
		public boolean isEnabledByDefault() {
			return false;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
		 */
		public String getDisplayName() {
			return CEditorMessages.getString("SemanticHighlighting_typeDef"); //$NON-NLS-1$
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		public boolean consumes(SemanticToken token) {
			IBinding binding= token.getBinding();
			if (binding instanceof ITypedef && !(binding instanceof IProblemBinding)) {
				return true;
			}
			return false;
		}
	}
	
	/**
	 * Semantic highlighting for namespaces.
	 */
	private static final class NamespaceHighlighting extends SemanticHighlighting {
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
		 */
		public String getPreferenceKey() {
			return NAMESPACE;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
		 */
		public RGB getDefaultTextColor() {
			return RGB_BLACK;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
		 */
		public boolean isBoldByDefault() {
			return false;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
		 */
		public boolean isItalicByDefault() {
			return false;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
		 */
		public boolean isEnabledByDefault() {
			return false;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
		 */
		public String getDisplayName() {
			return CEditorMessages.getString("SemanticHighlighting_namespace"); //$NON-NLS-1$
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		public boolean consumes(SemanticToken token) {
			IBinding binding= token.getBinding();
			if (binding instanceof ICPPNamespace && !(binding instanceof IProblemBinding)) {
				return true;
			}
			return false;
		}
	}
	
	/**
	 * Semantic highlighting for labels.
	 */
	private static final class LabelHighlighting extends SemanticHighlighting {
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
		 */
		public String getPreferenceKey() {
			return LABEL;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
		 */
		public RGB getDefaultTextColor() {
			return RGB_BLACK;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
		 */
		public boolean isBoldByDefault() {
			return false;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
		 */
		public boolean isItalicByDefault() {
			return false;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
		 */
		public boolean isEnabledByDefault() {
			return false;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
		 */
		public String getDisplayName() {
			return CEditorMessages.getString("SemanticHighlighting_label"); //$NON-NLS-1$
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		public boolean consumes(SemanticToken token) {
			IBinding binding= token.getBinding();
			if (binding instanceof ILabel && !(binding instanceof IProblemBinding)) {
				return true;
			}
			return false;
		}
	}
	
	/**
	 * Semantic highlighting for enumerators.
	 */
	private static final class EnumeratorHighlighting extends SemanticHighlighting {
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
		 */
		public String getPreferenceKey() {
			return ENUMERATOR;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
		 */
		public RGB getDefaultTextColor() {
			return RGB_BLACK;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
		 */
		public boolean isBoldByDefault() {
			return false;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
		 */
		public boolean isItalicByDefault() {
			return false;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
		 */
		public boolean isEnabledByDefault() {
			return false;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
		 */
		public String getDisplayName() {
			return CEditorMessages.getString("SemanticHighlighting_enumerator"); //$NON-NLS-1$
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		public boolean consumes(SemanticToken token) {
			IBinding binding= token.getBinding();
			if (binding instanceof IEnumerator && !(binding instanceof IProblemBinding)) {
				return true;
			}
			return false;
		}
	}
	
	/**
	 * Semantic highlighting for problems.
	 */
	private static final class ProblemHighlighting extends SemanticHighlighting {
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getPreferenceKey()
		 */
		public String getPreferenceKey() {
			return PROBLEM;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextColor()
		 */
		public RGB getDefaultTextColor() {
			return new RGB(224, 0, 0);
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDefaultTextStyleBold()
		 */
		public boolean isBoldByDefault() {
			return true;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isStrikethroughByDefault()
		 */
		public boolean isStrikethroughByDefault() {
			return true;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isItalicByDefault()
		 */
		public boolean isItalicByDefault() {
			return false;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#isEnabledByDefault()
		 */
		public boolean isEnabledByDefault() {
			return DEBUG;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#getDisplayName()
		 */
		public String getDisplayName() {
			return CEditorMessages.getString("SemanticHighlighting_problem"); //$NON-NLS-1$
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		public boolean consumes(SemanticToken token) {
			IBinding binding= token.getBinding();
			if (binding instanceof IProblemBinding) {
				return true;
			}
			return false;
		}
	}
	
	/**
	 * A named preference that controls the given semantic highlighting's color.
	 *
	 * @param semanticHighlighting the semantic highlighting
	 * @return the color preference key
	 */
	public static String getColorPreferenceKey(SemanticHighlighting semanticHighlighting) {
		return PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + semanticHighlighting.getPreferenceKey() + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_COLOR_SUFFIX;
	}

	/**
	 * A named preference that controls if the given semantic highlighting has the text attribute bold.
	 *
	 * @param semanticHighlighting the semantic highlighting
	 * @return the bold preference key
	 */
	public static String getBoldPreferenceKey(SemanticHighlighting semanticHighlighting) {
		return PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + semanticHighlighting.getPreferenceKey() + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_BOLD_SUFFIX;
	}

	/**
	 * A named preference that controls if the given semantic highlighting has the text attribute italic.
	 *
	 * @param semanticHighlighting the semantic highlighting
	 * @return the italic preference key
	 */
	public static String getItalicPreferenceKey(SemanticHighlighting semanticHighlighting) {
		return PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + semanticHighlighting.getPreferenceKey() + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ITALIC_SUFFIX;
	}

	/**
	 * A named preference that controls if the given semantic highlighting has the text attribute strikethrough.
	 *
	 * @param semanticHighlighting the semantic highlighting
	 * @return the strikethrough preference key
	 */
	public static String getStrikethroughPreferenceKey(SemanticHighlighting semanticHighlighting) {
		return PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + semanticHighlighting.getPreferenceKey() + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_STRIKETHROUGH_SUFFIX;
	}

	/**
	 * A named preference that controls if the given semantic highlighting has the text attribute underline.
	 *
	 * @param semanticHighlighting the semantic highlighting
	 * @return the underline preference key
	 */
	public static String getUnderlinePreferenceKey(SemanticHighlighting semanticHighlighting) {
		return PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + semanticHighlighting.getPreferenceKey() + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_UNDERLINE_SUFFIX;
	}

	/**
	 * A named preference that controls if the given semantic highlighting is enabled.
	 *
	 * @param semanticHighlighting the semantic highlighting
	 * @return the enabled preference key
	 */
	public static String getEnabledPreferenceKey(SemanticHighlighting semanticHighlighting) {
		return PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + semanticHighlighting.getPreferenceKey() + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED_SUFFIX;
	}

	/**
	 * @return The semantic highlightings, the order defines the precedence of matches, the first match wins.
	 */
	public static SemanticHighlighting[] getSemanticHighlightings() {
		if (fgSemanticHighlightings == null)
			fgSemanticHighlightings= new SemanticHighlighting[] {
				new MacroSubstitutionHighlighting(),  // before all others!
				new ClassHighlighting(),
//				new StaticConstFieldHighlighting(),
				new StaticFieldHighlighting(),
				new FieldHighlighting(),  // after all other fields
				new MethodDeclarationHighlighting(),
// TLETODO [semanticHighlighting] Static method invocations
//				new StaticMethodInvocationHighlighting(),
// TLETODO [semanticHighlighting] Virtual method invocations
//				new VirtualMethodInvocationHighlighting(),
// TLETODO [semanticHighlighting] Inherited method invocations
//				new InheritedMethodInvocationHighlighting(),
				new ParameterVariableHighlighting(),  // before local variables
				new LocalVariableDeclarationHighlighting(),
				new LocalVariableHighlighting(),
				new GlobalVariableHighlighting(),
// TLETODO [semanticHighlighting] Template parameter highlighting
				new TemplateParameterHighlighting(), // before template arguments!
				new MethodInvocationHighlighting(), // before types to get ctors
// TLETODO [semanticHighlighting] Template argument highlighting
//				new TemplateArgumentHighlighting(), // before other types
				new EnumHighlighting(),
// TLETODO [semanticHighlighting] Macro definition highlighting
//				new MacroDefinitionHighlighting(),
				new FunctionDeclarationHighlighting(),
				new FunctionInvocationHighlighting(),
				new TypedefHighlighting(),
				new NamespaceHighlighting(),
				new LabelHighlighting(),
				new EnumeratorHighlighting(),
				new ProblemHighlighting(),
			};
		return fgSemanticHighlightings;
	}

	/**
	 * Initialize default preferences in the given preference store.
	 * @param store The preference store
	 */
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED, DEBUG);

		SemanticHighlighting[] semanticHighlightings= getSemanticHighlightings();
		for (int i= 0, n= semanticHighlightings.length; i < n; i++) {
			SemanticHighlighting semanticHighlighting= semanticHighlightings[i];
			store.setDefault(SemanticHighlightings.getEnabledPreferenceKey(semanticHighlighting), DEBUG || semanticHighlighting.isEnabledByDefault());
			PreferenceConverter.setDefault(store, SemanticHighlightings.getColorPreferenceKey(semanticHighlighting), semanticHighlighting.getDefaultTextColor());
			store.setDefault(SemanticHighlightings.getBoldPreferenceKey(semanticHighlighting), semanticHighlighting.isBoldByDefault());
			store.setDefault(SemanticHighlightings.getItalicPreferenceKey(semanticHighlighting), semanticHighlighting.isItalicByDefault());
			store.setDefault(SemanticHighlightings.getStrikethroughPreferenceKey(semanticHighlighting), semanticHighlighting.isStrikethroughByDefault());
			store.setDefault(SemanticHighlightings.getUnderlinePreferenceKey(semanticHighlighting), DEBUG || semanticHighlighting.isUnderlineByDefault());
		}
	}

	/**
	 * Tests whether <code>event</code> in <code>store</code> affects the
	 * enablement of semantic highlighting.
	 *
	 * @param store the preference store where <code>event</code> was observed
	 * @param event the property change under examination
	 * @return <code>true</code> if <code>event</code> changed semantic
	 *         highlighting enablement, <code>false</code> if it did not
	 */
	public static boolean affectsEnablement(IPreferenceStore store, PropertyChangeEvent event) {
		if (event.getProperty().equals(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED)) {
			return true;
		}
		String relevantKey= null;
		SemanticHighlighting[] highlightings= getSemanticHighlightings();
		for (int i= 0; i < highlightings.length; i++) {
			if (event.getProperty().equals(getEnabledPreferenceKey(highlightings[i]))) {
				relevantKey= event.getProperty();
				break;
			}
		}
		if (relevantKey == null)
			return false;

		for (int i= 0; i < highlightings.length; i++) {
			String key= getEnabledPreferenceKey(highlightings[i]);
			if (key.equals(relevantKey))
				continue;
			if (store.getBoolean(key))
				return false; // another is still enabled or was enabled before
		}

		// all others are disabled, so toggling relevantKey affects the enablement
		return true;
	}

	/**
	 * Tests whether semantic highlighting is currently enabled.
	 *
	 * @param store the preference store to consult
	 * @return <code>true</code> if semantic highlighting is enabled,
	 *         <code>false</code> if it is not
	 */
	public static boolean isEnabled(IPreferenceStore store) {
		if (!store.getBoolean(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED)) {
			return false;
		}
		SemanticHighlighting[] highlightings= getSemanticHighlightings();
		boolean enable= false;
		for (int i= 0; i < highlightings.length; i++) {
			String enabledKey= getEnabledPreferenceKey(highlightings[i]);
			if (store.getBoolean(enabledKey)) {
				enable= true;
				break;
			}
		}

		return enable;
	}

	/**
	 * Do not instantiate
	 */
	private SemanticHighlightings() {
	}
}
