/*******************************************************************************
 * Copyright (c) 2007, 2012 Symbian Software Ltd. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.internal.index.provider.test;

import org.eclipse.cdt.core.index.provider.IPDOMDescriptor;
import org.eclipse.cdt.core.index.provider.IReadOnlyPDOMProvider;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.runtime.CoreException;

/**
 * Provides no pdom descriptors, used for testing the behaviour of IndexManager over
 * project lifecycles.
 */
public class AbstractDummyProvider implements IReadOnlyPDOMProvider {
	public AbstractDummyProvider() {
	}

	@Override
	public IPDOMDescriptor[] getDescriptors(ICConfigurationDescription config) {
		if (!DummyProviderTraces.getInstance().enabled)
			return new IPDOMDescriptor[0];
		DummyProviderTraces.getInstance().getCfgsTrace(getClass()).add(config);
		return new IPDOMDescriptor[0];
	}

	@Override
	public boolean providesFor(ICProject project) throws CoreException {
		if (!DummyProviderTraces.getInstance().enabled)
			return true;
		DummyProviderTraces.getInstance().getProjectsTrace(getClass()).add(project);
		return true;
	}
}
