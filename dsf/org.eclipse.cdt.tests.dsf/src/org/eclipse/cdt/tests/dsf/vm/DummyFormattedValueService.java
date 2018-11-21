/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.vm;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.DsfTestPlugin;
import org.osgi.framework.BundleContext;

public class DummyFormattedValueService extends AbstractDsfService implements IFormattedValues {

	public static String DUMMY_FORMAT = "dummy";
	public static String[] AVAILABLE_FORMATS = new String[] { DUMMY_FORMAT, HEX_FORMAT, OCTAL_FORMAT, BINARY_FORMAT,
			NATURAL_FORMAT, DECIMAL_FORMAT, STRING_FORMAT };

	public DummyFormattedValueService(DsfSession session) {
		super(session);
	}

	@Override
	public void initialize(RequestMonitor rm) {
		super.initialize(new RequestMonitor(getExecutor(), rm) {
			@Override
			protected void handleSuccess() {
				register(new String[0], new Hashtable<String, String>());
				super.handleSuccess();
			}
		});
	}

	@Override
	public void shutdown(RequestMonitor rm) {
		unregister();
		super.shutdown(rm);
	}

	@Override
	protected BundleContext getBundleContext() {
		return DsfTestPlugin.getBundleContext();
	}

	@Override
	public void getAvailableFormats(IFormattedDataDMContext dmc, DataRequestMonitor<String[]> rm) {
		rm.setData(AVAILABLE_FORMATS);
		rm.done();
	}

	@Override
	public FormattedValueDMContext getFormattedValueContext(IFormattedDataDMContext dmc, String formatId) {
		return new FormattedValueDMContext(this, dmc, formatId);
	}

	@Override
	public void getFormattedExpressionValue(FormattedValueDMContext dmc, DataRequestMonitor<FormattedValueDMData> rm) {
		rm.setData(new FormattedValueDMData(dmc.getFormatID()));
		rm.done();
	}

}
