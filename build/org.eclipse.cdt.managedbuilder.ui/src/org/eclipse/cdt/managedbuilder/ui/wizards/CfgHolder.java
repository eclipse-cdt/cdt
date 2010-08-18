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

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;

/**
 * This class is intended for data exchange between 
 * Configuration page and Handlers. 
 * It may hold configuration in case of managed project 
 * or to be a placeholder in case of make project  
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CfgHolder {
	private static final String DELIMITER = "_with_";  //$NON-NLS-1$
	private static final String LBR = " (v ";  //$NON-NLS-1$
	private static final String RBR = ")";  //$NON-NLS-1$

	private String name;
	private IConfiguration cfg;
	private IToolChain tc;
	
	public CfgHolder(IToolChain _tc, IConfiguration _cfg) {
		tc = _tc;
		cfg = _cfg;
		if (cfg == null) {
			if (tc == null || tc.getParent() == null)
				name = Messages.StdProjectTypeHandler_2; 
			else
				name = tc.getParent().getName();
		} else		
			name = cfg.getName();
	}
	
	/**
	 * @since 8.0
	 */
	public void setConfiguration(IConfiguration cfg) {
		this.cfg = cfg;
	}
	
	public boolean isSystem() {
		if (cfg == null) return false;
		return (cfg.isSystemObject());
	}
	public boolean isSupported() {
		if (cfg == null) return true;
		return (cfg.isSupported());
	}
	
	/**
	 * Checks whether names are unique
	 * 
	 * @param its
	 * @return
	 */
	
    public static boolean hasDoubles(CfgHolder[] its) {
   		for (int i=0; i<its.length; i++) {
       		String s = its[i].name;
       		for (int j=0; j<its.length; j++) {
       			if (i == j) continue;
       			if (s.equals(its[j].name)) 
       				return true;
       		}
       	}
   		return false;
    }

    /**
     * Creates array of holders on a basis
     * of configurations array.
     * 
     * @param cfgs
     * @return
     */
    public static CfgHolder[] cfgs2items(IConfiguration[] cfgs) {
    	CfgHolder[] its = new CfgHolder[cfgs.length];
    	for (int i=0; i<cfgs.length; i++) {
   			its[i] = new CfgHolder(cfgs[i].getToolChain(), cfgs[i]);
    	}
    	return its;
    }
 
    /**
     * Makes configuration's names unique.
     * Adds either version number or toolchain name. 
     * If it does not help, simply adds index.
     * 
     * @param its - list of items.
     * @return the same list with unique names. 
     */
    
    public static CfgHolder[] unique(CfgHolder[] its) {
    	// if names are not unique, add version name
    	if (hasDoubles(its)) {
       		for (int k=0; k<its.length; k++) {
       			if (its[k].tc != null) {
       				String ver = ManagedBuildManager.getVersionFromIdAndVersion(its[k].tc.getId());
       				if(ver != null)
       					its[k].name = its[k].name + LBR + ver + RBR;
       			}
       		}
		}
    	// if names are still not unique, add toolchain name
    	if (hasDoubles(its)) {
       		for (int k=0; k<its.length; k++) {
       			String s = its[k].name;
       			int x = s.indexOf(LBR);
       			if (x >= 0) 
       				s = s.substring(0, x); 
       			IToolChain tc = its[k].tc;
       			if (tc == null && its[k].cfg != null) 
       				tc = its[k].cfg.getToolChain();
       			if (tc != null)
       				its[k].name = s + DELIMITER + tc.getUniqueRealName();
       		}
		}
    	// if names are still not unique, add index
    	if (hasDoubles(its)) {
       		for (int k=0; k<its.length; k++) {
       			its[k].name = its[k].name + k;
       		}
		}
    	return its;
    }
    
    /**
     * Returns corresponding project type
     * obtained either from configuration
     * (if any) or from toolchain. 
     * 
     * @return projectType
     */
    
    public IProjectType getProjectType() {
    	if (cfg != null)
    		return cfg.getProjectType();
    	if (tc != null && tc.getParent() != null)
    		return tc.getParent().getProjectType();
    	return null;
    }

    /**
     * Reorders selected configurations in "physical" order.
     * Although toolchains are displayed in alphabetical 
     * order in Wizard, it's required to create corresponding
     * configurations in the same order as they are listed
     * in xml file, inside of single project type.   
     * 
     * @param its - items in initial order.
     * @return - items in "physical" order.
     */
    
    public static CfgHolder[] reorder(CfgHolder[] its) {
    	ArrayList<CfgHolder> ls = new ArrayList<CfgHolder>(its.length);
    	boolean found = true;
    	while (found) {
			found = false;
    		for (int i=0; i<its.length; i++) {
    			if (its[i] == null) 
    				continue;
    			found = true;
    			IProjectType pt = its[i].getProjectType();
    			if (pt == null) {
					ls.add(its[i]);
					its[i] = null;
    				continue;
    			}
    			IConfiguration[] cfs = pt.getConfigurations();
    			for (int j=0; j<cfs.length; j++) {
    				for (int k=0; k<its.length; k++) {
    					if (its[k] == null) 
    						continue;
    					if (cfs[j].equals(its[k].getTcCfg())) {
    						ls.add(its[k]);
    						its[k] = null;
    					}
    				}
    			}
    		}
    	}
    	return ls.toArray(new CfgHolder[ls.size()]); 
    }
    
    
    /*
     * Note that null configurations are ignored !
     */
    public static IConfiguration[] items2cfgs(CfgHolder[] its) {
    	ArrayList<IConfiguration> lst = new ArrayList<IConfiguration>(its.length);
    	for (CfgHolder h : its) 
    		if (h.cfg != null) 
    			lst.add(h.cfg);
    	return lst.toArray(new IConfiguration[lst.size()]);
    }
    
    public IConfiguration getTcCfg() {
    	if (tc != null)
    		return tc.getParent();
    	return null;
    }
    
    
	public Object getConfiguration() { return cfg; }
	public String getName() { return name; }
	public Object getToolChain() { return tc; }
}
