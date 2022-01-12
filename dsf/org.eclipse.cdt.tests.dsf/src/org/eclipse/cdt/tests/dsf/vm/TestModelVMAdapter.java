/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
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
package org.eclipse.cdt.tests.dsf.vm;

import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * @since 2.2
 */
public class TestModelVMAdapter extends AbstractVMAdapter {

	@Override
	protected IVMProvider createViewModelProvider(IPresentationContext context) {
		return new TestModelVMProvider(this, context);
	}

	public TestModelVMProvider getTestModelProvider(IPresentationContext context) {
		return (TestModelVMProvider) getVMProvider(context);
	}
}
