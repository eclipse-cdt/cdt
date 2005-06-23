/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
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
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;

public class SourceFolderSelectionDialog extends ElementTreeSelectionDialog {
    
    private static final Class[] VALIDATOR_CLASSES = new Class[] { ICContainer.class, ICProject.class };
    private static final TypedElementSelectionValidator fValidator = new TypedElementSelectionValidator(VALIDATOR_CLASSES, false) {
        public boolean isSelectedValid(Object element) {
            if (element instanceof ICProject) {
                ICProject cproject = (ICProject) element;
                IPath path = cproject.getProject().getFullPath();
                return (cproject.findSourceRoot(path) != null);
            } else if (element instanceof ICContainer) {
                // if (CModelUtil.getSourceFolder((ICContainer)obj) != null)
                return true;
            }
            return false;
        }
    };
    
    private static final Class[] FILTER_CLASSES = new Class[] { ICModel.class, ICContainer.class, ICProject.class };
    private static final ViewerFilter fFilter = new TypedViewerFilter(FILTER_CLASSES) {
        public boolean select(Viewer viewer, Object parent, Object element) {
            //	if (obj instanceof ICContainer
            //			&& CModelUtil.getSourceFolder((ICContainer)obj) != null) {
            //		return true;
            //	}
            return super.select(viewer, parent, element);
        }
    };
    
    private static final CElementContentProvider fContentProvider = new CElementContentProvider();
    private static final ILabelProvider fLabelProvider = new CElementLabelProvider(CElementLabelProvider.SHOW_DEFAULT);
    private static final ViewerSorter fSorter = new CElementSorter();
    
    public SourceFolderSelectionDialog(Shell parent) {
        super(parent, fLabelProvider, fContentProvider);
        setValidator(fValidator);
        setSorter(fSorter);
        addFilter(fFilter);
        setTitle(NewWizardMessages.getString("SourceFolderSelectionDialog.title")); //$NON-NLS-1$
        setMessage(NewWizardMessages.getString("SourceFolderSelectionDialog.description")); //$NON-NLS-1$
    }
}
