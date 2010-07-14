/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.wizards;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.wizards.ICDTCommonProjectWizard;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.properties.ManagedBuilderUIImages;
import org.eclipse.cdt.ui.wizards.CNewWizard;
import org.eclipse.cdt.ui.wizards.IWizardItemsListListener;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public abstract class AbstractCWizard extends CNewWizard {

	protected static final Image IMG0 = CPluginImages.get(CPluginImages.IMG_OBJS_CFOLDER);
	protected static final Image IMG1 = ManagedBuilderUIImages.get(ManagedBuilderUIImages.IMG_BUILD_CAT);
	protected static final Image IMG2 = ManagedBuilderUIImages.get(ManagedBuilderUIImages.IMG_BUILD_TOOL);

	protected Composite parent;
	protected IWizardItemsListListener listener;
	
	@Override
	public void setDependentControl(Composite _parent, IWizardItemsListListener _listener) {
		parent = _parent;
		listener = _listener;
	}
	
	/**
	 * Checks whether toolchain can be displayed 
	 * @param tc
	 * @return
	 */
	protected boolean isValid(IToolChain tc, boolean supportedOnly, IWizard w) {
		// Check for langiuage compatibility first in any case
		if (!isLanguageCompatible(tc, w))
			return false;
		
		// Do not do further check if all toolchains are permitted	
		if (!supportedOnly) 
			return true;
		
		// Filter off unsupported and system toolchains
		if (tc == null || !tc.isSupported() || tc.isAbstract() || tc.isSystemObject()) 
			return false;
		
		// Check for platform compatibility
		return ManagedBuildManager.isPlatformOk(tc);
	}

	/**
	 * Checks toolchain for Language ID, Content type ID 
	 * and Extensions, if they are required by wizard.
	 * 
	 * @param tc - toolchain to check
	 * @param w - wizard which provides selection criteria
	 * @return
	 */
	protected boolean isLanguageCompatible(IToolChain tc, IWizard w) {
		if (w == null) 
			return true;
		if (!(w instanceof ICDTCommonProjectWizard))
			return true;

		ITool[] tools = tc.getTools(); 
		ICDTCommonProjectWizard wz = (ICDTCommonProjectWizard)w;
		String[] langIDs = wz.getLanguageIDs(); 
		String[] ctypeIDs = wz.getContentTypeIDs();
		String[] exts = wz.getExtensions();
		
		// nothing requied ?   
		if (empty(langIDs) && empty(ctypeIDs) && empty(exts))
			return true;
		
		for (int i=0; i<tools.length; i++) {
			IInputType[] its = tools[i].getInputTypes();
			
			// no input types - check only extensions
			if (empty(its)) {  
				if (!empty(exts)) {
					String[] s = tools[i].getAllInputExtensions();
					if (contains(exts, s))
						return true; // extension fits
				}
				continue;
			}
			// normal tool with existing input type(s)
			for (int j = 0; j<its.length; j++) {
				// Check language IDs
				if (!empty(langIDs)) {
					String lang = its[j].getLanguageId(tools[i]);
					if (contains(langIDs, new String[] {lang})) {
						return true; // Language ID fits
					}
				}
				// Check content types
				if (!empty(ctypeIDs)) {
					String[] ct1 = its[j].getSourceContentTypeIds();
					String[] ct2 = its[j].getHeaderContentTypeIds();
					if (contains(ctypeIDs, ct1) ||
						contains(ctypeIDs, ct2)) 
					{
						return true; // content type fits
					}
				}					
				// Check extensions
				if (!empty(exts)) {
					String[] ex1 =its[j].getHeaderExtensions(tools[i]);
					String[] ex2 =its[j].getSourceExtensions(tools[i]);
					if (contains(exts, ex1) ||
						contains(exts, ex2)) {
						return true; // extension fits fits
					}
				}
			}
		}
		return false; // no one value fits to required
	}
	
	private boolean empty(Object[] s) {
		return (s == null || s.length == 0);
	}
	
	private boolean contains(String[] s1, String[] s2) {
		for (int i=0; i<s1.length; i++) 
			for (int j=0; j<s2.length; j++) 
				if (s1[i].equals(s2[j])) 
					return true;
		return false;
	}

	/* comment it out for now..
	protected boolean isSupportedForTemplate(TemplateInfo templateInfo, IToolChain tc) {
		String[] tcIds = templateInfo.getToolChainIds();
		if (tcIds.length != 0) {
			for (int i=0; i< tcIds.length; i++) {
				if (tcIds[i].equals(tc.getId()) || tcIds[i].equals(tc.getSuperClass().getId())) {
					return true;
				}
			}
			return false;
		} else { 
			return true;
		}
	}
*/	
	

}
