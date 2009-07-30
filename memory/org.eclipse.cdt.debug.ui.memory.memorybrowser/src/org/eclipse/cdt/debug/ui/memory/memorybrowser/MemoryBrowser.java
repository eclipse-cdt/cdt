/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.memorybrowser;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.memory.MemoryRenderingManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.debug.ui.memory.IMemoryRenderingSynchronizationService;
import org.eclipse.debug.ui.memory.IMemoryRenderingType;
import org.eclipse.debug.ui.memory.IRepositionableMemoryRendering;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * A lightweight rendering container.
 * 
 * Debug model requirements:
 * 		IMemoryBlockExtension (IMemoryBlock not supported)
 * 		IMemoryBlockRetrievalExtension
 * <p>
 * Rendering requirements:
 * 		IRepositionableMemoryRendering
 * 
 */

public class MemoryBrowser extends ViewPart implements IDebugContextListener, ILaunchListener, IMemoryRenderingSite
{
	public static final String ID = "org.eclipse.cdt.debug.ui.memory.memorybrowser.MemoryBrowser";  //$NON-NLS-1$
	
	protected StackLayout fStackLayout;
	private Composite fRenderingsComposite;
	private HashMap<Object,CTabFolder> fContextFolders = new HashMap<Object,CTabFolder> ();
	private GoToAddressBarWidget fGotoAddressBar;
	private Control fGotoAddressBarControl;
	private Label fUnsupportedLabel;
	private Composite fMainComposite;
	private String defaultRenderingTypeId = null;
	
	private ArrayList<IMemoryRenderingContainer> fCurrentContainers = new ArrayList<IMemoryRenderingContainer>();
	
	private final static String KEY_RENDERING = "RENDERING"; //$NON-NLS-1$
	private final static String KEY_CONTEXT   = "CONTEXT";   //$NON-NLS-1$
	private final static String KEY_RETRIEVAL = "RETRIEVAL"; //$NON-NLS-1$
	private final static String KEY_CONTAINER = "CONTAINER"; //$NON-NLS-1$

	public MemoryBrowser() {
	}
	
	private Control getControl()
	{
		return fMainComposite;
	}

	public void createPartControl(Composite parent) {
		// set default rendering type. use the traditional rendering if available. fallback on first registered type.
		// this should eventually be configurable via a preference page.
		boolean isTraditionalRenderingAvailable = false;
		final String traditionalRenderingId = "org.eclipse.cdt.debug.ui.memory.traditional.TraditionalRendering"; //$NON-NLS-1$
		IMemoryRenderingType[] types = getRenderingTypes();
		for(final IMemoryRenderingType type : types)
		{
			if(type.getId().equals(traditionalRenderingId))
			{
				isTraditionalRenderingAvailable = true;
				break;
			}
		}
		if(isTraditionalRenderingAvailable)
			defaultRenderingTypeId = traditionalRenderingId;
		else if(types.length > 0)
			defaultRenderingTypeId = types[0].getId();
		
		getSite().setSelectionProvider(new SelectionProviderAdapter());
		
		fMainComposite = new Composite(parent, SWT.NONE);
		
		FormLayout layout = new FormLayout();
		layout.spacing = 0;
		fMainComposite.setLayout(layout);
		
		fGotoAddressBar = new GoToAddressBarWidget();
		fGotoAddressBarControl = fGotoAddressBar.createControl(fMainComposite);
		
		fGotoAddressBar.getButton(IDialogConstants.OK_ID).addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				performGo(false);
			}
		});
		
		fGotoAddressBar.getButton(GoToAddressBarWidget.ID_GO_NEW_TAB).addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				performGo(true);
			}
		});
		
		fGotoAddressBar.getExpressionWidget().addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {
				if(e.keyCode == SWT.CR)
					performGo(false);
			}
		});
		
		FormData data = new FormData();
		data.top = new FormAttachment(0);
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		fGotoAddressBarControl.setLayoutData(data);
		
		fRenderingsComposite = new Composite(fMainComposite, SWT.NONE);
		data = new FormData();
		data.top = new FormAttachment(fGotoAddressBarControl);
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.bottom = new FormAttachment(100);
		fRenderingsComposite.setLayoutData(data);
		
		fStackLayout = new StackLayout();
	
		fRenderingsComposite.setLayout(fStackLayout);
		
		fUnsupportedLabel = new Label(fRenderingsComposite, SWT.NONE);
		fUnsupportedLabel.setText("");  //$NON-NLS-1$
		
		handleUnsupportedSelection();

		PlatformUI.getWorkbench().getHelpSystem().setHelp(fMainComposite, MemoryBrowserPlugin.PLUGIN_ID);
		makeActions();
		hookContextMenu();
		contributeToActionBars();
		
		Object selection = null;
        IDebugContextService contextService = 
            DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()); 
		if (isBug145635Patched()) {
		    String presentationContextId = getPresentationContextId();
            contextService.addDebugContextListener(this, presentationContextId); 
            selection = contextService.getActiveContext(presentationContextId);
		} else {
		    contextService.addDebugContextListener(this); 
            selection = contextService.getActiveContext();
		}
		
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
		
		if(selection instanceof StructuredSelection)
			handleDebugContextChanged(((StructuredSelection) selection).getFirstElement());
	}

    private boolean isBug145635Patched() {
        Type[] managerTypes = DebugUITools.getDebugContextManager().getClass().getGenericInterfaces();
        for (int i = 0; i < managerTypes.length; i++) {
            if (managerTypes[i] instanceof Class) {
                Class clazz = (Class)managerTypes[i];
                if ("org.eclipse.debug.ui.contexts.IBug145635Marker".equals(clazz.getName()) ) {
                    return true;
                }
            }
        }
        return false;
    }
	
	/**
     * Returns the presentation context id for this view.  Used to support the 
     * pin and clone feature patch from bug 145635. 
     * 
     * @return context id
     */
    private String getPresentationContextId() {
        IViewSite site = (IViewSite)getSite(); 
        return site.getId() + (site.getSecondaryId() != null ? (":" + site.getSecondaryId()) : "");
    }
	
	public void dispose() {
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
        IDebugContextService contextService = 
            DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()); 
        if (isBug145635Patched()) {
            String presentationContextId = getPresentationContextId();
            contextService.removeDebugContextListener(this, presentationContextId); 
        } else {
            contextService.removeDebugContextListener(this); 
        }
		super.dispose();
	}
	
	public void launchAdded(ILaunch launch) {}
	public void launchChanged(ILaunch launch) {}

	public void launchRemoved(ILaunch launch) {
		IMemoryBlockRetrieval retrieval = ((IMemoryBlockRetrieval) launch.getAdapter(IMemoryBlockRetrieval.class));
		if(retrieval != null)
			releaseTabFolder(retrieval);
	}
	
	public IMemoryRenderingContainer getContainer(String id) {
		return null;
	}

	public IMemoryRenderingContainer[] getMemoryRenderingContainers() {
		IMemoryRenderingContainer[] containerList = new IMemoryRenderingContainer[fCurrentContainers.size()];
		for ( int idx = 0 ; idx < fCurrentContainers.size() ; idx ++ ) {
			containerList[ idx ] = fCurrentContainers.get( idx );
		}
		return containerList;
	}
	
	public IMemoryRenderingSynchronizationService getSynchronizationService() {
		return null;
	}
	
	private void handleUnsupportedSelection()
	{
		fStackLayout.topControl = fUnsupportedLabel;
		fGotoAddressBarControl.setVisible(false);
	}
	
	private void performGo(boolean inNewTab)
	{
		final CTabFolder activeFolder = (CTabFolder) fStackLayout.topControl;
		if(activeFolder != null)
		{	
			final IMemoryBlockRetrieval retrieval = (IMemoryBlockRetrieval) activeFolder.getData(KEY_RETRIEVAL);
			final Object context = activeFolder.getData(KEY_CONTEXT);
			
			if(inNewTab || activeFolder.getSelection() == null)
			{
				CTabItem item = createTab(activeFolder, activeFolder.getSelectionIndex() + 1);
				populateTabWithRendering(item, retrieval, context);
				setTabFolder(retrieval, activeFolder);
				activeFolder.setSelection(item);
				getSite().getSelectionProvider().setSelection(new StructuredSelection(item.getData(KEY_RENDERING)));
			}
			
			final IRepositionableMemoryRendering rendering = (IRepositionableMemoryRendering) activeFolder.getSelection().getData(KEY_RENDERING);
			final String expression = fGotoAddressBar.getExpressionText();
			
			if(retrieval instanceof IMemoryBlockRetrievalExtension)
			{
				new Thread()
				{
					public void run()
					{
						IMemoryBlockRetrievalExtension retrievalExtension = (IMemoryBlockRetrievalExtension) retrieval;
						try {
							IMemoryBlockExtension newBlock = retrievalExtension.getExtendedMemoryBlock(expression, context);
							BigInteger newBase = newBlock.getBigBaseAddress();
							if(((IMemoryBlockExtension) rendering.getMemoryBlock()).supportBaseAddressModification())
								((IMemoryBlockExtension) rendering.getMemoryBlock()).setBaseAddress(newBase);
							rendering.goToAddress(newBase);
							Display.getDefault().asyncExec(new Runnable(){
								public void run()
								{
									updateLabel(activeFolder.getSelection(), rendering);
								}
							});
						} catch (DebugException e1) {
							MemoryBrowserPlugin.getDefault().getLog().log(new Status(Status.ERROR, MemoryBrowserPlugin.PLUGIN_ID, "", e1)); //$NON-NLS-1$
						}
					}
				}.start();
			}
		}
	}
	
	private void updateLabel(CTabItem tab, IMemoryRendering rendering)
	{
		String label = null;
		// would like to avoid using reflection
		try {
			Method m = rendering.getControl().getClass().getMethod("getViewportStartAddress", new Class[0]); //$NON-NLS-1$
			if(m != null)
        		label = "0x" + ((BigInteger) m.invoke(rendering.getControl(), new Object[0])).toString(16).toUpperCase(); //$NON-NLS-1$
		} 
		catch (Exception e) 
		{
		}
		
		if(label == null)
			label = rendering.getLabel();
			
		tab.setText(label);
	}
	
	private CTabFolder createTabFolder(Composite parent)
	{
		CTabFolder folder = new CTabFolder(parent, SWT.NO_REDRAW_RESIZE | SWT.NO_TRIM | SWT.FLAT);
		
		ColorRegistry reg = JFaceResources.getColorRegistry();
		Color c1 = reg.get("org.eclipse.ui.workbench.ACTIVE_TAB_BG_START"), //$NON-NLS-1$
			  c2 = reg.get("org.eclipse.ui.workbench.ACTIVE_TAB_BG_END"); //$NON-NLS-1$
		folder.setSelectionBackground(new Color[] {c1, c2},	new int[] {100}, true);
		folder.setSelectionForeground(reg.get("org.eclipse.ui.workbench.ACTIVE_TAB_TEXT_COLOR")); //$NON-NLS-1$
		folder.setSimple(PlatformUI.getPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));
		folder.setBorderVisible(true);
		
		folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				event.doit = false;
				CTabItem item = (CTabItem) event.item;
				IMemoryRenderingContainer container = (IMemoryRenderingContainer) item.getData(KEY_CONTAINER);
				fCurrentContainers.remove( container );
			}
		});
		return folder;
	}
	
	private CTabItem createTab(CTabFolder tabFolder, int index) {
		int swtStyle = SWT.CLOSE;
		CTabItem tab = new CTabItem(tabFolder, swtStyle, index);
		return tab;
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				MemoryBrowser.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(getControl());
		getControl().setMenu(menu);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		
		
		MenuManager sub = new MenuManager(Messages.getString("MemoryBrowser.DefaultRendering")); //$NON-NLS-1$
		
		for(final IMemoryRenderingType type : getRenderingTypes())
		{
			final Action action = new Action(
				type.getLabel(), IAction.AS_RADIO_BUTTON)
	        {
	            public void run()
	            {
	            	setDefaultRenderingTypeId(type.getId());
	            }
	        };
	        action.setChecked(type.getId().equals(getDefaultRenderingTypeId()));
	        sub.add(action);
		}
		
	    manager.add(sub);
	    
	    manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void makeActions() {

	}
	
	private IMemoryRenderingType[] getRenderingTypes()
	{
		 return MemoryRenderingManager.getDefault().getRenderingTypes(new IMemoryBlockExtension(){
			public void connect(Object client) {}
			public void disconnect(Object client) {}
			public void dispose() throws DebugException {}
			public int getAddressSize() throws DebugException { return 0; }
			public int getAddressableSize() throws DebugException { return 0; }
			public BigInteger getBigBaseAddress() throws DebugException { return null; }
			public BigInteger getBigLength() throws DebugException { return null; }
			public MemoryByte[] getBytesFromAddress(BigInteger address, long units) throws DebugException { return null; }
			public MemoryByte[] getBytesFromOffset(BigInteger unitOffset, long addressableUnits) throws DebugException { return null; }
			public Object[] getConnections() { return null; }
			public String getExpression() { return null; }
			public BigInteger getMemoryBlockEndAddress() throws DebugException { return null; }
			public IMemoryBlockRetrieval getMemoryBlockRetrieval() { return null; }
			public BigInteger getMemoryBlockStartAddress() throws DebugException { return null; }
			public void setBaseAddress(BigInteger address) throws DebugException {}
			public void setValue(BigInteger offset, byte[] bytes) throws DebugException {}
			public boolean supportBaseAddressModification() throws DebugException { return false; }
			public boolean supportsChangeManagement() { return false; }
			public byte[] getBytes() throws DebugException { return null; }
			public long getLength() { return 0; }
			public long getStartAddress() { return 0; }
			public void setValue(long offset, byte[] bytes) throws DebugException {}
			public boolean supportsValueModification() { return false; }
			public IDebugTarget getDebugTarget() { return null; }
			public ILaunch getLaunch() { return null; }
			public String getModelIdentifier() { return null; }
			public Object getAdapter(Class adapter) { return null; }
		});
	}

	public void setFocus() {
		getControl().setFocus();
	}
	
	public void debugContextChanged(DebugContextEvent event) {
		handleDebugContextChanged(((StructuredSelection) event.getContext()).getFirstElement());
	}
	
	public void handleDebugContextChanged(Object context) {
		if(defaultRenderingTypeId == null)
			return;
	
		if(context instanceof IAdaptable)
		{
			IMemoryBlockRetrieval retrieval = ((IMemoryBlockRetrieval) ((IAdaptable) context).getAdapter(IMemoryBlockRetrieval.class));
			if(retrieval != null)
			{
				fGotoAddressBarControl.setVisible(true);
				if(getTabFolder(retrieval) != null)
				{
					fStackLayout.topControl = getTabFolder(retrieval);
				}
				else
				{
					CTabFolder newFolder = this.createTabFolder(fRenderingsComposite);
					newFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
						public void close(CTabFolderEvent event) {
							event.doit = true;
							CTabItem item = (CTabItem) event.item;
							IMemoryRenderingContainer container = (IMemoryRenderingContainer) item.getData(KEY_CONTAINER);
							fCurrentContainers.remove( container );
						}
					});
					newFolder.addSelectionListener(new SelectionListener()
					{
						public void widgetDefaultSelected(SelectionEvent e) {}

						public void widgetSelected(SelectionEvent e) {
							getSite().getSelectionProvider().setSelection(new StructuredSelection(((CTabItem) e.item).getData(KEY_RENDERING)));
						}
					});
					
					newFolder.setData(KEY_CONTEXT, context);
					newFolder.setData(KEY_RETRIEVAL, retrieval);
					
					CTabItem item = createTab(newFolder, 0);
					populateTabWithRendering(item, retrieval, context);
					setTabFolder(retrieval, newFolder);
					
					fStackLayout.topControl = getTabFolder(retrieval);
				}
			}
			else
			{
				handleUnsupportedSelection();
			}
			fStackLayout.topControl.getParent().layout(true);
		}
	}
	
	private String getDefaultRenderingTypeId()
	{
		return defaultRenderingTypeId;
	}
	
	private void setDefaultRenderingTypeId(String id)
	{
		defaultRenderingTypeId = id;
	}
	
	private void populateTabWithRendering(final CTabItem tab, final IMemoryBlockRetrieval retrieval, Object context)
	{
		IMemoryRenderingType type = DebugUITools.getMemoryRenderingManager().getRenderingType(getDefaultRenderingTypeId());
		try {
			final IMemoryRendering rendering = type.createRendering();

			IMemoryRenderingContainer container = new IMemoryRenderingContainer()
			{
				public void addMemoryRendering(IMemoryRendering rendering) {}

				public IMemoryRendering getActiveRendering() {
					return rendering;
				}

				public String getId() {
					return "???"; //$NON-NLS-1$
				}

				public String getLabel() {
					return rendering.getLabel();
				}

				public IMemoryRenderingSite getMemoryRenderingSite() {
					return MemoryBrowser.this;
				}

				public IMemoryRendering[] getRenderings() {
					return new IMemoryRendering[] { rendering };
				}

				public void removeMemoryRendering(IMemoryRendering rendering) {}
				
			};
			
			IMemoryBlock block = retrieval.getMemoryBlock(0, 1024) ;
			if(retrieval instanceof IAdaptable)
			{
				IMemoryBlockRetrievalExtension retrievalExtension = (IMemoryBlockRetrievalExtension) 
					((IAdaptable) retrieval).getAdapter(IMemoryBlockRetrievalExtension.class);
				if(retrievalExtension != null)
					block = retrievalExtension.getExtendedMemoryBlock("0", context); //$NON-NLS-1$
			}
			
			fCurrentContainers.add(container);
			rendering.init(container, block);
			rendering.createControl(tab.getParent());
			tab.setControl(rendering.getControl());
			tab.getParent().setSelection(0);
			tab.setData(KEY_RENDERING, rendering);
			tab.setData(KEY_CONTAINER, container);
			getSite().getSelectionProvider().setSelection(new StructuredSelection(tab.getData(KEY_RENDERING)));
			updateLabel(tab, rendering);
			
			rendering.addPropertyChangeListener(new IPropertyChangeListener()
			{
				public void propertyChange(final PropertyChangeEvent event) {
					WorkbenchJob job = new WorkbenchJob("MemoryBrowser PropertyChanged") { //$NON-NLS-1$
						public IStatus runInUIThread(IProgressMonitor monitor) {
							if(tab.isDisposed())
								return Status.OK_STATUS;
								
							if (event.getProperty().equals(IBasicPropertyConstants.P_TEXT))
								updateLabel(tab, rendering);
							return Status.OK_STATUS;
						}
					};
					job.setSystem(true);
					job.schedule();
				}
			});
			
		} catch (CoreException e) {
			MemoryBrowserPlugin.getDefault().getLog().log(new Status(Status.ERROR, MemoryBrowserPlugin.PLUGIN_ID, "", e)); //$NON-NLS-1$
		}
	}
	
	private CTabFolder getTabFolder(Object context)
	{
		return fContextFolders.get(context);
	}
	
	private CTabFolder setTabFolder(Object context, CTabFolder folder)
	{
		return fContextFolders.put(context, folder);
	}	
	
	private void releaseTabFolder(Object context)
	{
		final CTabFolder folder = getTabFolder(context);
		if(folder != null)
		{
			for(CTabItem tab : folder.getItems()) {
				IMemoryRenderingContainer container = (IMemoryRenderingContainer) tab.getData(KEY_CONTAINER);
				fCurrentContainers.remove( container );
				tab.dispose();
			}
		}
		fContextFolders.remove(context);
	}
	
	class SelectionProviderAdapter implements ISelectionProvider {

	    List<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>();

	    ISelection theSelection = StructuredSelection.EMPTY;

	    public void addSelectionChangedListener(ISelectionChangedListener listener) {
	        listeners.add(listener);
	    }

	    public ISelection getSelection() {
	        return theSelection;
	    }

	    public void removeSelectionChangedListener(
	            ISelectionChangedListener listener) {
	        listeners.remove(listener);
	    }

	    public void setSelection(ISelection selection) {
	        theSelection = selection;
	        final SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
	        Object[] listenersArray = listeners.toArray();
	        
	        for (int i = 0; i < listenersArray.length; i++) {
	            final ISelectionChangedListener l = (ISelectionChangedListener) listenersArray[i];
	            SafeRunner.run(new SafeRunnable() {
	                public void run() {
	                    l.selectionChanged(e);
	                }
	            });
			}
	    }

	}
}


