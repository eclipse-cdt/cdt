/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
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

public interface IToolModification extends IModification {
	/**
	 * returns the detailed compatibility status for this tool
	 * for project tools specifies whether the tool is compatible
	 * with the configuration
	 * for system tools specifies whether the tool can be added to the
	 * configuration
	 * 
	 * @return
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
	 * 
	 * @return
	 */
	boolean isCompatible();
	
	/**
	 * returns the tool of the given Modification 
	 * @return
	 */
	ITool getTool();
	
	/**
	 * specifies whether the current tools belongs to the resource configuration
	 * @return
	 */
	boolean isProjectTool();
	
	/**
	 * returns the list of supported operations
	 * @return
	 */
	IModificationOperation[] getSupportedOperations();
}
