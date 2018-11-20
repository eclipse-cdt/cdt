/*******************************************************************************
 * Copyright (c) 2018 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lidia Popescu - [536255] initial API and implementation. Extension point for open call hierarchy view
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.callhierarchy.extension;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.model.ext.FunctionDeclarationHandle;
import org.eclipse.cdt.ui.ICHEContentProvider;
import org.eclipse.jface.viewers.IOpenListener;

/**
 * This class implements ICHEProvider and provides test information
 * */
public class CHContentProvider implements ICHEContentProvider {

	@Override
	public Object[] asyncComputeExtendedRoot(Object parentElement) {
		Object[] object = null;
		if (parentElement instanceof ICElement) {
			ICElement element = (ICElement) parentElement;
			if (isDslFunction(element)) {
				// check if this function declaration comes from a DSL file
				DslNode node = new DslNode(element);
				node.setProject(element.getCProject());
				return new Object[] { node };
			}
		}
		return object;
	}

	@Override
	public IOpenListener getCCallHierarchyOpenListener() {
		return new CHOpenListener();
	}

	/**
	 * E.g. A custom implementation, suppose that functions that ends with
	 * "_dsl" have been originally declared in a DSL file.
	 * @param cElement
	 * @return
	 */
	private static boolean isDslFunction(ICElement cElement) {
		if (cElement instanceof FunctionDeclarationHandle) {
			FunctionDeclarationHandle f = (FunctionDeclarationHandle) cElement;
			if (f.getElementName() != null & f.getElementName().endsWith("_dsl")) {
				return true;
			}
		}
		return false;
	}

}
