/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/package org.eclipse.cdt.managedbuilder.ui.wizards;

import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.managedbuilder.core.ITargetPlatform;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.ui.properties.ManagedBuilderUIImages;
import org.eclipse.cdt.utils.Platform;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public abstract class AbstractCWizard implements ICNewWizard {

	private static final String os = Platform.getOS();
	private static final String arch = Platform.getOSArch();
	private static final String ALL = "all";  //$NON-NLS-1$
	
	protected static final Image IMG0 = CPluginImages.get(CPluginImages.IMG_OBJS_CFOLDER);
	protected static final Image IMG1 = ManagedBuilderUIImages.get(ManagedBuilderUIImages.IMG_BUILD_CAT);
	protected static final Image IMG2 = ManagedBuilderUIImages.get(ManagedBuilderUIImages.IMG_BUILD_TOOL);

	protected Composite parent;
	protected IToolChainListListener listener;
	
	public void setDependentControl(Composite _parent, IToolChainListListener _listener) {
		parent = _parent;
		listener = _listener;
	}
	
	protected boolean isValid(IToolChain tc) {
		if (tc == null || !tc.isSupported() || tc.isAbstract() || tc.isSystemObject()) 
			return false;
		ITargetPlatform tp = tc.getTargetPlatform();
		if (tp != null) {
			List osList = Arrays.asList(tc.getOSList());
			if (osList.contains(ALL) || osList.contains(os)) {
				List archList = Arrays.asList(tc.getArchList());
				if (archList.contains(ALL) || archList.contains(arch)) 
					return true; // OS and ARCH fits
			}
			return false; // OS or ARCH does not fit
		}
		return true; // No platform: nothing to check 
	}

}
