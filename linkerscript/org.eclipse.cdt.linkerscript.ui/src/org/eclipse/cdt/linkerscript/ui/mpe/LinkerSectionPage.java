/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.ui.mpe;

import org.eclipse.cdt.linkerscript.ui.form.SectionMasterDetailsBlock;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;

public class LinkerSectionPage extends FormPage {
	public static final String ID = "sections.section"; //$NON-NLS-1$
	private SectionMasterDetailsBlock sectionMasterDetailsBlock;

	public LinkerSectionPage(FormEditor editor) {
		super(editor, ID, "Sections");
		sectionMasterDetailsBlock = new SectionMasterDetailsBlock(
				new Model(((MultiPageLinkerScriptEditor) getEditor())::getXtextDocument));
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		sectionMasterDetailsBlock.createContent(managedForm);

	}
}
