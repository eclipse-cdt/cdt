/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards;

import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.dialogs.TypedElementSelectionValidator;
import org.eclipse.cdt.internal.ui.dialogs.TypedViewerFilter;
import org.eclipse.cdt.ui.CElementContentProvider;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.CElementSorter;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;

public class SourceFolderSelectionDialog extends ElementTreeSelectionDialog {

	private static final Class<?>[] VALIDATOR_CLASSES = new Class<?>[] { ICContainer.class, ICProject.class };
	private static final TypedElementSelectionValidator fValidator = new TypedElementSelectionValidator(
			VALIDATOR_CLASSES, false);

	private static final Class<?>[] FILTER_CLASSES = new Class<?>[] { ICModel.class, ICContainer.class,
			ICProject.class };
	private static final ViewerFilter fFilter = new TypedViewerFilter(FILTER_CLASSES);

	private static final ViewerSorter fSorter = new CElementSorter();

	public SourceFolderSelectionDialog(Shell parent) {
		super(parent, createLabelProvider(), createContentProvider());
		setValidator(fValidator);
		setComparator(fSorter);
		addFilter(fFilter);
		setTitle(NewWizardMessages.SourceFolderSelectionDialog_title);
		setMessage(NewWizardMessages.SourceFolderSelectionDialog_description);
	}

	private static ITreeContentProvider createContentProvider() {
		return new CElementContentProvider();
	}

	private static ILabelProvider createLabelProvider() {
		return new CElementLabelProvider(CElementLabelProvider.SHOW_DEFAULT);
	}
}
