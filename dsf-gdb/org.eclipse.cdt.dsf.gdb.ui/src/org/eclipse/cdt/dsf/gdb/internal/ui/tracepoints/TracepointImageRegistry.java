/*******************************************************************************
 * Copyright (c) 2010, 2015 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Dmitry Kozlov (Mentor Graphics) - Trace control view enhancements (Bug 390827)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.tracepoints;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 *  @since 2.1
 */
public class TracepointImageRegistry extends AbstractImageRegistry {
	private static List<Object[]> fStore = new ArrayList<>();

	private static String add(String plugin, String[] dirs, String name) {
		String key = plugin + '/' + dirs[0] + '/' + name;
		fStore.add(new Object[] { key, plugin, dirs, name });
		return key;
	}

	private static final String ORG_ECLIPSE_UI_PLUGIN_ID = "org.eclipse.ui"; //$NON-NLS-1$
	private static final String ORG_ECLIPSE_CDT_DSF_GDB_UI_PLUGIN_ID = "org.eclipse.cdt.dsf.gdb.ui"; //$NON-NLS-1$

	public static final String ICON_Refresh_enabled = add(ORG_ECLIPSE_UI_PLUGIN_ID, new String[] { "full/elcl16" }, //$NON-NLS-1$
			"refresh_nav.png"); //$NON-NLS-1$
	public static final String ICON_Refresh_disabled = add(ORG_ECLIPSE_UI_PLUGIN_ID, new String[] { "full/dlcl16" }, //$NON-NLS-1$
			"refresh_nav.png"); //$NON-NLS-1$
	public static final String ICON_Refresh_Auto = add(ORG_ECLIPSE_CDT_DSF_GDB_UI_PLUGIN_ID,
			new String[] { "full/obj16" }, "refresh_auto.gif"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final String ICON_Disconnected_Tracing = add(ORG_ECLIPSE_CDT_DSF_GDB_UI_PLUGIN_ID,
			new String[] { "full/obj16" }, "disconnected_tracing.png"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final String ICON_Circular_Buffer = add(ORG_ECLIPSE_CDT_DSF_GDB_UI_PLUGIN_ID,
			new String[] { "full/obj16" }, "circular_buffer.png"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final String ICON_Trace_Variables = add(ORG_ECLIPSE_CDT_DSF_GDB_UI_PLUGIN_ID,
			new String[] { "full/obj16" }, "tracevariables.gif"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final String ICON_Exit_Visualization = add(ORG_ECLIPSE_CDT_DSF_GDB_UI_PLUGIN_ID,
			new String[] { "full/obj16" }, "stop_visual_trace.gif"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final String ICON_Edit_enabled = add(ORG_ECLIPSE_CDT_DSF_GDB_UI_PLUGIN_ID,
			new String[] { "full/obj16" }, "write_obj.gif"); //$NON-NLS-1$ //$NON-NLS-2$

	private static TracepointImageRegistry INSTANCE = new TracepointImageRegistry(GdbUIPlugin.getDefault());

	TracepointImageRegistry(Plugin plugin) {
		super(plugin);
		initialize();
	}

	void initialize() {
		for (Iterator<Object[]> iter = fStore.iterator(); iter.hasNext();) {
			Object[] element = iter.next();
			if (element.length == 2) {
				String dir = (String) element[0];
				String name = (String) element[1];
				localImage(name, dir, name);
			} else {
				String key = (String) element[0];
				String plugin = (String) element[1];
				String[] dirs = (String[]) element[2];
				String name = (String) element[3];
				externalImage(key, plugin, dirs, name);
			}
		}
	}

	public static ImageDescriptor getImageDescriptor(String key) {
		return INSTANCE.getDescriptor(key);
	}
}
