/*****************************************************************
 * Copyright (c) 2011 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Winnie Lai (Texas Instruments) - Individual Element Number Format example (Bug 202556)
 *****************************************************************/
package org.eclipse.cdt.examples.dsf.pda.ui.viewmodel;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

/**
 * Variable persistable factory for VariablePersistable
 */
public class VariablePersistableFactory implements IElementFactory {

	public static String getFactoryId() {
		return "org.eclipse.cdt.examples.dsf.pda.ui.variablePersitableFactory";
	}
	
	public IAdaptable createElement(IMemento memento) {
		VariablePersistable x = new VariablePersistable();
		x.restore(memento);
		return x;
	}

}
