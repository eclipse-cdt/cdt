/*******************************************************************************
 * Copyright (c) 2009,2011 QNX Software Systems
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
public interface IProblemPreference extends IProblemPreferenceValue, IProblemPreferenceDescriptor {
}
