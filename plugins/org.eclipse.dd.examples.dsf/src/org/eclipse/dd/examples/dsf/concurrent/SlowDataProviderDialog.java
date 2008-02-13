/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.dsf.concurrent;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog shared by all slow data provider examples.  It accepts the data 
 * provider and the content provider as arguments to the constructor.  So the 
 * only thing that the dialog does is to create the table viewer and 
 * initialize it with the providers. 
 */
public class SlowDataProviderDialog extends Dialog {
    
    private TableViewer fDataViewer;
    private DataProvider fDataProvider;
    private IContentProvider fContentProvider;
    
    public SlowDataProviderDialog(Shell parent, IContentProvider contentProvider, DataProvider dataProvider) {
        super(parent);
        setShellStyle(getShellStyle() | SWT.RESIZE);        
        fContentProvider = contentProvider;
        fDataProvider = dataProvider;
    }    
    
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        fDataViewer = new TableViewer(area, SWT.VIRTUAL);
        fDataViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
        fDataViewer.setContentProvider(fContentProvider);
        fDataViewer.setInput(fDataProvider);
        return area;
    }
}
