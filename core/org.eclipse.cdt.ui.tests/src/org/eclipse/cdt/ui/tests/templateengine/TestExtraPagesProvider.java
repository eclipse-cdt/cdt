/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.templateengine;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import org.eclipse.cdt.ui.templateengine.AbstractWizardDataPage;
import org.eclipse.cdt.ui.templateengine.IPagesAfterTemplateSelectionProvider;
import org.eclipse.cdt.ui.templateengine.IWizardDataPage;

/**
 * An example implementation of {@link IPagesAfterTemplateSelectionProvider} for
 * testing purposes.
 */
public class TestExtraPagesProvider implements IPagesAfterTemplateSelectionProvider {
	IWizardDataPage[] pages;
	
	@Override
	public IWizardDataPage[] createAdditionalPages(IWorkbenchWizard wizard,
			IWorkbench workbench, IStructuredSelection selection) {
		pages= new IWizardDataPage[3];
		pages[0]= new MyPage("Example custom page 1", "exampleAttr1", "Value1");
		pages[1]= new MyPage("Example custom page 2", "exampleAttr2", "Value2");
		pages[2]= new MyPage("Example custom page 3", "exampleAttr3", "Value3");
		return pages;
	}

	@Override
	public IWizardDataPage[] getCreatedPages(IWorkbenchWizard wizard) {
		return pages;
	}

	/**
	 * An example implementation of {@link IWizardDataPage} for test purposes.
	 */
	static class MyPage extends AbstractWizardDataPage implements IWizardDataPage {
		String labelText, dataKey, dataValue;
		
		public MyPage(String labelText, String dataKey, String dataValue) {
			super("CustomTestPageName", "Title", null);
			setMessage("Custom test page message");
			this.labelText= labelText;
			this.dataKey= dataKey;
			this.dataValue= dataValue;
		}
		
		@Override
		public Map<String, String> getPageData() {
			return Collections.singletonMap(dataKey, dataValue);
		}

		@Override
		public void createControl(Composite parent) {
			Label l= new Label(parent, SWT.NONE);
			l.setText(labelText);
			setControl(l);
		}
	}
}
