/*******************************************************************************
 * Copyright (c) 2007 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.breakpointactions;

import org.eclipse.cdt.debug.internal.core.model.CThread;
import org.eclipse.core.runtime.IAdapterFactory;

public class BreakpointActionAdapterFactory implements IAdapterFactory {

	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType.equals(ILogActionEnabler.class)) {
			if (adaptableObject instanceof CThread) {
				return new LogActionEnabler((CThread) adaptableObject);
			}
		}
		if (adapterType.equals(IResumeActionEnabler.class)) {
			if (adaptableObject instanceof CThread) {
				return new ResumeActionEnabler((CThread) adaptableObject);
			}
		}
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return new Class[] { ILogActionEnabler.class, IResumeActionEnabler.class, };
	}

}
