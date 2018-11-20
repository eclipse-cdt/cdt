/*******************************************************************************
 * Copyright (c) 2009, 2011 Wind River Systems and others.
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
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.vm;

import org.eclipse.debug.internal.ui.viewers.model.IInternalTreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualTreeModelViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 2.2
 */
public class VirtualViewerPerformanceTests extends PerformanceTests {

	public VirtualViewerPerformanceTests(String name) {
		super(name);
	}

	@Override
	protected IInternalTreeModelViewer createViewer(Display display, Shell shell) {
		return new VirtualTreeModelViewer(fDisplay, 0, new PresentationContext("TestViewer"));
	}

	@Override
	protected int getTestModelDepth() {
		return 7;
	}
}
