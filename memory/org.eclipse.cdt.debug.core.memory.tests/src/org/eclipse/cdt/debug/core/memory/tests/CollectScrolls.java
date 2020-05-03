/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.memory.tests;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class CollectScrolls implements Consumer<BigInteger> {

	private final List<BigInteger> collected;

	CollectScrolls() {
		collected = new ArrayList<>();
	}

	@Override
	public void accept(BigInteger t) {
		collected.add(t);
	}

	List<BigInteger> collected() {
		return collected;
	}

}
