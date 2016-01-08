/*******************************************************************************
 * Copyright (c) 2016 Alena Laskavaia
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia - initial API and implementation,
 *     inspired by work of Erik Johansson <erik.johansson.979@gmail.com>
 *
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.wizards.NewClassCreationWizard;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.PlatformUI;

public class QuickFixCreateNewClass extends AbstractCodanCMarkerResolution implements IMarkerResolution2 {
	@Override
	public String getLabel() {
		// TODO Should provide class name as message parameter
		return QuickFixMessages.QuickFixCreateClass_CreateNewClass;
	}

	@Override
	public boolean isApplicable(IMarker marker) {
		ITranslationUnit tu = getTranslationUnitViaEditor(marker);
		try {
			if (tu.getLanguage().getLinkageID() == ILinkage.C_LINKAGE_ID)
				return false;
		} catch (CoreException e) {
			// ignore
		}
		return true;
	}

	@Override
	public String getDescription() {
		return QuickFixMessages.QuickFixCreateClass_CreateNewClass;
	}

	@Override
	public Image getImage() {
		return CDTSharedImages.getImage("icons/etool16/newclass_wiz.gif"); //$NON-NLS-1$
	}

	@Override
	public void apply(IMarker marker, IDocument document) {
		String name = null;
		try {
			name = getProblemArgument(marker, 0);
		} catch (Exception e) {
			CheckersUiActivator.log(e);
			// fallthrough
			// will still open new wizard dialog but without class name filled
		}
		ICElement element = getCElementFromMarker(marker);
		IStructuredSelection selection = element == null ? new StructuredSelection() : new StructuredSelection(element);
		if (openWizard(name, selection) == Window.OK) {
			try {
				marker.delete();
			} catch (CoreException e) {
				CheckersUiActivator.log(e);
			}
		}
	}

	public int openWizard(final String className, IStructuredSelection selection) {
		NewClassCreationWizard wizard = new NewClassCreationWizard();
		wizard.setClassName(className);
		wizard.init(PlatformUI.getWorkbench(), selection);
		return new WizardDialog(null, wizard).open();
	}
}
