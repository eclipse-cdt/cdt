/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.ui.browser.typeinfo.TypeSelectionDialog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

public class NewBaseClassSelectionDialog extends TypeSelectionDialog {
    
    private static final String DIALOG_SETTINGS = NewBaseClassSelectionDialog.class.getName();
    private static final int[] VISIBLE_TYPES = { ICElement.C_CLASS, ICElement.C_STRUCT };
    private static final int ADD_ID = IDialogConstants.CLIENT_ID + 1;
	private List fTypeList;
	private List fTypeListeners;
    
	public interface ITypeSelectionListener {
	    void typeAdded(ITypeInfo baseClass);
	}
	
    public NewBaseClassSelectionDialog(Shell parent) {
        super(parent);
        setTitle(NewClassWizardMessages.getString("NewBaseClassSelectionDialog.title")); //$NON-NLS-1$
        setMessage(NewClassWizardMessages.getString("NewBaseClassSelectionDialog.message")); //$NON-NLS-1$
        setDialogSettings(DIALOG_SETTINGS);
        setVisibleTypes(VISIBLE_TYPES);
        setFilter("*", true); //$NON-NLS-1$
		setStatusLineAboveButtons(true);
		fTypeList = new ArrayList();
		fTypeListeners = new ArrayList();
    }
    
    public void addListener(ITypeSelectionListener listener) {
        if (!fTypeListeners.contains(listener))
            fTypeListeners.add(listener);
    }
    
    public void removeListener(ITypeSelectionListener listener) {
        fTypeListeners.remove(listener);
    }
    
    private void notifyTypeAddedListeners(ITypeInfo type) {
        // first copy listeners in case one calls removeListener
        List list = new ArrayList(fTypeListeners);
        for (Iterator i = list.iterator(); i.hasNext(); ) {
            ITypeSelectionListener listener = (ITypeSelectionListener) i.next();
            listener.typeAdded(type);
        }
    }
    
    public ITypeInfo[] getAddedTypes() {
        return (ITypeInfo[])fTypeList.toArray(new ITypeInfo[fTypeList.size()]);
    }

    /*
	 * @see Dialog#createButtonsForButtonBar
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, ADD_ID, NewClassWizardMessages.getString("NewBaseClassSelectionDialog.addButton.label"), true); //$NON-NLS-1$
		super.createButtonsForButtonBar(parent);
	}
	
	/*
	 * @see Dialog#buttonPressed
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == ADD_ID){
			addType(getLowerSelectedElement());
		}
		super.buttonPressed(buttonId);	
	}
	
	/*
	 * @see Dialog#okPressed
	 */
	protected void okPressed() {
	    addType(getLowerSelectedElement());
		super.okPressed();
	}
	
	private void addType(Object elem) {
		if (elem instanceof ITypeInfo) {
		    ITypeInfo type = (ITypeInfo)elem;
		    if (fTypeList.contains(type)) {
                String qualifiedName = type.getQualifiedTypeName().getFullyQualifiedName();
                String message = NewClassWizardMessages.getFormattedString("NewBaseClassSelectionDialog.classalreadyadded.info", qualifiedName); //$NON-NLS-1$
                updateStatus(new StatusInfo(IStatus.INFO, message));
            } else {
				String qualifiedName = type.getQualifiedTypeName().getFullyQualifiedName();
				String message = NewClassWizardMessages.getFormattedString("NewBaseClassSelectionDialog.addingclass.info", qualifiedName); //$NON-NLS-1$
				updateStatus(new StatusInfo(IStatus.INFO, message));

                boolean canAdd = true;
                if (verifyBaseClasses()) {
                    IProgressService service = PlatformUI.getWorkbench().getProgressService();
                    NewClassWizardUtil.resolveClassLocation(type, service);
                    canAdd = (type.getResolvedReference() != null);
                }
                
//				// resolve location of base class
//				if (type.getResolvedReference() == null) {
//					final ITypeInfo[] typesToResolve = new ITypeInfo[] { type };
//					IRunnableWithProgress runnable = new IRunnableWithProgress() {
//						public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
//							AllTypesCache.resolveTypeLocation(typesToResolve[0], progressMonitor);
//							if (progressMonitor.isCanceled()) {
//								throw new InterruptedException();
//							}
//						}
//					};
//					
//					IProgressService service = PlatformUI.getWorkbench().getProgressService();
//					try {
//						service.busyCursorWhile(runnable);
//					} catch (InvocationTargetException e) {
//						String title= NewClassWizardMessages.getString("NewBaseClassSelectionDialog.getClasses.exception.title"); //$NON-NLS-1$
//						String errorMessage= NewClassWizardMessages.getString("NewBaseClassSelectionDialog.getClasses.exception.message"); //$NON-NLS-1$
//						ExceptionHandler.handle(e, title, errorMessage);
//					} catch (InterruptedException e) {
//						// cancelled by user
//					}
//				}
				
                if (canAdd) {
					fTypeList.add(type);
					
					message = NewClassWizardMessages.getFormattedString("NewBaseClassSelectionDialog.classadded.info", qualifiedName); //$NON-NLS-1$
					updateStatus(new StatusInfo(IStatus.INFO, message));

					notifyTypeAddedListeners(type);
				} else {
					message = NewClassWizardMessages.getFormattedString("NewBaseClassSelectionDialog.error.classnotadded", qualifiedName); //$NON-NLS-1$
					updateStatus(new StatusInfo(IStatus.ERROR, message));
				}
		    }
		}
	}
    
    /**
     * Checks if the base classes need to be verified (ie they must exist in the project)
     * 
     * @return <code>true</code> if the base classes should be verified
     */
    public boolean verifyBaseClasses() {
        return NewClassWizardPrefs.verifyBaseClasses();
    }
	
	/*
	 * @see AbstractElementListSelectionDialog#handleDefaultSelected()
	 */
	protected void handleDefaultSelected() {
		if (validateCurrentSelection())
			buttonPressed(ADD_ID);
	}
}
