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

import java.text.MessageFormat;

import org.eclipse.cdt.testsrunner.internal.TestsRunnerPlugin;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Shows a simple tests count statics information (run/error/failed).
 */
public class CounterPanel extends Composite {

	/** Testing session to show statistics for. */
	private ITestingSession testingSession;

	/** Widget showing the failed tests count. */
	private Label failedCounterLabel;

	/** Widget showing the error tests count. */
	private Label abortedCounterLabel;

	/** Widget showing the run tests count. */
	private Label currentCounterLabel;

	/**
	 * Shows whether there were skipped tests. It is used to force layout of the
	 * counter widgets after skipped tests are appeared.
	 */
	private boolean hasSkipped;

	private final Image errorIcon = TestsRunnerPlugin.createAutoImage("ovr16/failed_counter.gif"); //$NON-NLS-1$
	private final Image failureIcon = TestsRunnerPlugin.createAutoImage("ovr16/aborted_counter.gif"); //$NON-NLS-1$

	public CounterPanel(Composite parent, ITestingSession testingSession) {
		super(parent, SWT.WRAP);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 9;
		gridLayout.makeColumnsEqualWidth = false;
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);

		currentCounterLabel = createLabel(UIViewMessages.CounterPanel_tests_run, null);
		abortedCounterLabel = createLabel(UIViewMessages.CounterPanel_tests_erred, errorIcon);
		failedCounterLabel = createLabel(UIViewMessages.CounterPanel_tests_failed, failureIcon);
		setTestingSession(testingSession);
	}

	/**
	 * Creates counter label widget.
	 *
	 * @param name widget text prefix
	 * @param image widget image or <code>null</code>
	 * @return created label
	 */
	private Label createLabel(String name, Image image) {
		Label label = new Label(this, SWT.NONE);
		if (image != null) {
			image.setBackground(label.getBackground());
			label.setImage(image);
		}
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		label = new Label(this, SWT.NONE);
		label.setText(name);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		Label value = new Label(this, SWT.READ_ONLY);
		value.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING));
		return value;
	}

	/**
	 * Sets the testing session to show information about.
	 *
	 * @param testingSession testing session (null is not acceptable)
	 */
	public void setTestingSession(ITestingSession testingSession) {
		this.testingSession = testingSession;
		this.hasSkipped = testingSession.getCount(ITestItem.Status.Skipped) != 0;
		updateInfoFromSession();
	}

	/**
	 * Updates the information on the panel from the currently set testing
	 * session.
	 */
	public void updateInfoFromSession() {
		setFailedCounter(testingSession.getCount(ITestItem.Status.Failed));
		setAbortedCounter(testingSession.getCount(ITestItem.Status.Aborted));
		setCurrentCounter(testingSession.getCurrentCounter(), testingSession.getCount(ITestItem.Status.Skipped));
		redraw();
	}

	/**
	 * Sets a new value for the failed tests counter.
	 *
	 * @param newValue new counter value
	 */
	private void setFailedCounter(int newValue) {
		failedCounterLabel.setText(Integer.toString(newValue));
	}

	/**
	 * Sets a new value for the error tests counter.
	 *
	 * @param newValue new counter value
	 */
	private void setAbortedCounter(int newValue) {
		abortedCounterLabel.setText(Integer.toString(newValue));
	}

	/**
	 * Sets a new value for the run tests counter.
	 *
	 * @param currentValue new counter value
	 * @param skippedValue skipped tests counter
	 */
	private void setCurrentCounter(int currentValue, int skippedValue) {
		if (!hasSkipped && skippedValue != 0) {
			layout();
		}
		String runString = (skippedValue == 0) ? Integer.toString(currentValue)
				: MessageFormat.format(UIViewMessages.CounterPanel_tests_skipped, currentValue, skippedValue);
		currentCounterLabel.setText(runString);
	}

}
