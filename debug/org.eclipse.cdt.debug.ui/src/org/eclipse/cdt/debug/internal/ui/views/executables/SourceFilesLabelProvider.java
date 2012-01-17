/*******************************************************************************
 * Copyright (c) 2008, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.views.executables;

import com.ibm.icu.text.DateFormat;

import java.util.Date;
import java.util.List;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.cdt.debug.core.executables.ExecutablesManager;
import org.eclipse.cdt.debug.core.executables.IExecutablesChangeListener;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.TreeColumnViewerLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

public class SourceFilesLabelProvider extends TreeColumnViewerLabelProvider implements IExecutablesChangeListener {

	private SourceFilesViewer viewer;

	private LocalResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

	public SourceFilesLabelProvider(SourceFilesViewer viewer) {
		super(new CElementLabelProvider());
		this.viewer = viewer;
		
		// brute-force clear the cache when executables change
		ExecutablesManager.getExecutablesManager().addExecutablesChangeListener(this);
		viewer.getControl().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				ExecutablesManager.getExecutablesManager().removeExecutablesChangeListener(SourceFilesLabelProvider.this);
			}
		});
	}

	@Override
	public void update(ViewerCell cell) {
		super.update(cell);

		SourceFilesViewer.TranslationUnitInfo tuInfo = null;
		Object element = cell.getElement();
		if (element instanceof ITranslationUnit) {
			tuInfo = SourceFilesViewer.fetchTranslationUnitInfo((Executable) viewer.getInput(), element);
		}
		
		int orgColumnIndex = cell.getColumnIndex();

		if (orgColumnIndex == 0) {
			if (element instanceof String) {
				cell.setText((String) element);
				Font italicFont = resourceManager.createFont(FontDescriptor.createFrom(viewer.getTree().getFont()).setStyle(SWT.ITALIC));
				cell.setFont(italicFont);
			} else {
				cell.setFont(viewer.getTree().getFont());
			}
		} else if (orgColumnIndex == 1) {
			cell.setText(null);
			if (tuInfo != null) {
				if (tuInfo.location != null) {
					cell.setText(tuInfo.location.toOSString());
					if (tuInfo.exists)
						cell.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
					else
						cell.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
				}
			}
			cell.setImage(null);
		} else if (orgColumnIndex == 2) {
			cell.setText(null);
			if (tuInfo != null && tuInfo.originalLocation != null) {
				cell.setText(tuInfo.originalLocation.toOSString());
				if (tuInfo.originalExists)
					cell.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
				else
					cell.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
			}
			cell.setImage(null);
		} else if (orgColumnIndex == 3) {
			cell.setText(null);
			if (tuInfo != null) {
				if (tuInfo.exists) {
					cell.setText(Long.toString(tuInfo.fileLength));
				}
			}
			cell.setImage(null);
		} else if (orgColumnIndex == 4) {
			cell.setText(null);
			if (tuInfo != null) {
				if (tuInfo.exists) {
					String dateTimeString = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(
							new Date(tuInfo.lastModified));
					cell.setText(dateTimeString);
				}
			}
			cell.setImage(null);
		} else if (orgColumnIndex == 5) {
			cell.setText(null);
			if (tuInfo != null) {
				if (tuInfo.location != null) {
					String fileExtension = tuInfo.location.getFileExtension();
					if (fileExtension != null)
						cell.setText(fileExtension.toLowerCase());
				}
			}
			cell.setImage(null);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.executables.IExecutablesChangeListener#executablesListChanged()
	 */
	@Override
	public void executablesListChanged() {
		SourceFilesViewer.flushTranslationUnitCache();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.executables.IExecutablesChangeListener#executablesChanged(java.util.List)
	 */
	@Override
	public void executablesChanged(List<Executable> executables) {
		// no mapping of executable -> TU maintained; just kill all for now
		SourceFilesViewer.flushTranslationUnitCache();
	}

}
	