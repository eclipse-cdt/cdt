/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.ui.view;

import org.eclipse.osgi.util.NLS;

public class UIViewMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.testsrunner.internal.ui.view.UIViewMessages"; //$NON-NLS-1$
	public static String CounterPanel_tests_erred;
	public static String CounterPanel_tests_failed;
	public static String CounterPanel_tests_run;
	public static String CounterPanel_tests_skipped;
	public static String MessagesPanel_label;
	public static String MessagesViewer_location_format;
	public static String MessagesViewer_message_format;
	public static String ResultsView_layout_menu_text;
	public static String TestsHierarchyViewer_test_path_format;
	public static String TestsHierarchyViewer_test_time_format;
	public static String UIUpdater_update_ui_job;
	public static String UIUpdater_view_caption_format;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, UIViewMessages.class);
	}

	private UIViewMessages() {
	}
}
