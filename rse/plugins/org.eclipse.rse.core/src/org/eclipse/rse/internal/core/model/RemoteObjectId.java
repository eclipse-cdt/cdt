/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 *******************************************************************************/

package org.eclipse.rse.internal.core.model;

import org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISystemDragDropAdapter;

/**
 * A RemoteObjectId uniquely identifies an object in the RSE Tree, and is valid
 * as long as its parent subsystem exists.
 *
 * It holds a Subsystem instance, and an absolute name. This class is meant as a
 * short-lived object for using in local comparisons only. Since it is only
 * valid as long as its parent subsystem exists, RemoteObjectId's cannot be
 * usefully persisted.
 *
 * This class is immutable and therefore thread-safe.
 * 
 * TODO It is unfortunate that we need to access an ISystemDragDropAdapter just
 * to get the ISubSystem. A lower-level object identifier should be available,
 * but making that happen would break almost all clients since their adapter
 * factories are not prepared for it.
 *
 * TODO This class would make sense to eventually promote to API. But currently,
 * it isn't used anywhere and is therefore dead code. But it is kept here in
 * order to be convey the original idea and have it available when needed.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the <a href="http://www.eclipse.org/tm/">Target Management</a>
 * team.
 * </p>
 *
 * @since org.eclipse.rse.core 3.0
 */
public final class RemoteObjectId {
	// Subsystem instance, may be null
	private final ISubSystem fSubSystem;
	// Absolute name, must not be null
	private final String fAbsoluteName;

	/**
	 * Construct a RemoteObjectId.
	 *
	 * @param ss subsystem instance. May be <code>null</code> if unknown.
	 * @param absoluteName absolute name. Must not be <code>null</code>.
	 */
	private RemoteObjectId(ISubSystem ss, String absoluteName) {
		assert absoluteName != null;
		fSubSystem = ss;
		fAbsoluteName = absoluteName;
	}

	/**
	 * Create an RemoteObjectId for any kind of Object. Tries to adapt the given
	 * object to an RSE {@link ISystemDragDropAdapter} or
	 * {@link IRemoteObjectIdentifier}, in order to obtain an absolute name
	 * and subsystem instance.
	 *
	 * @param element an element that needs to be adaptable such that an
	 *            absolute name can be obtained.
	 * @return an RemoteObjectId for the given element, or <code>null</code> if
	 *         the given element is not adaptable to the necessary RSE
	 *         adapter type, or no absolute name can be obtained.
	 */
	public static RemoteObjectId create(Object element) {
		ISystemDragDropAdapter dda = SystemRegistry.getSystemDragDropAdapter(element);
		if (dda != null) {
			String absoluteName = dda.getAbsoluteName(element);
			if (absoluteName != null) {
				return new RemoteObjectId(dda.getSubSystem(element), absoluteName);
			}
		}
		return null;
	}

	/**
	 * Return the absolute name of this object id.
	 *
	 * @return the absolute name
	 */
	public final String getAbsoluteName() {
		return fAbsoluteName;
	}

	/**
	 * Return the subsystem instance of this object id.
	 *
	 * @return the subsystem instance. May be <code>null</code> if the
	 *         subsystem instance could not be determined.
	 */
	public final ISubSystem getSubSystem() {
		return fSubSystem;
	}

	public final boolean equals(Object o) {
		try {
			// We're optimistic and think nobody will ever call
			// equals() on us with any other kind of Object
			RemoteObjectId other = (RemoteObjectId) o;
			return (fSubSystem == other.fSubSystem) && (fAbsoluteName.equals(other.fAbsoluteName));
		} catch (Exception e) {
			return false;
		}
	}

	public final int hashCode() {
		int hashCode = fAbsoluteName.hashCode();
		if (fSubSystem != null) {
			hashCode = hashCode * 31 + fSubSystem.hashCode();
		}
		return hashCode;
	}
}