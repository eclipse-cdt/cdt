/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/


package org.eclipse.cdt.managedbuilder.core;

public interface IConfigurationNameProvider {
	
	/*
	 * Returns the new  unique configuration name based on the 'configuration'
	 * object and the list of configuration names already in use in the project.
	 *  
	 */

	String getNewConfigurationName(IConfiguration configuration, String [] usedConfigurationNames );
}
