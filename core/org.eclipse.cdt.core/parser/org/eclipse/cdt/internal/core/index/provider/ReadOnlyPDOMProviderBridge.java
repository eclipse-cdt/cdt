/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.provider;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.index.provider.IPDOMDescriptor;
import org.eclipse.cdt.core.index.provider.IReadOnlyPDOMProvider;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.CoreException;

/**
 * Wraps the offline PDOM provider as an IIndexFragmentProvider
 */
public class ReadOnlyPDOMProviderBridge implements IIndexFragmentProvider {
	protected IReadOnlyPDOMProvider opp;

	public ReadOnlyPDOMProviderBridge(IReadOnlyPDOMProvider opp) {
		this.opp = opp;
	}

	public IIndexFragment[] getIndexFragments(ICConfigurationDescription config) throws CoreException {
		IPDOMDescriptor[] descs = opp.getDescriptors(config);

		List preresult = new ArrayList();

		if(descs!=null) {
			for(int i=0; i<descs.length; i++) {
				PDOM pdom= PDOMCache.getInstance().getPDOM(descs[i].getLocation(), descs[i].getIndexLocationConverter()); 
				if(pdom!=null) {
					preresult.add(pdom);
				}
			}
		}

		return (IIndexFragment[]) preresult.toArray(new IIndexFragment[preresult.size()]);
	}

	public boolean providesFor(ICProject cproject) throws CoreException {
		return opp.providesFor(cproject);
	}
}
