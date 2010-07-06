/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
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
import java.util.SortedMap;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyManager;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyType;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.BuildListComparator;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.graphics.Image;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ManagedBuildWizard extends AbstractCWizard {
	private static final Image IMG = CPluginImages.get(CPluginImages.IMG_OBJS_CONTAINER);
	public static final String OTHERS_LABEL = Messages.CNewWizard_0;  
	
	/**
	 * @since 5.1
	 */
	public static final String EMPTY_PROJECT = Messages.AbstractCWizard_0;  
	/**
	 * Creates and returns an array of items to be displayed 
	 */
	@Override
	public EntryDescriptor[] createItems(boolean supportedOnly, IWizard wizard) {
		IBuildPropertyManager bpm = ManagedBuildManager.getBuildPropertyManager();
		IBuildPropertyType bpt = bpm.getPropertyType(MBSWizardHandler.ARTIFACT);
		IBuildPropertyValue[] vs = bpt.getSupportedValues();
		Arrays.sort(vs, BuildListComparator.getInstance());
		
		ArrayList<EntryDescriptor> items = new ArrayList<EntryDescriptor>();
		// new style project types
		for (int i=0; i<vs.length; i++) {
			IToolChain[] tcs = ManagedBuildManager.getExtensionsToolChains(MBSWizardHandler.ARTIFACT, vs[i].getId(), false);
			if (tcs == null || tcs.length == 0) continue;
			MBSWizardHandler h = new MBSWizardHandler(vs[i], parent, wizard);
			for (int j=0; j<tcs.length; j++) {
				if (isValid(tcs[j], supportedOnly, wizard)) 
					h.addTc(tcs[j]);
			}
			if (h.getToolChainsCount() > 0) {
				// The project category item.
				items.add(new EntryDescriptor(vs[i].getId(), null, vs[i].getName(), true, h, null));
				// A default project type for that category -- not using any template.
				EntryDescriptor entryDescriptor = new EntryDescriptor(vs[i].getId() + ".default", vs[i].getId(), //$NON-NLS-1$
						EMPTY_PROJECT, false, h, null);
				entryDescriptor.setDefaultForCategory(true);
				items.add(entryDescriptor);
			}
		}
		
		// old style project types
		EntryDescriptor oldsRoot = null;
		SortedMap<String, IProjectType> sm = ManagedBuildManager.getExtensionProjectTypeMap();
		for (String s : sm.keySet()) {
			IProjectType pt = sm.get(s);
			if (pt.isAbstract() || pt.isSystemObject()) continue;
			if (supportedOnly && !pt.isSupported()) continue; // not supported
			String nattr = pt.getNameAttribute(); 
			if (nattr == null || nattr.length() == 0) continue; // new proj style 
			MBSWizardHandler h = new MBSWizardHandler(pt, parent, wizard);
			IToolChain[] tcs = ManagedBuildManager.getExtensionToolChains(pt);
			for(int i = 0; i < tcs.length; i++){
				IToolChain t = tcs[i];
				if(t.isSystemObject()) 
					continue;
				if (!isValid(t, supportedOnly, wizard))
					continue;
				
				h.addTc(t);
			}
//			IConfiguration[] cfgs = pt.getConfigurations();
//			if (cfgs == null || cfgs.length == 0) continue;
//			IToolChain tc = null;
//			for (int i=0; i<cfgs.length; i++) {
//				if (cfgs[i].isSystemObject()) continue;
//				IToolChain t = cfgs[i].getToolChain();
//				if (isValid(t, supportedOnly, wizard)) {
//					tc = t;
//					break;
//				}
//			}
//			if (tc ==  null) continue;
//			h.addTc(tc);

			String pId = null;
			if (CDTPrefUtil.getBool(CDTPrefUtil.KEY_OTHERS)) {
				if (oldsRoot == null) {
					oldsRoot = new EntryDescriptor(OTHERS_LABEL, null, OTHERS_LABEL, true, null, null);
					items.add(oldsRoot);
				}
				pId = oldsRoot.getId();
			} else {
				// do not group to <Others> - pId = null;
			}
			items.add(new EntryDescriptor(pt.getId(), pId, pt.getName(), false, h, IMG));
		}
		return items.toArray(new EntryDescriptor[items.size()]);
	}
}
