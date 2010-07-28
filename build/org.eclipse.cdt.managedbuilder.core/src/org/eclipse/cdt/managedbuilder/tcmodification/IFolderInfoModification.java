/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.tcmodification;

import org.eclipse.cdt.managedbuilder.core.IToolChain;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IFolderInfoModification extends IToolListModification {

	/**
	 * @return a set of tool-chains compatible with the current one
	 */
	IToolChain[] getCompatibleToolChains();
	
	/**
	 * @return compatibility status for the current tool-chain 
	 */
	CompatibilityStatus getToolChainCompatibilityStatus();
	
	/**
	 * @return whether the current tool-chain is compatible,
	 * i.e. whether the {@link #getToolChainCompatibilityStatus()} returns a
	 * non-ERROR status
	 */
	boolean isToolChainCompatible();
	
	/**
	 * @return the current tool-chain
	 */
	IToolChain getToolChain();

	/**
	 * sets the current tool-chain
	 * once the tool-chain is changed all child Tool and Builder Modifications
	 * become invalid, i.e. one should re-query the modifications needed
	 */
	void setToolChain(IToolChain tc);
}
