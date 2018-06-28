/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lidia Popescu - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.callhierarchy.extension;

import org.eclipse.jface.viewers.IOpenListener;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.ICHEContentProvider;

import org.eclipse.cdt.internal.core.model.ext.FunctionDeclarationHandle;

/**
 * This class implements ICHEProvider and provides test information
 * */
public class CHContentProvider implements ICHEContentProvider {

	@Override
	public Object[] asyncComputeExtendedRoot(Object parentElement) {
		Object[] object  =null;
		if (parentElement instanceof ICElement) {
			ICElement element = (ICElement)parentElement;
			DslNode node = new DslNode(element);
			if ( isDslFunction(element)) {
				// check if this function declaration comes from a DSL file
				return new Object[]{node};
			}
		}
		return object;
	}

	@Override
	public IOpenListener getCCallHierarchyOpenListener() {
		return new CHOpenListener();
	}

	/**
	 * E.g. A custom implementation, suppose that functions that ends with "_dsl" have been originally declared in a DSL file.
	 * */
	private static boolean isDslFunction(ICElement cElement) {
		if (cElement instanceof FunctionDeclarationHandle) {
			FunctionDeclarationHandle f = (FunctionDeclarationHandle)cElement;
			if (f.getElementName() !=null & f.getElementName().endsWith("_dsl")) {
				return true;
			}
		}
		return false;
	}

}
