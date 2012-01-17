/*******************************************************************************
 * Copyright (c) 2008 ARM Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Limited - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.disassembly.viewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.ui.disassembly.IDocumentElementContentProvider;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentPresentation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * Manages the mapping between the viewer model and the underlying debug model 
 * through the content, label and annotation adapters. 
 * Maintains the list of model proxies and reacts to the debug model changes.
 */
public class DocumentContentProvider implements IModelChangedListener {

    private VirtualSourceViewer fViewer;    
    private VirtualDocument fDocument;

    private Object fRoot;
    private Object fBase;
    private Object fInput;
    
    private IModelProxy fRootProxy;
    private IModelProxy fBaseProxy;
    private List<IModelProxy> fLineProxies = new ArrayList<IModelProxy>( 50 );
    private Map<Object, Integer> fLineElements = new HashMap<Object, Integer>( 20 );

    private DocumentUpdate fUpdateInProgress;

    public DocumentContentProvider( VirtualDocument document ) {
        super();
        fDocument = document;
    }

    protected void init( Object root ) {
        fRoot = root;
        installRootProxy( fRoot );
    }

    public void update( IDocumentPresentation presentationContext, int lineCount, int offset, boolean reveal ) {
        IDocumentElementContentProvider contentAdapter = getContentAdapter( getInput() );
        if ( contentAdapter != null && getRoot() != null && getBase() != null ) {
            DocumentContentUpdate update = new DocumentContentUpdate( this, contentAdapter, presentationContext, getRoot(), getBase(), getInput(), lineCount, offset, reveal );
            schedule( update );
        }
        else {
            updateCompleted( new DocumentContentUpdate( this, contentAdapter, presentationContext, getRoot(), getBase(), getInput(), lineCount, offset, reveal ) );
        }
    }

    public synchronized void updateCompleted( DocumentContentUpdate update ) {
        if ( fUpdateInProgress == update ) {
            fUpdateInProgress = null;
        }
        if ( !update.isCanceled() ) {
            disposeLineProxies();
            fLineElements.clear();
            getDocument().setCurrentOffset( update.getOffset() );
            Object[] elements = update.getElements();
            for ( int i = 0; i < elements.length; ++i ) {
                fLineElements.put( elements[i], Integer.valueOf( i ) );
                installLineProxy( i, elements[i] );
                getDocument().updateElement( getInput(), i, elements[i] );
            }
        }
        // TODO: display error content if status is not OK
    }

    protected IDocumentElementContentProvider getContentAdapter( Object element ) {
        IDocumentElementContentProvider adapter = null;
        if ( element instanceof IDocumentElementContentProvider ) {
            adapter = (IDocumentElementContentProvider)element;
        }
        else if ( element instanceof IAdaptable ) {
            IAdaptable adaptable = (IAdaptable)element;
            adapter = (IDocumentElementContentProvider)adaptable.getAdapter( IDocumentElementContentProvider.class );
        }
        return adapter;
    }

    protected VirtualDocument getDocument() {
        return fDocument;
    }

    public Object getRoot() {
        return fRoot;
    }

    public Object getInput() {
        return fInput;
    }
    
    public Object getBase() {
        return fBase;
    }

    public void dispose() {
        synchronized( this ) {
            if ( fUpdateInProgress != null ) {
                fUpdateInProgress.cancel();
            }
        }
        disposeRootProxy();
        fDocument = null;
        fInput = null;
        fViewer = null;
    }

    public void changeInput( VirtualSourceViewer viewer, IDocumentPresentation presentationContext, Object oldInput, Object newInput, int offset ) {
        fViewer = viewer;
        fInput = newInput;
        IDocumentElementContentProvider contentAdapter = getContentAdapter( getInput() );
        if ( contentAdapter != null ) {
            DocumentBaseChangeUpdate update = new DocumentBaseChangeUpdate( this, contentAdapter, presentationContext, getRoot(), getBase(), getInput(), offset );
            schedule( update );
        }
        else {
            inputChanged( new DocumentBaseChangeUpdate( this, contentAdapter, presentationContext, getRoot(), getBase(), getInput(), offset ) );
        }
    }

    public synchronized void inputChanged( DocumentBaseChangeUpdate update ) {
        if ( fUpdateInProgress == update ) {
            fUpdateInProgress = null;
        }
        Object newBase = update.getBaseElement();
        int newOffset = update.getOffset();
        VirtualDocument document = getDocument();
        if ( document != null ) {
            boolean needsUpdate = false;
            if ( newBase != getBase() ) {
                fBase = newBase;
                disposeBaseProxy();
                installBaseProxy( fBase );
                needsUpdate = true;
            }
            if ( newOffset != document.getCurrentOffset() ) {
                document.setCurrentOffset( newOffset );
                needsUpdate = true;
            }
            if ( needsUpdate ) {
                WorkbenchJob job = new WorkbenchJob( "refresh content" ) { //$NON-NLS-1$
                    
                    /* (non-Javadoc)
                     * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
                     */
                    @Override
                    public IStatus runInUIThread( IProgressMonitor monitor ) {
                        getViewer().refresh( true );
                        return Status.OK_STATUS;
                    }
                };
                job.setSystem( true );
                job.schedule();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener#modelChanged(org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta, org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy)
     */
    @Override
	public void modelChanged( final IModelDelta delta, final IModelProxy proxy ) {
        WorkbenchJob job = new WorkbenchJob( "process model delta" ) { //$NON-NLS-1$
            
            /* (non-Javadoc)
             * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
             */
            @Override
            public IStatus runInUIThread( IProgressMonitor monitor ) {
                if ( !proxy.isDisposed() ) {
                    handleModelChanges( delta );
                }
                return Status.OK_STATUS;
            }
        };
        job.setSystem( true );
        job.schedule();
    }

    protected void handleModelChanges( IModelDelta delta ) {
        updateNodes( new IModelDelta[] { delta } );
    }

    protected void updateNodes( IModelDelta[] nodes ) {
        for( int i = 0; i < nodes.length; i++ ) {
            IModelDelta node = nodes[i];
            int flags = node.getFlags();

            if ( (flags & IModelDelta.ADDED) != 0 ) {
                handleAdd( node );
            }
            if ( (flags & IModelDelta.REMOVED) != 0 ) {
                handleRemove( node );
            }
            if ( (flags & IModelDelta.CONTENT) != 0 ) {
                handleContent( node );
            }
            if ( (flags & IModelDelta.SELECT) != 0 ) {
                handleSelect( node );
            }
            if ( (flags & IModelDelta.STATE) != 0 ) {
                handleState( node );
            }
            if ( (flags & IModelDelta.INSERTED) != 0 ) {
                handleInsert( node );
            }
            if ( (flags & IModelDelta.REPLACED) != 0 ) {
                handleReplace( node );
            }
            if ( (flags & IModelDelta.INSTALL) != 0 ) {
                handleInstall( node );
            }
            if ( (flags & IModelDelta.UNINSTALL) != 0 ) {
                handleUninstall( node );
            }
            if ( (flags & IModelDelta.REVEAL) != 0 ) {
                handleReveal( node );
            }
            updateNodes( node.getChildDeltas() );
        }
    }

    protected void handleState( IModelDelta delta ) {
        int index = getElementIndex( delta.getElement() );
        if ( index >= 0 ) {
            getDocument().updateElement( getInput(), index, delta.getElement() );
        }
    }

    protected void handleSelect( IModelDelta delta ) {

    }

    protected void handleContent( IModelDelta delta ) {
        if ( delta.getElement().equals( getRoot() ) || delta.getElement().equals( getBase() ) ) {
            getViewer().refresh();
        }
    }

    protected void handleRemove( IModelDelta delta ) {

    }

    protected void handleAdd( IModelDelta delta ) {

    }

    protected void handleInsert( IModelDelta delta ) {

    }

    protected void handleReplace( IModelDelta delta ) {

    }

    protected void handleReveal( IModelDelta delta ) {

    }

    protected void handleInstall( IModelDelta delta ) {
    }

    protected void handleUninstall( IModelDelta delta ) {
    }   

    protected synchronized void installRootProxy( Object element ) {
        if ( element != null && (!element.equals( getRoot()) || fRootProxy == null) ) {
            disposeRootProxy();
            IModelProxyFactory modelProxyFactory = getModelProxyFactoryAdapter( element );
            if ( modelProxyFactory != null ) {
                final IModelProxy proxy = modelProxyFactory.createModelProxy( element, getPresentationContext() );
                if ( proxy != null ) {
                    fRootProxy = proxy;
                    Job job = new Job( "Model Proxy installed notification job" ) {//$NON-NLS-1$
                        
                        /* (non-Javadoc)
                         * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
                         */
                        @Override
                        protected IStatus run( IProgressMonitor monitor ) {
                            if ( !monitor.isCanceled() ) {
                                proxy.init( getPresentationContext() );
                                proxy.addModelChangedListener( DocumentContentProvider.this );
                                proxy.installed( getViewer() );
                            }
                            return Status.OK_STATUS;
                        }
                    };
                    job.setSystem( true );
                    job.schedule();
                }
            }
        }
    }

    protected synchronized void installBaseProxy( Object element ) {
        if ( element != null && (!element.equals( getBase()) || fBaseProxy == null) ) {
            IModelProxyFactory modelProxyFactory = getModelProxyFactoryAdapter( element );
            if ( modelProxyFactory != null ) {
                final IModelProxy proxy = modelProxyFactory.createModelProxy( element, getPresentationContext() );
                if ( proxy != null ) {
                    fBaseProxy = proxy;
                    Job job = new Job( "Model Proxy installed notification job" ) {//$NON-NLS-1$
                        
                        /* (non-Javadoc)
                         * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
                         */
                        @Override
                        protected IStatus run( IProgressMonitor monitor ) {
                            if ( !monitor.isCanceled() ) {
                                proxy.init( getPresentationContext() );
                                proxy.addModelChangedListener( DocumentContentProvider.this );
                                proxy.installed( getViewer() );
                            }
                            return Status.OK_STATUS;
                        }
                    };
                    job.setSystem( true );
                    job.schedule();
                }
            }
        }
    }

    protected synchronized void installLineProxy( int index, Object element ) {
        IModelProxyFactory modelProxyFactory = getModelProxyFactoryAdapter( element );
        if ( modelProxyFactory != null ) {
            final IModelProxy proxy = modelProxyFactory.createModelProxy( element, getPresentationContext() );
            if ( proxy != null ) {
                fLineProxies.add( index, proxy );
                Job job = new Job( "Model Proxy installed notification job" ) {//$NON-NLS-1$
                    
                    /* (non-Javadoc)
                     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
                     */
                    @Override
                    protected IStatus run( IProgressMonitor monitor ) {
                        if ( !monitor.isCanceled() ) {
                            proxy.init( getPresentationContext() );
                            proxy.addModelChangedListener( DocumentContentProvider.this );
                            proxy.installed( getViewer() );
                        }
                        return Status.OK_STATUS;
                    }
                };
                job.setSystem( true );
                job.schedule();
            }
        }
    }

    protected IModelProxyFactory getModelProxyFactoryAdapter( Object element ) {
        IModelProxyFactory adapter = null;
        if ( element instanceof IModelProxyFactory ) {
            adapter = (IModelProxyFactory)element;
        }
        else if ( element instanceof IAdaptable ) {
            IAdaptable adaptable = (IAdaptable)element;
            adapter = (IModelProxyFactory)adaptable.getAdapter( IModelProxyFactory.class );
        }
        return adapter;
    }

    protected IPresentationContext getPresentationContext() {
        return getDocument().getPresentationContext();
    }

    protected synchronized void disposeRootProxy() {
        disposeBaseProxy();
        if ( fRootProxy != null ) {
            fRootProxy.dispose();
        }
        fRootProxy = null;
    }

    protected synchronized void disposeBaseProxy() {
        disposeLineProxies();
        if ( fBaseProxy != null ) {
            fBaseProxy.dispose();
        }
        fBaseProxy = null;
    }

    protected synchronized void disposeLineProxies() {
        for ( IModelProxy proxy : fLineProxies ) {
            proxy.dispose();
        }
        fLineProxies.clear();
    }

    protected VirtualSourceViewer getViewer() {
        return fViewer;
    }

    synchronized void schedule( DocumentUpdate update ) {
        if ( fUpdateInProgress != null ) {
            if ( update instanceof DocumentBaseChangeUpdate ) {
                // cancel the earlier update and start the latest
                fUpdateInProgress.cancel();
                fUpdateInProgress.done();
                fUpdateInProgress = update;
                fUpdateInProgress.start();
            }
            else if ( fUpdateInProgress instanceof DocumentBaseChangeUpdate 
                      && update instanceof DocumentContentUpdate ) {
                    // cancel the content update because the base change update 
                    // will start a new one
                    update.cancel();
                    update.done();
            }
            else if ( fUpdateInProgress instanceof DocumentContentUpdate 
                      && update instanceof DocumentBaseChangeUpdate ) {
                    // cancel the content update and start the base change update
                    fUpdateInProgress.cancel();
                    fUpdateInProgress.done();
                    fUpdateInProgress = update;
                    fUpdateInProgress.start();
            }
        }
        else {
            fUpdateInProgress = update;
            fUpdateInProgress.start();
        }
    }

    private int getElementIndex( Object element ) {
        Integer index = fLineElements.get( element );
        return ( index != null ) ? index.intValue() : -1;
    }

    protected Object getElementAtLine( int lineNumber ) {
        synchronized( fLineElements ) {
            for ( Object element : fLineElements.keySet() ) {
                if ( fLineElements.get( element ).intValue() == lineNumber ) {
                    return element;
                }
            }
        }
        return null;
    }
}
