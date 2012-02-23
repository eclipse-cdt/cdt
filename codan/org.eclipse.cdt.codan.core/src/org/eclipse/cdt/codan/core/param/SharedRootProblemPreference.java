/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.param;


/**
 * Preferences that can be shared among several problems.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
public class SharedRootProblemPreference extends RootProblemPreference {
	@Override
	public Object clone() {
		SharedRootProblemPreference map = (SharedRootProblemPreference) super.clone();
		// alruiz: sharing the internal hash is the only way I could make this work.
		map.hash = hash;
		return map;
	}
}
