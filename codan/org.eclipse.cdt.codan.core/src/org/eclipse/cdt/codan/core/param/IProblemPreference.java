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
 * Problem preference. If problem has more than one it can be composite, i.e.
 * map. Instead of implementing this interface clients must extend
 * {@link AbstractProblemPreference} class.
 * 
 * Problem Preference constist of preference metadata
 * (IProblemPreferenceDescriptor)
 * and value of preference (IProblemPreferenceValue).
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IProblemPreference extends Cloneable, IProblemPreferenceValue,
		IProblemPreferenceDescriptor {
}
