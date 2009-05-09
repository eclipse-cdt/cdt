/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.checkers.sample;

import java.text.MessageFormat;

import org.eclipse.cdt.codan.checkers.Activator;
import org.eclipse.cdt.codan.core.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.core.resources.IFile;

/**
 * Checker to find that class has virtual method and non virtual destructor
 * 
 * @author Alena
 * 
 */
public class NonVirtualDestructor extends AbstractIndexAstChecker {
	private static final String ER_ID = "org.eclipse.cdt.codan.checkers.sample.NonVirtualDestructorProblem";

	@Override
	public void processAst(IASTTranslationUnit ast) {
		// traverse the ast using the visitor pattern.
		ast.accept(new OnEachClass());
	}

	class OnEachClass extends ASTVisitor {
		private IASTName className;
		private IBinding virMethodName;
		private IBinding destName;

		OnEachClass() {
			// shouldVisitDeclarations = true;
			shouldVisitDeclSpecifiers = true;
		}

		public int visit(IASTDeclSpecifier decl) {
			if (isClassDecl(decl)) {
				try {
					boolean err = hasErrorCondition(decl);
					if (err) {
						String mess;
						String clazz = className.toString();
						String method = virMethodName.getName();
						int line = 1;
						IFile file = getFile();
						if (destName != null) {
							if (destName instanceof ICPPInternalBinding) {
								ICPPInternalBinding bin = (ICPPInternalBinding) destName;
								IASTFileLocation fileLocation = bin
										.getDeclarations()[0].getFileLocation();
								line = fileLocation.getStartingLineNumber();
							}
							mess = MessageFormat
									.format(
											"Class ''{0}'' has virtual method ''{1}'' but non-virtual destructor ''{2}''",
											clazz, method, destName.getName());
							reportProblem(ER_ID, file, line, mess);
						}
					}
				} catch (DOMException e) {
					// ignore, no error
				} catch (Exception e) {
					Activator.log(e);
				}
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}

		/**
		 * @param decl
		 * @throws DOMException
		 */
		private boolean hasErrorCondition(IASTDeclSpecifier decl)
				throws DOMException {
			ICPPASTCompositeTypeSpecifier spec = (ICPPASTCompositeTypeSpecifier) decl;
			className = spec.getName();
			IBinding binding = className.getBinding();
			if (binding == null) {
				binding = className.resolveBinding();
			}
			if (binding instanceof ICPPClassType) {
				ICPPClassType type = (ICPPClassType) binding;
				virMethodName = null;
				destName = null;
				// check for the following conditions:
				// class has own virtual method and own non-virtual destructor
				// class has own virtual method and base non-virtual destructor
				// class has base virtual method and own non-virtual destructor
				ICPPMethod[] declaredMethods = type.getDeclaredMethods();
				boolean hasOwnVirtualMethod = false;
				boolean hasOwnNonVirDestructor = false;
				boolean hasDestructor = false;
				boolean hasVirtualMethod = false;
				for (int i = 0; i < declaredMethods.length; i++) {
					ICPPMethod icppMethod = declaredMethods[i];
					if (icppMethod.isVirtual() && !icppMethod.isDestructor()) {
						hasOwnVirtualMethod = true;
						virMethodName = icppMethod;
					}
					if (icppMethod.isDestructor()) {
						hasDestructor = true;
						if (!icppMethod.isVirtual()) {
							hasOwnNonVirDestructor = true;
							destName = icppMethod;
						}
					}
				}
				boolean hasVirDestructor = false;
				// class has own virtual method and own non-virtual destructor
				if (hasOwnVirtualMethod && hasOwnNonVirDestructor) {
					return true;
				}
				// class does not have virtual methods but has virtual
				// destructor
				// - not an error
				if (hasOwnVirtualMethod == false && hasDestructor == true
						&& hasOwnNonVirDestructor == false) {
					return false;
				}
				ICPPMethod[] allDeclaredMethods = type.getAllDeclaredMethods();
				for (int i = 0; i < allDeclaredMethods.length; i++) {
					ICPPMethod icppMethod = allDeclaredMethods[i];
					if (icppMethod.isVirtual() && !icppMethod.isDestructor()) {
						hasVirtualMethod = true;
						if (virMethodName == null)
							virMethodName = icppMethod;
					}
					if (icppMethod.isDestructor()) {
						hasDestructor = true;
						if (icppMethod.isVirtual()) {
							hasVirDestructor = true;
						} else {
							if (destName == null)
								destName = icppMethod;
						}
					}
				}
				if (hasOwnVirtualMethod) {
					// class has own virtual method and base non-virtual
					// destructor
					if (hasDestructor == true && hasVirDestructor == false) {
						return true;
					}
				} else if (hasVirtualMethod) {
					// class has base virtual method and own non-virtual
					// destructor
					if (hasOwnNonVirDestructor == true) {
						return true;
					}
				}
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
