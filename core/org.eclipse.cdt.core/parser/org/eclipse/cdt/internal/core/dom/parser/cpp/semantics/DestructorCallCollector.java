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

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTImplicitDestructorName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper.MethodKind;

/**
 * A visitor that collects temporaries that have destructors.
 */
public class DestructableTemporariesCollector extends ASTVisitor {
	private final IASTImplicitDestructorNameOwner owner;
	private IASTImplicitDestructorName[] destructorNames = IASTImplicitDestructorName.EMPTY_NAME_ARRAY;

	public DestructableTemporariesCollector(IASTImplicitDestructorNameOwner owner) {
		this.owner = owner;
		shouldVisitImplicitNames = true;
	}

	@Override
	public int visit(IASTName name) {
		if (name instanceof IASTImplicitName) {
			IBinding binding = name.resolveBinding();
			if (binding instanceof ICPPConstructor) {
				ICPPClassType classType = ((ICPPConstructor) binding).getClassOwner();
				ICPPMethod destructor = ClassTypeHelper.getMethodInClass(classType, MethodKind.DTOR, name);
				if (destructor != null) {
					CPPASTImplicitDestructorName destructorName =
							new CPPASTImplicitDestructorName(destructor.getNameCharArray(), owner, (IASTImplicitName) name);
					destructorNames = ArrayUtil.append(destructorNames, destructorName);
				}
			}
		}
		return PROCESS_CONTINUE;
	}

	public IASTImplicitDestructorName[] getDestructorCalls() {
		destructorNames = ArrayUtil.trim(destructorNames);
		return destructorNames;
	}
}
