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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

public class DisassemblyPane implements IPropertyChangeListener {

    private final static int VERTICAL_RULER_WIDTH = 12;
    private final static String CURRENT_LINE = AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE;
    private final static String CURRENT_LINE_COLOR = AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR;

    private Composite fControl;
    private VirtualSourceViewer fViewer;

    private IVerticalRuler fVerticalRuler;
    private IOverviewRuler fOverviewRuler;

    private SourceViewerDecorationSupport fSourceViewerDecorationSupport;
    private IAnnotationAccess fAnnotationAccess;
    private MarkerAnnotationPreferences fAnnotationPreferences;

    private String fViewContextMenuId;
    private String fRulerContextMenuId;

    private MenuManager fTextMenuManager;
    private MenuManager fRulerMenuManager;

    private Menu fRulerContextMenu;
    private Menu fTextContextMenu;

    private IMenuListener fMenuListener;
    private MouseListener fMouseListener;

    private Map<String, IAction> fActions = new HashMap<String, IAction>( 10 );

    public DisassemblyPane( String contextMenuId, String rulerMenuId ) {
        fAnnotationPreferences = new MarkerAnnotationPreferences();
        setViewContextMenuId( contextMenuId );
        setRulerContextMenuId( rulerMenuId );
    }

    public void create( Composite parent ) {
        Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayout( new GridLayout() );
        GridData data = new GridData( SWT.FILL, SWT.FILL, true, true );
        composite.setLayoutData( data );

        fVerticalRuler = createCompositeRuler();
        fOverviewRuler = createOverviewRuler( getSharedColors() );

        createActions();

        fViewer = createViewer( composite, fVerticalRuler, fOverviewRuler );
        
        fControl = composite;

        createViewContextMenu();
        createRulerContextMenu();
        
        if ( fSourceViewerDecorationSupport != null ) {
            fSourceViewerDecorationSupport.install( getEditorPreferenceStore() );
        }
    }

    public Control getControl() {
        return fControl;
    }

    public VirtualSourceViewer getViewer() {
        return fViewer;
    }

    public void dispose() {
        getEditorPreferenceStore().removePropertyChangeListener( this );
        JFaceResources.getFontRegistry().removeListener( this );
        JFaceResources.getColorRegistry().removeListener( this );
        if ( fSourceViewerDecorationSupport != null ) {
            fSourceViewerDecorationSupport.dispose();
            fSourceViewerDecorationSupport = null;
        }
        if ( fActions != null ) {
            fActions.clear();
            fActions = null;
        }
    }

    protected void createActions() {
    }

    public void setAction( String actionID, IAction action ) {
        Assert.isNotNull( actionID );
        if ( action == null ) {
            fActions.remove( actionID );
        }
        else {
            fActions.put( actionID, action );
        }
    }

    public IAction getAction( String actionID ) {
        Assert.isNotNull( actionID );
        return fActions.get( actionID );
    }

    protected void rulerContextMenuAboutToShow( IMenuManager menu ) {
        menu.add( new Separator( ITextEditorActionConstants.GROUP_REST ) );
        menu.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
        addAction( menu, IInternalCDebugUIConstants.ACTION_TOGGLE_BREAKPOINT );
        addAction( menu, IInternalCDebugUIConstants.ACTION_ENABLE_DISABLE_BREAKPOINT );
        addAction( menu, IInternalCDebugUIConstants.ACTION_BREAKPOINT_PROPERTIES );
    }

    protected void viewContextMenuAboutToShow( IMenuManager menu ) {
        menu.add( new Separator( ITextEditorActionConstants.GROUP_REST ) );
        menu.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
    }

    protected void addAction( IMenuManager menu, String group, String actionId ) {
        IAction action = getAction( actionId );
        if ( action != null ) {
            if ( action instanceof IUpdate )
                ((IUpdate)action).update();
            IMenuManager subMenu = menu.findMenuUsingPath( group );
            if ( subMenu != null )
                subMenu.add( action );
            else
                menu.appendToGroup( group, action );
        }
    }

    protected void addAction( IMenuManager menu, String actionId ) {
        IAction action = getAction( actionId );
        if ( action != null ) {
            if ( action instanceof IUpdate )
                ((IUpdate)action).update();
            menu.add( action );
        }
    }

    protected final IMenuListener getContextMenuListener() {
        if ( fMenuListener == null ) {
            fMenuListener = new IMenuListener() {

                @Override
				public void menuAboutToShow( IMenuManager menu ) {
                    String id = menu.getId();
                    if ( getRulerContextMenuId().equals( id ) ) {
//                        setFocus();
                        rulerContextMenuAboutToShow( menu );
                    }
                    else if ( getViewContextMenuId().equals( id ) ) {
//                        setFocus();
                        viewContextMenuAboutToShow( menu );
                    }
                }
            };
        }
        return fMenuListener;
    }

    protected final MouseListener getRulerMouseListener() {
        if ( fMouseListener == null ) {
            fMouseListener = new MouseListener() {

                private boolean fDoubleClicked = false;

                private void triggerAction( String actionID ) {
                    IAction action = getAction( actionID );
                    if ( action != null ) {
                        if ( action instanceof IUpdate )
                            ((IUpdate)action).update();
                        if ( action.isEnabled() )
                            action.run();
                    }
                }

                @Override
				public void mouseUp( MouseEvent e ) {
//                    setFocus();
                    if ( 1 == e.button && !fDoubleClicked )
                        triggerAction( ITextEditorActionConstants.RULER_CLICK );
                    fDoubleClicked = false;
                }

                @Override
				public void mouseDoubleClick( MouseEvent e ) {
                    if ( 1 == e.button ) {
                        fDoubleClicked = true;
                        triggerAction( IInternalCDebugUIConstants.ACTION_TOGGLE_BREAKPOINT );
                    }
                }

                @Override
				public void mouseDown( MouseEvent e ) {
                    StyledText text = getViewer().getTextWidget();
                    if ( text != null && !text.isDisposed() ) {
                        Display display = text.getDisplay();
                        Point location = display.getCursorLocation();
                        getRulerContextMenu().setLocation( location.x, location.y );
                    }
                }
            };
        }
        return fMouseListener;
    }

    private Menu getTextContextMenu() {
        return this.fTextContextMenu;
    }

    private void setTextContextMenu( Menu textContextMenu ) {
        this.fTextContextMenu = textContextMenu;
    }

    protected Menu getRulerContextMenu() {
        return this.fRulerContextMenu;
    }

    private void setRulerContextMenu( Menu rulerContextMenu ) {
        this.fRulerContextMenu = rulerContextMenu;
    }

    public String getRulerContextMenuId() {
        return fRulerContextMenuId;
    }

    private void setRulerContextMenuId( String rulerContextMenuId ) {
        Assert.isNotNull( rulerContextMenuId );
        fRulerContextMenuId = rulerContextMenuId;
    }

    public String getViewContextMenuId() {
        return fViewContextMenuId;
    }

    private void setViewContextMenuId( String viewContextMenuId ) {
        Assert.isNotNull( viewContextMenuId );
        fViewContextMenuId = viewContextMenuId;
    }

    private void createViewContextMenu() {
        String id = getViewContextMenuId();
        fTextMenuManager = new MenuManager( id, id );
        fTextMenuManager.setRemoveAllWhenShown( true );
        fTextMenuManager.addMenuListener( getContextMenuListener() );
        StyledText styledText = getViewer().getTextWidget();
        setTextContextMenu( fTextMenuManager.createContextMenu( styledText ) );
        styledText.setMenu( getTextContextMenu() );
    }

    private void createRulerContextMenu() {
        String id = getRulerContextMenuId();
        fRulerMenuManager = new MenuManager( id, id );
        fRulerMenuManager.setRemoveAllWhenShown( true );
        fRulerMenuManager.addMenuListener( getContextMenuListener() );
        Control rulerControl = fVerticalRuler.getControl();
        setRulerContextMenu( fRulerMenuManager.createContextMenu( rulerControl ) );
        rulerControl.setMenu( getRulerContextMenu() );
        rulerControl.addMouseListener( getRulerMouseListener() );
    }

    protected SourceViewerDecorationSupport getSourceViewerDecorationSupport( ISourceViewer viewer ) {
        if ( fSourceViewerDecorationSupport == null ) {
            fSourceViewerDecorationSupport = new SourceViewerDecorationSupport( viewer, getOverviewRuler(), getAnnotationAccess(), getSharedColors() );
            configureSourceViewerDecorationSupport( fSourceViewerDecorationSupport );
        }
        return fSourceViewerDecorationSupport;
    }

    protected VirtualSourceViewer createViewer( Composite parent, IVerticalRuler vertRuler, IOverviewRuler ovRuler ) {
        int styles = SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION;
        VirtualSourceViewer viewer = new VirtualSourceViewer( parent, fVerticalRuler, fOverviewRuler, true, styles );
        viewer.getControl().setLayoutData( parent.getLayoutData() );
        viewer.setEditable( false );
        viewer.getTextWidget().setFont( JFaceResources.getFont( IInternalCDebugUIConstants.DISASSEMBLY_FONT ) );
        viewer.setRangeIndicator( new DefaultRangeIndicator() );
        getSourceViewerDecorationSupport( viewer );
        viewer.configure( new SourceViewerConfiguration() );
        JFaceResources.getFontRegistry().addListener( this );
        JFaceResources.getColorRegistry().addListener( this );
        getEditorPreferenceStore().addPropertyChangeListener( this );
        return viewer;
    }

    private IAnnotationAccess createAnnotationAccess() {
        return new DefaultMarkerAnnotationAccess();
    }

    private void configureSourceViewerDecorationSupport( SourceViewerDecorationSupport support ) {
        for( Object pref : fAnnotationPreferences.getAnnotationPreferences() ) {
            support.setAnnotationPreference( (AnnotationPreference)pref );
        }
        support.setCursorLinePainterPreferenceKeys( CURRENT_LINE, CURRENT_LINE_COLOR );
    }

    private IAnnotationAccess getAnnotationAccess() {
        if ( fAnnotationAccess == null )
            fAnnotationAccess = createAnnotationAccess();
        return fAnnotationAccess;
    }

    private ISharedTextColors getSharedColors() {
        return EditorsUI.getSharedTextColors();
    }

    @SuppressWarnings("unchecked")
    protected IVerticalRuler createCompositeRuler() {
        CompositeRuler ruler = new CompositeRuler();
        ruler.addDecorator( 0, new AnnotationRulerColumn( VERTICAL_RULER_WIDTH, getAnnotationAccess() ) );
        for( Iterator iter = ruler.getDecoratorIterator(); iter.hasNext(); ) {
            IVerticalRulerColumn col = (IVerticalRulerColumn)iter.next();
            if ( col instanceof AnnotationRulerColumn ) {
                AnnotationRulerColumn column = (AnnotationRulerColumn)col;
                for( Iterator iter2 = fAnnotationPreferences.getAnnotationPreferences().iterator(); iter2.hasNext(); ) {
                    AnnotationPreference preference = (AnnotationPreference)iter2.next();
                    column.addAnnotationType( preference.getAnnotationType() );
                }
                column.addAnnotationType( Annotation.TYPE_UNKNOWN );
                break;
            }
        }
        return ruler;
    }

    private IOverviewRuler createOverviewRuler( ISharedTextColors sharedColors ) {
        IOverviewRuler ruler = new OverviewRuler( getAnnotationAccess(), VERTICAL_RULER_WIDTH, sharedColors );
        for( Object o : fAnnotationPreferences.getAnnotationPreferences() ) {
            AnnotationPreference preference = (AnnotationPreference)o;
            if ( preference.contributesToHeader() )
                ruler.addHeaderAnnotationType( preference.getAnnotationType() );
        }
        return ruler;
    }

    private IOverviewRuler getOverviewRuler() {
        if ( fOverviewRuler == null )
            fOverviewRuler = createOverviewRuler( getSharedColors() );
        return fOverviewRuler;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    @Override
	public void propertyChange( PropertyChangeEvent event ) {
        // TODO Auto-generated method stub
        
    }

    private IPreferenceStore getEditorPreferenceStore() {
        return EditorsUI.getPreferenceStore();
    }

    public MenuManager getTextMenuManager() {
        return fTextMenuManager;
    }

    public MenuManager getRulerMenuManager() {
        return fRulerMenuManager;
    }

    public IVerticalRuler getVerticalRuler() {
        return fVerticalRuler;
    }
}
