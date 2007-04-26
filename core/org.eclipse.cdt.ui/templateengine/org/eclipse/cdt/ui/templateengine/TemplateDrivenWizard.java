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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;

import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.ui.CUIPlugin;


/**
 * Any wizard intending to use template (@see org.eclipse.cdt.core.templateenginee.Template) based pages
 * can extend this wizard and use it. Alternatively, a wizard intending to show a choice of templates 
 * should use TemplatesChoiceWizard (@see org.eclipse.cdt.core.templateenginee.ui.TemplatesChoiceWizard)
 *  
 */
public abstract class TemplateDrivenWizard extends Wizard {
	protected List/*<IWizardPage>*/ pagesBeforeTemplatePages = new ArrayList/*<IWizardPage>*/();
	protected List/*<IWizardPage>*/ pagesAfterTemplatePages = new ArrayList/*<IWizardPage>*/();
	
	protected Template template;
	protected int pageIndex;
	protected Map/*<String, UIWizardPage>*/ templatePages;
	protected Composite pageContainer;
	protected List/*<String>*/ templatePagesOrderVector;
	
	public final void addPage(IWizardPage page) {
        page.setWizard(this);
	}

	public final void addPages() {
		IWizardPage[] pages = getPagesBeforeTemplatePages();
		for(int i=0; i<pages.length; i++) {
			addPageBeforeTemplatePages(pages[i]);
		}
		
		pages = getPagesAfterTemplatePages();
		for(int i=0; i<pages.length; i++) {
			addPageAfterTemplatePages(pages[i]);
		}
	}

	private void addPageBeforeTemplatePages(IWizardPage page) {
		addPage(page);
		pagesBeforeTemplatePages.add(page);
	}

	private void addPageAfterTemplatePages(IWizardPage page) {
		addPage(page);
		pagesAfterTemplatePages.add(page);
	}

	protected abstract IWizardPage[] getPagesBeforeTemplatePages();

	protected abstract IWizardPage[] getPagesAfterTemplatePages();
	
	/**
	 * @return  the template
	 */
	protected abstract Template getTemplate();

	public IWizardPage getPreviousPage(IWizardPage page) {
		if (pageIndex > pagesBeforeTemplatePages.size() + templatePagesOrderVector.size()) {//current is some page after template pages other than the first post-template page
			pageIndex--;
			return (IWizardPage) pagesAfterTemplatePages.get(pageIndex - pagesBeforeTemplatePages.size() - templatePagesOrderVector.size());
		} else if (pageIndex > pagesBeforeTemplatePages.size()) {//current is some template page other than the first
			pageIndex--;
	        return (IWizardPage) templatePages.get(templatePagesOrderVector.get(pageIndex - pagesBeforeTemplatePages.size()));
		} else if (pageIndex > 0) {
			pageIndex--;
			return (IWizardPage) pagesBeforeTemplatePages.get(pageIndex);
		}
		return null;
	}

	public IWizardPage getNextPage(IWizardPage page) {
		if (pageIndex < pagesBeforeTemplatePages.size() - 1) {//current is a page before template pages that is not the final one
			pageIndex++;
			return (IWizardPage) pagesBeforeTemplatePages.get(pageIndex);
		} else if (pageIndex < pagesBeforeTemplatePages.size() + templatePagesOrderVector.size() - 1) {
			if(pageIndex == pagesBeforeTemplatePages.size() - 1) {//current is final page before template pages
				Template template = getTemplate();
				if (this.template != null && !this.template.equals(template)) {//template changed
					this.template = template;
					//TODO: dispose old template pages
					templatePages = template.getUIPages();
					templatePagesOrderVector = template.getPagesOrderVector();
				}
			}//else current is some template page other than the final one
			pageIndex++;
			IWizardPage nextPage = (IWizardPage) templatePages.get(templatePagesOrderVector.get(pageIndex - pagesBeforeTemplatePages.size()));
	        nextPage.setWizard(this);
	        if (nextPage.getControl() == null) {
	        	nextPage.createControl(pageContainer);
	        }
	        return nextPage;
		} else if (pageIndex < pagesBeforeTemplatePages.size() + templatePagesOrderVector.size() + pagesAfterTemplatePages.size() - 1) {//current is final template page or a page after the final template page
			pageIndex++;
			return (IWizardPage) pagesAfterTemplatePages.get(pageIndex - pagesBeforeTemplatePages.size() - templatePagesOrderVector.size());
		}
		return null;
	}

	public final boolean canFinish() {
		for(Iterator i = pagesBeforeTemplatePages.iterator(); i.hasNext(); ) {
			IWizardPage page = (IWizardPage) i.next();
            if (!page.isPageComplete()) {
                return false;
            }
        }
        if (templatePages == null) {
        	return false;
        }
        for(Iterator i = templatePages.values().iterator(); i.hasNext(); ) {
        	IWizardPage page = (IWizardPage) i.next();
        	if (!page.isPageComplete()) {
                return false;
            }
        }
        for(Iterator i = pagesAfterTemplatePages.iterator(); i.hasNext(); ) {
			IWizardPage page = (IWizardPage) i.next();
            if (!page.isPageComplete()) {
                return false;
            }
        }
        return true;
	}

	public boolean performFinish() {
		IRunnableWithProgress op= new WorkspaceModifyDelegatingOperation(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				finishPage(monitor);
			}
		});

		try {
			getContainer().run(true, false, op);
		} catch (InvocationTargetException e) {
			return false;
		} catch  (InterruptedException e) {
			return false;
		}
		return true;
	}

	private boolean finishPage(IProgressMonitor monitor) {
	    IStatus[] statuses = template.executeTemplateProcesses(monitor, false);
	    if (statuses.length == 1 && statuses[0].getException() instanceof ProcessFailureException) {
	    	TemplateEngineUIUtil.showError(statuses[0].getMessage(), statuses[0].getException());
		    return false;
	    } else {
	    	String msg = Messages.getString("TemplateDrivenWizard.0"); //$NON-NLS-1$
	    	TemplateEngineUIUtil.showStatusDialog(msg, new MultiStatus(CUIPlugin.getPluginId(), IStatus.OK, statuses, msg, null));
		    return true;
	    }
	}
	
	public final void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
		this.pageContainer = pageContainer;
	}
}
