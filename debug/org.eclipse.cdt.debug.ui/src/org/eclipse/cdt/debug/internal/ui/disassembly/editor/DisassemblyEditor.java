/*******************************************************************************
 * Copyright (c) 2008, 2009 ARM Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Limited - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.disassembly.editor;

import org.eclipse.cdt.debug.core.disassembly.IDisassemblyContextProvider;
import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.cdt.debug.internal.ui.actions.breakpoints.CBreakpointPropertiesRulerAction;
import org.eclipse.cdt.debug.internal.ui.actions.breakpoints.EnableDisableBreakpointRulerAction;
import org.eclipse.cdt.debug.internal.ui.disassembly.viewer.DisassemblyDocumentProvider;
import org.eclipse.cdt.debug.internal.ui.disassembly.viewer.DisassemblyPane;
import org.eclipse.cdt.debug.internal.ui.disassembly.viewer.DocumentContentProvider;
import org.eclipse.cdt.debug.internal.ui.disassembly.viewer.VirtualDocument;
import org.eclipse.cdt.debug.internal.ui.disassembly.viewer.VirtualSourceViewer;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.ToggleBreakpointAction;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class DisassemblyEditor extends EditorPart implements ITextEditor, IReusableEditor, IDebugContextListener, IPropertyChangeListener {

    private DisassemblyPane fDisassemblyPane;

    public DisassemblyEditor() {
        super();
        fDisassemblyPane = new DisassemblyPane( "#DisassemblyEditorContext", "#DisassemblyEditorRulerContext" ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void doSave( IProgressMonitor monitor ) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#doSaveAs()
     */
    @Override
    public void doSaveAs() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    @SuppressWarnings("restriction")
    @Override
    public void init( IEditorSite site, IEditorInput input ) throws PartInitException {
        setSite( site );
        setInput( input );
        ((DisassemblyDocumentProvider)getDocumentProvider()).
                               getDocumentPresentation( input ).
                                   addPropertyChangeListener( this );
        DebugUITools.getDebugContextManager().addDebugContextListener( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#isDirty()
     */
    @Override
    public boolean isDirty() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
     */
    @Override
    public boolean isSaveAsAllowed() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl( Composite parent ) {
        fDisassemblyPane.create( parent );
        createActions();
        
        // register the context menu such that other plugins may contribute to it
        if ( getSite() != null ) {
            getSite().registerContextMenu( fDisassemblyPane.getViewContextMenuId(), fDisassemblyPane.getTextMenuManager(), getViewer() );
        }
        
        if ( getSite() != null ) {
            getSite().registerContextMenu( fDisassemblyPane.getRulerContextMenuId(), fDisassemblyPane.getTextMenuManager(), getViewer() );
        }
        
        VirtualSourceViewer viewer = fDisassemblyPane.getViewer();
        IEditorInput input = getEditorInput();
        if ( input instanceof DisassemblyEditorInput ) {       
            Object debugContext = ((DisassemblyEditorInput)input).getDebugContext();
            VirtualDocument document = (VirtualDocument)getDocumentProvider().getDocument( input );
            IAnnotationModel annotationModel = getDocumentProvider().getAnnotationModel( input );
            viewer.setDocument( document, annotationModel );
            ((VirtualDocument)viewer.getDocument()).getContentProvider().changeInput( viewer, document.getPresentationContext(), null, debugContext, document.getCurrentOffset() );
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @SuppressWarnings("restriction")
    @Override
    public void dispose() {
        DebugUITools.getDebugContextManager().removeDebugContextListener( this );
        ((DisassemblyDocumentProvider)getDocumentProvider()).
                    getDocumentPresentation( getEditorInput() ).
                        removePropertyChangeListener( this );
        getDocumentProvider().disconnect( getEditorInput() );
        fDisassemblyPane.dispose();
        super.dispose();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#setInput(org.eclipse.ui.IEditorInput)
     */
    @Override
    public final void setInput( IEditorInput input ) {
        super.setInput( input );
        Object debugContext = ((DisassemblyEditorInput)input).getDebugContext();
        try {
            getDocumentProvider().connect( input );
        }
        catch( CoreException e ) {
            // shouldn't happen
        }
        VirtualDocument document = (VirtualDocument)getDocumentProvider().getDocument( input );
        VirtualSourceViewer viewer = getViewer();
        if ( document != null && viewer != null ) {
            DocumentContentProvider contentProvider = document.getContentProvider();
            Object oldInput = contentProvider.getInput();
            contentProvider.changeInput( getViewer(), document.getPresentationContext(), oldInput, debugContext, document.getCurrentOffset() );
//          getViewer().refresh( false, true );
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.contexts.IDebugContextListener#debugContextChanged(org.eclipse.debug.ui.contexts.DebugContextEvent)
     */
    @Override
	public void debugContextChanged( DebugContextEvent event ) {
        ISelection selection = event.getContext();
        if ( selection instanceof IStructuredSelection ) {
            IStructuredSelection ss = (IStructuredSelection)selection;
            Object context = ss.getFirstElement();
            if ( context != null ) {
                IDisassemblyContextProvider contextProvider = getDisassemblyContextProvider( context );
                if ( contextProvider != null ) {
                    Object disassemblyContext = contextProvider.getDisassemblyContext( context );
                    if ( disassemblyContext != null ) {
                        DisassemblyEditorInput oldInput = (DisassemblyEditorInput)getEditorInput();
                        if ( oldInput.getDisassemblyContext().equals( disassemblyContext ) ) {
                            setInput( new DisassemblyEditorInput( context, disassemblyContext ) );
                        }
                    }
                }
            }
        }
    }

    private IDisassemblyContextProvider getDisassemblyContextProvider( Object element ) {
        IDisassemblyContextProvider adapter = null;
        if ( element instanceof IDisassemblyContextProvider ) {
            adapter = (IDisassemblyContextProvider)element;
        }
        else if ( element instanceof IAdaptable ) {
            IAdaptable adaptable = (IAdaptable)element;
            adapter = (IDisassemblyContextProvider)adaptable.getAdapter( IDisassemblyContextProvider.class );
        }
        return adapter;
    }

    private VirtualSourceViewer getViewer() {
        return fDisassemblyPane.getViewer();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.ITextEditor#close(boolean)
     */
    @Override
	public void close( boolean save ) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.ITextEditor#doRevertToSaved()
     */
    @Override
	public void doRevertToSaved() {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.ITextEditor#getAction(java.lang.String)
     */
    @Override
	public IAction getAction( String actionId ) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.ITextEditor#getDocumentProvider()
     */
    @Override
	public IDocumentProvider getDocumentProvider() {
        return CDebugUIPlugin.getDefault().getDisassemblyEditorManager().getDocumentProvider();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.ITextEditor#getHighlightRange()
     */
    @Override
	public IRegion getHighlightRange() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.ITextEditor#getSelectionProvider()
     */
    @Override
	public ISelectionProvider getSelectionProvider() {
        VirtualSourceViewer viewer = getViewer();
        return ( viewer != null ) ? viewer.getSelectionProvider() : null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.ITextEditor#isEditable()
     */
    @Override
	public boolean isEditable() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.ITextEditor#removeActionActivationCode(java.lang.String)
     */
    @Override
	public void removeActionActivationCode( String actionId ) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.ITextEditor#resetHighlightRange()
     */
    @Override
	public void resetHighlightRange() {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.ITextEditor#selectAndReveal(int, int)
     */
    @Override
	public void selectAndReveal( int offset, int length ) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.ITextEditor#setAction(java.lang.String, org.eclipse.jface.action.IAction)
     */
    @Override
	public void setAction( String actionID, IAction action ) {
        fDisassemblyPane.setAction( actionID, action );
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.ITextEditor#setActionActivationCode(java.lang.String, char, int, int)
     */
    @Override
	public void setActionActivationCode( String actionId, char activationCharacter, int activationKeyCode, int activationStateMask ) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.ITextEditor#setHighlightRange(int, int, boolean)
     */
    @Override
	public void setHighlightRange( int offset, int length, boolean moveCursor ) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.ITextEditor#showHighlightRangeOnly(boolean)
     */
    @Override
	public void showHighlightRangeOnly( boolean showHighlightRangeOnly ) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.ITextEditor#showsHighlightRangeOnly()
     */
    @Override
	public boolean showsHighlightRangeOnly() {
        // TODO Auto-generated method stub
        return false;
    }

    protected void createActions() {
        IVerticalRuler ruler = fDisassemblyPane.getVerticalRuler();
        IAction action= new ToggleBreakpointAction( this, null, ruler );
        setAction( IInternalCDebugUIConstants.ACTION_TOGGLE_BREAKPOINT, action );
        action= new EnableDisableBreakpointRulerAction( this, ruler );
        setAction( IInternalCDebugUIConstants.ACTION_ENABLE_DISABLE_BREAKPOINT, action );
        action= new CBreakpointPropertiesRulerAction( this, ruler );
        setAction( IInternalCDebugUIConstants.ACTION_BREAKPOINT_PROPERTIES, action );
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    @Override
	public void propertyChange( PropertyChangeEvent event ) {
        getViewer().refresh();
    }
    
    @Override
    public Object getAdapter(Class adapter) {
        if (IDocument.class.equals(adapter)) {
            return getDocumentProvider().getDocument(getEditorInput());
        }
        return super.getAdapter(adapter);
    }
}
