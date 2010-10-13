/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Patrick Chuong (Texas Instruments) - Bug fix (326670)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * DisassemblyImageRegistry
 */
public class DisassemblyImageRegistry extends AbstractImageRegistry {
	private static List<Object[]> fStore = new ArrayList<Object[]>();

	private static String add(String plugin, String[] dirs, String name) {
    	String key = plugin+'/'+dirs[0]+'/'+name;
		fStore.add(new Object[] {key, plugin, dirs, name});
	    return key;
	}

    private static final String ORG_ECLIPSE_DEBUG_UI_PLUGIN_ID = "org.eclipse.debug.ui"; //$NON-NLS-1$
    private static final String ORG_ECLIPSE_UI_PLUGIN_ID = "org.eclipse.ui"; //$NON-NLS-1$
	
    public static final String ICON_ToggleBreakpoint = add(ORG_ECLIPSE_DEBUG_UI_PLUGIN_ID, new String[] { "full/obj16"}, "brkp_obj.gif"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final String ICON_Refresh_enabled  = add(ORG_ECLIPSE_UI_PLUGIN_ID, new String[] {"full/elcl16"}, "refresh_nav.gif"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final String ICON_Refresh_disabled = add(ORG_ECLIPSE_UI_PLUGIN_ID, new String[] {"full/dlcl16"}, "refresh_nav.gif"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final String ICON_Copy_enabled 	 = add(ORG_ECLIPSE_UI_PLUGIN_ID, new String[] {"full/etool16"}, "copy_edit.gif"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final String ICON_Copy_disabled 	 = add(ORG_ECLIPSE_UI_PLUGIN_ID, new String[] {"full/dtool16"}, "copy_edit.gif"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final String ICON_Home_enabled 	 = add(ORG_ECLIPSE_UI_PLUGIN_ID, new String[] {"full/elcl16"}, "home_nav.gif"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final String ICON_Home_disabled 	 = add(ORG_ECLIPSE_UI_PLUGIN_ID, new String[] {"full/dlcl16"}, "home_nav.gif"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final String ICON_Sync_enabled 	 = add(ORG_ECLIPSE_DEBUG_UI_PLUGIN_ID, new String[] {"full/elcl16"}, "synced.gif"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final String ICON_Sync_disabled	 = add(ORG_ECLIPSE_DEBUG_UI_PLUGIN_ID, new String[] {"full/dlcl16"}, "synced.gif"); //$NON-NLS-1$ //$NON-NLS-2$

    private static DisassemblyImageRegistry INSTANCE= new DisassemblyImageRegistry(DsfUIPlugin.getDefault());
    
    DisassemblyImageRegistry(Plugin plugin) {
		super(plugin);
		initialize();
	}

    void initialize() {
    	for (Iterator<Object[]> iter = fStore.iterator(); iter.hasNext();) {
    		Object[] element = iter.next();
    		if (element.length == 2) {
    			String dir= (String) element[0];
    			String name= (String) element[1];
    			localImage(name, dir, name);
    		} else {
    			String key = (String) element[0];
    			String plugin= (String) element[1];
				String[] dirs= (String[]) element[2];
    			String name= (String) element[3];
    			externalImage(key, plugin, dirs, name);
    		}
    	}
	}
    
	public static ImageDescriptor getImageDescriptor(String key) {
		return INSTANCE.getDescriptor(key);
	}
}
