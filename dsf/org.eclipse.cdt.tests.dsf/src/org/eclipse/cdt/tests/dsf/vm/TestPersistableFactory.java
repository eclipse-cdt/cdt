/*****************************************************************
 * Copyright (c) 2011 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
