/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.TemplateDescriptor;
import org.eclipse.cdt.core.templateengine.TemplateEngine;
import org.eclipse.cdt.core.templateengine.TemplateInfo;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.templateengine.pages.UIPagesProvider;
import org.eclipse.cdt.ui.templateengine.pages.UIWizardPage;
import org.eclipse.cdt.ui.templateengine.uitree.UIElementTreeBuilderHelper;
import org.eclipse.cdt.ui.templateengine.uitree.UIElementTreeBuilderManager;

/**
 * Template class is responsible for initiating GUI construction. Collecting data from GUI and 
 * initiating process part of Template Engine. This is created per TemplateDescriptor basis. 
 * Once The Template is created it creates a TemplateDescriptor for the XML file name given.
 * 
 * @since 4.0
 */

public class Template extends TemplateCore {
	
	private TemplateDescriptor templateDescriptor;
	private UIElementTreeBuilderManager uiElementTreeBuilderManager;
	private UIPagesProvider uiPagesProvider;
	private Map/*<String, UIWizardPage>*/ pageMap;
	
	public Template(TemplateInfo templateInfo) throws IOException, ProcessFailureException, SAXException, ParserConfigurationException {
		super(templateInfo);
		templateDescriptor = getTemplateDescriptor();
		uiElementTreeBuilderManager = new UIElementTreeBuilderManager(new UIElementTreeBuilderHelper(templateDescriptor, templateInfo));
		uiPagesProvider = new UIPagesProvider();
	}

	/**
	 * 1. get PropertyGroupList. 
	 * 2. clear UIPage's display order Vector. 
	 * 3. for each PropertyGroup create the UIElementTree. 
	 * 4. Request the UIPagesProvider to generate UIPages for the Tree. 
	 * 5. return the HashMap of UIPages.
	 */
	public Map/*<String, UIWizardPage>*/ getUIPages() {
		if (pageMap == null) {
			pageMap = new HashMap/*<String, UIWizardPage>*/();
			List rootPropertyGrouplist = templateDescriptor.getPropertyGroupList();
			
			uiPagesProvider.clearOrderVector();
	
			for (int i = 0; i < rootPropertyGrouplist.size(); i++) {
				// since the tree is constructed for a list of PropertyGroup's tree
				// root is set to null
				// before invoking createUIElementTree(...).
				uiElementTreeBuilderManager.setUIElementTreeRootNull();
				uiElementTreeBuilderManager.createUIElementTree(null, (Element) rootPropertyGrouplist.get(i));
				pageMap.putAll(uiPagesProvider.getWizardUIPages(uiElementTreeBuilderManager.getUIElementTreeRoot(), getValueStore()));
			}
		}

		return pageMap;
	}
	
	
	public IWizardPage[] getTemplateWizardPages(IWizardPage predatingPage, IWizardPage followingPage, IWizard wizard) {
		List pages= new ArrayList();
//		if (predatingPage != null) { 
//			pages.add(predatingPage);
//		}
		
		Map templatePages = getUIPages();
		List templatePagesOrderVector = getPagesOrderVector();
		if (templatePagesOrderVector.size() != 0) {
			IWizardPage prevPage = predatingPage;

			for (int i=0; i < templatePagesOrderVector.size(); i++) {
				UIWizardPage page = (UIWizardPage) templatePages.get(templatePagesOrderVector.get(i));
				pages.add(page);
				page.setPreviousPage(prevPage);
				if (i+1 < templatePagesOrderVector.size()) {
					page.setNextPage((UIWizardPage) templatePages.get(templatePagesOrderVector.get(i+1)));
				} else {
					page.setNextPage(followingPage);
				}
				page.setWizard(wizard);
				prevPage = page;
			}
			
			try {
				IWizardDataPage[] extraPages = getExtraCreatedPages((IWorkbenchWizard)wizard);
				for (int i=0; i < extraPages.length; i++) {
					IWizardDataPage page = extraPages[i];
					pages.add(page);
					page.setPreviousPage(prevPage);
					//TODO: set the next page for page
					//page.setNextPage(extraPages[i+1]);
					page.setWizard(wizard);
					prevPage = page;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			followingPage.setPreviousPage(prevPage);
		} else {
			followingPage.setPreviousPage(predatingPage);	
		}
		
//		pages.add(followingPage);
		
		return (IWizardPage[]) pages.toArray(new IWizardPage[pages.size()]);
	}
	
	IWizardDataPage[] getExtraCreatedPages(IWorkbenchWizard wizard) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		TemplateInfo templateInfo = getTemplateInfo();
		String pagesProvider = templateInfo.getExtraPagesProvider();
		if (pagesProvider != null) {
			IPagesAfterTemplateSelectionProvider extraPagesProvider = (IPagesAfterTemplateSelectionProvider) Class.forName(pagesProvider).newInstance();
			if (extraPagesProvider != null) {
				List/*<IWizardDataPage>*/ pageList = new ArrayList/*<IWizardDataPage>*/();
				IWizardDataPage[] extraPages = extraPagesProvider.getCreatedPages(wizard);
				pageList.addAll(Arrays.asList(extraPages));
				return (IWizardDataPage[]) pageList.toArray(new IWizardDataPage[pageList.size()]);
			}
		}
		return new IWizardDataPage[0];		
	}
	
	
	/**
	 * 
	 * @return List,which contains Page display order
	 */

	public List/*<String>*/ getPagesOrderVector() {
		return uiPagesProvider.getOrderVector();
	}

	/**
	 * this method is for JUnit Test case excecution. return the
	 * UIElementTreeBuilderManager instance used by this Template.
	 * 
	 * @return UIElementTreeBuilderManager
	 */
	public UIElementTreeBuilderManager getUIElementTreeBuilderManager() {
		return uiElementTreeBuilderManager;
	}
	
	/**
	 * initializeProcessBlockList() will create the ProcessBlockList,
	 * processPorcessBlockList() will invoke each process execution by assigning
	 * resources to each process (Ref. ProcessResourceManager).
	 * @param monitor 
	 */
	public IStatus[] executeTemplateProcesses(IProgressMonitor monitor, final boolean showError) {
		setDirty();
		TemplateEngine.getDefault().updateSharedDefaults(this);
		final IStatus[][] result = new IStatus[1][];
		WorkspaceModifyOperation wmo = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException ,java.lang.reflect.InvocationTargetException ,InterruptedException {
				try {
					result[0] = getProcessHandler().processAll(monitor);
				} catch (ProcessFailureException e) {
					if (showError) {
						TemplateEngineUIUtil.showError(e.getMessage(), e.getCause());
					}
					result[0] = new IStatus[] {new Status(IStatus.ERROR, CUIPlugin.getPluginId(), IStatus.ERROR, e.getMessage(), e)};
				}
			}
		};
		try {
			wmo.run(monitor); // TODO support progress monitors
		} catch(InterruptedException ie) {
			throw new RuntimeException(ie);
		} catch(InvocationTargetException ite) {
			throw new RuntimeException(ite.getTargetException());
		}
		return result[0];
	}
	
}
