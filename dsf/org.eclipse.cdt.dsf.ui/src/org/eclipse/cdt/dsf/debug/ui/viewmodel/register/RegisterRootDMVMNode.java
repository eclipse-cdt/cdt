/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.register;

import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.RootDMVMNode;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;

/*
 * We are extending the ROOT VM node for the register view so we can
 * provide Memento providers for the root node. In the Register VM
 * Provider we are returning a pseudo VMContext selection when the
 * original input is a child of an execution context we return a selection
 * which represents an Execution Context instead.  This ensures that the
 * Register View does not collapse and redraw when going from frame to frame
 * when stepping or just when selecting within the view.
 */
public class RegisterRootDMVMNode extends RootDMVMNode implements IElementMementoProvider {

	public RegisterRootDMVMNode(AbstractVMProvider provider) {
		super(provider);
	}

	@Override
	public void compareElements(IElementCompareRequest[] requests) {
		for (IElementMementoRequest request : requests) {
			request.done();
		}
	}

	@Override
	public void encodeElements(IElementMementoRequest[] requests) {

		for (IElementMementoRequest request : requests) {
			request.done();
		}
	}
}
