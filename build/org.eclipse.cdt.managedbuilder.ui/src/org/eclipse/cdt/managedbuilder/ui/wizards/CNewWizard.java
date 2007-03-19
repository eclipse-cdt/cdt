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

import java.util.Iterator;
import java.util.SortedMap;

import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyManager;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyType;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.properties.Messages;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 *
 */
public class CNewWizard extends AbstractCWizard {
	/**
	 * Creates and returns an array of 
	 */
	public void createItems(Tree tree, boolean supportedOnly) {
		IBuildPropertyManager bpm = ManagedBuildManager.getBuildPropertyManager();
		IBuildPropertyType bpt = bpm.getPropertyType(ICWizardHandler.ARTIFACT);
		IBuildPropertyValue[] vs = bpt.getSupportedValues();
				
		// new style project types
		for (int i=0; i<vs.length; i++) {
			IToolChain[] tcs = ManagedBuildManager.getExtensionsToolChains(ICWizardHandler.ARTIFACT, vs[i].getId());
			if (tcs == null || tcs.length == 0) continue;
			CWizardHandler h = new CWizardHandler(vs[i], IMG1, parent, listener);
			for (int j=0; j<tcs.length; j++) {
				if (!supportedOnly || isValid(tcs[j])) h.addTc(tcs[j]);
			}
			if (h.getToolChainsCount() > 0) {
				TreeItem ti = new TreeItem(tree, SWT.NONE);
				ti.setText(h.getName());
				ti.setData(h);
				ti.setImage(h.getIcon());
			}
		}
		
		// old style project types
		TreeItem oldsRoot = null;
		SortedMap sm = ManagedBuildManager.getExtensionProjectTypeMap();
		Iterator it = sm.keySet().iterator();
		while(it.hasNext()) {
			String s = (String)it.next();
			IProjectType pt = (IProjectType)sm.get(s);
			if (pt.isAbstract() || pt.isTestProjectType()) continue;
			if (supportedOnly && !pt.isSupported()) continue; // not supported
			String nattr = pt.getNameAttribute(); 
			if (nattr == null || nattr.length() == 0) continue; // new proj style 
			CWizardHandler h = new CWizardHandler(pt.getName(), pt, IMG2, parent, listener);
			IConfiguration[] cfgs = pt.getConfigurations();
			if (cfgs == null || cfgs.length == 0) continue;
			IToolChain tc = null;
			for (int i=0; i<cfgs.length; i++) {
				if (cfgs[i].isSystemObject()) continue;
				IToolChain t = cfgs[i].getToolChain();
				if (!supportedOnly || isValid(t)) {
					tc = t;
					break;
				}
			}
			if (tc ==  null) continue;
			h.addTc(tc);

			TreeItem ti = null;
			if (CDTPrefUtil.getBool(CDTPrefUtil.KEY_OTHERS)) {
				if (oldsRoot == null) {
					oldsRoot = new TreeItem(tree, SWT.NONE);
					oldsRoot.setText(Messages.getString("CNewWizard.0")); //$NON-NLS-1$
					oldsRoot.setData(new DummyHandler(parent));
					oldsRoot.setImage(IMG0);				
				}
				ti = new TreeItem(oldsRoot, SWT.NONE);
			} else { // do not group to <Others>
				ti = new TreeItem(tree, SWT.NONE);
			}
			ti.setText(h.getName());
			ti.setData(h);
			ti.setImage(h.getIcon());
		}
	}
}
