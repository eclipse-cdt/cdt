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
package org.eclipse.cdt.dsf.ui.viewmodel.update;

/**
 * An update policy decorator which can override behaviour of an underlying update policy.
 *
 * @since 1.1
 */
public abstract class UpdatePolicyDecorator implements IVMUpdatePolicy {

	private final IVMUpdatePolicy fBasePolicy;

	protected UpdatePolicyDecorator(IVMUpdatePolicy base) {
		fBasePolicy = base;
	}

	protected final IVMUpdatePolicy getBaseUpdatePolicy() {
		return fBasePolicy;
	}

	@Override
	public final String getID() {
		return fBasePolicy.getID();
	}

	@Override
	public String getName() {
		return fBasePolicy.getName();
	}

	@Override
	public IElementUpdateTester getElementUpdateTester(Object event) {
		return fBasePolicy.getElementUpdateTester(event);
	}
}
