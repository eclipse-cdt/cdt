/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.core.runtime.CoreException;


/**
 * For implementation sharing between PDOMCPPClassType and PDOMCPPClassSpecialization
 */
class PDOMClassUtil {
	static class FieldCollector implements IPDOMVisitor {
		private List fields = new ArrayList();
		public boolean visit(IPDOMNode node) throws CoreException {
			if (node instanceof ICPPField)
				fields.add(node);
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
			if (node instanceof ICPPConstructor)
				fConstructors.add(node);
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
		private final boolean acceptImplicit;
		private final boolean acceptAll;
		public MethodCollector(boolean acceptImplicit) {
			this(acceptImplicit, true);
		}
		public MethodCollector(boolean acceptImplicit, boolean acceptExplicit) {
			this.methods = new ArrayList();
			this.acceptImplicit= acceptImplicit;
			this.acceptAll= acceptImplicit && acceptExplicit;
		}
		public boolean visit(IPDOMNode node) throws CoreException {
			if (node instanceof ICPPMethod) {
				if (acceptAll || ((ICPPMethod) node).isImplicit() == acceptImplicit) {
					methods.add(node);
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
