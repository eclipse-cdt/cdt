/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - Ted Williams - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.internal.provisional.model;


/**
 * This interface is EXPERIMENTAL.
 * 
 * @since 1.1
 */
public interface IMemoryBlockUpdatePolicyProvider 
{
	public String[] getUpdatePolicies();

	public String getUpdatePolicyDescription(String id);
	
	public String getUpdatePolicy();
	
	public void setUpdatePolicy(String id);
	
	public void clearCache();
}
