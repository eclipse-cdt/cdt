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
package org.eclipse.cdt.testsrunner.internal.qttest;

import org.eclipse.osgi.util.NLS;

public class QtTestsRunnerMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.testsrunner.internal.qttest.QtTestsRunnerMessages"; //$NON-NLS-1$
	public static String QtTestsRunner_error_format;
	public static String QtTestsRunner_io_error_prefix;
	public static String QtTestsRunner_no_test_cases_to_rerun;
	public static String QtTestsRunner_xml_error_prefix;
	public static String QtXmlLogHandler_benchmark_result_message;
	public static String QtXmlLogHandler_datatag_format;
	public static String QtXmlLogHandler_metrics_unit_events;
	public static String QtXmlLogHandler_metrics_unit_instructions;
	public static String QtXmlLogHandler_metrics_unit_msec;
	public static String QtXmlLogHandler_metrics_unit_ticks;
	public static String QtXmlLogHandler_unknown_benchmarck_metric;
	public static String QtXmlLogHandler_unknown_message_level;
	public static String QtXmlLogHandler_wrong_tag_name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, QtTestsRunnerMessages.class);
	}

	private QtTestsRunnerMessages() {
	}
}
