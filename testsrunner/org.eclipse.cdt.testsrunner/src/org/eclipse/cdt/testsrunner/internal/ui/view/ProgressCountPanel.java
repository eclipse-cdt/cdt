/*******************************************************************************
 * Copyright (c) 2011, 2012 Anton Gorenkov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.ui.view;

import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * A statistics panel that compounds counter panel and red/green progress bar.
 * Depending on orientation it may layout them vertically or horizontally.
 */
public class ProgressCountPanel extends Composite {

	/** Child widget: counter panel. */
	private CounterPanel counterPanel;

	/** Child widget: red/green progress bar */
	private ProgressBar progressBar;

	/**
	 * Dummy session is used when there is no "real" testing sessions to show
	 * (e.g. when there was no launched testing session or when all of them were
	 * cleared).
	 */
	private DummyUISession dummyUISession = new DummyUISession();

	public ProgressCountPanel(Composite parent, ResultsView.Orientation currOrientation) {
		super(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		setLayout(layout);
		setPanelOrientation(currOrientation);

		counterPanel = new CounterPanel(this, dummyUISession);
		counterPanel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		progressBar = new ProgressBar(this, dummyUISession);
		progressBar.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

		// Data for parent (view's) layout
		setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
	}

	/**
	 * Sets the testing session to show information about.
	 *
	 * @param testingSession testing session or null to set default empty
	 * session
	 */
	public void setTestingSession(ITestingSession testingSession) {
		ITestingSession newSession = (testingSession != null) ? testingSession : dummyUISession;
		counterPanel.setTestingSession(newSession);
		progressBar.setTestingSession(newSession);
	}

	/**
	 * Updates the information on the panel from the currently set testing
	 * session.
	 */
	public void updateInfoFromSession() {
		counterPanel.updateInfoFromSession();
		progressBar.updateInfoFromSession();
	}

	/**
	 * Sets the widget orientation.
	 *
	 * @param orientation new widget orientation (vertical or horizontal; auto
	 * is not supported)
	 */
	public void setPanelOrientation(ResultsView.Orientation orientation) {
		((GridLayout) getLayout()).numColumns = (orientation == ResultsView.Orientation.Horizontal) ? 2 : 1;
	}

}
