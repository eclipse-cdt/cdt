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

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;


/**
 * Viewer model node that populates the filesystem root elements.  
 */
@SuppressWarnings("restriction")
class FilesystemRootsVMNode extends AbstractVMNode 
    implements IElementLabelProvider
{
    public FilesystemRootsVMNode(AbstractVMProvider provider) {
        super(provider);
    }

    @Override
    public String toString() {
        return "FilesystemRootsVMNode"; 
    }
    
    public void update(final IChildrenUpdate[] updates) {
        new Job("") { //$NON-NLS-1$
            {
                setSystem(true);
                setPriority(INTERACTIVE);
            }
                
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                File[] files = File.listRoots();
                for (IChildrenUpdate update : updates) {
                    int offset = update.getOffset() != -1 ? update.getOffset() : 0;
                    int length = update.getLength() != -1 ? update.getLength() : files.length;
                    for (int i = offset; (i < files.length) && (i < (offset + length)); i++) {
                        update.setChild(new FileVMContext(FilesystemRootsVMNode.this, files[i]), i);
                    }
                    update.done();
                }
                return Status.OK_STATUS;
            }
        }.schedule();
    }
    
    public void update(final IHasChildrenUpdate[] updates) {
        for (IHasChildrenUpdate update : updates) {
            /*
             * Assume that all filesystem roots have children.  If user attempts 
             * to expand an empty directory, the plus sign will be removed
             * from the element.
             */
            update.setHasChilren(true);
            update.done();
        }
    }

    public void update(final IChildrenCountUpdate[] updates) {
        new Job("") { //$NON-NLS-1$
            {
                setSystem(true);
                setPriority(INTERACTIVE);
            }
                
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                for (IChildrenCountUpdate update : updates) {
                    if (!checkUpdate(update)) continue;
                    update.setChildCount(File.listRoots().length);
                    update.done();
                }                
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    public void update(final ILabelUpdate[] updates) {
        new Job("") { //$NON-NLS-1$
            {
                setSystem(true);
                setPriority(INTERACTIVE);
            }
                
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                for (ILabelUpdate update : updates) {
                    update.setLabel(getLabel((FileVMContext)update.getElement()), 0);
                    update.done();
                }
                
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    
    /**
     * Returs the text label to show in the tree for given element.  Filesystem 
     * roots return an empty string for call to File.getName(), use the abolute path 
     * string instead.
     */
    private String getLabel(FileVMContext vmc) {
        return vmc.getFile().getAbsolutePath();     
    }

    public int getDeltaFlags(Object e) {
        /*
         * @see buildDelta()
         */
        int retVal = IModelDelta.NO_CHANGE;
        if (e instanceof String) {
            retVal |= IModelDelta.SELECT | IModelDelta.EXPAND; 
        }
        
        return retVal;
    }
    
    public void buildDelta(final Object event, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor requestMonitor) {
        if (event instanceof String) {
            new Job("") { //$NON-NLS-1$
                {
                    setSystem(true);
                    setPriority(INTERACTIVE);
                }
                    
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    final File eventFile = new File((String)event);

                    if (eventFile.exists()) {
                        // Create a list containing all files in path of the file from the event
                        List<File> filePath = new LinkedList<File>();
                        for (File file = eventFile; file != null; file = file.getParentFile()) {
                            filePath.add(0, file);
                        }
                        File eventRoot = filePath.get(0);  
                        
                        // Get the index of the file in list of filesystem roots.
                        File[] roots = File.listRoots();
                        
                        int index = 0;
                        for (; index < roots.length; index++) {
                            if (eventRoot.equals(roots[index])) break;
                        }
                        
                        // Check if the specified file is not one of the roots.
                        if (index < roots.length) {
                            ModelDelta delta = parentDelta.addNode(
                                new FileVMContext(FilesystemRootsVMNode.this, eventRoot), 
                                index, IModelDelta.NO_CHANGE);

                            if (eventFile.equals(eventRoot)) {
                                // The event is for the root node.  Select it and extend parent node.
                                delta.setFlags(delta.getFlags() | IModelDelta.SELECT | IModelDelta.EXPAND);
                            }
                        } 
                    }
                    
                    // Invoke the request monitor.
                    requestMonitor.done();

                    return Status.OK_STATUS;
                }
            }.schedule();
        } else {
            requestMonitor.done();
        }            
    }
    
}
