/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     TimeSys Corporation - initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.core;
public interface IToolReference extends ITool {
	
	/**
	 * Answers <code>true</code> if the reference is a reference to the 
	 * tool specified in the argument.
	 * 
	 * @param target the tool that should be tested
	 * @return boolean
	 */
	public boolean references(ITool tool);
	
	/**
	 * Answers the tool that the reference has been created for.
	 * 
	 * @return
	 */
	public ITool getTool();
	
}
