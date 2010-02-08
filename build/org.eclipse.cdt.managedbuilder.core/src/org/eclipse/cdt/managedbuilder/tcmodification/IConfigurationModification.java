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

import org.eclipse.cdt.managedbuilder.core.IBuilder;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IConfigurationModification extends IFolderInfoModification {
	/**
	 * returns a builder currently assigned to this Configuration Modification
	 * @return
	 */
	IBuilder getBuilder();
	
	IBuilder getRealBuilder();
	
	/**
	 * returns a set of compatible builders 
	 * @return
	 */
	IBuilder[] getCompatibleBuilders();
	
	/**
	 * returns the compatibility status for the builder
	 * 
	 * @return
	 */
	CompatibilityStatus getBuilderCompatibilityStatus();
	
	/**
	 * returns the compatibility status for the builder, 
	 * i.e. true when the getCompatibilityStatus() returns an non-ERROR status,
	 * and false otherwise
	 * 
	 * @return
	 */	boolean isBuilderCompatible();
	
	/**
	 * sets the builder to this Configuration Modification
	 * Note that this does NOT apply the builder to the underlying configuration
	 * For applying the Modification settings the {@link IApplicableModification#apply()} 
	 * method should be called
	 * 
	 * @param builder
	 */
	void setBuilder(IBuilder builder);
}
