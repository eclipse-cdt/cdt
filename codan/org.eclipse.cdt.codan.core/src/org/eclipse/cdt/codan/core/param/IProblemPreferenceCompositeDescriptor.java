/*******************************************************************************
 * Copyright (c) 2009,2010 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.param;

/**
 * Composite descriptor. For descriptors like map and list.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IProblemPreferenceCompositeDescriptor {
	/**
	 * Available if type is composite. Returns value of subdescriptor with the
	 * name of key. For the "list" type key is the number (index).
	 *
	 * @param key
	 *        - name of the subdescriptor.
	 * @return child preference of the given key
	 */
	IProblemPreference getChildDescriptor(String key);

	/**
	 * Available for composite types. Returns array of children.
	 *
	 * @return array of children. 0 size of none.
	 */
	IProblemPreference[] getChildDescriptors();

	/**
	 * Add preference
	 *
	 * @param preference
	 * @return added preference
	 */
	IProblemPreference addChildDescriptor(IProblemPreference preference);

	/**
	 * Remove preference
	 *
	 * @param preference
	 */
	void removeChildDescriptor(IProblemPreference preference);
}
