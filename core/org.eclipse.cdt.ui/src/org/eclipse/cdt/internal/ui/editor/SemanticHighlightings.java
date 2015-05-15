/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICExternalBinding;
import org.eclipse.cdt.core.dom.ast.c.ICFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTClassVirtSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVirtSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeferredFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.ICColorConstants;
import org.eclipse.cdt.ui.text.ISemanticToken;

import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;

/**
 * Semantic highlightings.
 * Derived from JDT.
 *
 * @since 4.0
 */
public class SemanticHighlightings {
	private static final RGB RGB_BLACK = new RGB(0, 0, 0);

	/**
	 * A named preference part that controls the highlighting of static fields.
	 */
	public static final String STATIC_FIELD= "staticField"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of fields.
	 */
	public static final String FIELD= "field"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of method declarations.
	 */
	public static final String METHOD_DECLARATION= "methodDeclaration"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of static method invocations.
	 */
	public static final String STATIC_METHOD_INVOCATION= "staticMethod"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of function declarations.
	 */
	public static final String FUNCTION_DECLARATION= "functionDeclaration"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of functions.
	 */
	public static final String FUNCTION= "function"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of local variables.
	 */
	public static final String LOCAL_VARIABLE_DECLARATION= "localVariableDeclaration"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of local variable references.
	 */
	public static final String LOCAL_VARIABLE= "localVariable"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of global variables.
	 */
	public static final String GLOBAL_VARIABLE= "globalVariable"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of parameter variables.
	 */
	public static final String PARAMETER_VARIABLE= "parameterVariable"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of template parameters.
	 */
	public static final String TEMPLATE_PARAMETER= "templateParameter"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of methods.
	 */
	public static final String METHOD= "method"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of classes.
	 */
	public static final String CLASS= "class"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of enums.
	 */
	public static final String ENUM= "enum"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of macro references.
	 */
	public static final String MACRO_REFERENCE= "macroSubstitution"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of macro definitions.
	 */
	public static final String MACRO_DEFINITION= "macroDefinition"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of typedefs.
	 */
	public static final String TYPEDEF= "typedef"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of namespaces.
	 */
	public static final String NAMESPACE= "namespace"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of labels.
	 */
	public static final String LABEL= "label"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of enumerators.
	 */
	public static final String ENUMERATOR= "enumerator"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of problems.
	 */
	public static final String PROBLEM= "problem"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of external SDK.
	 */
	public static final String EXTERNAL_SDK= "externalSDK"; //$NON-NLS-1$

	/**
	 * A named preference part that controls the highlighting of operators that have been overloaded.
	 */
	public static final String OVERLOADED_OPERATOR= "overloadedOperator"; //$NON-NLS-1$
	
	/** Init debugging mode */
	private static final boolean DEBUG= "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.cdt.ui/debug/SemanticHighlighting"));  //$NON-NLS-1$//$NON-NLS-2$

	/**
	 * Semantic highlightings
	 */
	private static SemanticHighlighting[] fgSemanticHighlightings;

	/**
	 * Semantic highlighting for static fields.
	 */
	private static final class StaticFieldHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return STATIC_FIELD;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return new RGB(0, 0, 192);
		}

		@Override
		public boolean isBoldByDefault() {
			return false;
		}

		@Override
		public boolean isItalicByDefault() {
			return true;
		}

		@Override
		public boolean isEnabledByDefault() {
			return true;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_staticField;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName) node;
				if (name instanceof ICPPASTQualifiedName && name.isReference()) {
					return false;
				}
				IBinding binding= token.getBinding();
				if (binding instanceof IField && !(binding instanceof IProblemBinding)) {
					return ((IField)binding).isStatic();
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for fields.
	 */
	private static final class FieldHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return FIELD;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return new RGB(0, 0, 192);
		}

		@Override
		public boolean isBoldByDefault() {
			return false;
		}

		@Override
		public boolean isItalicByDefault() {
			return false;
		}

		@Override
		public boolean isEnabledByDefault() {
			return true;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_field;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName) node;
				if (name instanceof ICPPASTQualifiedName && name.isReference()) {
					return false;
				}
				IBinding binding= token.getBinding();
				if (binding instanceof IField) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for method declarations.
	 */
	private static final class MethodDeclarationHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return METHOD_DECLARATION;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return RGB_BLACK;
		}

		@Override
		public boolean isBoldByDefault() {
			return true;
		}

		@Override
		public boolean isItalicByDefault() {
			return false;
		}

		@Override
		public boolean isEnabledByDefault() {
			return true;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_methodDeclaration;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTImplicitName)
				return false;

			if (node instanceof IASTName) {
				IASTName name= (IASTName) node;
				if (!name.isReference()) {
					IBinding binding= token.getBinding();
					if (binding instanceof ICPPMethod) {
						return true;
					} else if (binding instanceof IProblemBinding) {
						// try to be derive from AST
						node= name.getParent();
						while (node instanceof IASTName) {
							node= node.getParent();
						}
						if (node instanceof ICPPASTFunctionDeclarator) {
							if (name instanceof ICPPASTQualifiedName) {
								ICPPASTQualifiedName qName= (ICPPASTQualifiedName) name;
								ICPPASTNameSpecifier[] qualifier= qName.getQualifier();
								if (qualifier.length > 0) {
									if (qualifier[qualifier.length - 1].resolveBinding() instanceof ICPPClassType) {
										return true;
									}
								}
							} else {
								while (node != token.getRoot() && !(node.getParent() instanceof IASTDeclSpecifier)) {
									node= node.getParent();
								}
								if (node instanceof ICPPASTCompositeTypeSpecifier) {
									return true;
								}
							}
						}
					}
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for static method invocations.
	 */
	private static final class StaticMethodInvocationHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return STATIC_METHOD_INVOCATION;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return RGB_BLACK;
		}

		@Override
		public boolean isBoldByDefault() {
			return false;
		}

		@Override
		public boolean isItalicByDefault() {
			return true;
		}

		@Override
		public boolean isEnabledByDefault() {
			return true;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_staticMethodInvocation;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName) node;
				if (name instanceof ICPPASTQualifiedName) {
					return false;
				}
				if (!name.isReference()) {
					return false;
				}
				IBinding binding= token.getBinding();
				if (binding instanceof ICPPMethod && !(binding instanceof IProblemBinding)) {
					return ((ICPPMethod) binding).isStatic();
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for methods.
	 */
	private static final class MethodHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return METHOD;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return RGB_BLACK;
		}

		@Override
		public boolean isBoldByDefault() {
			return false;
		}

		@Override
		public boolean isItalicByDefault() {
			return false;
		}

		@Override
		public boolean isEnabledByDefault() {
			return false;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_method;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTImplicitName)
				return false;
			if (node instanceof IASTName) {
				IASTName name= (IASTName) node;
				if (name instanceof ICPPASTQualifiedName && name.isReference()) {
					return false;
				}
				IBinding binding= token.getBinding();
				if (binding instanceof ICPPMethod) {
					return true;
				} else if (binding instanceof ICPPDeferredFunction) {
					ICPPFunction[] candidates = ((ICPPDeferredFunction) binding).getCandidates();
					if (candidates != null) {
						for (ICPPFunction candidate : candidates) {
							if (candidate instanceof ICPPMethod) {
								return true;
							}
						}
					}
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for function declarations.
	 */
	private static final class FunctionDeclarationHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return FUNCTION_DECLARATION;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return RGB_BLACK;
		}

		@Override
		public boolean isBoldByDefault() {
			return true;
		}

		@Override
		public boolean isItalicByDefault() {
			return false;
		}

		@Override
		public boolean isEnabledByDefault() {
			return true;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_functionDeclaration;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTImplicitName)
				return false;

			if (node instanceof IASTName) {
				IASTName name= (IASTName) node;
				if (name.isDeclaration()) {
					IBinding binding= token.getBinding();
					if (binding instanceof IFunction && !(binding instanceof ICPPMethod)) {
						return true;
					} else if (binding instanceof IProblemBinding) {
						// try to derive from AST
						if (name instanceof ICPPASTQualifiedName) {
							return false;
						}
						node= name.getParent();
						while (node instanceof IASTName) {
							node= node.getParent();
						}
						if (node instanceof IASTFunctionDeclarator) {
							while (node != token.getRoot() && !(node.getParent() instanceof IASTDeclSpecifier)) {
								node= node.getParent();
							}
							if (node instanceof ICPPASTCompositeTypeSpecifier) {
								return false;
							}
							return true;
						}
					}
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for functions.
	 */
	private static final class FunctionHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return FUNCTION;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return RGB_BLACK;
		}

		@Override
		public boolean isBoldByDefault() {
			return true;
		}

		@Override
		public boolean isItalicByDefault() {
			return false;
		}

		@Override
		public boolean isEnabledByDefault() {
			return false;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_function;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTImplicitName)
				return false;
			if (node instanceof IASTName) {
				IASTName name= (IASTName) node;
				if (name instanceof ICPPASTQualifiedName && name.isReference()) {
					return false;
				}
				IBinding binding= token.getBinding();
				if (binding instanceof IFunction && !(binding instanceof ICPPMethod)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for local variable declarations.
	 */
	private static final class LocalVariableDeclarationHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return LOCAL_VARIABLE_DECLARATION;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return new RGB(128, 0, 0);
		}

		@Override
		public boolean isBoldByDefault() {
			return false;
		}

		@Override
		public boolean isItalicByDefault() {
			return false;
		}

		@Override
		public boolean isEnabledByDefault() {
			return false;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_localVariableDeclaration;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName) node;
				if (name.isDeclaration()) {
					IBinding binding= token.getBinding();
					if (binding instanceof IVariable
							&& !(binding instanceof IField)
							&& !(binding instanceof IParameter)
							&& !(binding instanceof IProblemBinding)) {
						if (LocalVariableHighlighting.isLocalVariable((IVariable) binding)) {
							return true;
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
	private static final class LocalVariableHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return LOCAL_VARIABLE;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return RGB_BLACK;
		}

		@Override
		public boolean isBoldByDefault() {
			return false;
		}

		@Override
		public boolean isItalicByDefault() {
			return false;
		}

		@Override
		public boolean isEnabledByDefault() {
			return false;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_localVariable;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName) node;
				if (name.isReference()) {
					IBinding binding= token.getBinding();
					if (binding instanceof IVariable
							&& !(binding instanceof IField)
							&& !(binding instanceof IParameter)
							&& !(binding instanceof IProblemBinding)) {
						if (isLocalVariable((IVariable) binding)) {
							return true;
						}
					}
				}
			}
			return false;
		}

	    public static boolean isLocalVariable(IVariable variable) {
	    	// A variable marked 'extern' declares a global
	    	// variable even if the declaration is local.
	    	if (variable.isExtern()) {
	    		return false;
	    	}
	    	try {
		    	IScope scope= variable.getScope();
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
	    	} catch (DOMException exc) {
	    		CUIPlugin.log(exc);
	    	}
	    	return false;
	    }
	}

	/**
	 * Semantic highlighting for global variables.
	 */
	private static final class GlobalVariableHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return GLOBAL_VARIABLE;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return RGB_BLACK;
		}

		@Override
		public boolean isBoldByDefault() {
			return false;
		}

		@Override
		public boolean isItalicByDefault() {
			return true;
		}

		@Override
		public boolean isEnabledByDefault() {
			return false;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_globalVariable;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName) node;
				if (name instanceof ICPPASTQualifiedName) {
					return false;
				}
				IBinding binding= token.getBinding();
				if (binding instanceof IVariable
						&& !(binding instanceof IField)
						&& !(binding instanceof IParameter)
						&& !(binding instanceof ICPPTemplateNonTypeParameter)
						&& !(binding instanceof IProblemBinding)) {
					if (!LocalVariableHighlighting.isLocalVariable((IVariable) binding)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for parameter variables.
	 */
	private static final class ParameterVariableHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return PARAMETER_VARIABLE;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return RGB_BLACK;
		}

		@Override
		public boolean isBoldByDefault() {
			return false;
		}

		@Override
		public boolean isItalicByDefault() {
			return false;
		}

		@Override
		public boolean isEnabledByDefault() {
			return false;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_parameterVariable;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IBinding binding= token.getBinding();
			if (binding instanceof IParameter) {
				return true;
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for template parameters.
	 */
	private static final class TemplateParameterHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return TEMPLATE_PARAMETER;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return new RGB(100, 70, 50);
		}

		@Override
		public boolean isBoldByDefault() {
			return true;
		}

		@Override
		public boolean isItalicByDefault() {
			return false;
		}

		@Override
		public boolean isEnabledByDefault() {
			return true;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_templateParameter;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IBinding binding= token.getBinding();
				if (binding instanceof ICPPTemplateParameter) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for classes.
	 */
	private static final class ClassHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return CLASS;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return new RGB(0, 80, 50);
		}

		@Override
		public boolean isBoldByDefault() {
			return false;
		}

		@Override
		public boolean isItalicByDefault() {
			return false;
		}

		@Override
		public boolean isEnabledByDefault() {
			return true;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_classes;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof ICPPASTQualifiedName || node instanceof ICPPASTTemplateId) {
				return false;
			}
			if (node instanceof IASTName) {
				IBinding binding= token.getBinding();
				if (binding instanceof ICompositeType && !(binding instanceof ICPPTemplateParameter)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for enums.
	 */
	private static final class EnumHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return ENUM;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return new RGB(100, 70, 50);
		}

		@Override
		public boolean isBoldByDefault() {
			return false;
		}

		@Override
		public boolean isItalicByDefault() {
			return false;
		}

		@Override
		public boolean isEnabledByDefault() {
			return false;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_enums;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IBinding binding= token.getBinding();
				if (binding instanceof IEnumeration) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for macro references.
	 */
	private static final class MacroReferenceHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return MACRO_REFERENCE;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return RGB_BLACK;
		}

		@Override
		public boolean isBoldByDefault() {
			return false;
		}

		@Override
		public boolean isItalicByDefault() {
			return false;
		}

		@Override
		public boolean isEnabledByDefault() {
			return false;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_macroSubstitution;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IBinding binding= token.getBinding();
			if (binding instanceof IMacroBinding) {
				IASTName name= (IASTName)token.getNode();
				if (name.isReference()) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for macro definitions.
	 */
	private static final class MacroDefinitionHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return MACRO_DEFINITION;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return RGB_BLACK;
		}

		@Override
		public boolean isBoldByDefault() {
			return false;
		}

		@Override
		public boolean isItalicByDefault() {
			return false;
		}

		@Override
		public boolean isEnabledByDefault() {
			return false;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_macroDefintion;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IBinding binding= token.getBinding();
			if (binding instanceof IMacroBinding) {
				IASTName name= (IASTName)token.getNode();
				if (!name.isReference()) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for typedefs.
	 */
	private static final class TypedefHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return TYPEDEF;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return new RGB(0, 80, 50);
		}

		@Override
		public boolean isBoldByDefault() {
			return false;
		}

		@Override
		public boolean isItalicByDefault() {
			return false;
		}

		@Override
		public boolean isEnabledByDefault() {
			return true;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_typeDef;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName) node;
				if (name instanceof ICPPASTQualifiedName) {
					return false;
				}
				IBinding binding= token.getBinding();
				if (binding instanceof ITypedef) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for namespaces.
	 */
	private static final class NamespaceHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return NAMESPACE;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return RGB_BLACK;
		}

		@Override
		public boolean isBoldByDefault() {
			return false;
		}

		@Override
		public boolean isItalicByDefault() {
			return false;
		}

		@Override
		public boolean isEnabledByDefault() {
			return false;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_namespace;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IBinding binding= token.getBinding();
			if (binding instanceof ICPPNamespace) {
				return true;
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for labels.
	 */
	private static final class LabelHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return LABEL;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return RGB_BLACK;
		}

		@Override
		public boolean isBoldByDefault() {
			return false;
		}

		@Override
		public boolean isItalicByDefault() {
			return false;
		}

		@Override
		public boolean isEnabledByDefault() {
			return false;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_label;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IBinding binding= token.getBinding();
			if (binding instanceof ILabel) {
				return true;
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for enumerators.
	 */
	private static final class EnumeratorHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return ENUMERATOR;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return new RGB(0, 0, 192);
		}

		@Override
		public boolean isBoldByDefault() {
			return false;
		}

		@Override
		public boolean isItalicByDefault() {
			return true;
		}

		@Override
		public boolean isEnabledByDefault() {
			return true;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_enumerator;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName) node;
				if (name instanceof ICPPASTQualifiedName) {
					return false;
				}
				IBinding binding= token.getBinding();
				if (binding instanceof IEnumerator) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for problems.
	 */
	private static final class ProblemHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return PROBLEM;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return new RGB(224, 0, 0);
		}

		@Override
		public boolean isBoldByDefault() {
			return true;
		}

		@Override
		public boolean isStrikethroughByDefault() {
			return false;
		}

		@Override
		public boolean isItalicByDefault() {
			return false;
		}

		@Override
		public boolean isEnabledByDefault() {
			return false;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_problem;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IASTNode node= token.getNode();
			if (node == null) {
				return false;
			}
			if (node.getTranslationUnit().isBasedOnIncompleteIndex()) {
				// Do not highlight problems if the AST is unreliable.
				return false;
			}
			if (node instanceof IASTProblem) {
				return true;
			}
			IBinding binding= token.getBinding();
			if (binding instanceof IProblemBinding) {
				IProblemBinding problemBinding = (IProblemBinding) binding;
				if (problemBinding.getID() == IProblemBinding.SEMANTIC_NAME_NOT_FOUND &&
						CharArrayUtils.startsWith(problemBinding.getNameCharArray(), "__builtin_")) { //$NON-NLS-1$
					return false;  // Ignore an unknown built-in.
				}
				return true;
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for external SDK references.
	 */
	private static final class ExternalSDKHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return EXTERNAL_SDK;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return new RGB(100, 40, 128);
		}

		@Override
		public boolean isBoldByDefault() {
			return true;
		}

		@Override
		public boolean isStrikethroughByDefault() {
			return false;
		}

		@Override
		public boolean isItalicByDefault() {
			return false;
		}

		@Override
		public boolean isEnabledByDefault() {
			return true;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_externalSDK;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName) node;
				if (name instanceof ICPPASTQualifiedName) {
					return false;
				}
				if (name instanceof IASTImplicitName) {
					return false;
				}
				if (name.isReference()) {
					IBinding binding= token.getBinding();
					IIndex index= token.getRoot().getIndex();
					return isExternalSDKReference(binding, index);
				}
			}
			return false;
		}

		private boolean isExternalSDKReference(IBinding binding, IIndex index) {
			if (binding instanceof IFunction) {
				try {
					if (binding instanceof IIndexBinding) {
						if (((IIndexBinding) binding).isFileLocal()) {
							return false;
						}
					}
					else if (!(binding instanceof ICExternalBinding)) {
						return false;
					}
					IIndexName[] decls= index.findNames(binding, IIndex.FIND_DECLARATIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
					for (IIndexName decl : decls) {
						IIndexFile indexFile= decl.getFile();
						if (indexFile != null && indexFile.getLocation().getFullPath() != null) {
							return false;
						}
					}
					if (decls.length != 0) {
						return true;
					}
				} catch (CoreException exc) {
					CUIPlugin.log(exc.getStatus());
					return false;
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for functions.
	 */
	private static final class OverloadedOperatorHighlighting extends SemanticHighlightingWithOwnPreference {
		@Override
		public String getPreferenceKey() {
			return OVERLOADED_OPERATOR;
		}

		@Override
		public boolean requiresImplicitNames() {
			return true;
		}

		@Override
		public RGB getDefaultDefaultTextColor() {
			return new RGB(200, 100, 0); // orange
		}

		@Override
		public boolean isBoldByDefault() {
			return false;
		}

		@Override
		public boolean isItalicByDefault() {
			return false;
		}

		@Override
		public boolean isEnabledByDefault() {
			return false;
		}

		@Override
		public String getDisplayName() {
			return CEditorMessages.SemanticHighlighting_overloadedOperators;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IASTNode node = token.getNode();
			// So far we only have implicit names for overloaded operators and destructors,
			// so this works.
			if (node instanceof IASTImplicitName) {
				IASTImplicitName name = (IASTImplicitName) node;
				if (name.isReference() && name.isOperator()) {
					IBinding binding = name.resolveBinding();
					if (binding instanceof ICPPMethod && !(binding instanceof IProblemBinding)
							&& ((ICPPMethod) binding).isImplicit()) {
						return false;
					}
					if (binding instanceof ICPPUnknownBinding)
						return false;
					char[] chars = name.toCharArray();
					if (chars[0] == '~' || OverloadableOperator.isNew(chars)
							|| OverloadableOperator.isDelete(chars)) {
						return false;
					}
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * Semantic highlighting for context-sensitive keywords.
	 * 
	 * This does not get its own color and style; rather, it uses
	 * the color and style of the 'Keyword' syntactic highlighting.
	 */
	private static final class ContextSensitiveKeywordHighlighting extends SemanticHighlighting {
		@Override
		public String getPreferenceKey() {
			return ICColorConstants.C_KEYWORD;
		}
		
		@Override
		public boolean isEnabledByDefault() {
			return true;
		}

		@Override
		public boolean requiresImplicitNames() {
			return false;
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			// Currently the only context-sensitive keywords are the
			// 'final' and 'override' virt-specifiers at the end of a
			// method declaration, and the 'final' class-virt-specifier
			// after a class name.
			return token.getNode() instanceof ICPPASTVirtSpecifier
					|| token.getNode() instanceof ICPPASTClassVirtSpecifier;
		}
	}

	// Note on the get___PreferenceKey() functions below:
	//  - For semantic highlightings deriving from SemanticHighlightingWithOwnPreference,
	//    these functions return keys for accessing the highlighting's own preferences.
	//  - For semantic highlightings not deriving from SemanticHighlightingWithOwnPreference,
	//    their getPreferenceKey() returns the preference key for a corresponding syntactic
	//    highlighting, and these functions build preference keys for specific preferences
	//    (e.g. color) based on that.
	//  - getEnabledPreferenceKey() is special in that there is no corresponding preference
	//    for synactic highlightings (they are always enabled), but we need to allow all
	//    semantic highlightings to be disabled for testing purposes, so we build a preference
	//    key using the naming scheme for semantic preferences for all semantic highlightings.
	//    From a user's perspective, semantic highlightings not deriving from
	//    SemanticHighlightingWithOwnPreference are still always enabled because there is no
	//    way to disable them in the UI.
	
	/**
	 * A named preference that controls the given semantic highlighting's color.
	 *
	 * @param semanticHighlighting the semantic highlighting
	 * @return the color preference key
	 */
	public static String getColorPreferenceKey(SemanticHighlighting semanticHighlighting) {
		if (semanticHighlighting instanceof SemanticHighlightingWithOwnPreference) {
			return PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + semanticHighlighting.getPreferenceKey() + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_COLOR_SUFFIX;
		} else {
			return semanticHighlighting.getPreferenceKey();
		}
	}

	/**
	 * A named preference that controls if the given semantic highlighting has the text attribute bold.
	 *
	 * @param semanticHighlighting the semantic highlighting
	 * @return the bold preference key
	 */
	public static String getBoldPreferenceKey(SemanticHighlighting semanticHighlighting) {
		if (semanticHighlighting instanceof SemanticHighlightingWithOwnPreference) {
			return PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + semanticHighlighting.getPreferenceKey() + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_BOLD_SUFFIX;
		} else {
			return semanticHighlighting.getPreferenceKey() + PreferenceConstants.EDITOR_BOLD_SUFFIX;
		}
	}

	/**
	 * A named preference that controls if the given semantic highlighting has the text attribute italic.
	 *
	 * @param semanticHighlighting the semantic highlighting
	 * @return the italic preference key
	 */
	public static String getItalicPreferenceKey(SemanticHighlighting semanticHighlighting) {
		if (semanticHighlighting instanceof SemanticHighlightingWithOwnPreference) {
			return PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + semanticHighlighting.getPreferenceKey() + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ITALIC_SUFFIX;
		} else {
			return semanticHighlighting.getPreferenceKey() + PreferenceConstants.EDITOR_ITALIC_SUFFIX;
		}
	}

	/**
	 * A named preference that controls if the given semantic highlighting has the text attribute strikethrough.
	 *
	 * @param semanticHighlighting the semantic highlighting
	 * @return the strikethrough preference key
	 */
	public static String getStrikethroughPreferenceKey(SemanticHighlighting semanticHighlighting) {
		if (semanticHighlighting instanceof SemanticHighlightingWithOwnPreference) {			
			return PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + semanticHighlighting.getPreferenceKey() + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_STRIKETHROUGH_SUFFIX;
		} else {
			return semanticHighlighting.getPreferenceKey() + PreferenceConstants.EDITOR_STRIKETHROUGH_SUFFIX;
		}
	}

	/**
	 * A named preference that controls if the given semantic highlighting has the text attribute underline.
	 *
	 * @param semanticHighlighting the semantic highlighting
	 * @return the underline preference key
	 */
	public static String getUnderlinePreferenceKey(SemanticHighlighting semanticHighlighting) {
		if (semanticHighlighting instanceof SemanticHighlightingWithOwnPreference) {
			return PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + semanticHighlighting.getPreferenceKey() + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_UNDERLINE_SUFFIX;
		} else {
			return semanticHighlighting.getPreferenceKey() + PreferenceConstants.EDITOR_UNDERLINE_SUFFIX;
		}
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

	private static class Key implements Comparable<Key> {
		public final int priority;
		public final String id;

		public Key(int priority) {
			this(priority, null);
		}

		public Key(int priority, String id) {
			this.priority = priority;
			this.id = id;
		}

		@Override
		public int compareTo(Key o) {
			if (priority < o.priority)
				return -1;
			if (o.priority < priority)
				return 1;

			if (id == null)
				return o.id == null ? 0 : -1;

			return o.id == null ? 1 : id.compareTo(o.id);
		}

		@Override
		public String toString() {
			StringBuilder str = new StringBuilder();
			str.append(priority);
			if (id != null) {
				str.append(' ');
				str.append(id);
			}

			return str.toString();
		}
	}

	private static void loadBuiltInSemanticHighlightings(Map<Key, SemanticHighlighting> highlightings) {

		highlightings.put(new Key(10), new MacroReferenceHighlighting()); // before all others!
		highlightings.put(new Key(20), new ProblemHighlighting());
		highlightings.put(new Key(30), new ExternalSDKHighlighting());
		highlightings.put(new Key(40), new ClassHighlighting());
		highlightings.put(new Key(50), new StaticFieldHighlighting());
		highlightings.put(new Key(60), new FieldHighlighting()); // after all other fields
		highlightings.put(new Key(70), new MethodDeclarationHighlighting());
		highlightings.put(new Key(80), new StaticMethodInvocationHighlighting());
		highlightings.put(new Key(90), new ParameterVariableHighlighting()); // before local variables
		highlightings.put(new Key(100), new LocalVariableDeclarationHighlighting());
		highlightings.put(new Key(110), new LocalVariableHighlighting());
		highlightings.put(new Key(120), new GlobalVariableHighlighting());
		highlightings.put(new Key(130), new TemplateParameterHighlighting()); // before template arguments!
		highlightings.put(new Key(140), new OverloadedOperatorHighlighting()); // before both method and function
		highlightings.put(new Key(150), new MethodHighlighting()); // before types to get ctors
		highlightings.put(new Key(160), new EnumHighlighting());
		highlightings.put(new Key(170), new MacroDefinitionHighlighting());
		highlightings.put(new Key(180), new FunctionDeclarationHighlighting());
		highlightings.put(new Key(190), new FunctionHighlighting());
		highlightings.put(new Key(200), new TypedefHighlighting());
		highlightings.put(new Key(210), new NamespaceHighlighting());
		highlightings.put(new Key(220), new LabelHighlighting());
		highlightings.put(new Key(230), new EnumeratorHighlighting());
		highlightings.put(new Key(240), new ContextSensitiveKeywordHighlighting());
	}

	private static final String ExtensionPoint = "semanticHighlighting"; //$NON-NLS-1$

	private static SemanticHighlighting[] loadSemanticHighlightings() {

		Map<Key, SemanticHighlighting> highlightings = new TreeMap<SemanticHighlightings.Key, SemanticHighlighting>();

		// load the built-in highlightings
		loadBuiltInSemanticHighlightings(highlightings);

		// load the extensions
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
				CUIPlugin.getPluginId(), ExtensionPoint);
		for (IConfigurationElement element : elements) {
			ContributedSemanticHighlighting contributedHighlighting = new ContributedSemanticHighlighting(
					element);

			Key key = new Key(contributedHighlighting.getPriority(), contributedHighlighting.getId());
			highlightings.put(key, contributedHighlighting);
		}

		return highlightings.values().toArray(new SemanticHighlighting[highlightings.size()]);
	}

	private static final Object SemanticHighlightingsLock = new Object();

	/**
	 * @return The semantic highlightings, the order defines the precedence of matches, the first match wins.
	 */
	public static SemanticHighlighting[] getSemanticHighlightings() {
		if (fgSemanticHighlightings == null)
			synchronized (SemanticHighlightingsLock) {
				if (fgSemanticHighlightings == null)
					fgSemanticHighlightings = loadSemanticHighlightings();
			}
		return fgSemanticHighlightings;
	}

	/**
	 * Initialize default preferences in the given preference store.
	 * @param store The preference store
	 */
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED, true);

		SemanticHighlighting[] semanticHighlightings= getSemanticHighlightings();
		for (SemanticHighlighting semanticHighlighting : semanticHighlightings) {
			store.setDefault(SemanticHighlightings.getEnabledPreferenceKey(semanticHighlighting), DEBUG || semanticHighlighting.isEnabledByDefault());
			if (semanticHighlighting instanceof SemanticHighlightingWithOwnPreference) {
				SemanticHighlightingWithOwnPreference highlighting = (SemanticHighlightingWithOwnPreference) semanticHighlighting;
				PreferenceConverter.setDefault(store, SemanticHighlightings.getColorPreferenceKey(highlighting), highlighting.getDefaultTextColor());
				store.setDefault(SemanticHighlightings.getBoldPreferenceKey(highlighting), highlighting.isBoldByDefault());
				store.setDefault(SemanticHighlightings.getItalicPreferenceKey(highlighting), highlighting.isItalicByDefault());
				store.setDefault(SemanticHighlightings.getStrikethroughPreferenceKey(highlighting), highlighting.isStrikethroughByDefault());
				store.setDefault(SemanticHighlightings.getUnderlinePreferenceKey(highlighting), DEBUG || highlighting.isUnderlineByDefault());
			}
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
		for (SemanticHighlighting highlighting : highlightings) {
			if (event.getProperty().equals(getEnabledPreferenceKey(highlighting))) {
				relevantKey= event.getProperty();
				break;
			}
		}
		if (relevantKey == null)
			return false;

		for (SemanticHighlighting highlighting : highlightings) {
			String key= getEnabledPreferenceKey(highlighting);
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
		for (SemanticHighlighting highlighting : highlightings) {
			String enabledKey= getEnabledPreferenceKey(highlighting);
			if (store.getBoolean(enabledKey)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Do not instantiate
	 */
	private SemanticHighlightings() {
	}
}
