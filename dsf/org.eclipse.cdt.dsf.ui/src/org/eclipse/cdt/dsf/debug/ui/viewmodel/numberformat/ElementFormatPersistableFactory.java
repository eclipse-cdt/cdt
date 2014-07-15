/*****************************************************************
 * Copyright (c) 2011, 2014 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Dallaway - initial API and implementation
 *     Marc Khouzam (Ericsson) - Create generic element factory for format persistence (bug 439624) 
 *****************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

/**
 * Element format persistable factory for ElementFormatPersistable
 */
public class ElementFormatPersistableFactory implements IElementFactory {

	// Must match id in plugin.xml
	public static String FACTORY_ID = "org.eclipse.cdt.dsf.ui.elementFormatPersistableFactory"; //$NON-NLS-1$
	
	@Override
	public IAdaptable createElement(IMemento memento) {
		ElementFormatPersistable x = new ElementFormatPersistable();
		x.restore(memento);
		return x;
	}
}
