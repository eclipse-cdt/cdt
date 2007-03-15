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

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IToolChain;

/*
 * This class is intended for data exchange between 
 * Configuration page and Handlers. 
 * It may hold configuration in case of managed project 
 * or to be a placeholder in case of make project  
 */
public class CfgHolder {
	private static final String DELIMITER = "_with_";  //$NON-NLS-1$

	String name;
	IConfiguration cfg;
	IToolChain tc;
	
	public CfgHolder(IToolChain _tc, IConfiguration _cfg) {
		tc = _tc;
		cfg = _cfg;
		if (cfg == null) {
			if (tc == null)
				name = IDEWorkbenchMessages.getString("StdProjectTypeHandler.2"); //$NON-NLS-1$
			else	
				name = tc.getName(); 
		} else		
			name = cfg.getName();
	}
	public boolean isSystem() {
		if (cfg == null) return false;
		return (cfg.isSystemObject());
	}
	public boolean isSupported() {
		if (cfg == null) return true;
		return (cfg.isSupported());
	}
	
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

    public static CfgHolder[] cfgs2items(IConfiguration[] cfgs) {
    	CfgHolder[] its = new CfgHolder[cfgs.length];
    	for (int i=0; i<cfgs.length; i++) {
   			its[i] = new CfgHolder(cfgs[i].getToolChain(), cfgs[i]);
    	}
    	return its;
    }
 
    public static CfgHolder[] unique(CfgHolder[] its) {
    	// if names are not unique, add toolchain name
    	if (hasDoubles(its)) {
       		for (int k=0; k<its.length; k++) {
       			its[k].name = its[k].name + DELIMITER + its[k].cfg.getToolChain().getName();
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
    
    /*
     * Note that null configurations are ignored !
     */
    public static IConfiguration[] items2cfgs(CfgHolder[] its) {
    	ArrayList lst = new ArrayList(its.length);
    	for (int i=0; i<its.length; i++) 
    		if (its[i].cfg != null) lst.add(its[i].cfg);
    	return (IConfiguration[])lst.toArray(new IConfiguration[lst.size()]);
    }
}
