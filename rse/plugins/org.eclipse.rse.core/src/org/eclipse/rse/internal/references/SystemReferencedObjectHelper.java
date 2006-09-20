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

package org.eclipse.rse.internal.references;

import java.text.MessageFormat;
import java.util.Vector;

import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.references.IRSEBaseReferencedObject;
import org.eclipse.rse.core.references.IRSEBaseReferencingObject;
import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * This is a class that implements all the methods in the IRSEReferencedObject.
 * It makes implementing this interface trivial.
 * The easiest use of this class is to subclass it, but since that is not
 * always possible, it is not abstract and hence can be leveraged via containment.
 */
public class SystemReferencedObjectHelper implements IRSEBaseReferencedObject {

	private Vector referencingObjects = new Vector();
	private IRSEBaseReferencedObject parent = null;

	/**
	 * Constructor for SystemReferencedObjectHelper
	 * @param parent the SystemReferencedObject creating this helper
	 */
	public SystemReferencedObjectHelper(IRSEBaseReferencedObject parent) {
		this.parent = parent;
	}

	/**
	 * @see IRSEBaseReferencedObject#addReference(IRSEBaseReferencingObject)
	 */
	public int addReference(IRSEBaseReferencingObject ref) {
//		String fromName = getReferencingName(ref);
//		String toName = getReferencedName();
//		System.out.println(MessageFormat.format("Adding reference from {0} to {1}", new Object[] {fromName, toName}));
		referencingObjects.addElement(ref);
		return referencingObjects.size();
	}

	private String getReferencedName() {
		String toName = "unknown";
		if (parent instanceof ISystemFilterPool) {
			ISystemFilterPool fp = (ISystemFilterPool) parent;
			toName = fp.getName();
		}
		return toName;
	}
	
	private String getReferencingName(IRSEBaseReferencingObject object) {
		String fromName = "unknown";
		if (object instanceof ISystemFilterPoolReference) {
			ISystemFilterPoolReference fpr = (ISystemFilterPoolReference) object;
			ISystemFilterPoolReferenceManagerProvider provider = fpr.getProvider();
			String prefix = "unknown|unknown|unknown";
			if (provider instanceof ISubSystem) {
				ISubSystem subsystem = (ISubSystem) provider;
				IHost host = subsystem.getHost();
				prefix = host.getAliasName() + "|" + subsystem.getName();
				fromName = prefix + fpr.getName();
			}
		}
		return fromName;
	}

	/**
	 * @see IRSEBaseReferencedObject#removeReference(IRSEBaseReferencingObject)
	 */
	public int removeReference(IRSEBaseReferencingObject ref) {
//		String fromName = getReferencingName(ref);
//		String toName = getReferencedName();
//		System.out.println(MessageFormat.format("Removing reference from {0} to {1}", new Object[] {fromName, toName}));
		boolean found = referencingObjects.removeElement(ref);
		assertThis(!found, "removeReference failed for " + ref); //$NON-NLS-1$
		return referencingObjects.size();
	}

	/**
	 * @see IRSEBaseReferencedObject#getReferenceCount()
	 */
	public int getReferenceCount() {
		return referencingObjects.size();
	}

	/**
	 * Clear the list of referenced objects.
	 */
	public void removeAllReferences() {
		IRSEBaseReferencingObject[] references = getReferencingObjects();
		for (int i = 0; i < references.length; i++) {
			IRSEBaseReferencingObject reference = references[i];
			removeReference(reference);
		}
//		referencingObjects.removeAllElements();
	}

	/**
	 * @see IRSEBaseReferencedObject#getReferencingObjects()
	 */
	public IRSEBaseReferencingObject[] getReferencingObjects() {
		IRSEBaseReferencingObject[] references = new IRSEBaseReferencingObject[referencingObjects.size()];
		referencingObjects.toArray(references);
		return references;
	}

	/**
	 * Assertion method for debugging purposes. All instances of assertion failure should be removed by 
	 * release.
	 * @param assertion a boolean (usually an expression) that is to be tested
	 * @param msg the message printed on System.out
	 */
	protected void assertThis(boolean assertion, String msg) {
//		if (!assertion) System.out.println("ASSERTION FAILED IN SystemReferencedObject: " + msg); //$NON-NLS-1$
	}

}