/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.model;
/**
 * Interface that listeners interesting in changes to local resources in the RSE model
 *  implement, and subsequently register their interest, via SystemRegistry.
 * <p>
 * If you list any of the resource types defined in {@link org.eclipse.rse.model.ISystemModelChangeEvents}
 *  you should monitor by implementing this interface, and registering with the 
 *  system registry via {@link org.eclipse.rse.model.ISystemRegistry#addSystemModelChangeListener(ISystemModelChangeListener)}.
 *  In your view's dispose method, you must also de-register by calling
 *  {@link org.eclipse.rse.model.ISystemRegistry#removeSystemModelChangeListener(ISystemModelChangeListener)}.
 * <p>
 * If you are interesting in firing model change events, see 
 * {@link org.eclipse.rse.model.ISystemRegistry#fireModelChangeEvent(int, int, Object, String)}.
 */
public interface ISystemModelChangeListener 
{

	/**
	 * This is the method in your class that will be called when a resource in the 
	 *  RSE model changes. You will be called after the resource is changed.
	 * @see ISystemModelChangeEvent
	 */
    public void systemModelResourceChanged(ISystemModelChangeEvent event);
}