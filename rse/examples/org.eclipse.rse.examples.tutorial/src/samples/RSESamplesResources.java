/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [235626] initial API and implementation
 *******************************************************************************/

package samples;

import org.eclipse.osgi.util.NLS;

public class RSESamplesResources extends NLS {
	private static String BUNDLE_NAME = "samples.rseSamplesResources"; //$NON-NLS-1$

	public static String pp_size_label;
	public static String pp_size_tooltip;
	public static String pp_files_label;
	public static String pp_files_tooltip;
	public static String pp_folders_label;
	public static String pp_folders_tooltip;
	public static String pp_stopButton_label;
	public static String pp_stopButton_tooltip;

	// Tutorial #3: Creating a Subsystem Configuration
	public static String connectorservice_devr_name;
	public static String connectorservice_devr_desc;

	public static String property_devr_resource_type;
	public static String property_devr_id_name;
	public static String property_devr_id_desc;
	public static String property_devr_dept_name;
	public static String property_devr_dept_desc;
	public static String property_team_resource_type;
	public static String filter_default_name;

	// Tutorial #3a: Adding a Custom Filter
	public static String property_type_teamfilter;
	public static String property_type_devrfilter;

	public static String filter_team_dlgtitle;
	public static String filter_team_pagetitle;
	public static String filter_team_pagetext;

	public static String filter_devr_dlgtitle;
	public static String filter_devr_pagetitle;
	public static String filter_devr_pagetext;
	public static String filter_devr_teamprompt_label;
	public static String filter_devr_teamprompt_tooltip;
	public static String filter_devr_devrprompt_label;
	public static String filter_devr_devrprompt_tooltip;

	static {
		NLS.initializeMessages(BUNDLE_NAME, RSESamplesResources.class);
	}


}
