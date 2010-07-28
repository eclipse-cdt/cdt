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

import org.eclipse.cdt.managedbuilder.core.ITool;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IToolModification extends IModification {
	/**
	 * returns the detailed compatibility status for this tool
	 * for project tools specifies whether the tool is compatible
	 * with the configuration
	 * for system tools specifies whether the tool can be added to the
	 * configuration
	 */
	CompatibilityStatus getCompatibilityStatus();
	
	/**
	 * returns the compatibility status for this tool, 
	 * i.e. true when the getCompatibilityStatus() returns an non-ERROR status,
	 * and false otherwise
	 * 
	 * for project tools specifies whether the tool is compatible
	 * with the configuration
	 * for system tools specifies whether the tool can be added to the
	 * configuration
	 */
	boolean isCompatible();
	
	/**
	 * returns the tool of the given Modification 
	 */
	ITool getTool();
	
	/**
	 * specifies whether the current tools belongs to the resource configuration
	 */
	boolean isProjectTool();
	
	/**
	 * returns the list of supported operations
	 */
	IModificationOperation[] getSupportedOperations();
}
