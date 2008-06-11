/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.texteditor.spelling.IPreferenceStatusMonitor;
import org.eclipse.ui.texteditor.spelling.ISpellingPreferenceBlock;

import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;

/**
 * Spelling preference block
 */
public class SpellingPreferenceBlock implements ISpellingPreferenceBlock {
	
	private class NullStatusChangeListener implements IStatusChangeListener {
		/*
		 * @see org.eclipse.cdt.internal.ui.wizards.IStatusChangeListener#statusChanged(org.eclipse.core.runtime.IStatus)
		 */
		public void statusChanged(IStatus status) {
		}
	}

	private class StatusChangeListenerAdapter implements IStatusChangeListener {
		private IPreferenceStatusMonitor fMonitor;
		
		private IStatus fStatus;
		
		public StatusChangeListenerAdapter(IPreferenceStatusMonitor monitor) {
			super();
			fMonitor= monitor;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.wizards.IStatusChangeListener#statusChanged(org.eclipse.core.runtime.IStatus)
		 */
		public void statusChanged(IStatus status) {
			fStatus= status;
			fMonitor.statusChanged(status);
		}
		
		public IStatus getStatus() {
			return fStatus;
		}
	}

	private SpellingConfigurationBlock fBlock= new SpellingConfigurationBlock(new NullStatusChangeListener(), null, null);
	
	private SpellingPreferenceBlock.StatusChangeListenerAdapter fStatusMonitor;
	
	/*
	 * @see org.eclipse.ui.texteditor.spelling.ISpellingPreferenceBlock#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent) {
		return fBlock.createContents(parent);
	}

	/*
	 * @see org.eclipse.ui.texteditor.spelling.ISpellingPreferenceBlock#initialize(org.eclipse.ui.texteditor.spelling.IPreferenceStatusMonitor)
	 */
	public void initialize(IPreferenceStatusMonitor statusMonitor) {
		fStatusMonitor= new StatusChangeListenerAdapter(statusMonitor);
		fBlock.fContext= fStatusMonitor;
	}

	/*
	 * @see org.eclipse.ui.texteditor.spelling.ISpellingPreferenceBlock#canPerformOk()
	 */
	public boolean canPerformOk() {
		return fStatusMonitor == null || fStatusMonitor.getStatus() == null || !fStatusMonitor.getStatus().matches(IStatus.ERROR);
	}

	/*
	 * @see org.eclipse.ui.texteditor.spelling.ISpellingPreferenceBlock#performOk()
	 */
	public void performOk() {
		fBlock.performOk();
	}

	/*
	 * @see org.eclipse.ui.texteditor.spelling.ISpellingPreferenceBlock#performDefaults()
	 */
	public void performDefaults() {
		fBlock.performDefaults();
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.spelling.ISpellingPreferenceBlock#performRevert()
	 */
	public void performRevert() {
		fBlock.performRevert();
	}

	/*
	 * @see org.eclipse.ui.texteditor.spelling.ISpellingPreferenceBlock#dispose()
	 */
	public void dispose() {
		fBlock.dispose();
	}

	/*
	 * @see org.eclipse.ui.texteditor.spelling.ISpellingPreferenceBlock#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		fBlock.setEnabled(enabled);
	}
}
