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

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.RootVMNode;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * 
 */
@SuppressWarnings("restriction")
public class FileBrowserVMProvider extends AbstractVMProvider 
{
    /**
     * The object to be set to the viewer that shows contents supplied by this provider.
     * @see org.eclipse.jface.viewers.TreeViewer#setInput(Object)  
     */
    private final IAdaptable fViewerInputObject = 
        new IAdaptable() {
            /**
             * The input object provides the viewer access to the viewer model adapter.
             */
            @SuppressWarnings("unchecked")
            public Object getAdapter(Class adapter) {
                if ( adapter.isInstance(getVMAdapter()) ) {
                    return getVMAdapter();
                }
                return null;
            }
            
            @Override
            public String toString() {
                return "File Browser Viewer Input"; //$NON-NLS-1$
            }
        };

    /**
     * Constructor creates and configures the layout nodes to display file 
     * system contents.  
     * @param adapter The viewer model adapter that this provider is registered with.
     * @param presentationContext The presentation context that this provider is 
     * generating contents for.
     */
    public FileBrowserVMProvider(AbstractVMAdapter adapter, IPresentationContext presentationContext) {
        super(adapter, presentationContext);

        IRootVMNode root = new RootVMNode(this); 
        IVMNode fileSystemRoots = new FilesystemRootsVMNode(this);
        addChildNodes(root, new IVMNode[] { fileSystemRoots });
        IVMNode files = new FileVMNode(this);
        addChildNodes(fileSystemRoots, new IVMNode[] { files });
        addChildNodes(files, new IVMNode[] { files });
        setRootNode(root);
    }
    
    /**
     * Returns the input object to be set to the viewer that shows contents
     * supplied by this provider.  
     */
    public Object getViewerInputObject() {
        return fViewerInputObject;
    }    

    /**
     * Event handler for file selection text changes in the dialog.
     * @param text New text entered in file selection text box.
     */
    void selectionTextChanged(final String text) {
        if (isDisposed()) return;
        
        // We're in the UI thread.  Re-dispach to VM Adapter executor thread 
        // and then call root layout node.
        try {
            getExecutor().execute(new Runnable() {
                public void run() {
                    if (isDisposed()) return;
                    handleEvent(text);
                }});
        } catch (RejectedExecutionException e) {
            // Ignore.  This exception could be thrown if the provider is being 
            // shut down.  
        }
    }
}
