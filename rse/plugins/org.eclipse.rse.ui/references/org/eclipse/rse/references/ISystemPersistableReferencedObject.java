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

import org.eclipse.rse.core.references.IRSEBasePersistableReferencedObject;
import org.eclipse.rse.core.references.IRSEReferencedObject;

/**
 * A simple class that implements ISystemPersistableReferencedObject.
 * This is an object that can have shadow (reference) objects, which simply
 * contain a pointer (in memory) to this object, and a copy of this object's
 * unique name or key (for storing on disk).
 * <p>
 * The intention is that in your Rose model, your class extends this class.
 * Do this for any class for which you wish to persist a list of references to
 * that class.
 */
/**
 * @lastgen interface SystemPersistableReferencedObject extends SystemReferencedObject {}
 */

public interface ISystemPersistableReferencedObject extends IRSEReferencedObject, IRSEBasePersistableReferencedObject
{

}