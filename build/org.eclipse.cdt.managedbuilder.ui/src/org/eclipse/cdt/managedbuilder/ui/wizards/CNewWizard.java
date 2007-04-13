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

import java.util.ArrayList;
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
import org.eclipse.cdt.ui.wizards.ICWizardHandler;
import org.eclipse.cdt.ui.wizards.WizardItemData;
import org.eclipse.jface.wizard.IWizard;

/**
 *
 */
public class CNewWizard extends AbstractCWizard {
	/**
	 * Creates and returns an array of items to be displayed 
	 */
	public WizardItemData[] createItems(boolean supportedOnly, IWizard wizard) {
		IBuildPropertyManager bpm = ManagedBuildManager.getBuildPropertyManager();
		IBuildPropertyType bpt = bpm.getPropertyType(ICWizardHandler.ARTIFACT);
		IBuildPropertyValue[] vs = bpt.getSupportedValues();
		
		ArrayList items = new ArrayList();
		// new style project types
		for (int i=0; i<vs.length; i++) {
			IToolChain[] tcs = ManagedBuildManager.getExtensionsToolChains(ICWizardHandler.ARTIFACT, vs[i].getId());
			if (tcs == null || tcs.length == 0) continue;
			MBSWizardHandler h = new MBSWizardHandler(vs[i], IMG1, parent, wizard);
			for (int j=0; j<tcs.length; j++) {
				if (!supportedOnly || isValid(tcs[j])) h.addTc(tcs[j]);
			}
			if (h.getToolChainsCount() > 0) {
				WizardItemData wd = new WizardItemData(); 
				wd.name = h.getName();
				wd.handler = h;
				wd.image = h.getIcon();
				wd.id = h.getName();
				wd.parentId = null;
				items.add(wd);
			}
		}
		
		// old style project types
		WizardItemData oldsRoot = null;
		SortedMap sm = ManagedBuildManager.getExtensionProjectTypeMap();
		Iterator it = sm.keySet().iterator();
		while(it.hasNext()) {
			String s = (String)it.next();
			IProjectType pt = (IProjectType)sm.get(s);
			if (pt.isAbstract() || pt.isTestProjectType()) continue;
			if (supportedOnly && !pt.isSupported()) continue; // not supported
			String nattr = pt.getNameAttribute(); 
			if (nattr == null || nattr.length() == 0) continue; // new proj style 
			MBSWizardHandler h = new MBSWizardHandler(pt.getName(), pt, IMG2, parent, wizard);
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

			WizardItemData wd = new WizardItemData(); 
			if (CDTPrefUtil.getBool(CDTPrefUtil.KEY_OTHERS)) {
				if (oldsRoot == null) {
					oldsRoot = new WizardItemData();
					oldsRoot.name = Messages.getString("CNewWizard.0"); //$NON-NLS-1$
					oldsRoot.handler = null;
					oldsRoot.image =IMG0;
					oldsRoot.id = oldsRoot.name;
					oldsRoot.parentId = null;
					items.add(oldsRoot);
				}
				wd.parentId = oldsRoot.id;
			} else { // do not group to <Others>
				wd.parentId = null;
			}
			wd.name = h.getName();
			wd.handler = h;
			wd.image = h.getIcon();
			wd.id = h.getName();
			items.add(wd);
		}
		return (WizardItemData[])items.toArray(new WizardItemData[items.size()]);
	}
}
