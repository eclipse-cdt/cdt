/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.PlatformObject;

public class MakeTargetsContainer extends PlatformObject {

	private final IContainer container;
	private final IMakeTarget[] targets;

	public MakeTargetsContainer(IContainer container, IMakeTarget[] targets) {
		this.container = container;
		this.targets = targets;
	}

	public IContainer getContainer() {
		return container;
	}

	public IMakeTarget[] getTargets() {
		return targets;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MakeTargetsContainer other = (MakeTargetsContainer) obj;
		if (container == null) {
			if (other.container != null)
				return false;
		} else if (!container.equals(other.container))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((container == null) ? 0 : container.hashCode());
		return result;
	}

}
