/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.filebrowser;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * File Browser example dialog.  It hold a tree viewer that displays 
 * file system contents and a text box for entering a file path to be
 * shown in the tree. 
 */
@SuppressWarnings("restriction")
public class FileBrowserDialog extends Dialog {

    /**
     * Tree viewer for showing the filesystem contents.
     */
    private TreeModelViewer fViewer;

    /**
     * The model adapter for the tree viewer. 
     */
    private FileBrowserModelAdapter fModelAdapter;
    
    /**
     * Flag used to disable text-box changed events, when the text
     * box is updated due to selection change in tree.
     */
    private boolean fDisableTextChangeNotifications = false;
    
    public FileBrowserDialog(Shell parent) {
        super(parent);
        setShellStyle(getShellStyle() | SWT.RESIZE);        
    }    

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        IPresentationContext presentationContext = new PresentationContext("org.eclipse.cdt.examples.dsf.filebrowser");  //$NON-NLS-1$
        
        fViewer = new TreeModelViewer(area, SWT.VIRTUAL, presentationContext);
        fViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        
        fModelAdapter = new FileBrowserModelAdapter(presentationContext);
        fViewer.setInput(fModelAdapter.getVMProvider().getViewerInputObject());

        final Text text = new Text(area, SWT.SINGLE | SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                /*
                 * Update the file name in the text control, to match the 
                 * selection in the tree.  Do this only if the user is not
                 * actively typing in the text field (test if text has focus).
                 */
                if (!text.isFocusControl() &&
                    event.getSelection() instanceof IStructuredSelection &&
                    ((IStructuredSelection)event.getSelection()).getFirstElement() instanceof FileVMContext)
                {
                    FileVMContext fileVmc = (FileVMContext)((IStructuredSelection)event.getSelection()).getFirstElement();
                    
                    fDisableTextChangeNotifications = true;
                    text.setText(fileVmc.getFile().getAbsolutePath());
                    fDisableTextChangeNotifications = false;
                }
            }
        });
        
        text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (!fDisableTextChangeNotifications) {
                    fModelAdapter.getVMProvider().selectionTextChanged(text.getText());
                }
            }
        });
        
        return area;
    }
    
    @Override
    public boolean close() {
        if (super.close()) {
            fModelAdapter.dispose();
            fModelAdapter = null;
            return true;
        }
        return false;
    }

}
