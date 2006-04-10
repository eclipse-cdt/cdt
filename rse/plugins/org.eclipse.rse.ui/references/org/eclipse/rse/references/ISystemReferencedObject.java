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

package org.eclipse.rse.references;
/**
 * A class to encapsulate the operations required of an object which
 * supports references to it by other objects (SystemReferencingObject).
 * This type of class needs to support maintaining an in-memory list of
 * all who reference it so that list can be following on delete and
 * rename operations.
 * <p>
 * These references are not persistent. Persistent references are managed
 * by the subclass SystemPersistableReferencedObject.
 */
/**
 * @lastgen interface SystemReferencedObject  {}
 */

public interface ISystemReferencedObject extends ISystemBaseReferencedObject{

}