/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.view;

/**
 * An adapter can elect to suppot this interface and answer
 * whether it support copy/move/drop from the source (given adapter 
 * type) to the specified destination.
 * @author A.Kent Hawley
 */
public interface ISystemViewDropDestination 
{

	/**
	 * ask source adapter if it supports drop of its type on this target
	 * @param resource (type) in question
	 * @return true if copy/move/drop is supported
	 */
	public boolean supportDropDestination(Object target);
}