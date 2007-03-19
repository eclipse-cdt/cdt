/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.wizards;

import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.properties.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class StdBuildWizard extends AbstractCWizard {
	
	public void createItems(Tree tree, boolean supportedOnly) {
		StdProjectTypeHandler h = new StdProjectTypeHandler(Messages.getString("StdBuildWizard.0"), IMG0, parent); //$NON-NLS-1$
		h.addTc(null); // add default toolchain
		IToolChain[] tcs = ManagedBuildManager.getRealToolChains();
		for (int i=0; i<tcs.length; i++)
			if (!supportedOnly || isValid(tcs[i])) h.addTc(tcs[i]);
		TreeItem ti = new TreeItem(tree, SWT.NONE);
		ti.setText(h.getName());
		ti.setData(h);
		ti.setImage(h.getIcon());
	}
}
