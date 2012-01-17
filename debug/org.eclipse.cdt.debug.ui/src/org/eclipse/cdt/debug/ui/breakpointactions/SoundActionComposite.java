/*******************************************************************************
 * Copyright (c) 2007, 2011 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpointactions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;

public class SoundActionComposite extends Composite {

	private static final String[] soundFileExtensions = new String[] { "*.wav", "*.mid", "*.au", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"*.aiff" }; //$NON-NLS-1$

	private Combo combo_1;
	private ModifyListener comboModifyListener = null;
	private File selectedSoundFile = null;
	private SoundActionPage soundActionPage;
	private Label soundFilePathLabel;
	private Button tryItButton;

	/**
	 * Create the composite
	 * 
	 * @param parent
	 * @param style
	 * @param page
	 */
	public SoundActionComposite(Composite parent, int style, SoundActionPage page) {
		super(parent, style);
		soundActionPage = page;
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		final Label playSoundLabel = new Label(this, SWT.NONE);
		playSoundLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		playSoundLabel.setText(Messages.getString("SoundActionComposite.4")); //$NON-NLS-1$

		combo_1 = new Combo(this, SWT.READ_ONLY);
		final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		combo_1.setLayoutData(gridData);

		comboModifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (combo_1.getText().length() > 0) {
					String filePath = combo_1.getText();
					File soundFile = new File(filePath);
					if (soundFile.exists()) {
						soundFilePathLabel.setText(filePath);
						tryItButton.setEnabled(true);
						selectedSoundFile = soundFile;
					} else {
						soundFilePathLabel.setText(Messages.getString("SoundActionComposite.9")); //$NON-NLS-1$
						tryItButton.setEnabled(false);
					}
				}
			}
		};
		rebuildRecentSoundsCombo();
		combo_1.addModifyListener(comboModifyListener);

		final String mediaPath = page.getMediaPath();

		final Button browseButton = new Button(this, SWT.NONE);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				dialog.setText(Messages.getString("SoundActionComposite.5")); //$NON-NLS-1$
				dialog.setFilterExtensions(soundFileExtensions);
				if (mediaPath.length() > 0)
					dialog.setFilterPath(mediaPath);

				String res = dialog.open();
				if (res != null) {
					setSoundFile(res);
				}
			}
		});
		browseButton.setText(Messages.getString("SoundActionComposite.6")); //$NON-NLS-1$

		tryItButton = new Button(this, SWT.NONE);
		tryItButton.setLayoutData(new GridData());
		tryItButton.setText(Messages.getString("SoundActionComposite.7")); //$NON-NLS-1$
		tryItButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				File soundFile = new File(soundFilePathLabel.getText());
				playSoundFile(soundFile);
			}
		});

		soundFilePathLabel = new Label(this, SWT.NONE);
		final GridData gridData_1 = new GridData(GridData.FILL_HORIZONTAL);
		gridData_1.horizontalSpan = 2;
		soundFilePathLabel.setLayoutData(gridData_1);
		soundFilePathLabel.setText(""); //$NON-NLS-1$

		//
		if (soundActionPage.getSoundAction().getSoundFile() != null)
			setSoundFile(soundActionPage.getSoundAction().getSoundFile().getAbsolutePath());
	}

	private void addRecentSound(File soundFile) {
		soundActionPage.addRecentSound(soundFile);
		rebuildRecentSoundsCombo();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public File getSoundFile() {
		return selectedSoundFile;
	}

	protected void playSoundFile(File soundFile) {
		SoundAction.playSoundFile(soundFile);
	}

	private void rebuildRecentSoundsCombo() {
		combo_1.removeAll();

		ArrayList<File> sortedSounds = new ArrayList<File>(soundActionPage.getRecentSounds());
		Collections.sort(sortedSounds);

		for (Iterator<File> iter = sortedSounds.iterator(); iter.hasNext();) {
			File element = iter.next();
			combo_1.add(element.getAbsolutePath());
		}
	}

	private void setSoundFile(String filePath) {
		combo_1.removeModifyListener(comboModifyListener);
		File soundFile = new File(filePath);
		if (soundFile.exists()) {
			addRecentSound(soundFile);
			combo_1.setText(soundFile.getAbsolutePath());
			soundFilePathLabel.setText(filePath);
			tryItButton.setEnabled(true);
			selectedSoundFile = soundFile;
		} else {
			soundFilePathLabel.setText(Messages.getString("SoundActionComposite.9")); //$NON-NLS-1$
			tryItButton.setEnabled(false);
		}
		combo_1.addModifyListener(comboModifyListener);
	}

}
