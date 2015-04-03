/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTImplicitDestructorName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper.MethodKind;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPASTInternalScope;

/**
 * Finds destructor calls for temporaries and local variables.
 */
public class DestructorCallCollector {
	/**
	 * Returns the implicit names corresponding to the destructor calls for temporaries destroyed at the end
	 * of the expression. Only expression that is not a subexpression of another expression may contain
	 * implicit destructor calls.
	 *
	 * @param expression the expression to get the destructor calls for
	 * @return an array of destructor names
	 */
	public static IASTImplicitDestructorName[] getTemporariesDestructorCalls(ICPPASTExpression expression) {
		if (expression.getParent() instanceof ICPPASTExpression)
			return IASTImplicitDestructorName.EMPTY_NAME_ARRAY; // Not a full-expression
		TemporariesDestructorCollector collector = new TemporariesDestructorCollector(expression);
		expression.accept(collector);
		return collector.getDestructorCalls();
	}

	/**
	 * Returns the implicit names corresponding to the destructor calls for the local variables destroyed at
	 * the end of the statement.
	 *
	 * @param statement the expression to get the destructor calls for
	 * @return an array of destructor names
	 */
	public static IASTImplicitDestructorName[] getLocalVariablesDestructorCalls(IASTStatement statement) {
		if (!(statement instanceof IASTImplicitDestructorNameOwner))
			return IASTImplicitDestructorName.EMPTY_NAME_ARRAY;
		LocalVariablesDestructorCollector collector =
				new LocalVariablesDestructorCollector((IASTImplicitDestructorNameOwner) statement);
		statement.accept(collector);
		return collector.getDestructorCalls();
	}

	// Not instantiatable. All methods are static.
	private DestructorCallCollector() {
	}

	private abstract static class DestructorCollector extends ASTVisitor {
		private IASTImplicitDestructorName[] destructorNames = IASTImplicitDestructorName.EMPTY_NAME_ARRAY;

		IASTImplicitDestructorName[] getDestructorCalls() {
			destructorNames = ArrayUtil.trim(destructorNames);
			return destructorNames;
		}

		static ICPPMethod findDestructor(ICPPClassType classType, IASTNode point) {
			return ClassTypeHelper.getMethodInClass(classType, MethodKind.DTOR, point);
		}

		protected void addDestructorCall(IASTName name, ICPPMethod destructor,
				IASTImplicitDestructorNameOwner owner) {
			CPPASTImplicitDestructorName destructorName =
					new CPPASTImplicitDestructorName(destructor.getNameCharArray(), owner, name);
			destructorName.setBinding(destructor);
			ASTNode parentNode = (ASTNode) owner;
			int offset = parentNode.getOffset() + parentNode.getLength();
			if (!(owner instanceof ICPPASTExpression))
				offset--;  // Before the closing brace.
			destructorName.setOffsetAndLength(offset, 0);
			destructorNames = ArrayUtil.prepend(destructorNames, destructorName);
		}
	}

	private static class TemporariesDestructorCollector extends DestructorCollector {
		private final ICPPASTExpression owner;
		
		private TemporariesDestructorCollector(ICPPASTExpression owner) {
			this.owner = owner;
			shouldVisitImplicitNames = true;
		}

		@Override
		public int visit(IASTName name) {
			if (name instanceof IASTImplicitName && !(name.getParent() instanceof ICPPASTNewExpression)) {
				IBinding binding = name.resolveBinding();
				if (binding instanceof ICPPConstructor) {
					ICPPClassType classType = ((ICPPConstructor) binding).getClassOwner();
					ICPPMethod destructor = findDestructor(classType, name);
					if (destructor != null && !isBoundToVariable(name)) {
						addDestructorCall(name, destructor, owner);
					}
				}
			}
			return PROCESS_CONTINUE;
		}

		private boolean isBoundToVariable(IASTName name) {
			IASTNode parent = name.getParent();
			if (!(parent instanceof IASTExpression))
				return false;

			parent = parent.getParent();
			if (!(parent instanceof IASTBinaryExpression))
				return false;

			IASTBinaryExpression binExpr = (IASTBinaryExpression) parent;
			if (binExpr.getOperator() != IASTBinaryExpression.op_assign)
				return false;

			IASTExpression left = binExpr.getOperand1();
			if (left instanceof IASTIdExpression) {
				IASTName name2 = ((IASTIdExpression) left).getName();
				IBinding binding = name2.resolveBinding();
				if (binding instanceof ICPPVariable) {
					return true;
				}
			}
			return false;
		}
	}

	private static class LocalVariablesDestructorCollector extends DestructorCollector {
		private final IASTImplicitDestructorNameOwner owner;
		
		private LocalVariablesDestructorCollector(IASTImplicitDestructorNameOwner owner) {
			this.owner = owner;
			shouldVisitNames = true;
			shouldVisitImplicitNames = true;
		}

		@Override
		public int visit(IASTName name) {
			if (name.getPropertyInParent() == IASTDeclarator.DECLARATOR_NAME) {
				IBinding binding = name.resolveBinding();
				if (binding instanceof ICPPVariable) {
					ICPPVariable var = (ICPPVariable) binding;
					try {
						IScope scope = var.getScope();
						if (scope.getKind() == EScopeKind.eLocal && scope instanceof ICPPASTInternalScope) {
							IASTNode scopeNode = ((ICPPASTInternalScope) scope).getPhysicalNode();
							if (scopeNode.equals(owner)) {
								IType type = SemanticUtil.getNestedType(var.getType(), TDEF | CVTYPE);
								if (type instanceof ICPPClassType) {
									ICPPMethod destructor = findDestructor((ICPPClassType) type, name);
									addDestructorCall(name, destructor, owner);
								} else if (type instanceof ICPPReferenceType) {
									IASTDeclarator decl = (IASTDeclarator) name.getParent();
									addDestructorCallForTemporaryBoundToReference(decl);
								}
							}
						}
					} catch (DOMException e) {
						CCorePlugin.log(e);
					}
				}
			}
			return PROCESS_CONTINUE;
		}

		private void addDestructorCallForTemporaryBoundToReference(IASTDeclarator decl) {
			IASTInitializer initializer = decl.getInitializer();
			if (initializer instanceof IASTEqualsInitializer) {
				IASTInitializerClause clause = ((IASTEqualsInitializer) initializer).getInitializerClause();
				if (clause instanceof IASTImplicitNameOwner) {
					IASTImplicitName[] implicitNames = ((IASTImplicitNameOwner) clause).getImplicitNames();
					if (implicitNames.length != 0) {
						IASTImplicitName name = implicitNames[0];
						IBinding binding = name.resolveBinding();
						if (binding instanceof ICPPConstructor) {
							ICPPClassType classType = ((ICPPConstructor) binding).getClassOwner();
							ICPPMethod destructor = findDestructor(classType, name);
							if (destructor != null) {
								addDestructorCall(name, destructor, owner);
							}
						}
					}
				}
			}
		}
	}
}
