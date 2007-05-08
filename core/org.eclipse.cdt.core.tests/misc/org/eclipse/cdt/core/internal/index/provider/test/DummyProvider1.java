/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.internal.index.provider.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.index.provider.IPDOMDescriptor;
import org.eclipse.cdt.core.index.provider.IReadOnlyPDOMProvider;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.runtime.CoreException;

/**
 * Provides no pdom descriptors, used for testing the behaviour of IndexManager over
 * project lifecycles.
 */
public class DummyProvider1 implements IReadOnlyPDOMProvider {
	static List prjTrace= Collections.synchronizedList(new ArrayList());
	static List cfgTrace= Collections.synchronizedList(new ArrayList());
	
	public static void reset() {
		prjTrace.clear();
		cfgTrace.clear();
	}
	
	public static List getProjectsTrace() {
		return prjTrace;
	}
	
	public static List getCfgsTrace() {
		return cfgTrace;
	}
	
	public IPDOMDescriptor[] getDescriptors(ICConfigurationDescription config) {
		cfgTrace.add(config);
		return new IPDOMDescriptor[0];
	}
	
	public boolean providesFor(ICProject project) throws CoreException {
		prjTrace.add(project);
		return true;
	}
}
