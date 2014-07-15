/*****************************************************************
 * Copyright (c) 2011, 2014 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *****************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel;

import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

/**
 * Persistable factory the simple map persistable.
 * 
 * @since 2.5
 * 
 * @see SimpleMapPersistable
 */
public class SimpleMapPersistableFactory implements IElementFactory {

	public static String getFactoryId() {
		return "org.eclipse.cdt.dsf.debug.simpleMapPersistableFactory"; //$NON-NLS-1$
	}
	
	@Override
	public IAdaptable createElement(IMemento memento) {
	    SimpleMapPersistable<Object> x = new SimpleMapPersistable<>();
		try {
            x.restore(memento);
        } catch (ClassNotFoundException e) {
            DsfUIPlugin.log(new Status(
                IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, "Cannot restore persistable." , e)); //$NON-NLS-1$
            return null;
        }
		return x;
	}
}
