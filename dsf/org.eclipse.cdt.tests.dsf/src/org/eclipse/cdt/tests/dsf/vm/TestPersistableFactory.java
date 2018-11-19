/*****************************************************************
 * Copyright (c) 2011, 2015 Texas Instruments and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Winnie Lai (Texas Instruments) - Individual Element Number Format (Bug 202556)
 *****************************************************************/
package org.eclipse.cdt.tests.dsf.vm;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

/**
 * Factory for TestPersitable
 */
public class TestPersistableFactory implements IElementFactory {

	static String factoryId = "org.eclipse.cdt.tests.dsf.vm.testPersistableFactory";

	@Override
	public IAdaptable createElement(IMemento memento) {
		TestPersistable x = new TestPersistable();
		x.restore(memento);
		return x;
	}

}
