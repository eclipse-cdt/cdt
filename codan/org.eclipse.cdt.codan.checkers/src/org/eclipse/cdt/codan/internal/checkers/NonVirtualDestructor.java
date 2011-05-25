/*******************************************************************************
 * Copyright (c) 2009, 2011 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     Patrick Hofer [bug 315528]
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.checkers.CodanCheckersActivator;
import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;

/**
 * Checker to find that class has virtual method and non virtual destructor
 * 
 * @author Alena Laskavaia
 */
public class NonVirtualDestructor extends AbstractIndexAstChecker {
	public static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.NonVirtualDestructorProblem"; //$NON-NLS-1$

	public void processAst(IASTTranslationUnit ast) {
		// Traverse the ast using the visitor pattern.
		ast.accept(new OnEachClass());
	}

	class OnEachClass extends ASTVisitor {
		private IASTName className;
		private IBinding virtualMethod;
		private IBinding destructor;
		private IASTDeclSpecifier warningLocation;

		OnEachClass() {
			shouldVisitDeclSpecifiers = true;
		}

		public int visit(IASTDeclSpecifier decl) {
			if (isClassDecl(decl)) {
				try {
					boolean err = hasErrorCondition(decl);
					if (err) {
						String clazz = className.toString();
						String method = virtualMethod.getName();
						IASTNode node = decl;
						if (destructor != null) {
							if (destructor instanceof ICPPInternalBinding) {
								IASTNode[] decls = ((ICPPInternalBinding) destructor).getDeclarations();
								if (warningLocation == null) {
									if (decls != null && decls.length > 0)
										node = decls[0];
								} else {
									node = warningLocation;
								}
							}
						}
						reportProblem(ER_ID, node, clazz, method);
					}
				} catch (DOMException e) {
					// Ignore, not an error.
				} catch (Exception e) {
					CodanCheckersActivator.log(e);
				}
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}

		/**
		 * @param decl
		 * @throws DOMException
		 */
		private boolean hasErrorCondition(IASTDeclSpecifier decl) throws DOMException {
			ICPPASTCompositeTypeSpecifier spec = (ICPPASTCompositeTypeSpecifier) decl;
			className = spec.getName();
			IBinding binding = className.resolveBinding();
			if ((binding instanceof ICPPClassType) == false) {
				return false;
			}
			ICPPClassType classType = (ICPPClassType) binding;
			virtualMethod = null;
			destructor = null;
			warningLocation = null;
			// Check for the following conditions:
			// class has own virtual method and own non-virtual destructor
			// class has own virtual method and base non-virtual destructor
			// class has base virtual method and own non-virtual destructor
			ICPPMethod[] declaredMethods = classType.getDeclaredMethods();
			boolean hasOwnVirtualMethod = false;
			boolean hasOwnNonVirDestructor = false;  // implicit destructor
			boolean hasDestructor = false;
			boolean hasVirtualMethod = false;
			for (ICPPMethod method : declaredMethods) {
				if (method.isVirtual() && !method.isDestructor()) {
					hasOwnVirtualMethod = true;
					virtualMethod = method;
				}
				if (method.isDestructor()) {
					hasDestructor = true;
					if (!method.isVirtual()) {
						hasOwnNonVirDestructor = true;
						destructor = method;
					}
				}
			}
			boolean hasVirtualDestructor = false;
			// Class has own virtual method and own non-virtual destructor.
			if (hasOwnVirtualMethod && hasOwnNonVirDestructor) {
				if (classType.getFriends().length == 0) {
					if (destructor instanceof ICPPMethod) {
						// Check visibility of dtor. No error if it is protected or private.
						if (((ICPPMethod) destructor).getVisibility() != ICPPASTVisibilityLabel.v_public) {
							return false;
						}
					}
					// Check if one of its base classes has a virtual destructor.
					return !hasVirtualDtorInBaseClass(classType);
				}
				// Destructor can be called from a class declared as friend.
				return true;	
			}
			// Class does have virtual methods and no destructors, compiler will generate implicit public destructor
			if (hasOwnVirtualMethod && !hasDestructor) {
				if (classType.getBases().length == 0) {
					return true;
				}
			}
			// Class does not have virtual methods but has virtual destructor
			// - not an error
			if (!hasOwnVirtualMethod && hasDestructor && !hasOwnNonVirDestructor) {
				return false;
			}
			// Check inherited methods
			ICPPMethod[] allDeclaredMethods = classType.getAllDeclaredMethods();
			for (ICPPMethod method : allDeclaredMethods) {
				if (method.isVirtual() && !method.isDestructor()) {
					hasVirtualMethod = true;
					if (virtualMethod == null)
						virtualMethod = method;
				}
				if (method.isDestructor()) {
					hasDestructor = true;
					if (method.isVirtual()) {
						hasVirtualDestructor = true;
					} else {
						if (destructor == null)
							destructor = method;
					}
				}
			}
			if (hasOwnVirtualMethod) {
				// Class has own virtual method and base non-virtual destructor.
				if (hasDestructor && !hasVirtualDestructor) {
					boolean noVirtualDtorInBase = !hasVirtualDtorInBaseClass(classType);
					if (noVirtualDtorInBase) {
						warningLocation = decl;
					}
					return noVirtualDtorInBase;
				}
				
			} else if (hasVirtualMethod) {
				// Class has base virtual method and own non-virtual destructor.
				if (hasOwnNonVirDestructor) {
					return true;
				}
				boolean noVirtualDtorInBase = !hasVirtualDtorInBaseClass(classType);
				if (noVirtualDtorInBase) {
					warningLocation = decl;
				}
				return noVirtualDtorInBase;
			}
			return false;
		}

		private boolean hasVirtualDtorInBaseClass(ICPPClassType classType) {
			ICPPBase[] bases = classType.getBases();   
			for (ICPPBase base : bases) {
				if (!(base.getBaseClass() instanceof ICPPClassType)) {
					continue;
				}
				ICPPClassType testedBaseClass = (ICPPClassType) base.getBaseClass();
				ICPPMethod[] declaredBaseMethods = testedBaseClass.getDeclaredMethods();
				for (ICPPMethod method : declaredBaseMethods) {
					if (method.isDestructor() && method.isVirtual()) {
						return true;
					}
				}
				if (hasVirtualDtorInBaseClass(testedBaseClass))
					return true;
			}
			return false;
		}

		/**
		 * @param decl
		 * @return
		 */
		private boolean isClassDecl(IASTDeclSpecifier decl) {
			if (decl instanceof ICPPASTCompositeTypeSpecifier) {
				return true;
			}
			return false;
		}
	}
}
