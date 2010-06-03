/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public AbstractDummyProvider() {}
	
	public IPDOMDescriptor[] getDescriptors(ICConfigurationDescription config) {
		if (!DummyProviderTraces.getInstance().enabled)
			return new IPDOMDescriptor[0];
		DummyProviderTraces.getInstance().getCfgsTrace(getClass()).add(config);
		return new IPDOMDescriptor[0];
	}
	
	public boolean providesFor(ICProject project) throws CoreException {
		if (!DummyProviderTraces.getInstance().enabled)
			return true;
		DummyProviderTraces.getInstance().getProjectsTrace(getClass()).add(project);
		return true;
	}
}
