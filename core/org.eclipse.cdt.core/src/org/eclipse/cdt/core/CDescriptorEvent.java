/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import java.util.EventObject;

import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @deprecated register {@link ICProjectDescriptionListener} for {@link CProjectDescriptionEvent}
 */
@Deprecated
public class CDescriptorEvent extends EventObject {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 3257009869059143225L;
	public static final int CDTPROJECT_CHANGED = 1;
	public static final int CDTPROJECT_ADDED = 2;
	public static final int CDTPROJECT_REMOVED = 3;

	public static final int OWNER_CHANGED = 0x10;
	public static final int EXTENSION_CHANGED = 0x20;

	private static final int FLAGS_MASK = 0xf;

	int fType;

	public CDescriptorEvent(ICDescriptor descriptor, int type, int flags) {
		super(descriptor);
		fType = type | flags;
	}

	public ICDescriptor getDescriptor() {
		return (ICDescriptor) getSource();
	}

	public int getType() {
		return fType & FLAGS_MASK;
	}

	public int getFlags() {
		return fType & ~FLAGS_MASK;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		switch (getType()) {
		case CDTPROJECT_ADDED:
			buf.append("CDTPROJECT_ADDED"); //$NON-NLS-1$
			break;
		case CDTPROJECT_REMOVED:
			buf.append("CDTPROJECT_REMOVED"); //$NON-NLS-1$
			break;
		case CDTPROJECT_CHANGED:
			buf.append("CDTPROJECT_CHANGED"); //$NON-NLS-1$
			break;
		}
		if ((getFlags() & OWNER_CHANGED) != 0) {
			buf.append("[OWNER CHANGED]"); //$NON-NLS-1$
		}
		if ((getFlags() & EXTENSION_CHANGED) != 0) {
			buf.append("[EXTENSION CHANGED]"); //$NON-NLS-1$
		}
		if (getFlags() == 0) {
			buf.append("[UNSPECIFIED]"); //$NON-NLS-1$
		}
		return buf.toString();
	}
}
