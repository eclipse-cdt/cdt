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
import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedMap;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyManager;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyType;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.properties.BuildListComparator;
import org.eclipse.cdt.managedbuilder.ui.properties.Messages;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.graphics.Image;

/**
 *
 */
public class ManagedBuildWizard extends AbstractCWizard {
	private static final Image IMG = CPluginImages.get(CPluginImages.IMG_OBJS_CONTAINER);
	private static final String OTHERS_LABEL = Messages.getString("CNewWizard.0");  //$NON-NLS-1$
	/**
	 * Creates and returns an array of items to be displayed 
	 */
	public EntryDescriptor[] createItems(boolean supportedOnly, IWizard wizard) {
		IBuildPropertyManager bpm = ManagedBuildManager.getBuildPropertyManager();
		IBuildPropertyType bpt = bpm.getPropertyType(MBSWizardHandler.ARTIFACT);
		IBuildPropertyValue[] vs = bpt.getSupportedValues();
		Arrays.sort(vs, BuildListComparator.getInstance());
		
		ArrayList items = new ArrayList();
		// new style project types
		for (int i=0; i<vs.length; i++) {
			IToolChain[] tcs = ManagedBuildManager.getExtensionsToolChains(MBSWizardHandler.ARTIFACT, vs[i].getId());
			if (tcs == null || tcs.length == 0) continue;
			MBSWizardHandler h = new MBSWizardHandler(vs[i], parent, wizard);
			for (int j=0; j<tcs.length; j++) {
				if (isValid(tcs[j], supportedOnly, wizard)) 
					h.addTc(tcs[j]);
			}
			if (h.getToolChainsCount() > 0) {
				items.add(new EntryDescriptor(vs[i].getId(), null, vs[i].getName(), true, h, null));
			}
		}
		
		// old style project types
		EntryDescriptor oldsRoot = null;
		SortedMap sm = ManagedBuildManager.getExtensionProjectTypeMap();
		Iterator it = sm.keySet().iterator();
		while(it.hasNext()) {
			String s = (String)it.next();
			IProjectType pt = (IProjectType)sm.get(s);
			if (pt.isAbstract() || pt.isSystemObject()) continue;
			if (supportedOnly && !pt.isSupported()) continue; // not supported
			String nattr = pt.getNameAttribute(); 
			if (nattr == null || nattr.length() == 0) continue; // new proj style 
			MBSWizardHandler h = new MBSWizardHandler(pt, parent, wizard);
			IConfiguration[] cfgs = pt.getConfigurations();
			if (cfgs == null || cfgs.length == 0) continue;
			IToolChain tc = null;
			for (int i=0; i<cfgs.length; i++) {
				if (cfgs[i].isSystemObject()) continue;
				IToolChain t = cfgs[i].getToolChain();
				if (isValid(t, supportedOnly, wizard)) {
					tc = t;
					break;
				}
			}
			if (tc ==  null) continue;
			h.addTc(tc);

			String pId = null;
			if (CDTPrefUtil.getBool(CDTPrefUtil.KEY_OTHERS)) {
				if (oldsRoot == null) {
					oldsRoot = new EntryDescriptor(OTHERS_LABEL, null, OTHERS_LABEL, true, null, null);
					items.add(oldsRoot);
				}
				pId = oldsRoot.getId();
			} else { // do not group to <Others>
				pId = null;
			}
			items.add(new EntryDescriptor(pt.getId(), pId, pt.getName(), true, h, IMG));
		}
		return (EntryDescriptor[])items.toArray(new EntryDescriptor[items.size()]);
	}
}
