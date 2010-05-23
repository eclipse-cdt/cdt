/*******************************************************************************
 * Copyright (c) 2009,2010 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	 *            - name of the subdescriptor.
	 * @return
	 */
	IProblemPreference getChildDescriptor(String key);

	/**
	 * Available if type is list or map. Returns array of children.
	 * 
	 * @return
	 */
	IProblemPreference[] getChildDescriptors();

	IProblemPreference addChildDescriptor(IProblemPreference info);

	void removeChildDescriptor(IProblemPreference info);
}
