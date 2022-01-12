/*****************************************************************
 * Copyright (c) 2011, 2014 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *****************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel;

import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.core.runtime.CoreException;
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
		// Must be the same id as the one used in the plugin.xml file
		return "org.eclipse.cdt.dsf.ui.simpleMapPersistableFactory"; //$NON-NLS-1$
	}

	@Override
	public IAdaptable createElement(IMemento memento) {
		try {
			SimpleMapPersistable<Object> x = new SimpleMapPersistable<>(memento);
			return x;
		} catch (CoreException e) {
			DsfUIPlugin.log(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, "Cannot restore persistable.", e)); //$NON-NLS-1$
		}
		return null;
	}
}
