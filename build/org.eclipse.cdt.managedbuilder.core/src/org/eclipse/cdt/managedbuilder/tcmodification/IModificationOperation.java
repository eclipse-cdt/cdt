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
public interface IModificationOperation {
	/**
	 * specifies the replacement tool, i.e. tool that is to replace the tools
	 * presented with the IToolModification containing this operation
	 * For project IToolModification the replacement tool is a system tool
	 * that is to replace the project tool
	 * For system IToolModification the replacement tool is a project tool
	 * to be replaced with the system tool
	 */
	ITool getReplacementTool();
	
	/**
	 * returns the containing {@link IToolModification}
	 */
	IToolModification getToolModification();
}
