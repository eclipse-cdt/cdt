/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation. All rights reserved.
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
 * David Dykstal (IBM) - [226561] Add API markup for noextend / noimplement where needed
 * David Dykstal (IBM) - [261486][api] add noextend to interfaces that require it
 ********************************************************************************/

package org.eclipse.rse.core.references;

/**
 * An interface to encapsulate the operations required of an object which
 * supports references to it by other objects (IRSEReferencingObject).
 * This type of class needs to support maintaining an in-memory list of
 * all who reference it so that list can be following on delete and
 * rename operations.
 * <p>
 * These references are not persistent. Persistent references are managed
 * by the subtype IRSEPersistableReferencedObject.
 * @noimplement This interface is not intended to be implemented by clients.
 * The standard implementations are included in the framework.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IRSEReferencedObject extends IRSEBaseReferencedObject {

}