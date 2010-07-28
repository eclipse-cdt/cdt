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
	 * @return a builder currently assigned to this Configuration Modification
	 */
	IBuilder getBuilder();
	
	IBuilder getRealBuilder();
	
	/**
	 * @return a set of compatible builders 
	 */
	IBuilder[] getCompatibleBuilders();
	
	/**
	 * @return the compatibility status for the builder
	 */
	CompatibilityStatus getBuilderCompatibilityStatus();
	
	/**
	 * @return the compatibility status for the builder, 
	 * i.e. true when the getCompatibilityStatus() returns an non-ERROR status,
	 * and false otherwise
	 */
	boolean isBuilderCompatible();
	
	/**
	 * sets the builder to this Configuration Modification
	 * Note that this does NOT apply the builder to the underlying configuration
	 * For applying the Modification settings the {@link IApplicableModification#apply()} 
	 * method should be called
	 */
	void setBuilder(IBuilder builder);
}
