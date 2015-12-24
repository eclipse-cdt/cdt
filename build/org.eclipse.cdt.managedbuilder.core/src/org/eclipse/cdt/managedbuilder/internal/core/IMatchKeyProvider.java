/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.List;

public interface IMatchKeyProvider<T extends BuildObject> extends Comparable<T> {
	MatchKey<T> getMatchKey();
	
	void setIdenticalList(List<T> list);

	List<T> getIdenticalList();
}
