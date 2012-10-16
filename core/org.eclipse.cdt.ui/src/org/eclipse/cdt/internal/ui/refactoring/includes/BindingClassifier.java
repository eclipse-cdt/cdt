/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *	   Mathias Kunter
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.includes;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.ALLCVQ;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.ARRAY;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.PTR;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getNestedType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;

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
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IndexFilter;

/**
 * The inclusion resolver finds the bindings which have to be included from a given AST.
 */
public class BindingClassifier {
	private final InclusionContext fContext;

	/** Stores the AST node which this resolver is working on. */
	private final IASTNode fNode;

	/** Stores the bindings which require a full definition. */
	private final Set<IBinding> fBindingsToDefine;

	/** Stores the bindings which only require a simple forward declaration. */
	private final Set<IBinding> fBindingsToDeclare;
	private final IncludePreferences fPreferences;

	/**
	 * Constructs a new inclusion resolver.
	 *
	 * @param context the context for binding classification
	 * @param node the AST node which should be processed by the resolver
	 */
	public BindingClassifier(InclusionContext context, IASTNode node) {
		fContext = context;
		fPreferences = context.getPreferences();
		fNode = node;
		fBindingsToDefine = new HashSet<IBinding>();
		fBindingsToDeclare = new HashSet<IBinding>();

		processNode(fNode);
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
	public Set<IBinding> getBindingsToDeclare() {
		return fBindingsToDeclare;
	}

	/**
	 * Processes the given node and all of its children recursively.
	 *
	 * @param node The node to process.
	 */
	private void processNode(IASTNode node) {
		if (processSingleNode(node)) {
			// Process all children of this node as well.
			IASTNode[] children = node.getChildren();
			for (IASTNode child : children) {
				processNode(child);
			}
		}
	}

	/**
	 * Processes the given node, but none of its children.
	 *
	 * @param node The node to process.
	 * @return Whether the children of the node must be processed.
	 */
	private boolean processSingleNode(IASTNode node) {
		if (node == null) {
			return false;
		}

		if (node instanceof IASTSimpleDeclaration) {
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
			 * The type specifier of a simple function declaration also doesn't need to be defined:
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
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) node;
			IASTDeclSpecifier declSpecifier = simpleDeclaration.getDeclSpecifier();
			IASTDeclarator[] declarators = simpleDeclaration.getDeclarators();

			if (declSpecifier instanceof IASTNamedTypeSpecifier) {
				// We only handle simple declarations here whose declaration specifiers are named
				// type specifiers.
				boolean staticMember =
						simpleDeclaration.getParent() instanceof IASTCompositeTypeSpecifier &&
						declSpecifier.getStorageClass() == IASTDeclSpecifier.sc_static;

				// Declare the named type specifier if all declarators are either pointers,
				// references or functions.
				boolean canBeDeclared = true;
				if (!staticMember) {
					for (IASTDeclarator declarator : declarators) {
						if (!(declarator instanceof IASTFunctionDeclarator) &&
								declarator.getPointerOperators().equals(IASTPointerOperator.EMPTY_ARRAY)) {
							canBeDeclared = false;
							break;
						}
					}
				}

				if (!canBeDeclared) {
					defineBinding(((IASTNamedTypeSpecifier) declSpecifier).getName().resolveBinding());
				}
			}
		} else if (node instanceof ICPPASTBaseSpecifier) {
			/*
			 * The type of a base specifier must always be defined.
			 *
			 * Example:
			 * 	class Y : X {};			// definition of X is required here
			 */
			defineBinding(((ICPPASTBaseSpecifier) node).getName().resolveBinding());
		} else if (node instanceof IASTInitializer) {
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

			IASTInitializer initializer = (IASTInitializer) node;

			// Get the binding of the initialized AST name first.
			IASTNode memberNode = node;
			IASTName memberName = null;
			IBinding memberBinding = null;

			while (memberNode != null) {
				if (memberNode instanceof IASTDeclarator) {
					memberName = ((IASTDeclarator) memberNode).getName();
					break;
				} else if (memberNode instanceof ICPPASTConstructorChainInitializer) {
					memberName = ((ICPPASTConstructorChainInitializer) memberNode).getMemberInitializerId();
					break;
				}
				memberNode = memberNode.getParent();
			}
			if (memberName != null) {
				memberBinding = memberName.resolveBinding();
			}

			// Get the arguments of the initializer.
			IASTInitializerClause[] actualParameters = new IASTInitializerClause[] { };
			if (initializer instanceof ICPPASTConstructorInitializer) {
				ICPPASTConstructorInitializer constructorInitializer = (ICPPASTConstructorInitializer) initializer;
				actualParameters = constructorInitializer.getArguments();
			} else if (initializer instanceof IASTEqualsInitializer) {
				IASTEqualsInitializer equalsInitializer = (IASTEqualsInitializer) initializer;
				actualParameters = new IASTInitializerClause[] { equalsInitializer.getInitializerClause() };
			}

			if (memberBinding instanceof IVariable) {
				// Variable construction.
				IType memberType = ((IVariable) memberBinding).getType();
				if (!(memberType instanceof IPointerType) && !(memberType instanceof ICPPReferenceType)) {
					// We're constructing a non-pointer type. We need to define the member type
					// either way since we must be able to call its constructor.
					defineTypeExceptTypedef(memberType);

					// TODO: Process the arguments. But how to get the corresponding IParameter[] array here?
					// processParameters(declaredParameters, arguments);
				} else {
					// We're constructing a pointer type. No constructor is called. We however have
					// to check whether the argument type matches the declared type.
					memberType = getNestedType(memberType, REF);
					for (IASTInitializerClause actualParameter : actualParameters) {
						if (actualParameter instanceof IASTExpression) {
							IType parameterType = ((IASTExpression) actualParameter).getExpressionType();
							if (!isSameType(memberType, parameterType)) {
								// Types don't match. Define both types.
								defineTypeExceptTypedef(memberType);
								defineTypeExceptTypedef(parameterType);
							}
						}
					}
				}
			} else if (memberBinding instanceof ICPPConstructor) {
				// Class construction
				ICPPConstructor constructor = (ICPPConstructor) memberBinding;

				// We need to define the owning type of the constructor.
				defineBinding(constructor.getOwner());

				// Process the parameters.
				processParameters(constructor.getParameters(), actualParameters);
			}

		} else if (node instanceof IASTElaboratedTypeSpecifier) {
			/*
			 * The type specifier of an elaborated type neither needs to be defined nor needs to be
			 * declared. This is because an elaborated type specifier is a self-sufficient
			 * statement.
			 *
			 * Example:
			 * 	class X;			// neither definition nor declaration of X is required here
			 */
			return false;
		} else if (node instanceof IASTFunctionDefinition) {
			/*
			 * The type specifier of a function definition doesn't need to be defined if it is
			 * a pointer or reference type.
			 *
			 * Example 1:
			 * 	X *foo() { }		// definition of X is not required here
			 *
			 * Example 2:
			 * 	X& foo() { }		// definition of X is not required here
			 */
			IBinding binding = ((IASTFunctionDefinition) node).getDeclarator().getName().resolveBinding();
			if (binding instanceof IFunction) {
				IFunction function = (IFunction) binding;

				// Define the return type if necessary
				IType returnType = function.getType().getReturnType();
				if (!(returnType instanceof IPointerType) && !(returnType instanceof ICPPReferenceType)) {
					defineTypeExceptTypedef(returnType);
				}

				// Define parameter types if necessary
				IType[] parameterTypes = function.getType().getParameterTypes();
				for (IType type : parameterTypes) {
					if (!(type instanceof IPointerType) && !(type instanceof ICPPReferenceType)) {
						defineTypeExceptTypedef(type);
					}
				}
			}
		} else if (node instanceof IASTIfStatement ||
				node instanceof IASTForStatement ||
				node instanceof IASTWhileStatement ||
				node instanceof IASTDoStatement) {
			IASTExpression conditionExpression = null;

			if (node instanceof IASTIfStatement) {
				/*
				 * The type of the condition expression of an if statement doesn't need to be
				 * defined if it's a pointer type.
				 *
				 * Example:
				 * 	void foo(X* x) {
				 * 		if (x) { }				// definition of typeof(x) is not required here
				 * 	}
				 */
				conditionExpression = ((IASTIfStatement) node).getConditionExpression();
			} else if (node instanceof IASTForStatement) {
				/*
				 * The type of the condition expression of a for statement doesn't need to be
				 * defined if it's a pointer type.
				 *
				 * Example:
				 * 	void foo(X* x) {
				 * 		for (; x; ) { }			// definition of typeof(x) is not required here
				 * 	}
				 */
				conditionExpression = ((IASTForStatement) node).getConditionExpression();
			} else if (node instanceof IASTWhileStatement) {
				/*
				 * The type of the condition expression of a while statement doesn't need to be
				 * defined if it's a pointer type.
				 *
				 * Example:
				 * 	void foo(X* x) {
				 * 		while (x) { }			// definition of typeof(x) is not required here
				 * 	}
				 */
				conditionExpression = ((IASTWhileStatement) node).getCondition();
			} else if (node instanceof IASTDoStatement) {
				/*
				 * The type of the condition expression of a do statement doesn't need to be
				 * defined if it's a pointer type.
				 *
				 * Example:
				 * 	void foo(X* x) {
				 * 		do { } while (x);		// definition of typeof(x) is not required here
				 * 	}
				 */
				conditionExpression = ((IASTDoStatement) node).getCondition();
			}

			if (conditionExpression != null) {
				IType conditionExpressionType = conditionExpression.getExpressionType();
				if (!(conditionExpressionType instanceof IPointerType)) {
					defineTypeExceptTypedef(conditionExpressionType);
				}
			}
		} else if (node instanceof IASTReturnStatement) {
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
			IASTReturnStatement returnStatement = (IASTReturnStatement) node;

			IASTExpression returnValue = returnStatement.getReturnValue();
			if (returnValue != null) {
				// Get the containing function definition.
				IASTNode functionDefinitionNode = returnStatement;
				while (functionDefinitionNode != null && !(functionDefinitionNode instanceof IASTFunctionDefinition)) {
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
							// Don't care about reference types since they can be converted into
							// non-reference types and vice versa without requiring a definition.
							IType returnType = function.getType().getReturnType();
							returnType = getNestedType(returnType, REF);
							IType expressionType = getNestedType(returnValue.getExpressionType(), REF);

							// Compare the two types.
							if (!isSameType(returnType, expressionType)) {
								// Not the same type. Define both types.
								defineTypeExceptTypedef(returnType);
								defineTypeExceptTypedef(expressionType);
							}
						}
					}
				}
			}
		} else if (node instanceof IASTIdExpression) {
			/*
			 * The type of an identifier expression doesn't need to be defined if it's a pointer or
			 * a reference type.
			 *
			 * Example:
			 * 	void foo(X& x) {
			 * 		x;				// definition of typeof(x) is not required here
			 * 	}
			 */
			IASTIdExpression idExpression = (IASTIdExpression) node;

			IBinding binding = idExpression.getName().resolveBinding();
			if (binding instanceof IVariable) {
				// Get the declared type.
				IType expressionType = ((IVariable) binding).getType();
				if (!(expressionType instanceof IPointerType) && !(expressionType instanceof ICPPReferenceType)) {
					defineTypeExceptTypedef(expressionType);
				}
			}
		} else if (node instanceof IASTUnaryExpression) {
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
			IASTUnaryExpression unaryExpression = (IASTUnaryExpression) node;

			boolean expressionDefinitionRequired = true;
			switch (unaryExpression.getOperator()) {
				case IASTUnaryExpression.op_amper:
				case IASTUnaryExpression.op_bracketedPrimary:
					// The ampersand operator as well as brackets never require a definition.
					expressionDefinitionRequired = false;
					break;
				case IASTUnaryExpression.op_alignOf:
				case IASTUnaryExpression.op_not:
				case IASTUnaryExpression.op_plus:
				case IASTUnaryExpression.op_sizeof:
				case IASTUnaryExpression.op_typeid:
					// If the operand is a pointer type, then it doesn't need to be defined.
					if (unaryExpression.getOperand().getExpressionType() instanceof IPointerType) {
						expressionDefinitionRequired = false;
					}
			}

			if (expressionDefinitionRequired) {
				defineTypeExceptTypedef(unaryExpression.getOperand().getExpressionType());
			}
		} else if (node instanceof IASTBinaryExpression) {
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

			IASTBinaryExpression binaryExpression = (IASTBinaryExpression) node;

			IType operand1Type = binaryExpression.getOperand1().getExpressionType();
			IType operand2Type = binaryExpression.getOperand2().getExpressionType();

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
					// If both operands are identical pointer types, then they don't need to be
					// defined.
					if (operand1Type instanceof IPointerType && operand2Type instanceof IPointerType) {
						if (isSameType(operand1Type, operand2Type)) {
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
				defineTypeExceptTypedef(operand1Type);
			}
			if (expression2DefinitionRequired) {
				defineTypeExceptTypedef(operand2Type);
			}
		} else if (node instanceof IASTConditionalExpression) {
			/*
			 * The type of the condition of a conditional expression doesn't need to be defined
			 * if it's a pointer type.
			 *
			 * Example:
			 * 	void foo(X* x) {
			 * 		x ? 1 : 0;				// definition of typeof(x) is not required here
			 * 	}
			 */

			IASTConditionalExpression conditionalExpression = (IASTConditionalExpression) node;
			IASTExpression logicalConditionExpression = conditionalExpression.getLogicalConditionExpression();

			if (logicalConditionExpression != null) {
				IType logicalConditionExpressionType = logicalConditionExpression.getExpressionType();
				if (!(logicalConditionExpressionType instanceof IPointerType)) {
					defineTypeExceptTypedef(logicalConditionExpressionType);
				}
			}
		} else if (node instanceof IASTFunctionCallExpression) {
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
			IASTFunctionCallExpression functionCallExpression = (IASTFunctionCallExpression) node;
			IASTExpression functionNameExpression = functionCallExpression.getFunctionNameExpression();

			if (functionNameExpression instanceof IASTIdExpression) {
				IBinding binding = ((IASTIdExpression) functionNameExpression).getName().resolveBinding();
				if (binding instanceof IFunction) {
					IFunction function = (IFunction) binding;
					// Handle return or expression type of the function or constructor call.
					IType returnType = function.getType().getReturnType();
					if (!(returnType instanceof IPointerType) && !(returnType instanceof ICPPReferenceType)) {
						// The return type needs a full definition.
						defineTypeExceptTypedef(returnType);
					}

					// Handle parameters.
					processParameters(function.getParameters(), functionCallExpression.getArguments());
				} else if (binding instanceof IType) {
					// The binding resolves to a composite type, as it does for constructor calls.
					// We have to define the binding.
					defineTypeExceptTypedef((IType) binding);
				}
			}
		} else if (node instanceof IASTFieldReference) {
			/*
			 * The type of the expression part of a field reference always requires a definition.
			 *
			 * Example:
			 * 	void foo(X& x1, X* x2) {
			 * 		x1.bar();			// definition of typeof(x1) is required here
			 * 		x2->bar();			// definition of typeof(x2) is required here
			 * 	}
			 */

			defineTypeExceptTypedef(((IASTFieldReference) node).getFieldOwner().getExpressionType());
		} else if (node instanceof ICPPASTNewExpression) {
			/*
			 * The type specifier of a "new" expression always requires a definition.
			 *
			 * Example:
			 * 	void foo() {
			 * 		new X();			// definition of X is required here
			 * 	}
			 */
			defineTypeExceptTypedef(((ICPPASTNewExpression) node).getExpressionType());
		} else if (node instanceof ICPPASTDeleteExpression) {
			/*
			 * The expression type of a "delete" expression always requires a full definition.
			 * This is necessary because the compiler needs to be able to call the destructor.
			 *
			 * Example:
			 * 	void foo(X* x) {
			 * 		delete x;			// definition of typeof(x) is required here
			 * 	}
			 */
			defineTypeExceptTypedef(((ICPPASTDeleteExpression) node).getOperand().getExpressionType());
		} else if (node instanceof IASTCastExpression) {
			/*
			 * Explicit type casts always need the definition of the underlying types.
			 *
			 * Example:
			 * 	void foo(X* x) {
			 * 		(Y*) x;				// definition of both Y and typeof(x) is required here
			 * 	}
			 */
			IASTCastExpression castExpression = (IASTCastExpression) node;
			IType targetType = castExpression.getExpressionType();
			IType sourceType = castExpression.getOperand().getExpressionType();

			if (!isSameType(targetType, sourceType)) {
				// Source and target types of the cast expression are different. We need to define
				// both types, even if they're pointers.
				defineTypeExceptTypedef(targetType);
				defineTypeExceptTypedef(sourceType);
			} else if (!(targetType instanceof IPointerType) && !(targetType instanceof ICPPReferenceType)) {
				// Define the target type if it's not a pointer or reference type.
				defineTypeExceptTypedef(targetType);
			}
		} else if (node instanceof ICPPASTCatchHandler) {
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
			ICPPASTCatchHandler catchHandler = (ICPPASTCatchHandler) node;
			IASTDeclaration declaration = catchHandler.getDeclaration();
			if (declaration instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
				IASTDeclSpecifier declSpecifier = simpleDeclaration.getDeclSpecifier();
				if (declSpecifier instanceof IASTNamedTypeSpecifier) {
					IASTNamedTypeSpecifier namedTypeSpecifier = (IASTNamedTypeSpecifier) declSpecifier;
					defineBinding(namedTypeSpecifier.getName().resolveBinding());
				}
			}
		} else if (node instanceof IASTName) {
			// We've found an AST name. Add it to the bindings which can be declared (we assume
			// that all bindings which have to be defined are already explicitly handled by
			// the code above).
			IASTName name = (IASTName) node;

			if (name instanceof ICPPASTQualifiedName) {
				// Any qualifying names must be defined.
				IASTName[] names = ((ICPPASTQualifiedName) name).getNames();
				for (int i = 0; i + 1 < names.length; i++) {
					defineBinding(names[i].resolveBinding());
				}
			}

			IBinding binding = name.resolveBinding();
			IBinding owner = binding.getOwner();
			if (owner instanceof IType) {
				defineBinding(owner);		// Member access requires definition of the containing type.
			} else {
				declareBinding(binding);	// Declare the binding of this name.
			}
		}

		// Return true to signal that all children of this node must be processed as well.
		return true;
	}

	/**
	 * Defines the required types of the parameters of a function or constructor call expression by
	 * comparing the declared parameters with the actual arguments.
	 */
	private void processParameters(IParameter[] declaredParameters, IASTInitializerClause[] arguments) {
		for (int i = 0; i < declaredParameters.length; i++) {
			IType declaredParameterType = declaredParameters[i].getType();
			IType actualParameterType = null;
			boolean canBeDeclared = false;
			if (declaredParameterType instanceof IPointerType || declaredParameterType instanceof ICPPReferenceType) {
				// The declared parameter type is a pointer or reference type. A declaration is
				// sufficient if it matches the actual parameter type.
				declaredParameterType = getNestedType(declaredParameterType, REF);
				if (i < arguments.length) {
					// This parameter is present within the function call expression.
					// It's therefore not a default parameter.
					IASTInitializerClause actualParameter = arguments[i];
					if (actualParameter instanceof IASTExpression) {
						actualParameterType = ((IASTExpression) actualParameter).getExpressionType();
						actualParameterType = getNestedType(actualParameterType, REF);

						if (isSameType(declaredParameterType, actualParameterType)) {
							canBeDeclared = true;
						}
					}
				} else {
					// This is a default value parameter. The function call itself doesn't need
					// a definition of this parameter type.
					canBeDeclared = true;
				}
			}

			if (canBeDeclared) {
				// The declared parameter type must be declared. We must explicitly do this here
				// because this type doesn't appear within the AST.
				declareType(declaredParameterType);
			} else {
				// Both the type of the declared parameter as well as the type of the actual
				// parameter require a full definition.
				defineTypeExceptTypedef(declaredParameterType);
				defineTypeExceptTypedef(actualParameterType);
			}
		}
	}

	/**
	 * Returns whether the two given types are identical. This does the same as IType.isSameType()
	 * with the exception that it considers a pointer and the zero literal identical.
	 */
	private boolean isSameType(IType type1, IType type2) {
		if (type1 == null || type2 == null) {
			return false;
		}
		if (type1.isSameType(type2)) {
			return true;
		}

		if (type1 instanceof IPointerType || type2 instanceof IPointerType) {
			if ((type1 instanceof IBasicType && ((IBasicType) type1).getKind() == Kind.eInt)
					|| (type2 instanceof IBasicType && ((IBasicType) type2).getKind() == Kind.eInt)) {
				return true;
			}
		}

		return false;
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
	private List<IBinding> resolveBinding(IBinding binding) {
		if (binding instanceof IVariable) {
			// Resolve the type of the variable.
			binding = getTypeBinding(((IVariable) binding).getType());
		} else if (binding instanceof IType) {
			// Resolve the type.
			binding = getTypeBinding((IType) binding);
		} else if (binding instanceof ICPPConstructor) {
			// If the binding is a constructor, get its owning composite type.
			binding = binding.getOwner();
		} else if (binding instanceof ICPPNamespace) {
			// Namespaces are neither declared nor defined.
			binding = null;
		}

		if (binding instanceof ICPPSpecialization) {
			// Get the specialized binding - e.g. get the binding for X if the current binding is
			// for the template specialization X<Y>. Resolution of the specialization
			// (i.e. template) arguments is handled separately by the caller of this method.
			binding = ((ICPPSpecialization) binding).getSpecializedBinding();
		}

		List<IBinding> bindings = new ArrayList<IBinding>();

		if (binding instanceof IProblemBinding) {
			IProblemBinding problemBinding = (IProblemBinding) binding;

			IBinding[] candidateBindings = problemBinding.getCandidateBindings();
			if (candidateBindings.length > 0) {
				// There are candidate bindings available. We simply use them all here since those
				// different candidates are very often defined within the same target file anyway,
				// so it won't affect the list of generated include directives. This therefore
				// allows us to be a little more fault tolerant here.
				for (IBinding candidateBinding : candidateBindings) {
					bindings.add(candidateBinding);
				}
			} else {
				// No candidate bindings available. Check whether this is a macro.
				try {
					IIndexMacro[] indexMacros = fContext.getIndex().findMacros(binding.getNameCharArray(),
							IndexFilter.ALL, null);
					for (IIndexMacro indexMacro : indexMacros) {
						bindings.add(indexMacro);
					}
				} catch (CoreException e) {
				}
			}
		} else if (binding != null) {
			bindings.add(binding);
		}

		return bindings;
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
	private IBinding getTypeBinding(IType type) {
		type = getNestedType(type, ALLCVQ | PTR | ARRAY | REF);
		if (type instanceof IBinding) {
			return (IBinding) type;
		}
		return null;
	}

	/**
	 * Adds the given binding to the list of bindings which have to be forward declared.
	 *
	 * @param binding The binding to add.
	 */
	private void declareBinding(IBinding binding) {
		List<IBinding> resolvedBindings = resolveBinding(binding);

		for (IBinding resolvedBinding : resolvedBindings) {
			if (fBindingsToDeclare.contains(resolvedBinding) || fBindingsToDefine.contains(resolvedBinding)) {
				return;
			}

			// Check whether the user actually wants to declare this binding.
			boolean doDeclareBinding = true;
			if (resolvedBinding instanceof ICompositeType) {
				doDeclareBinding = fPreferences.forwardDeclareCompositeTypes;
			} else if (resolvedBinding instanceof IEnumeration) {
				doDeclareBinding = fPreferences.forwardDeclareEnums;
			} else if (resolvedBinding instanceof IFunction && !(resolvedBinding instanceof ICPPMethod)) {
				doDeclareBinding = fPreferences.forwardDeclareFunctions;
			} else if (resolvedBinding instanceof IIndexMacro || resolvedBinding instanceof ITypedef
					|| resolvedBinding instanceof IEnumerator) {
				// Macros, typedefs and enumerators can never be declared.
				doDeclareBinding = false;
			}

			if (doDeclareBinding) {
				fBindingsToDeclare.add(resolvedBinding);
			} else {
				fBindingsToDefine.add(resolvedBinding);
			}
		}
	}

	/**
	 * Adds the given type to the list of bindings which have to be declared.
	 *
	 * @param type The type to add.
	 */
	private void declareType(IType type) {
		IBinding typeBinding = getTypeBinding(type);
		if (typeBinding != null)
			declareBinding(typeBinding);
	}

	/**
	 * Adds the given type to the list of bindings which have to be defined. Typedefs are skipped
	 * since they must be defined in the file that references them by name. If a typedef is
	 * explicitly referenced in this translation unit, it will be defined independently from this
	 * method.
	 *
	 * @param type The type to add.
	 */
	private void defineTypeExceptTypedef(IType type) {
		IBinding typeBinding = getTypeBinding(type);
		if (typeBinding != null && !(typeBinding instanceof ITypedef)) {
			defineBinding(typeBinding);
		}
	}

	/**
	 * Adds the given binding to the list of bindings which have to be defined.
	 *
	 * @param binding The binding to add.
	 */
	private void defineBinding(IBinding binding) {
		if (binding instanceof ICPPTemplateInstance) {
			defineTemplateArguments((ICPPTemplateInstance) binding);
		}

		List<IBinding> resolvedBindings = resolveBinding(binding);
		for (IBinding resolvedBinding : resolvedBindings) {
			fBindingsToDeclare.remove(resolvedBinding);
			fBindingsToDefine.add(resolvedBinding);
		}
	}

	/**
	 * Defines non-pointer template arguments.
	 */
	protected void defineTemplateArguments(ICPPTemplateInstance instance) {
		ICPPTemplateDefinition templateDefinition = instance.getTemplateDefinition();
		ICPPTemplateParameter[] templateParameters = templateDefinition.getTemplateParameters();
		ICPPTemplateArgument[] templateArguments = instance.getTemplateArguments();
		for (int i = 0; i < templateArguments.length; i++) {
			ICPPTemplateArgument argument = templateArguments[i];
			ICPPTemplateParameter parameter = templateParameters[i];
			ICPPTemplateArgument parameterDefault = parameter.getDefaultValue();
			if (parameterDefault != null) {
				// Skip the template arguments if it is the same as parameter default.
				if (argument.isSameValue(parameterDefault))
					continue;
				if (argument.isTypeValue() && parameterDefault.isTypeValue()) {
					IType argType = argument.getTypeValue();
					IType defType = parameterDefault.getTypeValue();
					if (argType instanceof ICPPTemplateInstance && defType instanceof ICPPTemplateInstance) {
						IType argTemplate = (IType) ((ICPPTemplateInstance) argType).getTemplateDefinition();
						IType defTemplate = (IType) ((ICPPTemplateInstance) defType).getTemplateDefinition();
						if (argTemplate.isSameType(defTemplate)) {
							defineTemplateArguments((ICPPTemplateInstance) argType);
							continue;
						}
					}
				}
			}
			IType type = argument.getTypeValue();
			if (!(type instanceof IPointerType) && !(type instanceof ICPPReferenceType)) {
				IBinding binding = getTypeBinding(type);
				if (binding != null)
					defineBinding(binding);
			}
		}
	}
}