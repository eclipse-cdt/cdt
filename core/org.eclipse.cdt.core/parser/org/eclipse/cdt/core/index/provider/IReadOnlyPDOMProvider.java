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
package org.eclipse.cdt.core.index.provider;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;

/**
 * This interface is intended for ISVs to implement when plugging a mechanism
 * for read-only/offline indexes into the CIndex.ReadOnlyPDOMProvider extension point element.
 */
public interface IReadOnlyPDOMProvider extends IIndexProvider {
	/**
	 * Returns the descriptors 
	 * @param cproject
	 * @param config
	 * @return
	 */
	public IPDOMDescriptor[] getDescriptors(ICConfigurationDescription config);
}
