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
	private String defaultRenderingTypeId = null; //$NON-NLS-1$
	
	private final static String KEY_RENDERING = "RENDERING"; //$NON-NLS-1$
	private final static String KEY_CONTEXT = "CONTEXT"; //$NON-NLS-1$
	private final static String KEY_RETRIEVAL = "RETRIEVAL"; //$NON-NLS-1$

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
		
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow())
			.addDebugContextListener(this);
		
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
		
		Object selection = DebugUITools.getDebugContextManager()
			.getContextService(getSite().getWorkbenchWindow()).getActiveContext();
		if(selection instanceof StructuredSelection)
			handleDebugContextChanged(((StructuredSelection) selection).getFirstElement());
	}
	
	public void dispose() {
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow())
			.removeDebugContextListener(this);
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
		return new IMemoryRenderingContainer[] {
			new IMemoryRenderingContainer()
			{

				public void addMemoryRendering(IMemoryRendering rendering) {
					
				}

				public IMemoryRendering getActiveRendering() {
					if(fStackLayout.topControl instanceof CTabFolder)
					{
						CTabFolder activeFolder = (CTabFolder) fStackLayout.topControl;
						if(activeFolder.getSelection() != null)
							return (IMemoryRendering) activeFolder.getSelection().getData(KEY_RENDERING);
					}
					return null;
				}

				public String getId() {
					return null;
				}

				public String getLabel() {
					return null;
				}

				public IMemoryRenderingSite getMemoryRenderingSite() {
					return MemoryBrowser.this;
				}

				public IMemoryRendering[] getRenderings() {
					return null;
				}

				public void removeMemoryRendering(IMemoryRendering rendering) {
					
				}
				
			}
		};
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
				type.getLabel(), IAction.AS_CHECK_BOX)
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
					return null;
				}

				public void removeMemoryRendering(IMemoryRendering rendering) {}
				
			};
			
			IMemoryBlock block = null;
			if(retrieval instanceof IAdaptable)
			{
				IMemoryBlockRetrievalExtension retrievalExtension = (IMemoryBlockRetrievalExtension) 
					((IAdaptable) retrieval).getAdapter(IMemoryBlockRetrievalExtension.class);
				if(retrievalExtension != null)
					block = retrievalExtension.getExtendedMemoryBlock("0", context); //$NON-NLS-1$
			}
			
			rendering.init(container, block);
			rendering.createControl(tab.getParent());
			tab.setControl(rendering.getControl());
			tab.getParent().setSelection(0);
			tab.setData(KEY_RENDERING, rendering);
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
			for(CTabItem tab : folder.getItems())
				tab.dispose();
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


