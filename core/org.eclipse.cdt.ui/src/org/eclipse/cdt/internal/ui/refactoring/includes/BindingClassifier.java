/*******************************************************************************
 * Copyright (c) 2012, 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *	   Mathias Kunter
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.includes;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor.STD;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.ALLCVQ;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.ARRAY;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.PTR;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getNestedType;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClosureType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper.MethodKind;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.LookupData;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.core.runtime.CoreException;

/**
 * For a whole translation unit or a part of it determines a set of externally defined bindings that
 * must be defined and a set of bindings that must be declared.
 */
public class BindingClassifier {
	private static final String[] TEMPLATES_ALLOWING_INCOMPLETE_ARGUMENT_TYPE = {
			// Please keep alphabetical order.
			"enable_shared_from_this", // 20.7.2.4 //$NON-NLS-1$
			"declval", // 20.2.4 //$NON-NLS-1$
			"default_delete", // 20.7.1.1 //$NON-NLS-1$
			"shared_ptr", // 20.7.2.2 //$NON-NLS-1$
			"unique_ptr", // 20.7.1 //$NON-NLS-1$
			"weak_ptr" // 20.7.2.3 //$NON-NLS-1$
	};

	private class BindingCollector extends ASTVisitor {
		BindingCollector() {
			super(true);
		}

		@Override
		public int leave(IASTDeclaration declaration) {
			if (isPartOfExternalMacroDefinition(declaration))
				return PROCESS_CONTINUE;

			if (declaration instanceof IASTSimpleDeclaration) {
				/*
				 * The type specifier of a simple declaration of a variable must always be defined
				 * except within the situations shown by the following examples:
				 *
				 * Example 1:
				 * 	X* x;					// definition of X is not required here - pointer type
				 *
				 * Example 2:
				 * 	X& x;					// definition of X is not required here - reference type
				 *
				 * The type specifier of a simple function declaration also doesn't need to be
				 * defined:
				 *
				 * Example 3:
				 * 	X foo();				// definition of X is not required here
				 *
				 * The type specifier of static member declarations also doesn't need to be defined:
				 *
				 * Example 4:
				 * 	class Y {
				 * 		static X x;			// definition of X is not required here
				 * 	};
				 */
				IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
				IASTDeclSpecifier declSpecifier = simpleDeclaration.getDeclSpecifier();
				IASTDeclarator[] declarators = simpleDeclaration.getDeclarators();

				if (declSpecifier instanceof IASTNamedTypeSpecifier) {
					// We only handle simple declarations here whose declaration specifiers are
					// named type specifiers.
					boolean staticMember = simpleDeclaration.getParent() instanceof IASTCompositeTypeSpecifier
							&& declSpecifier.getStorageClass() == IASTDeclSpecifier.sc_static;

					// Declare the named type specifier if all declarators are either pointers,
					// references or functions.
					boolean canBeDeclared = true;
					if (!staticMember) {
						for (IASTDeclarator declarator : declarators) {
							if (!(declarator instanceof IASTFunctionDeclarator)
									&& declarator.getPointerOperators().length == 0) {
								canBeDeclared = false;
								break;
							}
						}
					}

					if (!canBeDeclared) {
						IASTName name = ((IASTNamedTypeSpecifier) declSpecifier).getName();
						defineBindingForName(name);
					}
				}
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int leave(IASTDeclarator declarator) {
			if (isPartOfExternalMacroDefinition(declarator))
				return PROCESS_CONTINUE;

			if (declarator instanceof IASTFunctionDeclarator) {
				/*
				 * The type specifier of a function definition doesn't need to be defined if it is
				 * a pointer or a reference type.
				 *
				 * Example 1:
				 * 	X *foo() { }		// definition of X is not required here
				 *
				 * Example 2:
				 * 	X& foo() { }		// definition of X is not required here
				 */
				IASTName name = declarator.getPropertyInParent() == ICPPASTLambdaExpression.DECLARATOR
						? ((ICPPASTLambdaExpression) declarator.getParent()).getFunctionCallOperatorName()
						: declarator.getName();
				IBinding binding = name.resolveBinding();
				if (binding instanceof IFunction) {
					IFunction function = (IFunction) binding;

					IFunctionType functionType = function.getType();
					try {
						CPPSemantics.pushLookupPoint(declarator);
						if (declarator.getPropertyInParent() == IASTFunctionDefinition.DECLARATOR
								|| declarator.getPropertyInParent() == ICPPASTLambdaExpression.DECLARATOR) {
							// Define the return type if necessary.
							IType returnType = functionType.getReturnType();
							if (!(returnType instanceof IPointerType || returnType instanceof ICPPReferenceType)) {
								defineTypeExceptTypedefOrNonFixedEnum(returnType);
							}

							// Define parameter types if necessary.
							IType[] parameterTypes = functionType.getParameterTypes();
							for (IType type : parameterTypes) {
								if (!(type instanceof IPointerType)) {
									if (!(type instanceof ICPPReferenceType) || isTypeWithConvertingConstructor(type)) {
										defineTypeExceptTypedefOrNonFixedEnum(type);
									}
								}
							}
						} else {
							// As a matter of policy, a function declaration is responsible for
							// providing definitions of parameter types that have implicit
							// converting constructors.
							IType[] parameterTypes = functionType.getParameterTypes();
							for (IType type : parameterTypes) {
								if (!(type instanceof IPointerType)) {
									if (isTypeWithConvertingConstructor(type)) {
										defineTypeExceptTypedefOrNonFixedEnum(type);
									}
								}
							}
						}
					} finally {
						CPPSemantics.popLookupPoint();
					}
				}
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int leave(IASTDeclSpecifier declSpec) {
			if (isPartOfExternalMacroDefinition(declSpec))
				return PROCESS_CONTINUE;

			if (declSpec instanceof IASTElaboratedTypeSpecifier) {
				/*
				 * The type specifier of an elaborated type neither needs to be defined nor needs to be
				 * declared. This is because an elaborated type specifier is a self-sufficient
				 * statement.
				 *
				 * Example:
				 * 	class X;			// neither definition nor declaration of X is required here
				 */
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int leave(ICPPASTBaseSpecifier baseSpecifier) {
			if (isPartOfExternalMacroDefinition(baseSpecifier))
				return PROCESS_CONTINUE;

			/*
			 * The type of a base specifier must always be defined.
			 *
			 * Example:
			 * 	class Y : X {};			// definition of X is required here
			 */
			defineBinding(baseSpecifier.getNameSpecifier().resolveBinding());
			return PROCESS_CONTINUE;
		}

		@Override
		public int leave(IASTInitializer initializer) {
			if (isPartOfExternalMacroDefinition(initializer))
				return PROCESS_CONTINUE;

			/*
			 * The type of the member of a constructor chain initializer doesn't need to be defined
			 * if it is a pointer or reference type and if the argument type matches.
			 *
			 * Example 1:
			 * 	class X {
			 * 		X* x1;
			 * 		X(X* x2) :
			 * 			x1(x2) {}			// definition of typeof(x1) is not required here
			 * 	};
			 *
			 * Example 2:
			 * 	class X {
			 * 		X& x1;
			 * 		X(X& x2) :
			 * 			x1(x2) {}			// definition of typeof(x1) is not required here
			 * 	};
			 *
			 * The type of a constructor initializer doesn't need to be defined if it matches with
			 * the type of the declaration.
			 *
			 * Example:
			 * 	void foo(X& x1) {
			 * 		X& x2(x1);				// definition of typeof(x1) is not required here
			 * 	}
			 *
			 * The type of an equals initializer doesn't need to be defined if it matches with
			 * the type of the declaration.
			 *
			 * Example:
			 * 	void foo(X& x1) {
			 * 		X& x2 = x1;				// definition of typeof(x1) is not required here
			 * 	}
			 */

			// Get the binding of the initialized AST name first.
			IASTNode variableNode = initializer;
			IASTName variableName = null;
			IBinding variableBinding = null;

			while (variableNode != null) {
				if (variableNode instanceof IASTDeclarator) {
					variableName = ((IASTDeclarator) variableNode).getName();
					break;
				} else if (variableNode instanceof ICPPASTConstructorChainInitializer) {
					variableName = ((ICPPASTConstructorChainInitializer) variableNode).getMemberInitializerId();
					break;
				}
				variableNode = variableNode.getParent();
			}

			if (variableName != null)
				variableBinding = variableName.resolveBinding();

			// Get the arguments of the initializer.
			IASTInitializerClause[] arguments = IASTExpression.EMPTY_EXPRESSION_ARRAY;
			if (initializer instanceof ICPPASTConstructorInitializer) {
				ICPPASTConstructorInitializer constructorInitializer = (ICPPASTConstructorInitializer) initializer;
				arguments = constructorInitializer.getArguments();
			} else if (initializer instanceof IASTEqualsInitializer) {
				IASTEqualsInitializer equalsInitializer = (IASTEqualsInitializer) initializer;
				arguments = new IASTInitializerClause[] { equalsInitializer.getInitializerClause() };
			}

			if (variableBinding instanceof IVariable) {
				// Variable construction.
				boolean defineVariableType = true;
				if (initializer.getPropertyInParent() == IASTDeclarator.INITIALIZER) {
					IASTDeclarator declarator = (IASTDeclarator) initializer.getParent();
					IASTDeclSpecifier declSpec = getDeclarationSpecifier(declarator);
					if (declSpec != null && isPartOfExternalMacroDefinition(declSpec))
						defineVariableType = false;
				}
				IType targetType = ((IVariable) variableBinding).getType();
				if (!(targetType instanceof IPointerType || targetType instanceof ICPPReferenceType)) {
					if (defineVariableType) {
						// We're constructing a non-pointer type. We need to define the member type
						// either way since we must be able to call its constructor.
						defineTypeExceptTypedefOrNonFixedEnum(targetType);
					}

					// TODO: Process the arguments. But how to get the corresponding IParameter[] array here?
					// processParameters(declaredParameters, arguments);
				} else {
					// We're constructing a pointer type. No constructor is called. We however have
					// to check whether the argument type matches the declared type.
					for (IASTInitializerClause argument : arguments) {
						if (argument instanceof IASTExpression) {
							IASTExpression expression = (IASTExpression) argument;
							IType argumentType = expression.getExpressionType();
							if (targetType instanceof ICPPReferenceType && expression instanceof IASTUnaryExpression
									&& ((IASTUnaryExpression) expression)
											.getOperator() == IASTUnaryExpression.op_star) {
								argumentType = new CPPReferenceType(argumentType, false);
							}
							if (isTypeDefinitionRequiredForConversion(argumentType, targetType)) {
								// Types don't match. Define both types.
								if (defineVariableType)
									defineTypeExceptTypedefOrNonFixedEnum(targetType);
								defineTypeExceptTypedefOrNonFixedEnum(argumentType);
							}
						}
					}
				}
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int leave(IASTStatement statement) {
			if (isPartOfExternalMacroDefinition(statement))
				return PROCESS_CONTINUE;

			if (statement instanceof IASTReturnStatement) {
				/*
				 * The type of the return value expression doesn't need to be defined if the actual
				 * return type matches the declared return type (i.e. if no implicit conversion is
				 * necessary).
				 *
				 * Example:
				 * 	X& foo(X& x) {
				 * 		return x;				// definition of typeof(x) is not required here
				 * 	}
				 */
				IASTReturnStatement returnStatement = (IASTReturnStatement) statement;

				IASTExpression returnValue = returnStatement.getReturnValue();
				if (returnValue != null) {
					// Get the containing function definition.
					IASTNode functionDefinitionNode = returnStatement;
					while (functionDefinitionNode != null
							&& !(functionDefinitionNode instanceof IASTFunctionDefinition)) {
						functionDefinitionNode = functionDefinitionNode.getParent();
					}

					// Check whether the declared return type matches the actual return type.
					if (functionDefinitionNode != null) {
						IASTFunctionDefinition functionDefinition = (IASTFunctionDefinition) functionDefinitionNode;
						IASTFunctionDeclarator functionDeclarator = functionDefinition.getDeclarator();
						if (functionDeclarator != null) {
							IBinding binding = functionDeclarator.getName().resolveBinding();
							if (binding instanceof IFunction) {
								IFunction function = (IFunction) binding;

								// Get the declared return type and the actual expression type.
								IType returnType = function.getType().getReturnType();
								IType returnValueType = returnValue.getExpressionType();
								if (isTypeDefinitionRequiredForConversion(returnValueType, returnType)) {
									// Both types have to be defined for conversion.
									defineTypeExceptTypedefOrNonFixedEnum(returnType);
									defineTypeExceptTypedefOrNonFixedEnum(returnValueType);
								}
							}
						}
					}
				}
			} else if (statement instanceof ICPPASTCatchHandler) {
				/*
				 * Catch handles always need the definition of the caught type, even if it's a pointer
				 * or reference type.
				 *
				 * Example:
				 * 	void foo() {
				 * 		try {
				 *      } catch (X& x) {		// Definition of X is required here
				 *      }
				 * 	}
				 */
				ICPPASTCatchHandler catchHandler = (ICPPASTCatchHandler) statement;
				IASTDeclaration declaration = catchHandler.getDeclaration();
				if (declaration instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
					IASTDeclSpecifier declSpecifier = simpleDeclaration.getDeclSpecifier();
					if (declSpecifier instanceof IASTNamedTypeSpecifier) {
						IASTNamedTypeSpecifier namedTypeSpecifier = (IASTNamedTypeSpecifier) declSpecifier;
						defineBindingForName(namedTypeSpecifier.getName());
					}
				}
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int leave(IASTExpression expression) {
			if (isPartOfExternalMacroDefinition(expression))
				return PROCESS_CONTINUE;

			try {
				CPPSemantics.pushLookupPoint(expression);
				ASTNodeProperty propertyInParent = expression.getPropertyInParent();
				if (propertyInParent == IASTIfStatement.CONDITION || propertyInParent == IASTForStatement.CONDITION
						|| propertyInParent == IASTWhileStatement.CONDITIONEXPRESSION
						|| propertyInParent == IASTDoStatement.CONDITION
						|| propertyInParent == IASTConditionalExpression.LOGICAL_CONDITION) {
					/*
					 * The type of the condition expression doesn't need to be defined if it's
					 * a pointer type.
					 *
					 * Example:
					 * 	void foo(X* x) {
					 * 		if (x) { }				// definition of typeof(x) is not required here
					 * 	}
					 */
					IType conditionExpressionType = expression.getExpressionType();
					if (!(conditionExpressionType instanceof IPointerType)) {
						defineTypeExceptTypedefOrNonFixedEnum(conditionExpressionType);
					}
				}

				if (expression instanceof IASTIdExpression) {
					/*
					 * The type of an identifier expression doesn't need to be defined if it's a pointer
					 * or a reference type.
					 *
					 * Example:
					 * 	void foo(X& x) {
					 * 		x;				// definition of typeof(x) is not required here
					 * 	}
					 */
					IASTIdExpression idExpression = (IASTIdExpression) expression;

					IBinding binding = idExpression.getName().resolveBinding();
					if (binding instanceof IVariable) {
						// Get the declared type.
						IType variableType = ((IVariable) binding).getType();
						defineTypeForBinding(binding, variableType);
					}
				} else if (expression instanceof IASTUnaryExpression) {
					/*
					 * The type of the operand of an unary expression doesn't need to be defined if
					 * the operator is the ampersand operator:
					 *
					 * Example:
					 * 	void foo(X& x) {
					 * 		&x;						// ampersand operator
					 * 	}
					 *
					 * If the operand is a pointer type, the following operators also don't require
					 * a definition:
					 *
					 * Example:
					 * 	void foo(X* x) {
					 * 		__alignof(x);			// alignof operator
					 * 		!x;						// not operator
					 * 		+x;						// unary plus operator
					 * 		sizeof(x);				// sizeof operator
					 * 		typeid(x);				// typeid operator
					 * 	}
					 */
					IASTUnaryExpression unaryExpression = (IASTUnaryExpression) expression;
					IASTExpression operand = unaryExpression.getOperand();
					if (operand != null) { // A throw expression may have no operand.
						if (unaryExpression instanceof ICPPASTUnaryExpression) {
							ICPPFunction overload = ((ICPPASTUnaryExpression) unaryExpression).getOverload();
							if (overload != null) {
								defineForFunctionCall(overload, true, new IASTInitializerClause[] { operand });
								return PROCESS_CONTINUE;
							}
						}

						boolean expressionDefinitionRequired = true;
						switch (unaryExpression.getOperator()) {
						case IASTUnaryExpression.op_amper:
						case IASTUnaryExpression.op_bracketedPrimary:
							// The ampersand operator as well as brackets never require a definition.
							expressionDefinitionRequired = false;
							break;
						case IASTUnaryExpression.op_star:
							if (expression.getParent() instanceof IASTExpression)
								break;
							//$FALL-THROUGH$
						case IASTUnaryExpression.op_alignOf:
						case IASTUnaryExpression.op_not:
						case IASTUnaryExpression.op_plus:
						case IASTUnaryExpression.op_sizeof:
						case IASTUnaryExpression.op_typeid:
							// If the operand is a pointer type, then it doesn't need to be defined.
							if (operand.getExpressionType() instanceof IPointerType) {
								expressionDefinitionRequired = false;
							}
							break;
						}

						if (expressionDefinitionRequired) {
							defineTypeExceptTypedefOrNonFixedEnum(operand.getExpressionType());
						}
					}
				} else if (expression instanceof IASTBinaryExpression) {
					/*
					 * The types of the operands of a binary expression don't need to be defined for
					 * the following operators if the operands are pointer types:
					 *
					 * Example:
					 * 	void foo(X* x) {
					 * 		x = x;			// assignment operator
					 * 		x == x;			// equals operator
					 * 		x != x;			// not equals operator
					 * 		x >= x;			// greater equal operator
					 * 		x > x;			// greater operator
					 * 		x <= x;			// less equal operator
					 * 		x < x;			// less operator
					 * 		x && x;			// logical and operator
					 * 		x || x;			// logical or operator
					 * 	}
					 *
					 * However, note that if both operands are pointers of different types, then only
					 * the following operators don't require a definition of the types of the operands:
					 *
					 * 	void foo(X* x, Y* y) {
					 * 		x && y;			// logical and operator
					 * 		x || y;			// logical or operator
					 * 	}
					 */
					IASTBinaryExpression binaryExpression = (IASTBinaryExpression) expression;
					if (binaryExpression instanceof ICPPASTBinaryExpression) {
						ICPPFunction overload = ((ICPPASTBinaryExpression) binaryExpression).getOverload();
						if (overload != null) {
							IASTInitializerClause[] arguments = new IASTInitializerClause[] {
									binaryExpression.getOperand1(), binaryExpression.getOperand2() };
							defineForFunctionCall(overload, true, arguments);
							return PROCESS_CONTINUE;
						}
					}

					IType operand1Type = binaryExpression.getOperand1().getExpressionType();
					IASTInitializerClause operand2 = binaryExpression.getInitOperand2();
					IType operand2Type;
					if (operand2 instanceof IASTExpression) {
						operand2Type = ((IASTExpression) operand2).getExpressionType();
					} else if (operand2 instanceof ICPPASTInitializerList) {
						ICPPASTInitializerList initializerList = (ICPPASTInitializerList) operand2;
						if (binaryExpression.getOperator() == IASTBinaryExpression.op_assign
								&& initializerList.getSize() == 1) {
							IASTInitializerClause element = initializerList.getClauses()[0];
							if (element instanceof IASTExpression) {
								operand2Type = ((IASTExpression) element).getExpressionType();
							} else {
								operand2Type = initializerList.getEvaluation().getType();
							}
						} else {
							operand2Type = initializerList.getEvaluation().getType();
						}
					} else {
						operand2Type = operand1Type;
					}

					boolean expression1DefinitionRequired = true;
					boolean expression2DefinitionRequired = true;

					switch (binaryExpression.getOperator()) {
					case IASTBinaryExpression.op_logicalAnd:
					case IASTBinaryExpression.op_logicalOr:
						// Pointer types don't need to be defined for logical operations.
						if (operand1Type instanceof IPointerType) {
							expression1DefinitionRequired = false;
						}
						if (operand2Type instanceof IPointerType) {
							expression2DefinitionRequired = false;
						}
						break;
					case IASTBinaryExpression.op_assign:
					case IASTBinaryExpression.op_equals:
					case IASTBinaryExpression.op_notequals:
					case IASTBinaryExpression.op_greaterEqual:
					case IASTBinaryExpression.op_greaterThan:
					case IASTBinaryExpression.op_lessEqual:
					case IASTBinaryExpression.op_lessThan:
						// If both operands are identical pointer types, then they don't need to be defined.
						if (operand1Type instanceof IPointerType && operand2Type instanceof IPointerType) {
							if (!isTypeDefinitionRequiredForConversion(operand2Type, operand1Type)) {
								expression1DefinitionRequired = false;
								expression2DefinitionRequired = false;
							}
						} else if (operand1Type instanceof IPointerType) {
							// Only the first operand is a pointer type. It doesn't have to be defined.
							expression1DefinitionRequired = false;
						} else if (operand2Type instanceof IPointerType) {
							// Only the second operand is a pointer type. It doesn't have to be defined.
							expression2DefinitionRequired = false;
						}
					}

					if (expression1DefinitionRequired) {
						defineTypeExceptTypedefOrNonFixedEnum(operand1Type);
					}
					if (expression2DefinitionRequired) {
						defineTypeExceptTypedefOrNonFixedEnum(operand2Type);
					}
				} else if (expression instanceof IASTFunctionCallExpression) {
					/*
					 * The return type and argument types of a function call expression don't need to be
					 * defined if they're pointer or reference types. The declared and actual types of
					 * the arguments must further be identical, since implicit type conversions require
					 * a definition of both the source and the target type.
					 *
					 * Example:
					 * 	X& foo(X& x);
					 * 	void bar(X& x) {
					 * 		foo(x);			// definition of typeof(foo) and typeof(x) is not required here
					 * 	}
					 *
					 * Also note that the function call itself doesn't require a definition as long as
					 * it's not a constructor call:
					 *
					 * Example 1:
					 * 	void foo() {
					 * 		bar();		// definition of bar() is not required here (assuming bar is a function)
					 * 	}
					 *
					 * Example 2:
					 * 	void foo() {
					 * 		X();		// definition of X is required here (assuming X is a composite type)
					 * 	}
					 */
					IASTFunctionCallExpression functionCallExpression = (IASTFunctionCallExpression) expression;
					IASTExpression functionNameExpression = functionCallExpression.getFunctionNameExpression();
					if (isPartOfExternalMacroDefinition(functionNameExpression))
						return PROCESS_CONTINUE;

					IASTInitializerClause[] arguments = functionCallExpression.getArguments();
					IASTName functionName = getNameOfIdOrFieldReferenceExpression(functionNameExpression);
					if (functionName != null) {
						IBinding function = functionName.resolveBinding();
						if (function instanceof IProblemBinding) {
							IBinding[] candidates = ((IProblemBinding) function).getCandidateBindings();
							if (candidates.length != 0) {
								for (IBinding candidate : candidates) {
									defineBindingForFunctionCall(candidate, true, arguments);
								}
							} else {
								defineBinding(function);
							}
						} else {
							IASTName name = functionName;
							if (functionName instanceof ICPPASTTemplateId) {
								name = ((ICPPASTTemplateId) functionName).getTemplateName();
							}
							boolean defineFunction = !isPartOfExternalMacroDefinition(name);

							if (defineFunction) {
								LookupData data = new LookupData(functionName);
								IType impliedObjectType = data.getImpliedObjectType();
								if (impliedObjectType != null)
									defineTypeExceptTypedefOrNonFixedEnum(impliedObjectType);
							}

							defineBindingForFunctionCall(function, defineFunction, arguments);
						}
					}

					if (functionCallExpression instanceof IASTImplicitNameOwner) {
						IASTImplicitName[] implicitNames = ((IASTImplicitNameOwner) functionCallExpression)
								.getImplicitNames();
						for (IASTName name : implicitNames) {
							IBinding binding = name.resolveBinding();
							if (binding instanceof IFunction) {
								defineForFunctionCall((IFunction) binding, true, arguments);
							}
						}
					}
				} else if (expression instanceof IASTFieldReference) {
					/*
					 * The type of the expression part of a field reference always requires a definition.
					 *
					 * Example:
					 * 	void foo(X& x1, X* x2) {
					 * 		x1.bar();			// definition of typeof(x1) is required here
					 * 		x2->bar();			// definition of typeof(x2) is required here
					 * 	}
					 */

					IASTExpression fieldOwner = ((IASTFieldReference) expression).getFieldOwner();
					IType expressionType = fieldOwner.getExpressionType();
					defineIndirectTypes(expressionType);
					IASTName name = getNameOfIdOrFieldReferenceExpression(fieldOwner);
					if (name != null) {
						IBinding binding = name.resolveBinding();
						defineTypeForBinding(binding, expressionType);
					}
				} else if (expression instanceof ICPPASTNewExpression) {
					/*
					 * The type specifier of a "new" expression always requires a definition.
					 *
					 * Example:
					 * 	void foo() {
					 * 		new X();			// definition of X is required here
					 * 	}
					 */
					defineTypeExceptTypedefOrNonFixedEnum(((ICPPASTNewExpression) expression).getExpressionType());
				} else if (expression instanceof ICPPASTDeleteExpression) {
					/*
					 * The expression type of a "delete" expression always requires a full definition.
					 * This is necessary because the compiler needs to be able to call the destructor.
					 *
					 * Example:
					 * 	void foo(X* x) {
					 * 		delete x;			// definition of typeof(x) is required here
					 * 	}
					 */
					defineTypeExceptTypedefOrNonFixedEnum(
							((ICPPASTDeleteExpression) expression).getOperand().getExpressionType());
				} else if (expression instanceof IASTCastExpression) {
					/*
					 * Explicit type casts always need the definition of the underlying types.
					 *
					 * Example:
					 * 	void foo(X* x) {
					 * 		(Y*) x;				// definition of both Y and typeof(x) is required here
					 * 	}
					 */
					IASTCastExpression castExpression = (IASTCastExpression) expression;
					IType targetType = castExpression.getExpressionType();
					IType sourceType = castExpression.getOperand().getExpressionType();

					if (isTypeDefinitionRequiredForConversion(sourceType, targetType)) {
						// Source and target types of the cast expression are different.
						// We need to define both types, even if they're pointers.
						defineTypeExceptTypedefOrNonFixedEnum(targetType);
						defineTypeExceptTypedefOrNonFixedEnum(sourceType);
					} else if (!(targetType instanceof IPointerType || targetType instanceof ICPPReferenceType)) {
						// Define the target type if it's not a pointer or reference type.
						defineTypeExceptTypedefOrNonFixedEnum(targetType);
					}
				}
				return PROCESS_CONTINUE;
			} finally {
				CPPSemantics.popLookupPoint();
			}
		}

		protected void defineBindingForFunctionCall(IBinding binding, boolean defineFunction,
				IASTInitializerClause[] arguments) {
			if (binding instanceof IFunction) {
				defineForFunctionCall((IFunction) binding, defineFunction, arguments);
			} else if (defineFunction) {
				if (binding instanceof ICPPMember) {
					try {
						IType memberType = ((ICPPMember) binding).getType();
						defineIndirectTypes(memberType);
					} catch (DOMException e) {
					}
				} else if (binding instanceof ITypedef) {
					defineBinding(binding);
				}
			}
		}

		@Override
		public int leave(IASTName name) {
			if (name instanceof ICPPASTQualifiedName || name instanceof ICPPASTTemplateId) {
				return PROCESS_CONTINUE;
			}
			if (isPartOfExternalMacroDefinition(name))
				return PROCESS_CONTINUE;

			// Add the binding associated with the name to the bindings that can be declared
			// (we assume that all bindings which have to be defined are already explicitly handled
			// elsewhere).
			if (name instanceof ICPPASTQualifiedName) {
				// All qualifying names must be defined.
				ICPPASTNameSpecifier[] qualifier = ((ICPPASTQualifiedName) name).getQualifier();
				for (ICPPASTNameSpecifier nameSpec : qualifier) {
					defineBinding(nameSpec.resolveBinding());
				}
			}

			IBinding binding = name.resolveBinding();
			if (binding != null) {
				if (isTemplateArgumentRequiringCompleteType(name)) {
					// The name is part of a template argument - define the corresponding binding.
					defineBinding(binding);
				} else if (name.getPropertyInParent() != IASTFieldReference.FIELD_NAME) { // Field references are handled separately.
					IBinding owner = binding.getOwner();
					if (owner instanceof IType) {
						defineBinding(owner); // Member access requires definition of the containing type.
						if (binding instanceof IProblemBinding)
							declareBinding(binding);
						if (!isDefinedLocally(owner)
								&& (!(binding instanceof ICPPMember) || !((ICPPMember) binding).isStatic())) {
							// Record the fact that the header file defining the owner must also
							// provide a definition of the type of this member (bug 442841).
							IType type = null;
							if (binding instanceof IVariable) {
								type = ((IVariable) binding).getType();
							} else if (binding instanceof IFunction) {
								type = ((IFunction) binding).getType().getReturnType();
							}
							type = getNestedType(type, ALLCVQ);
							if (type instanceof IBinding) {
								markAsDefined((IBinding) type);
							}
						}
					} else { // ID expressions are handled separately.
						declareBinding(binding); // Declare the binding of this name.
					}
				}

				if (binding instanceof ICPPTemplateInstance
						&& !((ICPPTemplateInstance) binding).isExplicitSpecialization()
						&& isDeclaredLocally(((ICPPTemplateInstance) binding).getSpecializedBinding())) {
					fInstancesOfLocallyDefinedTemplates.add((ICPPTemplateInstance) binding);
				}
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int leave(IASTTranslationUnit tu) {
			for (IASTPreprocessorMacroExpansion macroExpansion : tu.getMacroExpansions()) {
				IASTPreprocessorMacroDefinition macroDefinition = macroExpansion.getMacroDefinition();
				IASTName name = macroDefinition.getName();
				IMacroBinding macroBinding = (IMacroBinding) name.getBinding();
				// Ignore trivial macros like '#define false false'
				if (!CharArrayUtils.equals(name.getSimpleID(), macroBinding.getExpansion())) {
					defineBinding(macroBinding);
				}
			}
			return PROCESS_CONTINUE;
		}
	}

	private static class LocalNameFinder extends ASTVisitor {
		boolean found;

		LocalNameFinder() {
			shouldVisitNames = true;
		}

		@Override
		public int visit(IASTName name) {
			if (!(name instanceof ICPPASTQualifiedName)) {
				IASTImageLocation imageLocation = name.getImageLocation();
				if (imageLocation != null
						&& imageLocation.getFileName().equals(name.getTranslationUnit().getFilePath())) {
					found = true;
					return PROCESS_ABORT;
				}
			}
			return PROCESS_CONTINUE;
		}
	}

	private final IncludeCreationContext fContext;
	private final IncludePreferences fPreferences;
	/** The bindings which require a full definition. */
	private final Set<IBinding> fBindingsToDefine = new HashSet<>();
	/** The bindings which only require a simple forward declaration. */
	private final Set<IBinding> fBindingsToForwardDeclare = new HashSet<>();
	/** The AST that the classifier is working on. */
	private IASTTranslationUnit fAst;
	private final BindingCollector fBindingCollector = new BindingCollector();
	private final Set<IBinding> fProcessedDefinedBindings = new HashSet<>();
	private final Set<IBinding> fProcessedDeclaredBindings = new HashSet<>();
	public final Set<ICPPTemplateInstance> fInstancesOfLocallyDefinedTemplates = new HashSet<>();

	/**
	 * @param context the context for binding classification
	 */
	public BindingClassifier(IncludeCreationContext context) {
		fContext = context;
		fPreferences = context.getPreferences();
	}

	public void classifyNodeContents(IASTNode node) {
		if (fAst == null) {
			fAst = node.getTranslationUnit();
		}
		try {
			CPPSemantics.pushLookupPoint(fAst);
			// Enable promiscuous binding resolution for this AST traversal,
			// to allow names to be resolved even if the declarations of their
			// target bindings are in a header not reachable via includes.
			CPPSemantics.enablePromiscuousBindingResolution();
			node.accept(fBindingCollector);
			postprocessTemplates();
		} finally {
			CPPSemantics.disablePromiscuousBindingResolution();
			CPPSemantics.popLookupPoint();
		}
	}

	private void postprocessTemplates() {
		Set<ICPPTemplateParameter> templatearametersRequiringDefinition = new HashSet<>();
		for (Iterator<IBinding> it = fBindingsToDefine.iterator(); it.hasNext();) {
			IBinding binding = it.next();
			if (binding instanceof ICPPTemplateParameter) {
				templatearametersRequiringDefinition.add((ICPPTemplateParameter) binding);
				it.remove();
			}
		}
		if (templatearametersRequiringDefinition.isEmpty())
			return;

		for (ICPPTemplateInstance instance : fInstancesOfLocallyDefinedTemplates) {
			ICPPTemplateParameterMap parameterMap = instance.getTemplateParameterMap();
			ICPPTemplateParameter[] params = instance.getTemplateDefinition().getTemplateParameters();
			for (ICPPTemplateParameter param : params) {
				if (templatearametersRequiringDefinition.contains(param)) {
					ICPPTemplateArgument argument = parameterMap.getArgument(param);
					if (argument != null) {
						defineTemplateArgument(argument);
					} else {
						for (ICPPTemplateArgument arg : parameterMap.getPackExpansion(param)) {
							defineTemplateArgument(arg);
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the bindings which require a full definition.
	 */
	public Set<IBinding> getBindingsToDefine() {
		return fBindingsToDefine;
	}

	/**
	 * Returns the bindings which only require a simple forward declaration.
	 */
	public Set<IBinding> getBindingsToForwardDeclare() {
		return fBindingsToForwardDeclare;
	}

	/**
	 * Defines the required types of the parameters of a function or constructor call expression by
	 * comparing the declared parameters with the actual arguments.
	 */
	private void processFunctionParameters(IFunction function, boolean defineFunction,
			IASTInitializerClause[] arguments) {
		boolean functionIsDeclared = fProcessedDefinedBindings.contains(function);
		IParameter[] parameters = function.getParameters();
		for (int i = 0; i < parameters.length && i < arguments.length; i++) {
			IType parameterType = parameters[i].getType();
			IASTInitializerClause argument = arguments[i];
			if (argument instanceof IASTExpression) {
				IType argumentType = ((IASTExpression) argument).getExpressionType();
				if (!isTypeDefinitionRequiredForConversion(argumentType, parameterType)) {
					// A declaration is sufficient if the argument type matches the parameter type.
					// We don't need to provide a declaration of the parameter type since it is
					// a responsibility of the header declaring the function.
					if (!functionIsDeclared && defineFunction) {
						declareType(parameterType);
					}
					continue;
				}

				// The type of the argument requires a full definition.
				defineTypeExceptTypedefOrNonFixedEnum(argumentType);
			}
			if (defineFunction) {
				// As a matter of policy, a header declaring the function is responsible for
				// defining parameter types that allow implicit conversion.
				parameterType = getNestedType(parameterType, REF | ALLCVQ);
				if (!(parameterType instanceof ICPPClassType) || fAst.getDeclarationsInAST(function).length != 0
						|| !hasConvertingConstructor((ICPPClassType) parameterType, argument)) {
					defineTypeExceptTypedefOrNonFixedEnum(parameterType);
				} else if (!functionIsDeclared) {
					declareType(parameterType);
				}
			}
		}
	}

	/**
	 * Checks if the two given types have to be defined for the first type to be implicitly
	 * converted to the second one.
	 *
	 * @param sourceType the type to be converted
	 * @param targetType the type to be converted to
	 * @return {@code true} if the types have to be defined
	 */
	private boolean isTypeDefinitionRequiredForConversion(IType sourceType, IType targetType) {
		if (!(targetType instanceof IPointerType || targetType instanceof ICPPReferenceType))
			return true;
		if (targetType instanceof IPointerType && Conversions.isNullPointerConstant(sourceType))
			return false;
		sourceType = getNestedType(sourceType, REF | ALLCVQ);
		targetType = getNestedType(targetType, REF | ALLCVQ);

		if (sourceType instanceof IPointerType && targetType instanceof IPointerType) {
			sourceType = getNestedType(((IPointerType) sourceType).getType(), ALLCVQ);
			targetType = getNestedType(((IPointerType) targetType).getType(), ALLCVQ);
		}
		return !sourceType.isSameType(targetType);
	}

	/**
	 * Returns {@code true} if the {@code classType} has a constructor that can be used for
	 * implicit conversion from {@code argument}.
	 */
	private boolean hasConvertingConstructor(ICPPClassType classType, IASTInitializerClause argument) {
		CPPASTName astName = new CPPASTName();
		astName.setName(classType.getNameCharArray());
		astName.setOffsetAndLength((ASTNode) argument);
		CPPASTIdExpression idExp = new CPPASTIdExpression(astName);
		idExp.setParent(argument.getParent());
		idExp.setPropertyInParent(IASTFunctionCallExpression.FUNCTION_NAME);

		LookupData lookupData = new LookupData(astName);
		lookupData.setFunctionArguments(false, new IASTInitializerClause[] { argument });
		lookupData.qualified = true;
		try {
			IBinding constructor = CPPSemantics.resolveFunction(lookupData, classType.getConstructors(), false, false);
			if (constructor instanceof ICPPConstructor && !((ICPPConstructor) constructor).isExplicit())
				return true;
		} catch (DOMException e) {
		}

		return false;
	}

	/**
	 * Returns {@code true} if {@code classType} has a constructor that can be used for
	 * implicit conversion from some other type.
	 */
	private boolean hasConvertingConstructor(ICPPClassType classType) {
		ICPPConstructor[] constructors = classType.getConstructors();
		for (ICPPConstructor constructor : constructors) {
			if (!constructor.isExplicit()) {
				ICPPParameter[] parameters = constructor.getParameters();
				if (parameters.length != 0 && CPPFunction.getRequiredArgumentCount(parameters) <= 1) {
					IType type = parameters[0].getType();
					if (type instanceof IBasicType && ((IBasicType) type).getKind() == IBasicType.Kind.eVoid)
						continue;
					type = getNestedType(type, REF | ALLCVQ);
					if (!classType.isSameType(type))
						return true;
				}
			}
		}
		return false;
	}

	private boolean isTypeWithConvertingConstructor(IType type) {
		type = getNestedType(type, REF | ALLCVQ);
		return type instanceof ICPPClassType && hasConvertingConstructor((ICPPClassType) type);
	}

	/**
	 * Resolves the given binding and returns the binding(s) which we actually have to either
	 * declare or define. As an example, if the given binding is a variable, this function returns
	 * the binding for the type of the variable. This is because we actually have to declare or
	 * define the type of a variable, not the variable itself.
	 *
	 * @param binding The binding to resolve.
	 * @return The binding(s) which is/are suitable for either declaration or definition,
	 *     or an empty list if no such binding is available.
	 */
	private Set<IBinding> getRequiredBindings(IBinding binding) {
		if (binding instanceof ICPPNamespaceAlias)
			return Collections.singleton(binding);
		if (binding instanceof ICPPNamespace)
			return Collections.emptySet();
		if (binding instanceof ICPPUsingDeclaration) {
			Set<IBinding> result = new HashSet<>();
			Collections.addAll(result, ((ICPPUsingDeclaration) binding).getDelegates());
			return result;
		}

		Deque<IBinding> queue = new ArrayDeque<>();

		addRequiredBindings(binding, queue);

		Set<IBinding> bindings = new HashSet<>();

		while ((binding = queue.poll()) != null) {
			if (binding instanceof ICPPSpecialization) {
				ICPPTemplateParameterMap parameterMap = ((ICPPSpecialization) binding).getTemplateParameterMap();
				for (Integer position : parameterMap.getAllParameterPositions()) {
					ICPPTemplateArgument argument = parameterMap.getArgument(position);
					if (argument != null) {
						IType type = argument.getTypeValue();
						// Normally we don't need to define parameters of a template specialization
						// that were not specified explicitly. std::hash and __gnu_cxx::hash are
						// exceptions from that rule.
						if (type instanceof IBinding
								&& CharArrayUtils.equals(((IBinding) type).getNameCharArray(), "hash")) { //$NON-NLS-1$
							IBinding owner = ((IBinding) type).getOwner();
							if (owner instanceof ICPPNamespace && (CharArrayUtils.equals(owner.getNameCharArray(), STD)
									|| CharArrayUtils.equals(owner.getNameCharArray(), "__gnu_cxx"))) { //$NON-NLS-1$
								addRequiredBindings((IBinding) type, queue);
							}
						}
					}
				}
				// Get the specialized binding - e.g. get the binding for X if the current binding
				// is for the template specialization X<Y>.
				addRequiredBindings(((ICPPSpecialization) binding).getSpecializedBinding(), queue);
			} else {
				bindings.add(binding);
			}
		}

		return bindings;
	}

	private void addRequiredBindings(IBinding binding, Deque<IBinding> newBindings) {
		if (binding instanceof IProblemBinding) {
			IProblemBinding problemBinding = (IProblemBinding) binding;

			IBinding[] candidateBindings = problemBinding.getCandidateBindings();
			if (candidateBindings.length > 0) {
				// There are candidate bindings available. We simply use them all here since those
				// different candidates are very often defined within the same target file anyway,
				// so it won't affect the list of generated include directives. This therefore
				// allows us to be a little more fault tolerant here.
				Collections.addAll(newBindings, candidateBindings);
			} else {
				// No candidate bindings available. Check whether this is a macro.
				try {
					IIndexMacro[] indexMacros = fContext.getIndex().findMacros(binding.getNameCharArray(),
							IndexFilter.ALL, null);
					Collections.addAll(newBindings, indexMacros);
				} catch (CoreException e) {
				}
			}
		} else if (binding instanceof ICPPTemplateParameter) {
			newBindings.add(binding);
		} else if (binding instanceof ICPPUnknownBinding) {
			IBinding owner = binding.getOwner();
			if (owner != null)
				newBindings.add(owner);
		} else if (binding instanceof ICPPMethod) {
			newBindings.add(binding); // Include the method in case we need its inline definition.
			if (binding instanceof ICPPConstructor)
				newBindings.add(binding.getOwner());
		} else if (binding instanceof IType) {
			// Remove type qualifiers.
			IBinding b = getTypeBinding((IType) binding);
			if (b != null)
				newBindings.add(b);
		} else {
			newBindings.add(binding);
		}
	}

	private void declareType(IType type) {
		IBinding binding = getTypeBinding(type);
		if (binding != null)
			declareBinding(binding);
	}

	/**
	 * Adds the given binding to the list of bindings which have to be forward declared.
	 *
	 * @param binding The binding to add.
	 */
	private void declareBinding(IBinding binding) {
		if (binding instanceof ICPPNamespace && !(binding instanceof ICPPNamespaceAlias))
			return;
		if (fProcessedDefinedBindings.contains(binding))
			return;
		if (!fProcessedDeclaredBindings.add(binding))
			return;
		if (isDeclaredLocally(binding))
			return; // Declared locally.
		if (!canForwardDeclare(binding)) {
			defineBinding(binding);
			return;
		}

		Collection<IBinding> requiredBindings = getRequiredBindings(binding);

		for (IBinding requiredBinding : requiredBindings) {
			if (fBindingsToForwardDeclare.contains(requiredBinding) || fBindingsToDefine.contains(requiredBinding)) {
				return;
			}
			if (isDeclaredLocally(requiredBinding) || isDefinedLocally(requiredBinding)) {
				return; // Declared or defined locally.
			}

			if (canForwardDeclare(requiredBinding)) {
				if (requiredBinding == binding) {
					fBindingsToForwardDeclare.add(requiredBinding);
				} else {
					declareBinding(requiredBinding);
				}
			} else {
				if (requiredBinding == binding) {
					fBindingsToDefine.add(requiredBinding);
				} else {
					defineBinding(requiredBinding);
				}
			}
		}
	}

	/**
	 * Checks whether the binding can be forward declared according to preferences.
	 */
	private boolean canForwardDeclare(IBinding binding) {
		boolean canDeclare = false;
		if (binding instanceof IProblemBinding && ((IProblemBinding) binding).getCandidateBindings().length != 0) {
			return true; // Return true to consider delegates later on.
		}
		if (binding instanceof ICPPUsingDeclaration) {
			return true; // Return true to consider delegates later on.
		}
		if (binding instanceof ICompositeType) {
			canDeclare = fPreferences.forwardDeclareCompositeTypes;
		} else if (binding instanceof IEnumeration) {
			canDeclare = fPreferences.forwardDeclareEnums && isEnumerationWithoutFixedUnderlyingType(binding);
		} else if (binding instanceof IFunction && !(binding instanceof ICPPMethod)) {
			canDeclare = fPreferences.forwardDeclareFunctions;
		} else if (binding instanceof IVariable) {
			if (((IVariable) binding).isExtern())
				canDeclare = fPreferences.forwardDeclareExternalVariables;
		}

		if (canDeclare && !fPreferences.forwardDeclareTemplates
				&& (binding instanceof ICPPTemplateDefinition || binding instanceof ICPPSpecialization)) {
			canDeclare = false;
		}
		return canDeclare;
	}

	/**
	 * Checks whether the type may be forward declared according to language rules.
	 */
	private boolean mayBeForwardDeclared(IType type) {
		IBinding binding = getTypeBinding(type);
		if (binding == null)
			return false;
		if (!fPreferences.assumeTemplatesMayBeForwardDeclared
				&& (binding instanceof ICPPTemplateDefinition || binding instanceof ICPPSpecialization)) {
			return false;
		}
		if (binding instanceof ICompositeType)
			return true;
		return binding instanceof ICPPEnumeration && ((ICPPEnumeration) binding).getFixedType() != null;
	}

	/**
	 * Adds the given type to the list of bindings which have to be defined. Typedefs and
	 * enumerations without fixed underlying type are skipped since they must be defined in the file
	 * that references them by name. If the type is explicitly referenced in this translation unit,
	 * it will be defined independently from this method.
	 *
	 * @param type The type to add.
	 */
	private void defineTypeExceptTypedefOrNonFixedEnum(IType type) {
		IBinding typeBinding = getTypeBinding(type);
		if (typeBinding != null && !(typeBinding instanceof ITypedef)
				&& !isEnumerationWithoutFixedUnderlyingType(typeBinding)) {
			defineBinding(typeBinding);
		}
	}

	/**
	 * Adds the given binding to the list of bindings which have to be defined.
	 *
	 * @param binding The binding to add.
	 */
	private void defineBinding(IBinding binding) {
		if (binding instanceof ICPPNamespace && !(binding instanceof ICPPNamespaceAlias))
			return;
		if (!markAsDefined(binding))
			return;
		if (isDefinedLocally(binding))
			return; // Defined locally.

		Collection<IBinding> requiredBindings = getRequiredBindings(binding);
		for (IBinding requiredBinding : requiredBindings) {
			fBindingsToForwardDeclare.remove(requiredBinding);
			if (requiredBinding != binding) {
				if (!markAsDefined(requiredBinding))
					continue;
				if (isDefinedLocally(requiredBinding))
					continue; // Defined locally.
			}
			fBindingsToDefine.add(requiredBinding);
		}
	}

	private boolean isDefinedLocally(IBinding binding) {
		return binding instanceof CPPClosureType || fAst.getDefinitionsInAST(binding).length != 0;
	}

	private boolean isDeclaredLocally(IBinding binding) {
		IASTName[] declarations = fAst.getDeclarationsInAST(binding);
		for (IASTName name : declarations) {
			IASTNode node = name;
			if (node.getPropertyInParent() == ICPPASTQualifiedName.SEGMENT_NAME)
				node = node.getParent();
			if (node.getPropertyInParent() != ICPPASTUsingDeclaration.NAME)
				return true;
		}
		return false;
	}

	private void defineBindingForName(IASTName name) {
		IBinding binding = name.resolveBinding();
		if (!isPartOfExternalMacroDefinition(name))
			defineBinding(binding);
	}

	/**
	 * Marks the given binding as defined.
	 *
	 * @param binding the binding to mark
	 * @return {{@code true} if the binding has not yet been marked as defined,
	 *     {@code false} otherwise.
	 */
	private boolean markAsDefined(IBinding binding) {
		if (!fProcessedDefinedBindings.add(binding))
			return false;

		if (binding instanceof ITypedef) {
			IType type = ((ITypedef) binding).getType();
			type = SemanticUtil.getNestedType(type, ALLCVQ);
			if (type instanceof IBinding) {
				// Record the fact that we also have a definition of the typedef's target type.
				markAsDefined((IBinding) type);
			}
		} else if (binding instanceof ICPPClassType && fAst.getDefinitionsInAST(binding).length == 0) {
			// The header that defines a class must provide definitions of all its base classes.
			ICPPClassType[] bases = ClassTypeHelper.getAllBases((ICPPClassType) binding);
			for (ICPPClassType base : bases) {
				fProcessedDefinedBindings.add(base);
				fBindingsToDefine.remove(base);
				fBindingsToForwardDeclare.remove(base);
			}
		}

		return true;
	}

	private void defineForFunctionCall(IFunction function, boolean defineFunction, IASTInitializerClause[] arguments) {
		if (defineFunction) {
			if (!fProcessedDefinedBindings.contains(function)) {
				if (!(function instanceof ICPPMethod)
						&& (!canForwardDeclare(function) || isDefinedInHeaderFile(function))) {
					// Since the function is defined in a header file, its definition has to be
					// reachable through includes to make a function call.
					defineBinding(function);
				} else {
					declareBinding(function);
				}
			}

			// Handle return or expression type of the function or constructor call.
			IType returnType = function.getType().getReturnType();
			defineTypeForBinding(function, returnType);
		}

		// Handle parameters.
		processFunctionParameters(function, defineFunction, arguments);

		fProcessedDefinedBindings.add(function);
	}

	private boolean isDefinedInHeaderFile(IFunction function) {
		try {
			IIndexName[] definitions = fContext.getIndex().findDefinitions(function);
			for (IIndexName name : definitions) {
				IIndexFile file = name.getFile();
				if (file != null && canBeIncluded(file))
					return true;
			}
		} catch (CoreException e) {
			// Ignore to return false.
		}
		return false;
	}

	private boolean canBeIncluded(IIndexFile indexFile) throws CoreException {
		return !IncludeUtil.isSource(indexFile, fContext.getProject())
				|| fContext.getIndex().findIncludedBy(indexFile, 0).length != 0;
	}

	private void defineTypeForBinding(IBinding binding, IType type) {
		if (isDefined(binding) && !mayBeForwardDeclared(type)) {
			defineIndirectTypes(type);
		} else if (!(type instanceof IPointerType || type instanceof ICPPReferenceType)) {
			defineTypeExceptTypedefOrNonFixedEnum(type);
		}
	}

	/**
	 * For a pointer or a reference type, defines the contained type. For an instance of a template
	 * allowing incomplete argument types, defines the argument type.
	 */
	private void defineIndirectTypes(IType type) {
		IType resolvedType = removeQualifiers(resolveTypedef(type));
		if (resolvedType instanceof IPointerType || resolvedType instanceof ICPPReferenceType) {
			defineTypeExceptTypedefOrNonFixedEnum(resolvedType);
		} else {
			if (resolvedType instanceof ICPPTemplateInstance) {
				ICPPTemplateInstance instance = (ICPPTemplateInstance) resolvedType;
				IBinding template = instance.getSpecializedBinding();
				if (isTemplateAllowingIncompleteArgumentType(template)) {
					ICPPTemplateArgument[] arguments = instance.getTemplateArguments();
					if (arguments.length != 0) {
						IType argumentType = arguments[0].getTypeValue();
						defineTypeExceptTypedefOrNonFixedEnum(argumentType);
					}
				}
			}
		}
	}

	private void defineTemplateArgument(ICPPTemplateArgument argument) {
		IType type = argument.getOriginalTypeValue();
		if (type != null)
			defineTypeExceptTypedefOrNonFixedEnum(type);
	}

	private boolean isDefined(IBinding binding) {
		if (fBindingsToDefine.contains(binding))
			return true;
		IBinding owner = binding.getOwner();
		if (owner instanceof IType && fBindingsToDefine.contains(owner))
			return true;
		return false;
	}

	private static boolean isTemplateAllowingIncompleteArgumentType(IBinding binding) {
		String name = binding.getName();
		int pos = Arrays.binarySearch(TEMPLATES_ALLOWING_INCOMPLETE_ARGUMENT_TYPE, name);
		if (pos < 0)
			return false;
		IBinding owner = binding.getOwner();
		if (!(owner instanceof ICPPNamespace))
			return false;
		return CharArrayUtils.equals(owner.getNameCharArray(), STD) && owner.getOwner() == null;
	}

	/**
	 * Returns the name corresponding to the ID or the field reference expression.
	 */
	private static IASTName getNameOfIdOrFieldReferenceExpression(IASTExpression expression) {
		if (expression instanceof IASTIdExpression) {
			return ((IASTIdExpression) expression).getName();
		} else if (expression instanceof IASTFieldReference) {
			return ((IASTFieldReference) expression).getFieldName();
		}
		return null;
	}

	/**
	 * Resolves the given type to a binding which we actually have to either declare or define.
	 * As an example if the given type is a pointer type, this function returns the binding for
	 * the raw (i.e. nested) type of the pointer. This is because we actually have to declare or
	 * define the raw type of a pointer, not the pointer type itself.
	 *
	 * @param type The type to resolve.
	 * @return A binding which is suitable for either declaration or definition, or {@code null}
	 *     if no such binding is available.
	 */
	private static IBinding getTypeBinding(IType type) {
		type = getNestedType(type, ALLCVQ | PTR | ARRAY | REF);
		if (type instanceof IBinding) {
			return (IBinding) type;
		}
		return null;
	}

	/**
	 * If the given type is a typedef, returns the ultimate type the typedef is pointing to.
	 * Otherwise returns the given type.
	 */
	private IType resolveTypedef(IType type) {
		while (type instanceof ITypedef) {
			type = ((ITypedef) type).getType();
		}
		return type;
	}

	/**
	 * If the given type is a qualified type, returns the corresponding unqualified type.
	 * Otherwise returns the given type.
	 */
	private IType removeQualifiers(IType type) {
		while (type instanceof IQualifierType) {
			type = ((IQualifierType) type).getType();
		}
		return type;
	}

	/**
	 * Checks if the given name is part of a template argument.
	 */
	private boolean isTemplateArgumentRequiringCompleteType(IASTName name) {
		ICPPASTTypeId typeId = ASTQueries.findAncestorWithType(name, ICPPASTTypeId.class);
		if (typeId == null || typeId.getPropertyInParent() != ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT)
			return false;
		ICPPASTTemplateId templateId = (ICPPASTTemplateId) typeId.getParent();
		IBinding template = templateId.resolveBinding();
		if (template instanceof IProblemBinding)
			return true;
		IBinding owner = template.getOwner();
		if (!(owner instanceof ICPPNamespace) || !CharArrayUtils.equals(owner.getNameCharArray(), STD)
				|| owner.getOwner() != null) {
			return true;
		}
		if (!isTemplateAllowingIncompleteArgumentType(template))
			return true;

		// For most templates allowing incomplete argument type a full definition of the argument
		// type is required if the destructor is called. Since the AST doen't contain all destructor
		// calling points, we have to use an indirect approach by examining the containing scope.
		IASTNode parent = templateId.getParent();
		if (!(parent instanceof ICPPASTNamedTypeSpecifier))
			return false;
		parent = parent.getParent();
		if (!(parent instanceof IASTSimpleDeclaration))
			return true;
		parent = parent.getParent();
		if (!(parent instanceof ICPPASTCompositeTypeSpecifier))
			return true;
		ICPPClassScope classScope = ((ICPPASTCompositeTypeSpecifier) parent).getScope();
		ICPPClassType classType = classScope.getClassType();
		try {
			CPPSemantics.pushLookupPoint(parent);
			ICPPMethod destructor = ClassTypeHelper.getMethodInClass(classType, MethodKind.DTOR);
			if (destructor != null && fAst.getDefinitionsInAST(destructor).length != 0)
				return true;
			return false;
		} finally {
			CPPSemantics.popLookupPoint();
		}
	}

	private static boolean isEnumerationWithoutFixedUnderlyingType(IBinding typeBinding) {
		return typeBinding instanceof IEnumeration && (!(typeBinding instanceof ICPPEnumeration)
				|| ((ICPPEnumeration) typeBinding).getFixedType() == null);
	}

	private static boolean isPartOfExternalMacroDefinition(IASTNode node) {
		IASTNodeLocation[] locations = node.getNodeLocations();
		if (locations.length != 1 || !(locations[0] instanceof IASTMacroExpansionLocation))
			return false;

		IASTMacroExpansionLocation macroExpansionLocation = (IASTMacroExpansionLocation) locations[0];
		IASTPreprocessorMacroExpansion macroExpansion = macroExpansionLocation.getExpansion();
		IASTPreprocessorMacroDefinition macroDefinition = macroExpansion.getMacroDefinition();
		if (macroDefinition.isPartOfTranslationUnitFile())
			return false;
		if (!(macroDefinition instanceof IASTPreprocessorFunctionStyleMacroDefinition))
			return true;
		return !containsNameFromTranslationUnit(node);
	}

	private static boolean containsNameFromTranslationUnit(IASTNode node) {
		LocalNameFinder localNameFinder = new LocalNameFinder();
		node.accept(localNameFinder);
		return localNameFinder.found;
	}

	/**
	 * Returns the declaration specifier node for the given declarator.
	 */
	private static IASTDeclSpecifier getDeclarationSpecifier(IASTDeclarator declarator) {
		declarator = CPPVisitor.findOutermostDeclarator(declarator);
		IASTNode parent = declarator.getParent();

		IASTDeclSpecifier declSpec = null;
		if (parent instanceof IASTSimpleDeclaration) {
			declSpec = ((IASTSimpleDeclaration) parent).getDeclSpecifier();
		} else if (parent instanceof IASTParameterDeclaration) {
			declSpec = ((IASTParameterDeclaration) parent).getDeclSpecifier();
		} else if (parent instanceof IASTFunctionDefinition) {
			declSpec = ((IASTFunctionDefinition) parent).getDeclSpecifier();
		} else if (parent instanceof ICPPASTTypeId) {
			declSpec = ((ICPPASTTypeId) parent).getDeclSpecifier();
		}
		return declSpec;
	}
}
