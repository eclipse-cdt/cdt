/********************************************************************************
 * Copyright (c) 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.core;

import org.eclipse.core.resources.IResourceChangeListener;

public interface ISystemResourceListener extends IResourceChangeListener
{
	public void ensureOnResourceEventListening();
    public void turnOffResourceEventListening();
    public void turnOnResourceEventListening();
    public void addResourceChangeListener(IResourceChangeListener l);
	public void removeResourceChangeListener(IResourceChangeListener l);

    

}
