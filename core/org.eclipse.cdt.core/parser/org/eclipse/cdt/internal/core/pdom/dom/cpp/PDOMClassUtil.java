/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.core.runtime.CoreException;


/**
 * For implementation sharing between PDOMCPPClassType and PDOMCPPClassSpecialization
 */
class PDOMClassUtil {
	static class FieldCollector implements IPDOMVisitor {
		private List fields = new ArrayList();
		public boolean visit(IPDOMNode node) throws CoreException {
			if (node instanceof ICPPField) {
				ICPPField field= (ICPPField) node;
				if (IndexFilter.ALL_DECLARED_OR_IMPLICIT.acceptBinding(field)) {
					fields.add(node);
				}
			}
			return false;
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
		public ICPPField[] getFields() {
			return (ICPPField[])fields.toArray(new ICPPField[fields.size()]);
		}
	}
	
	static class ConstructorCollector implements IPDOMVisitor {
		private List fConstructors = new ArrayList();
		public boolean visit(IPDOMNode node) throws CoreException {
			if (node instanceof ICPPConstructor) {
				ICPPConstructor cons= (ICPPConstructor) node;
				if (IndexFilter.ALL_DECLARED_OR_IMPLICIT.acceptBinding(cons)) {
					fConstructors.add(cons);
				}
			}
			return false;
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
		public ICPPConstructor[] getConstructors() {
			return (ICPPConstructor[])fConstructors.toArray(new ICPPConstructor[fConstructors.size()]);
		}
	}

	static class MethodCollector implements IPDOMVisitor {
		private final List methods;
		private final boolean acceptNonImplicit;
		private final IndexFilter filter;
		public MethodCollector(boolean acceptImplicit) {
			this(acceptImplicit, true);
		}
		public MethodCollector(boolean acceptImplicit, boolean acceptNonImplicit) {
			this.methods = new ArrayList();
			this.acceptNonImplicit= acceptNonImplicit;
			this.filter= acceptImplicit ? IndexFilter.ALL_DECLARED_OR_IMPLICIT : IndexFilter.ALL_DECLARED;
		}
		public boolean visit(IPDOMNode node) throws CoreException {
			if (node instanceof ICPPMethod) {
				ICPPMethod method= (ICPPMethod) node;
				if (filter.acceptBinding(method)) {
					if (acceptNonImplicit || method.isImplicit()) {
						methods.add(node);
					}
				}
			}
			return false; // don't visit the method
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
		public ICPPMethod[] getMethods() {
			return (ICPPMethod[])methods.toArray(new ICPPMethod[methods.size()]); 
		}
	}
}
