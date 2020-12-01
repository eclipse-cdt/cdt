/*******************************************************************************
 * Copyright (c) 2020 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.debug.core.memory.transport;

import java.math.BigInteger;

@FunctionalInterface
public interface IScrollMemory {
	void accept(BigInteger t);

	static IScrollMemory ignore() {
		return t -> {
			// ignore
		};
	}

}