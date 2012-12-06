/*****************************************************************
 * Copyright (c) 2011, 2012 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Dallaway - DSF-GDB register format persistence (bug 395909)
 *****************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

/**
 * Register persistable factory for ElementFormatPersistable
 */
public class RegisterPersistableFactory implements IElementFactory {

	public static String FACTORY_ID = "org.eclipse.cdt.dsf.gdb.ui.registerPersistableFactory"; //$NON-NLS-1$
	
	@Override
	public IAdaptable createElement(IMemento memento) {
		ElementFormatPersistable x = new ElementFormatPersistable(FACTORY_ID);
		x.restore(memento);
		return x;
	}

}
