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

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.TreeColumnViewerLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

public class SourceFilesLabelProvider extends TreeColumnViewerLabelProvider {

	private SourceFilesViewer viewer;

	private LocalResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

	public SourceFilesLabelProvider(SourceFilesViewer viewer) {
		super(new CElementLabelProvider());
		this.viewer = viewer;
	}

	@Override
	public void update(ViewerCell cell) {
		super.update(cell);

		int orgColumnIndex = cell.getColumnIndex();

		if (orgColumnIndex == 0) {
			if (cell.getElement() instanceof String) {
				cell.setText((String) cell.getElement());
				Font boldFont = resourceManager.createFont(FontDescriptor.createFrom(viewer.getTree().getFont()).setStyle(SWT.BOLD));
				cell.setFont(boldFont);
			}
		} else if (orgColumnIndex == 1) {
			cell.setText(null);
			if (cell.getElement() instanceof ITranslationUnit) {
				ITranslationUnit tu = (ITranslationUnit) cell.getElement();
				IPath path = tu.getLocation();
				if (path != null) {
					cell.setText(path.toOSString());
					if (path.toFile().exists())
						cell.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
					else
						cell.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
				}
			}
			cell.setImage(null);
		} else if (orgColumnIndex == 2) {
			cell.setText(null);
			if (cell.getElement() instanceof ITranslationUnit) {
				Executable executable = (Executable) viewer.getInput();
				Path path = new Path(executable.getOriginalLocation((ITranslationUnit) cell.getElement()));
				cell.setText(path.toOSString());
				if (path.toFile().exists())
					cell.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
				else
					cell.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
			}
			cell.setImage(null);
		} else if (orgColumnIndex == 3) {
			cell.setText(null);
			if (cell.getElement() instanceof ITranslationUnit) {
				ITranslationUnit tu = (ITranslationUnit) cell.getElement();
				IPath path = tu.getLocation();
				if (path != null && path.toFile().exists()) {
					long fileLength = path.toFile().length();
					cell.setText(Long.toString(fileLength));
				}
			}
			cell.setImage(null);
		} else if (orgColumnIndex == 4) {
			cell.setText(null);
			if (cell.getElement() instanceof ITranslationUnit) {
				ITranslationUnit tu = (ITranslationUnit) cell.getElement();
				IPath path = tu.getLocation();
				if (path != null && path.toFile().exists()) {
					long modified = path.toFile().lastModified();
					String dateTimeString = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(modified));
					cell.setText(dateTimeString);
				}
			}
			cell.setImage(null);
		} else if (orgColumnIndex == 5) {
			cell.setText(null);
			if (cell.getElement() instanceof ITranslationUnit) {
				ITranslationUnit tu = (ITranslationUnit) cell.getElement();
				IPath path = tu.getLocation();
				if (path != null) {
					String fileExtension = path.getFileExtension();
					if (fileExtension != null)
						cell.setText(fileExtension.toLowerCase());
				}
			}
			cell.setImage(null);
		}
	}

}
