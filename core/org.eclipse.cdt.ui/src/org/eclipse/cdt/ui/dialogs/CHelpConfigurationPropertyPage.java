/**********************************************************************
 * Copyright (c) 2004, 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.CHelpProviderManager;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.internal.ui.text.CHelpBookDescriptor;
import org.eclipse.cdt.internal.ui.util.ImageDescriptorRegistry;
import org.eclipse.cdt.internal.ui.util.PixelConverter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.CheckedListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * This class defines a project property page 
 * for C/C++ project help settings configuration
 * @since 2.1
 */
public class CHelpConfigurationPropertyPage extends PropertyPage implements
		IWorkbenchPreferencePage {

	private CHelpSettingsDisplay fCHelpSettingsDisplay;
	
	private class CHelpBookListLabelProvider extends LabelProvider {
		private ImageDescriptor fHelpProviderIcon;
		private ImageDescriptorRegistry fRegistry;
		
		public CHelpBookListLabelProvider() {
			fRegistry= CUIPlugin.getImageDescriptorRegistry();
			fHelpProviderIcon= CPluginImages.DESC_OBJS_LIBRARY;
		}
		
		public String getText(Object element) {
			if (element instanceof CHelpBookDescriptor) {
				return ((CHelpBookDescriptor)element).getCHelpBook().getTitle();
			}
			return super.getText(element);
		}

		public Image getImage(Object element) {
			if (element instanceof CHelpBookDescriptor) {
				return fRegistry.get(fHelpProviderIcon);
			} 
			return null;
		}
	}
	
	private class CHelpSettingsDisplay {
		private CheckedListDialogField fCHelpBookList;
		private IProject fProject;
		private CHelpBookDescriptor fCHelpBookDescriptors[];
		
		public CHelpSettingsDisplay() {
		
			String[] buttonLabels= new String[] {
				/* 0 */ CUIMessages.getString("CHelpConfigurationPropertyPage.buttonLabels.CheckAll"), //NewWizardMessages.getString("BuildPathsBlock.classpath.checkall.button"), //$NON-NLS-1$
				/* 1 */ CUIMessages.getString("CHelpConfigurationPropertyPage.buttonLabels.UncheckAll") //NewWizardMessages.getString("BuildPathsBlock.classpath.uncheckall.button") //$NON-NLS-1$
			};
		
			fCHelpBookList= new CheckedListDialogField(null, buttonLabels, new CHelpBookListLabelProvider());
			fCHelpBookList.setLabelText(CUIMessages.getString("CHelpConfigurationPropertyPage.HelpBooks")); //$NON-NLS-1$
			fCHelpBookList.setCheckAllButtonIndex(0);
			fCHelpBookList.setUncheckAllButtonIndex(1);
		}
		
		public Control createControl(Composite parent){
			PixelConverter converter= new PixelConverter(parent);
			
			Composite composite= new Composite(parent, SWT.NONE);
			
			LayoutUtil.doDefaultLayout(composite, new DialogField[] { fCHelpBookList }, true);
			LayoutUtil.setHorizontalGrabbing(fCHelpBookList.getListControl(null));

			int buttonBarWidth= converter.convertWidthInCharsToPixels(24);
			fCHelpBookList.setButtonsMinWidth(buttonBarWidth);
			
			return composite;
		}
		
		public void init(final IResource resource) {
			if(!(resource instanceof IProject))
				return;
			fProject = (IProject)resource;
			fCHelpBookDescriptors = CHelpProviderManager.getDefault().getCHelpBookDescriptors(new ICHelpInvocationContext(){
				public IProject getProject(){return (IProject)resource;}
				public ITranslationUnit getTranslationUnit(){return null;}
				}
			);

			List allTopicsList= Arrays.asList(fCHelpBookDescriptors);
			List enabledTopicsList= getEnabledEntries(allTopicsList);
			
			fCHelpBookList.setElements(allTopicsList);
			fCHelpBookList.setCheckedElements(enabledTopicsList);
		}

		private List getEnabledEntries(List list) {
			int size = list.size();
			List desList= new ArrayList();

			for (int i= 0; i < size; i++) {
				CHelpBookDescriptor el = (CHelpBookDescriptor)list.get(i);
				if(el.isEnabled())
					desList.add(el);
			}
			return desList;
		}
		
		public void performOk(){
			List list = fCHelpBookList.getElements();
			final IProject project = fProject;
			
			for(int i = 0; i < list.size(); i++){
				Object obj = list.get(i);
				if(obj != null && obj instanceof CHelpBookDescriptor){
					((CHelpBookDescriptor)obj).enable(fCHelpBookList.isChecked(obj));
				}
			}
			CHelpProviderManager.getDefault().serialize(new ICHelpInvocationContext(){
				public IProject getProject(){return project;}
				public ITranslationUnit getTranslationUnit(){return null;}
				});
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		fCHelpSettingsDisplay= new CHelpSettingsDisplay();
		fCHelpSettingsDisplay.init((IResource)getElement());
		return fCHelpSettingsDisplay.createControl(parent);
	}

	public boolean performOk() {
		fCHelpSettingsDisplay.performOk();
		super.performOk();
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

}
