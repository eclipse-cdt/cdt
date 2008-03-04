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
package org.eclipse.dd.examples.dsf.filebrowser;

import java.io.File;

import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.IVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.IVMNode;

class FileVMContext extends AbstractVMContext {
    private File fFile;
    FileVMContext(IVMAdapter adapter, IVMNode layoutNode, File file) {
        super(adapter, layoutNode);
        fFile = file;
    }
    
    File getFile() { return fFile; }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof FileVMContext && ((FileVMContext)obj).getFile().equals(fFile);
    }
    
    @Override
    public int hashCode() {
        return fFile.hashCode();
    }
    
    @Override
    public String toString() {
        return fFile.toString();
    }
}