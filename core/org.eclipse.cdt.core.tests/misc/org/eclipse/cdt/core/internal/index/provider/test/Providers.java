/*******************************************************************************
 * Copyright (c) 2007, 2012 Symbian Software Ltd. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.internal.index.provider.test;

import org.eclipse.cdt.core.index.provider.IPDOMDescriptor;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;

public class Providers {
	public static class Dummy1 extends AbstractDummyProvider {
	}

	public static class Dummy2 extends AbstractDummyProvider {
	}

	public static class Dummy3 extends AbstractDummyProvider {
	}

	public static class Dummy4 extends AbstractDummyProvider {
	}

	public static class Dummy5 extends AbstractDummyProvider {
	}

	public static class Counter extends AbstractDummyProvider {
		public static int fCounter;

		@Override
		public IPDOMDescriptor[] getDescriptors(ICConfigurationDescription config) {
			fCounter++;
			return super.getDescriptors(config);
		}
	}
}
