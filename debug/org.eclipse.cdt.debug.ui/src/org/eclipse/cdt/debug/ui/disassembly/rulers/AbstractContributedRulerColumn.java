/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems, Inc. - adapted for for disassembly parts
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.disassembly.rulers;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.ui.IWorkbenchPart;


/**
 * Helper class for contributions to the
 * <code>org.eclipse.cdt.debug.ui.disassemblyRulerColumns</code> extension point.
 * <p>
 * Subclasses must have a zero-argument constructor so that they can be created by
 * {@link IConfigurationElement#createExecutableExtension(String)}.</p>
 *
 * @since 7.2
 */
public abstract class AbstractContributedRulerColumn implements IContributedRulerColumn {
	/** The contribution descriptor. */
	private RulerColumnDescriptor fDescriptor;
	/** The target disassembly part. */
	private IWorkbenchPart fDisassembly;

	@Override
	public final RulerColumnDescriptor getDescriptor() {
		return fDescriptor;
	}

	@Override
	public final void setDescriptor(RulerColumnDescriptor descriptor) {
		Assert.isLegal(descriptor != null);
		Assert.isTrue(fDescriptor == null);
		fDescriptor= descriptor;
	}

	@Override
	public final void setDisassemblyPart(IWorkbenchPart disassembly) {
		Assert.isLegal(disassembly != null);
		Assert.isTrue(fDisassembly == null);
		fDisassembly= disassembly;
	}

	@Override
	public final IWorkbenchPart getDisassemblyPart() {
		return fDisassembly;
	}

	@Override
	public void columnCreated() {
	}

	@Override
	public void columnRemoved() {
	}
}
