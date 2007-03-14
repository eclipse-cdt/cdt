/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.dd.debug.memory.renderings.traditional;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.memory.AbstractMemoryRendering;
import org.eclipse.debug.ui.memory.AbstractTableRendering;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.UIJob;

/**
 * A memory rendering displaying memory in a traditional 
 * memory view look and feel, optimized for minimal IO traffic.
 * <p>
 * requirements of the debug model implementation:
 *   - An IMemoryBlockExtension is required.
 *  
 *  Since it is not possible to size the memory block to match
 *  the size of the viewport, memory block change notification
 *  is not useful. Such events are ignored by this rendering.
 *  
 *  Value validation happens on a per character basis at edit time.
 *  A minor drawback to this approach, the user may have to decrement
 *  a less significant digit before being able to increment a more
 *  significant digit. 
 *  
 *  TODO: - refactoring (reduce code duplication)
 *        - comments (javadoc) / clean up
 *        - unicode - the cell boundaries make multi-byte character display
 *          not so useful. the text pane could be reworked. or, maybe this
 *          style of memory view is not appropriate for unicode.
 *        - utf 16 support
 *  </p>
 */

public class TraditionalRendering extends AbstractMemoryRendering
{

    Rendering fRendering;
    
    private Action displayEndianBigAction;
    private Action displayEndianLittleAction;

    private IWorkbenchAdapter fWorkbenchAdapter;

    public TraditionalRendering(String id)
    {
        super(id);

        JFaceResources.getFontRegistry().addListener(
            new IPropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent event)
                {
                    if(event.getProperty().equals(
                        IInternalDebugUIConstants.FONT_NAME))
                    {
                        TraditionalRendering.this.fRendering
                            .handleFontPreferenceChange(JFaceResources
                                .getFont(IInternalDebugUIConstants.FONT_NAME));
                    }
                }
            });

        this.addPropertyChangeListener(new IPropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent event)
            {
                IMemoryRendering sourceRendering = (IMemoryRendering) event
                    .getSource();
                if(!sourceRendering.getMemoryBlock().equals(getMemoryBlock()))
                    return;

                Object address = event.getNewValue();

                if(event.getProperty().equals(
                    AbstractTableRendering.PROPERTY_SELECTED_ADDRESS)
                    && address instanceof BigInteger)
                {
                    TraditionalRendering.this.fRendering
                        .ensureVisible((BigInteger) address);
                }
            }
        });
        
        TraditionalRenderingPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(
    		new IPropertyChangeListener()
    		{
    			public void propertyChange(PropertyChangeEvent event)
    			{
    				disposeColors();
    				allocateColors();
    				applyPreferences();
    			}
    		});
        
    }

    public Control createControl(Composite parent)
    {
    	allocateColors();
    	
        this.fRendering = new Rendering(parent, this);
        
        applyPreferences();

        createMenus();
        
        return this.fRendering;
    }
    
    public void gotoAddress(BigInteger address)
    {
      this.fRendering.gotoAddress(address);
    }
    
    public void updateRenderingLabels()
	{
		UIJob job = new UIJob("updateLabels"){ //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				
				// update tab labels
				String fLabel = getLabel();
				firePropertyChangedEvent(new PropertyChangeEvent(TraditionalRendering.this, 
						IBasicPropertyConstants.P_TEXT, null, fLabel));
				
				return Status.OK_STATUS;
			}};
		job.setSystem(true);
		job.schedule();
	}
    
    private Color colorBackground;
    private Color colorChanged;
    private Color colorEdit;
    private Color colorSelection;
    private Color colorText;
    private Color colorTextAlternate;
    
    public void allocateColors()
    {
    	IPreferenceStore store = TraditionalRenderingPlugin.getDefault().getPreferenceStore();
    	
    	colorBackground = new Color(Display.getDefault(), PreferenceConverter.getColor(store, 
    			TraditionalRenderingPreferenceConstants.MEM_COLOR_BACKGROUND));
    	
    	colorChanged = new Color(Display.getDefault(), PreferenceConverter.getColor(store, 
    			TraditionalRenderingPreferenceConstants.MEM_COLOR_CHANGED));
    	
    	colorEdit = new Color(Display.getDefault(), PreferenceConverter.getColor(store, 
    			TraditionalRenderingPreferenceConstants.MEM_COLOR_EDIT));
    	
    	colorSelection = new Color(Display.getDefault(), PreferenceConverter.getColor(store, 
    			TraditionalRenderingPreferenceConstants.MEM_COLOR_SELECTION));
    	
    	colorText = new Color(Display.getDefault(), PreferenceConverter.getColor(store, 
    			TraditionalRenderingPreferenceConstants.MEM_COLOR_TEXT));
    	
    	// alternate cell color
    	Color textColor = getColorText();
    	int red = textColor.getRed();
    	int green = textColor.getGreen();
    	int blue = textColor.getBlue();
    	
    	float scale = (float) store.getInt(
    			TraditionalRenderingPreferenceConstants.MEM_LIGHTEN_DARKEN_ALTERNATE_CELLS);
    	
		red = (int) Math.min(red + ((255 - red) / 10) * scale, 255);
		green = (int) Math.min(green + ((255 - green) / 10) * scale, 255);
		blue = (int) Math.min(blue + ((255 - blue) / 10) * scale, 255);

    	colorTextAlternate = new Color(Display.getDefault(), new RGB(red, green, blue));
    }
    
    public void disposeColors()
    {
    	if(colorBackground != null)
    		colorBackground.dispose();
    	colorBackground = null;
    	
    	if(colorChanged != null)
    		colorChanged.dispose();
    	colorChanged = null;
    	
    	if(colorEdit != null)
    		colorEdit.dispose();
    	colorEdit = null;
    	
    	if(colorSelection != null)
    		colorSelection.dispose();
    	colorSelection = null;
    	
    	if(colorText != null)
    		colorText.dispose();
    	colorText = null;
    	
    	if(colorTextAlternate != null)
    		colorTextAlternate.dispose();
    	colorTextAlternate = null;
    }

    public void applyPreferences()
    {
    	fRendering.setBackground(getColorBackground());
    	
    	AbstractPane panes[] = fRendering.getRenderingPanes();
    	for(int i = 0; i < panes.length; i++)
    		panes[i].setBackground(getColorBackground());
    	
    	fRendering.redrawPanes();
    }
    
    public Color getColorBackground()
    {
    	IPreferenceStore store = TraditionalRenderingPlugin.getDefault().getPreferenceStore();
    	
    	if(store.getBoolean(TraditionalRenderingPreferenceConstants.MEM_USE_GLOBAL_BACKGROUND)) 
    		return Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
    	else
    		return colorBackground; 
    }
    
    public Color getColorChanged()
    {
    	return colorChanged; 
    }
    
    public Color getColorEdit()
    {
    	return colorEdit; 
    }
    
    public Color getColorSelection()
    {
    	IPreferenceStore store = TraditionalRenderingPlugin.getDefault().getPreferenceStore();
    	
    	if(store.getBoolean(TraditionalRenderingPreferenceConstants.MEM_USE_GLOBAL_SELECTION))    	
        	return Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION);
    	else
    		return colorSelection; 
    }
    
    public Color getColorText()
    {
    	IPreferenceStore store = TraditionalRenderingPlugin.getDefault().getPreferenceStore();
    	
    	if(store.getBoolean(TraditionalRenderingPreferenceConstants.MEM_USE_GLOBAL_TEXT))    	
        	return Display.getDefault().getSystemColor(SWT.COLOR_LIST_FOREGROUND);
    	else
    		return colorText;
    }
    
    public Color getColorTextAlternate()
    {
    	return colorTextAlternate;
    }
    
    public void createMenus()
    {
        // add the menu to each of the rendering panes
        Control[] renderingControls = this.fRendering.getRenderingPanes();
        for(int i = 0; i < renderingControls.length; i++)
            super.createPopupMenu(renderingControls[i]);
        super.createPopupMenu(this.fRendering);

        // copy

        final Action copyAction = new CopyAction(this.fRendering);

        // go to address

        final Action gotoAddressAction = new Action(
            TraditionalRenderingMessages
                .getString("TraditionalRendering.GO_TO_ADDRESS")) //$NON-NLS-1$
        {
            public void run()
            {
                Display.getDefault().asyncExec(new Runnable()
                {
                    public void run()
                    {
                        TraditionalRendering.this.fRendering
                            .setVisibleAddressBar(true);
                    }
                });
            }
        };
        gotoAddressAction.setAccelerator(SWT.CTRL | 'G');

        // reset to base address
        
        final Action gotoBaseAddressAction = new Action(
            TraditionalRenderingMessages
                .getString("TraditionalRendering.RESET_TO_BASE_ADDRESS")) //$NON-NLS-1$
        {
            public void run()
            {
                Display.getDefault().asyncExec(new Runnable()
                {
                    public void run()
                    {
                        TraditionalRendering.this.fRendering
                        	.gotoAddress(TraditionalRendering.this.fRendering.fBaseAddress);
                    }
                });
            }
        };

        
        // refresh

        final Action refreshAction = new Action(
            TraditionalRenderingMessages
                .getString("TraditionalRendering.REFRESH")) //$NON-NLS-1$
        {
            public void run()
            {
                Display.getDefault().asyncExec(new Runnable()
                {
                    public void run()
                    {
                        TraditionalRendering.this.fRendering.refresh();
                    }
                });
            }
        };
        
        // display address

        final Action displayAddressPaneAction = new Action(
            TraditionalRenderingMessages
                .getString("TraditionalRendering.ADDRESS"), //$NON-NLS-1$
            IAction.AS_CHECK_BOX)
        {
            public void run()
            {
                TraditionalRendering.this.fRendering.setPaneVisible(
                    Rendering.PANE_ADDRESS, isChecked());
            }
        };
        displayAddressPaneAction.setChecked(this.fRendering
            .getPaneVisible(Rendering.PANE_ADDRESS));

        // display hex

        final Action displayBinaryPaneAction = new Action(
            TraditionalRenderingMessages
                .getString("TraditionalRendering.BINARY"), //$NON-NLS-1$
            IAction.AS_CHECK_BOX)
        {
            public void run()
            {
                TraditionalRendering.this.fRendering.setPaneVisible(
                    Rendering.PANE_BINARY, isChecked());
            }
        };
        displayBinaryPaneAction.setChecked(this.fRendering
            .getPaneVisible(Rendering.PANE_BINARY));

        // display text

        final Action displayTextPaneAction = new Action(
            TraditionalRenderingMessages
                .getString("TraditionalRendering.TEXT"), //$NON-NLS-1$
            IAction.AS_CHECK_BOX)
        {
            public void run()
            {
                TraditionalRendering.this.fRendering.setPaneVisible(
                    Rendering.PANE_TEXT, isChecked());
            }
        };
        displayTextPaneAction.setChecked(this.fRendering
            .getPaneVisible(Rendering.PANE_TEXT));

        // display size

        final Action displaySize1BytesAction = new Action(
            TraditionalRenderingMessages
                .getString("TraditionalRendering.1_BYTE"), //$NON-NLS-1$
            IAction.AS_RADIO_BUTTON)
        {
            public void run()
            {
                TraditionalRendering.this.fRendering.setBytesPerColumn(1);
            }
        };
        displaySize1BytesAction
            .setChecked(this.fRendering.getBytesPerColumn() == 1);

        final Action displaySize2BytesAction = new Action(
            TraditionalRenderingMessages
                .getString("TraditionalRendering.2_BYTES"), IAction.AS_RADIO_BUTTON) //$NON-NLS-1$
        {
            public void run()
            {
                TraditionalRendering.this.fRendering.setBytesPerColumn(2);
            }
        };
        displaySize2BytesAction
            .setChecked(this.fRendering.getBytesPerColumn() == 2);

        final Action displaySize4BytesAction = new Action(
            TraditionalRenderingMessages
                .getString("TraditionalRendering.4_BYTES"), IAction.AS_RADIO_BUTTON) //$NON-NLS-1$
        {
            public void run()
            {
                TraditionalRendering.this.fRendering.setBytesPerColumn(4);
            }
        };
        displaySize4BytesAction
            .setChecked(this.fRendering.getBytesPerColumn() == 4);

        final Action displaySize8BytesAction = new Action(
            TraditionalRenderingMessages
                .getString("TraditionalRendering.8_BYTES"), IAction.AS_RADIO_BUTTON) //$NON-NLS-1$
        {
            public void run()
            {
                TraditionalRendering.this.fRendering.setBytesPerColumn(8);
            }
        };
        displaySize8BytesAction
            .setChecked(this.fRendering.getBytesPerColumn() == 8);

        // text / unicode ?

        final Action displayCharactersISO8859Action = new Action(
                TraditionalRenderingMessages
                    .getString("TraditionalRendering.ISO-8859-1"), //$NON-NLS-1$
                IAction.AS_RADIO_BUTTON)
        {
            public void run()
            {
                TraditionalRendering.this.fRendering
                    .setTextMode(Rendering.TEXT_ISO_8859_1);
            }
        };
        displayCharactersISO8859Action.setChecked(this.fRendering
        		.getTextMode() == Rendering.TEXT_ISO_8859_1);
        
        final Action displayCharactersUSASCIIAction = new Action(
            TraditionalRenderingMessages
                .getString("TraditionalRendering.USASCII"), //$NON-NLS-1$
            IAction.AS_RADIO_BUTTON)
        {
            public void run()
            {
                TraditionalRendering.this.fRendering
                    .setTextMode(Rendering.TEXT_USASCII);
            }
        };
        displayCharactersUSASCIIAction.setChecked(this.fRendering
        		.getTextMode() == Rendering.TEXT_USASCII);

        final Action displayCharactersUTF8Action = new Action(
            TraditionalRenderingMessages
                .getString("TraditionalRendering.UTF8"), IAction.AS_RADIO_BUTTON) //$NON-NLS-1$
        {
            public void run()
            {
                TraditionalRendering.this.fRendering
                .setTextMode(Rendering.TEXT_UTF8);
            }
        };
        displayCharactersUTF8Action.setChecked(this.fRendering
        		.getTextMode() == Rendering.TEXT_UTF8);
        
//        final Action displayCharactersUTF16Action = new Action(
//                TraditionalRenderingMessages
//                    .getString("TraditionalRendering.UTF16"), IAction.AS_RADIO_BUTTON) //$NON-NLS-1$
//        {
//            @Override
//            public void run()
//            {
//                TraditionalRendering.this.fRendering
//                .setTextMode(Rendering.TEXT_UTF16);
//            }
//        };
//        displayCharactersUTF16Action.setChecked(this.fRendering
//            .getTextMode() == Rendering.TEXT_UTF16);

        // endian

        displayEndianBigAction = new Action(
            TraditionalRenderingMessages
                .getString("TraditionalRendering.BIG"), //$NON-NLS-1$
            IAction.AS_RADIO_BUTTON)
        {
            public void run()
            {
                TraditionalRendering.this.fRendering
                    .setLittleEndian(false);
            }
        };
        displayEndianBigAction.setChecked(!this.fRendering.isLittleEndian());

        displayEndianLittleAction = new Action(
            TraditionalRenderingMessages
                .getString("TraditionalRendering.LITTLE"), //$NON-NLS-1$
            IAction.AS_RADIO_BUTTON)
        {
            public void run()
            {
                TraditionalRendering.this.fRendering
                    .setLittleEndian(true);
            }
        };
        displayEndianLittleAction.setChecked(this.fRendering.isLittleEndian());

        // radix

        final Action displayRadixHexAction = new Action(
            TraditionalRenderingMessages
                .getString("TraditionalRendering.HEX"), //$NON-NLS-1$
            IAction.AS_RADIO_BUTTON)
        {
            public void run()
            {
                TraditionalRendering.this.fRendering
                    .setRadix(Rendering.RADIX_HEX);
            }
        };
        displayRadixHexAction
            .setChecked(this.fRendering.getRadix() == Rendering.RADIX_HEX);

        final Action displayRadixDecSignedAction = new Action(
            TraditionalRenderingMessages
                .getString("TraditionalRendering.DECIMAL_SIGNED"), IAction.AS_RADIO_BUTTON) //$NON-NLS-1$
        {
            public void run()
            {
                TraditionalRendering.this.fRendering
                    .setRadix(Rendering.RADIX_DECIMAL_SIGNED);
            }
        };
        displayRadixDecSignedAction
            .setChecked(this.fRendering.getRadix() == Rendering.RADIX_DECIMAL_SIGNED);

        final Action displayRadixDecUnsignedAction = new Action(
            TraditionalRenderingMessages
                .getString("TraditionalRendering.DECIMAL_UNSIGNED"), IAction.AS_RADIO_BUTTON) //$NON-NLS-1$
        {
            public void run()
            {
                TraditionalRendering.this.fRendering
                    .setRadix(Rendering.RADIX_DECIMAL_UNSIGNED);
            }
        };
        displayRadixDecUnsignedAction
            .setChecked(this.fRendering.getRadix() == Rendering.RADIX_DECIMAL_UNSIGNED);

        final Action displayRadixOctAction = new Action(
            TraditionalRenderingMessages
                .getString("TraditionalRendering.OCTAL"), //$NON-NLS-1$
            IAction.AS_RADIO_BUTTON)
        {
            public void run()
            {
                TraditionalRendering.this.fRendering
                    .setRadix(Rendering.RADIX_OCTAL);
            }
        };
        displayRadixOctAction
            .setChecked(this.fRendering.getRadix() == Rendering.RADIX_OCTAL);

        final Action displayRadixBinAction = new Action(
            TraditionalRenderingMessages
                .getString("TraditionalRendering.BINARY"), //$NON-NLS-1$
            IAction.AS_RADIO_BUTTON)
        {
            public void run()
            {
                TraditionalRendering.this.fRendering
                    .setRadix(Rendering.RADIX_BINARY);
            }
        };
        displayRadixBinAction
            .setChecked(this.fRendering.getRadix() == Rendering.RADIX_BINARY);

        getPopupMenuManager().addMenuListener(new IMenuListener()
        {
            public void menuAboutToShow(IMenuManager manager)
            {
                manager.add(new Separator());

                MenuManager sub = new MenuManager(
                    TraditionalRenderingMessages
                        .getString("TraditionalRendering.PANES")); //$NON-NLS-1$
                sub.add(displayAddressPaneAction);
                sub.add(displayBinaryPaneAction);
                sub.add(displayTextPaneAction);
                manager.add(sub);

                sub = new MenuManager(TraditionalRenderingMessages
                    .getString("TraditionalRendering.ENDIAN")); //$NON-NLS-1$
                sub.add(displayEndianBigAction);
                sub.add(displayEndianLittleAction);
                manager.add(sub);

                sub = new MenuManager(TraditionalRenderingMessages
                    .getString("TraditionalRendering.TEXT")); //$NON-NLS-1$
                sub.add(displayCharactersISO8859Action);
                sub.add(displayCharactersUSASCIIAction);
                sub.add(displayCharactersUTF8Action);
                //sub.add(displayCharactersUTF16Action);
                manager.add(sub);

                sub = new MenuManager(TraditionalRenderingMessages
                    .getString("TraditionalRendering.CELL_SIZE")); //$NON-NLS-1$
                sub.add(displaySize1BytesAction);
                sub.add(displaySize2BytesAction);
                sub.add(displaySize4BytesAction);
                sub.add(displaySize8BytesAction);
                manager.add(sub);

                sub = new MenuManager(TraditionalRenderingMessages
                    .getString("TraditionalRendering.RADIX")); //$NON-NLS-1$
                sub.add(displayRadixHexAction);
                sub.add(displayRadixDecSignedAction);
                sub.add(displayRadixDecUnsignedAction);
                sub.add(displayRadixOctAction);
                sub.add(displayRadixBinAction);
                manager.add(sub);
                manager.add(new Separator());

                manager.add(copyAction);

                manager.add(gotoAddressAction);
                manager.add(gotoBaseAddressAction);
                manager.add(refreshAction);
                manager.add(new Separator());
                manager.add(new Separator(
                    IWorkbenchActionConstants.MB_ADDITIONS));
            }
        });

    }

    public Control getControl()
    {
        return this.fRendering;
    }

    protected void bytesAreLittleEndian(boolean areLE)
    {
    	// once we actually read memory we can determine the
    	// endianess and need to set these actions accordingly.
        displayEndianBigAction.setChecked(!areLE);
        displayEndianLittleAction.setChecked(areLE);    	
    }
    
    public void dispose()
    {
        if(this.fRendering != null)
            this.fRendering.dispose();
        super.dispose();
    }

    public Object getAdapter(Class adapter)
    {

        if(adapter == IWorkbenchAdapter.class)
        {
            if(this.fWorkbenchAdapter == null)
            {
                this.fWorkbenchAdapter = new IWorkbenchAdapter()
                {
                    public Object[] getChildren(Object o)
                    {
                        return new Object[0];
                    }

                    public ImageDescriptor getImageDescriptor(Object object)
                    {
                        return null;
                    }

                    public String getLabel(Object o)
                    {
                        return TraditionalRenderingMessages
                            .getString("TraditionalRendering.RENDERING_NAME"); //$NON-NLS-1$
                    }

                    public Object getParent(Object o)
                    {
                        return null;
                    }
                };
            }
            return this.fWorkbenchAdapter;
        }

        return super.getAdapter(adapter);
    }

}


/*
 * A place holder address bar for go to address. For consistency, 
 * this will eventually be replaced by a standard memory view
 * address bar. 
 */

class AddressBar extends Composite
{
    Text fTextControl;

    Label fLabelControl;
    
    private final static int DUMMY_WIDTH = 100;

    public AddressBar(final Rendering rendering)
    {
        super(rendering, SWT.BORDER);

        this.fLabelControl = new Label(this, SWT.SINGLE);
        this.fTextControl = new Text(this, SWT.SINGLE);

        GridData layoutData = new GridData();

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		layout.marginHeight = 0;
		layout.marginLeft = 0;
		
        this.setLayout(layout);

        layoutData.horizontalAlignment = SWT.FILL;
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.grabExcessVerticalSpace = true;
        
        fLabelControl.setText(TraditionalRenderingMessages
                .getString("TraditionalRendering.GO_TO_ADDRESS")); //$NON-NLS-1$
        
        this.fTextControl.setLayoutData(layoutData);

        this.fTextControl.addFocusListener(new FocusListener()
        {
            public void focusGained(FocusEvent e)
            {
                // do nothing
            }

            public void focusLost(FocusEvent e)
            {
                rendering.setVisibleAddressBar(false);
            }
        });

        this.fTextControl.addKeyListener(new KeyListener()
        {
            public void keyPressed(KeyEvent ke)
            {
                if(ke.keyCode == SWT.ESC)
                {
                    rendering.setVisibleAddressBar(false);
                }
                else if(ke.character == '\r')
                {
                    int radix = 10;
                    String s = fTextControl.getText().trim();
                    if(s.toUpperCase().indexOf("0X") >= 0) //$NON-NLS-1$
                    {
                        s = s.substring(2);
                        radix = 16;
                    }
                    try
                    {
                        BigInteger newAddress = new BigInteger(s, radix);
                        rendering.setVisibleAddressBar(false);
                        rendering.gotoAddress(newAddress);
                    }
                    catch(Exception e)
                    {
                        // do nothing
                    }

                }
            }

            public void keyReleased(KeyEvent ke)
            {
                // do nothing
            }
        });
    }

    public void setText(String data)
    {
        this.fTextControl.setText(data);
        this.fTextControl.forceFocus();
        this.fTextControl.selectAll();
    }

    public Point computeSize(int wHint, int hHint)
    {
    	return new Point(DUMMY_WIDTH, this.fTextControl.computeSize(10, 24,
                true).y);
    }
}

class TraditionalMemoryByte extends MemoryByte
{
	private boolean isEdited = false;
	
	public TraditionalMemoryByte(byte byteValue)
	{
		super(byteValue);
	}
	
	public boolean isEdited()
	{
		return isEdited;
	}
	
	public void setEdited(boolean edited)
	{
		isEdited = edited;
	}
}

class Rendering extends Composite implements IDebugEventSetListener
{
    // the IMemoryRendering parent
    private TraditionalRendering fParent;

    // controls
    private Slider fScrollBar;

    private AddressPane fAddressPane;

    private DataPane fBinaryPane;

    private TextPane fTextPane;

    private AddressBar fAddressBar;

    private Selection fSelection = new Selection();

    // storage
 
    BigInteger fViewportAddress = null; // default visibility for performance

    BigInteger fMemoryBlockStartAddress = null;
    BigInteger fMemoryBlockEndAddress = null;
    
    BigInteger fBaseAddress = null; // remember the base address
    
    private int fColumnCount = 0; 	// auto calculate can be disabled by user,
    								// making this user settable
    
    private int fBytesPerRow = 0;	// current number of bytes per row are displayed
    
    private int fCurrentScrollSelection = 0;	// current scroll selection;
    
    // user settings
    
    private int fTextMode = 1;  // ASCII default, TODO make preference?

    private int fBytesPerColumn = 4; // 4 byte cell width default

    private int fRadix = RADIX_HEX;

    private boolean fLittleEndian = false;
    
    private boolean fCheckedLittleEndian = false;

    // constants used to identify radix
    protected final static int RADIX_HEX = 1;

    protected final static int RADIX_DECIMAL_SIGNED = 2;

    protected final static int RADIX_DECIMAL_UNSIGNED = 3;

    protected final static int RADIX_OCTAL = 4;

    protected final static int RADIX_BINARY = 5;

    // constants used to identify panes
    protected final static int PANE_ADDRESS = 1;

    protected final static int PANE_BINARY = 2;

    protected final static int PANE_TEXT = 3;
    
    // constants used to identify text, maybe java should be queried for all available sets
    protected final static int TEXT_ISO_8859_1 = 1;
    protected final static int TEXT_USASCII = 2;
    protected final static int TEXT_UTF8 = 3;
    protected final static int TEXT_UTF16 = 4;
    
    // view internal settings
    private int fCellPadding = 2;

    private int fPaneSpacing = 16;
    
    // flag whether the memory cache is dirty
    private boolean fCacheDirty = false;

    public Rendering(Composite parent,
        TraditionalRendering renderingParent)
    {
        super(parent, SWT.DOUBLE_BUFFERED | SWT.NO_BACKGROUND);

        this.setFont(JFaceResources
            .getFont(IInternalDebugUIConstants.FONT_NAME)); // TODO internal?

        this.fParent = renderingParent;

        // instantiate the panes, TODO default visibility from state or
        // plugin.xml?
        this.fAddressPane = new AddressPane(this);
        this.fBinaryPane = new DataPane(this);
        this.fTextPane = new TextPane(this);
        
        // FIXME temporary address bar; will be replaced by standard Memory bar
        this.fAddressBar = new AddressBar(this);
        this.fAddressBar.setVisible(false);

        // initialize the viewport start
        IMemoryBlockExtension memoryBlock = getMemoryBlock();
        if(memoryBlock != null)
        {
            try
            {
                fViewportAddress = memoryBlock.getMemoryBlockStartAddress();
                // this will be null if memory may be retrieved at any address less than
                // this memory block's base.  if so use the base address.
                if (fViewportAddress == null)
                	fViewportAddress = memoryBlock.getBigBaseAddress();
                fBaseAddress = fViewportAddress;
            }
            catch(DebugException e)
            {
                fViewportAddress = null;
                if(isDebug())
                    Rendering.this
                        .logError(
                            TraditionalRenderingMessages
                                .getString("TraditionalRendering.FAILURE_RETRIEVE_START_ADDRESS"), e); //$NON-NLS-1$
            }
        }

        fScrollBar = new Slider(this, SWT.V_SCROLL);
        fScrollBar.addSelectionListener(new SelectionListener()
        {
            public void widgetSelected(SelectionEvent se)
            {
            	int addressableSize = getAddressableSize();
            	
                switch(se.detail)
                {
                    case SWT.ARROW_DOWN:
                        fViewportAddress = fViewportAddress.add(BigInteger
                            .valueOf(getAddressableCellsPerRow()));
                        ensureViewportAddressDisplayable();
                        redrawPanes();
                        break;
                    case SWT.PAGE_DOWN:
                        fViewportAddress = fViewportAddress.add(BigInteger
                            .valueOf(getAddressableCellsPerRow()
                                * (Rendering.this.getRowCount() - 1)));
                        ensureViewportAddressDisplayable();
                        redrawPanes();
                        break;
                    case SWT.ARROW_UP:
                        fViewportAddress = fViewportAddress.subtract(BigInteger
                            .valueOf(getAddressableCellsPerRow()));
                        ensureViewportAddressDisplayable();
                        redrawPanes();
                        break;
                    case SWT.PAGE_UP:
                        fViewportAddress = fViewportAddress.subtract(BigInteger
                            .valueOf(getAddressableCellsPerRow()
                                * (Rendering.this.getRowCount() - 1)));
                        ensureViewportAddressDisplayable();
                        redrawPanes();
                        break;
                    case SWT.SCROLL_LINE:
                    	if(fScrollBar.getSelection() == fScrollBar.getMinimum())
                    	{
                    		// Set view port start address to the start address of the Memory Block
                    		fViewportAddress = Rendering.this.getMemoryBlockStartAddress();
                    	}
                    	else if(fScrollBar.getSelection() == fScrollBar.getMaximum())
                    	{
                    		// The view port end address should be less or equal to the the end address of the Memory Block
                    		// Set view port address to be bigger than the end address of the Memory Block for now
                    		// and let ensureViewportAddressDisplayable() to figure out the correct view port start address
                    		fViewportAddress = Rendering.this.getMemoryBlockEndAddress();
                    	}
                    	else
                    	{
                            // Figure out the delta
                        	int delta = fScrollBar.getSelection() - fCurrentScrollSelection;
                    		fViewportAddress = fViewportAddress.add(BigInteger.valueOf(
                    				getAddressableCellsPerRow() * delta));
                    	}
                        ensureViewportAddressDisplayable();
                        // Update tooltip
                    	fScrollBar.setToolTipText(Rendering.this.getAddressString(fViewportAddress));
                        // Update the addresses on the Address pane. 
                    	// Do not update the Binary and Text panes until dragging of the thumb nail stops
                        if(fAddressPane.isPaneVisible())
                        {
                            fAddressPane.redraw();
                        }                        
                    	break;
                    case SWT.NONE:
                        // Dragging of the thumb nail stops. Redraw the panes
                        redrawPanes();
                    	break;
                }

            }

            public void widgetDefaultSelected(SelectionEvent se)
            {
                // do nothing
            }
        });

        this.addPaintListener(new PaintListener()
        {
            public void paintControl(PaintEvent pe)
            {
            	pe.gc.setBackground(Rendering.this.getTraditionalRendering().getColorBackground());
                pe.gc.fillRectangle(0, 0, Rendering.this.getBounds().width, 
                		Rendering.this.getBounds().height);
            }
        });

        this.setLayout(new Layout()
        {
            public void layout(Composite composite, boolean changed)
            {
                int x = 0;
                int y = 0;

                if(fAddressBar.isVisible())
                {
                    fAddressBar.setBounds(0, 0,
                        Rendering.this.getBounds().width, fAddressBar
                            .computeSize(1, 1).y);
                    y = fAddressBar.getBounds().height;
                }

                if(fAddressPane.isPaneVisible())
                {
                    fAddressPane.setBounds(0, y,
                        fAddressPane.computeSize(0, 0).x, Rendering.this
                            .getBounds().height
                            - y);
                    x = fAddressPane.getBounds().x
                        + fAddressPane.getBounds().width;
                }

                if(fBinaryPane.isPaneVisible())
                {
                    fBinaryPane.setBounds(x, y,
                        fBinaryPane.computeSize(0, 0).x, Rendering.this
                            .getBounds().height
                            - y);
                    x = fBinaryPane.getBounds().x
                        + fBinaryPane.getBounds().width;
                }

                Point scrollBarPreferredSize = fScrollBar.computeSize(20,
                    Rendering.this.getBounds().height);

                if(fTextPane.isPaneVisible())
                {
                    fTextPane.setBounds(x, y, Rendering.this.getBounds().width
                        - x - scrollBarPreferredSize.x, Rendering.this
                        .getBounds().height
                        - y);
                }

                fScrollBar.setBounds(Rendering.this.getBounds().width
                    - scrollBarPreferredSize.x, y, scrollBarPreferredSize.x,
                    Rendering.this.getBounds().height - y);
                
                fScrollBar.moveAbove(null);
            }

            protected Point computeSize(Composite composite, int wHint,
                int hHint, boolean flushCache)
            {
                return new Point(100, 100); // dummy data
            }
        });

        this.addControlListener(new ControlListener()
        {
            public void controlMoved(ControlEvent ce)
            {
            }

            public void controlResized(ControlEvent ce)
            {
                packColumns();
            }
        });

        DebugPlugin.getDefault().addDebugEventListener(this);
    }

    protected TraditionalRendering getTraditionalRendering() // TODO rename
    {
    	return fParent;
    }
    
    // Ensure that all addresses displayed are within the addressable range
    protected void ensureViewportAddressDisplayable()
    {
        if(fViewportAddress.compareTo(Rendering.this.getMemoryBlockStartAddress()) < 0)
        {
        	fViewportAddress = Rendering.this.getMemoryBlockStartAddress();
        }
        else if(getViewportEndAddress().compareTo(getMemoryBlockEndAddress().add(BigInteger.ONE)) > 0)
        {
            fViewportAddress = getMemoryBlockEndAddress().subtract(BigInteger.valueOf(getAddressableCellsPerRow() 
            		* getRowCount() - 1));
        }

        setCurrentScrollSelection();
    }
    
    protected Selection getSelection()
    {
        return fSelection;
    }
    
    protected void logError(String message, Exception e)
    {
        Status status = new Status(IStatus.ERROR, fParent.getRenderingId(),
            DebugException.INTERNAL_ERROR, message, e);

        DebugUIPlugin.getDefault().getLog().log(status);
    }

    public void handleFontPreferenceChange(Font font)
    {
        setFont(font);

        Control controls[] = this.getRenderingPanes();
        for(int i = 0; i < controls.length; i++)
            controls[i].setFont(font);

        packColumns();
        redrawPanes();
    }

    public void handleDebugEvents(DebugEvent[] events)
    {
    	for(int i = 0; i < events.length; i++)
        {
            if(events[0].getSource() instanceof IDebugElement)
            {
                final int kind = events[i].getKind();
                final int detail = events[i].getDetail();
                final IDebugElement source = (IDebugElement) events[i]
                    .getSource();
                
                // TODO allow extensible customization of event handling;
                // integration with user configurable update policies should happen here.
                if(source.getDebugTarget() == getMemoryBlock()
                        .getDebugTarget())
                {
                	if(kind == DebugEvent.SUSPEND && detail == 0)
                	{
	                    Display.getDefault().asyncExec(new Runnable()
	                    {
	                        public void run()
	                        {
	                            refresh();
	                        }
	                    });
                	}
                	else if(kind == DebugEvent.CHANGE)
                	{
	                    Display.getDefault().asyncExec(new Runnable()
	                    {
	                        public void run()
	                        {
	                            refresh();
	                        }
	                    });
                	}
                	else if(kind == DebugEvent.RESUME)
                	{
	                    Display.getDefault().asyncExec(new Runnable()
	                    {
	                        public void run()
	                        {
	                            archiveDeltas();
	                        }
	                    });
                	}
                }
            }
        }
    }

    // return true to enable development debug print statements
    protected boolean isDebug()
    {
        return false;
    }

    protected IMemoryBlockExtension getMemoryBlock()
    {
        IMemoryBlock block = fParent.getMemoryBlock();
        if(block != null)
            return (IMemoryBlockExtension) block
                .getAdapter(IMemoryBlockExtension.class);

        return null;
    }

    protected int getAddressableSize()
    {
    	try
    	{
    		return getMemoryBlock().getAddressableSize();
    	}
    	catch(DebugException e)
    	{
    		return 1;
    	}
    }
    
    protected ViewportCache getViewportCache()
    {
        return fViewportCache;
    }

    protected MemoryByte[] getBytes(BigInteger address, int bytes)
        throws DebugException
    {
        return fViewportCache.getBytes(address, bytes);
    }

    // default visibility for performance
    ViewportCache fViewportCache = new ViewportCache(); 

    private interface Request
    {
    }
    
    class ViewportCache extends Thread
    {
    	class ArchiveDeltas implements Request
    	{
    		
    	}
    	
        class AddressPair implements Request
        {
            BigInteger startAddress;

            BigInteger endAddress;
        }

        class MemoryUnit
        {
            BigInteger start;

            BigInteger end;

            MemoryByte[] bytes;

            public MemoryUnit clone()
            {
                MemoryUnit b = new MemoryUnit();

                b.start = this.start;
                b.end = this.end;
                b.bytes = new MemoryByte[this.bytes.length];
                for(int i = 0; i < this.bytes.length; i++)
                	b.bytes[i] = new MemoryByte(this.bytes[i].getValue());

                return b;
            }

            public boolean isValid()
            {
                return this.start != null && this.end != null
                    && this.bytes != null;
            }
        }

        private HashMap fEditBuffer = new HashMap();

        private boolean fDisposed = false;

        private Vector fQueue = new Vector();

        protected MemoryUnit fCache = null;

        protected MemoryUnit fHistoryCache = null;

        public ViewportCache()
        {
            start();
        }

        public void dispose()
        {
            fDisposed = true;
            synchronized(this)
            {
                this.notify();
            }
        }

        protected void refresh()
        {
            assert Thread.currentThread().equals(
                Display.getDefault().getThread()) : TraditionalRenderingMessages
                .getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

            if(fCache != null)
            {
                queueRequest(fViewportAddress, getViewportEndAddress());
            }
        }
        
        protected void archiveDeltas()
        {
        	assert Thread.currentThread().equals(
                Display.getDefault().getThread()) : TraditionalRenderingMessages
                .getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

            if(fCache != null)
            {
                queueRequestArchiveDeltas();
            }
        }

        
        
        private void queueRequest(BigInteger startAddress, BigInteger endAddress)
        {
            AddressPair pair = new AddressPair();
            pair.startAddress = startAddress;
            pair.endAddress = endAddress;
            synchronized(fQueue)
            {
                fQueue.addElement(pair);
            }
            synchronized(this)
            {
                this.notify();
            }
        }
        
        private void queueRequestArchiveDeltas()
        {
        	ArchiveDeltas archive = new ArchiveDeltas();
        	synchronized(fQueue)
            {
                fQueue.addElement(archive);
            }
            synchronized(this)
            {
                this.notify();
            }
        }

        public void run()
        {
            while(!fDisposed)
            {
                AddressPair pair = null;
                boolean archiveDeltas = false;
                synchronized(fQueue)
                {
                    if(fQueue.size() > 0)
                    {
                    	Request request = (Request) fQueue.elementAt(0);
                    	Class type = null;
                    	if(request instanceof ArchiveDeltas)
                    	{
                    		archiveDeltas = true;
                    		type = ArchiveDeltas.class;
                    	}
                    	else if(request instanceof AddressPair)
                    	{
                    		pair = (AddressPair) request;
                    		type = AddressPair.class;
                    	}
                    	
                    	while(fQueue.size() > 0 && type.isInstance(fQueue.elementAt(0)))
                    		fQueue.removeElementAt(0);
                    }
                }
                if(archiveDeltas)
                {
                    fHistoryCache = fCache.clone();
                }
                else if(pair != null)
                {
                    populateCache(pair.startAddress, pair.endAddress);
                }
                else
                {
                    synchronized(this)
                    {
                        try
                        {
                            this.wait();
                        }
                        catch(Exception e)
                        {
                            // do nothing
                        }
                    }
                }
            }
        }

        // cache memory necessary to paint viewport
        // TODO: user setting to buffer +/- x lines
        // TODO: reuse existing cache? probably only a minor performance gain
        private void populateCache(final BigInteger startAddress,
            final BigInteger endAddress)
        {
            try
            {
                final IMemoryBlockExtension memoryBlock = getMemoryBlock();

                final BigInteger lengthInBytes = endAddress.subtract(startAddress);
                final BigInteger addressableSize = BigInteger.valueOf(getAddressableSize());
                
                final long units = lengthInBytes.divide(addressableSize).add(
                		lengthInBytes.mod(addressableSize).compareTo(BigInteger.ZERO) > 0
                			? BigInteger.ONE : BigInteger.ZERO).longValue();
                
                // CDT (and maybe other backends) will call setValue() on these MemoryBlock objects.
                // We don't want this to happen, because it interferes with this rendering's own
                // change history. Ideally, we should strictly use the back end change notification
                // and history, but it is only guaranteed to work for bytes within the address range
                // of the MemoryBlock. 
                final MemoryByte readBytes[] = memoryBlock
                	.getBytesFromAddress(startAddress, units);
                
                final MemoryByte cachedBytes[] = new MemoryByte[readBytes.length];
                for(int i = 0; i < readBytes.length; i++)
                	cachedBytes[i] = new MemoryByte(readBytes[i].getValue(), readBytes[i].getFlags());

                // we need to set the default endianess.  before it was set to BE
                // by default which wasn't very useful for LE targets.  now we will
                // query the first byte to get the endianess.  if not known then we'll
                // leave it as BE.  note that we only do this when reading the first
                // bit of memory for this rendering.  what happens when scrolling
                // through  memory and it changes endianess?  for now we just leave
                // it in the original endianess.
            	if (!fCheckedLittleEndian && cachedBytes.length > 0) {
                	if (cachedBytes[0].isEndianessKnown()) {
                		fLittleEndian = !cachedBytes[0].isBigEndian();
                    	fCheckedLittleEndian = true;
                    	fParent.bytesAreLittleEndian(fLittleEndian);
                	}
            	}
                
                Display.getDefault().asyncExec(new Runnable()
                {
                    public void run()
                    {
                        // generate deltas
                        if(fHistoryCache != null && fHistoryCache.isValid())
                        {
                            BigInteger maxStart = startAddress
                                .max(fHistoryCache.start);
                            BigInteger minEnd = endAddress
                                .min(fHistoryCache.end).subtract(
                                    BigInteger.valueOf(1));

                            BigInteger overlapLength = minEnd
                                .subtract(maxStart);
                            if(overlapLength.compareTo(BigInteger.valueOf(0)) > 0)
                            {
                                // there is overlap

                                int offsetIntoOld = maxStart.subtract(
                                    fHistoryCache.start).intValue();
                                int offsetIntoNew = maxStart.subtract(
                                    startAddress).intValue();

                                for(int i = overlapLength.intValue(); i >= 0; i--)
                                {
                                	cachedBytes[offsetIntoNew + i]
                                        .setChanged(cachedBytes[offsetIntoNew
                                            + i].getValue() != fHistoryCache.bytes[offsetIntoOld
                                            + i].getValue());
                                }
                            }
                        }

                        fCache = new MemoryUnit();
                        fCache.start = startAddress;
                        fCache.end = endAddress;
                        fCache.bytes = cachedBytes;

                        Rendering.this.redrawPanes();
                    }
                });

            }
            catch(Exception e)
            {
                logError(
                    TraditionalRenderingMessages
                        .getString("TraditionalRendering.FAILURE_READ_MEMORY"), e); //$NON-NLS-1$
            }
        }

        // bytes will be fetched from cache
        protected MemoryByte[] getBytes(BigInteger address, int bytesRequested)
            throws DebugException
        {
            assert Thread.currentThread().equals(
                Display.getDefault().getThread()) : TraditionalRenderingMessages
                .getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

            if(containsEditedCell(address)) // cell size cannot be switched during an edit
                return getEditedMemory(address);

            IMemoryBlockExtension memoryBlock = getMemoryBlock();

            boolean contains = false;
            if(fCache != null && fCache.start != null)
            {
            	// see if all of the data requested is in the cache
            	BigInteger dataEnd = address.add(BigInteger.valueOf(bytesRequested));

                if(fCache.start.compareTo(address) <= 0
                	&& fCache.end.compareTo(dataEnd) >= 0)
                    contains = true;
            }

            if(contains)
            {
                int offset = address.subtract(fCache.start).intValue();
                MemoryByte bytes[] = new MemoryByte[bytesRequested];
                for(int i = 0; i < bytes.length; i++)
                {
                    bytes[i] = fCache.bytes[offset + i];
                }

                return bytes;
            }
            
            MemoryByte bytes[] = new MemoryByte[bytesRequested];
            for(int i = 0; i < bytes.length; i++)
            {
                bytes[i] = new MemoryByte();
                bytes[i].setReadable(false);
            }

            fViewportCache.queueRequest(fViewportAddress,
                getViewportEndAddress());

            return bytes;    
        }

        private boolean containsEditedCell(BigInteger address)
        {
            assert Thread.currentThread().equals(
                Display.getDefault().getThread()) : TraditionalRenderingMessages
                .getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

            return fEditBuffer.containsKey(address);
        }

        private MemoryByte[] getEditedMemory(BigInteger address)
        {
            assert Thread.currentThread().equals(
                Display.getDefault().getThread()) : TraditionalRenderingMessages
                .getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

            return (MemoryByte[]) fEditBuffer.get(address);
        }

        protected void clearEditBuffer()
        {
            assert Thread.currentThread().equals(
                Display.getDefault().getThread()) : TraditionalRenderingMessages
                .getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

            fEditBuffer.clear();
            Rendering.this.redrawPanes();
        }

        protected void writeEditBuffer()
        {
            assert Thread.currentThread().equals(
                Display.getDefault().getThread()) : TraditionalRenderingMessages
                .getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

            Set keySet = fEditBuffer.keySet();
            Iterator iterator = keySet.iterator();

            while(iterator.hasNext())
            {
                try
                {
                    BigInteger address = (BigInteger) iterator.next();
                    MemoryByte[] bytes = (MemoryByte[]) fEditBuffer
                        .get(address);

                    byte byteValue[] = new byte[bytes.length];
                    for(int i = 0; i < bytes.length; i++)
                        byteValue[i] = bytes[i].getValue();

                    getMemoryBlock().setValue(address.subtract(getMemoryBlock().getBigBaseAddress()), byteValue);
                }
                catch(Exception e)
                {
                    logError(
                        TraditionalRenderingMessages
                            .getString("TraditionalRendering.FAILURE_WRITE_MEMORY"), e); //$NON-NLS-1$
                }
            }
            
            clearEditBuffer();
        }

        protected void setEditedValue(BigInteger address, MemoryByte[] bytes)
        {
            assert Thread.currentThread().equals(
                Display.getDefault().getThread()) : TraditionalRenderingMessages
                .getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

            fEditBuffer.put(address, bytes);
            Rendering.this.redrawPanes();
        }
    }

    public void setVisibleAddressBar(boolean visible)
    {
        fAddressBar.setVisible(visible);
        layout(true);
        layoutPanes();
        fAddressBar.layout(true);
        fAddressBar.setText(getAddressString(getViewportStartAddress()));
    }
    
    public void setDirty(boolean needRefresh)
    {
    	fCacheDirty = needRefresh; 	
    }

    public boolean isDirty()
    {
    	return fCacheDirty; 	
    }

    public void dispose()
    {
        if(fViewportCache != null)
        {
            fViewportCache.dispose();
            fViewportCache = null;
        }
        super.dispose();
    }

    class Selection
    {
        private BigInteger fStartHigh;
        private BigInteger fStartLow;

        private BigInteger fEndHigh;
        private BigInteger fEndLow;

        protected void clear()
        {
            fEndHigh = fEndLow = fStartHigh = fStartLow = null;
            redrawPanes();
        }
        
        protected boolean hasSelection()
        {
        	return fStartHigh != null && fStartLow != null
        		&& fEndHigh != null && fEndLow != null;
        }
        
        protected boolean isSelected(BigInteger address)
        {
            // do we have valid start and end addresses
            if(getEnd() == null || getStart() == null)
                return false;
    
            // if end is greater than start
            if(getEnd().compareTo(getStart()) >= 0)
            {
                if(address.compareTo(getStart()) >= 0
                    && address.compareTo(getEnd()) < 0)
                    return true;
            }
            // if start is greater than end
            else if(getStart().compareTo(getEnd()) >= 0)
            {
                if(address.compareTo(getEnd()) >= 0
                    && address.compareTo(getStart()) < 0)
                    return true;
            }
            
            return false;
        }
    
        protected void setStart(BigInteger high, BigInteger low)
        {
            if(high == null && low == null)
            {
                if(fStartHigh != null && fStartLow != null)
                {
                    fStartHigh = null;
                    fStartLow = null;
                    redrawPanes();
                }
    
                return;
            }
    
            boolean changed = false;
            
            if(fStartHigh == null || !high.equals(fStartHigh))
            {
                fStartHigh = high;
                changed = true;
            }
            
            if(fStartLow == null || !low.equals(fStartLow))
            {
                fStartLow = low;
                changed = true;
            }
            
            if(changed)
                redrawPanes();
        }
    
        protected void setEnd(BigInteger high, BigInteger low)
        {
            if(high == null && low == null)
            {
                if(fEndHigh != null && fEndLow != null)
                {
                    fEndHigh = null;
                    fEndLow = null;
                    redrawPanes();
                }
    
                return;
            }
    
            boolean changed = false;
            
            if(fEndHigh == null || !high.equals(fEndHigh))
            {
                fEndHigh = high;
                changed = true;
            }
            
            if(fEndLow == null || !low.equals(fEndLow))
            {
                fEndLow = low;
                changed = true;
            }
            
            if(changed)
                redrawPanes();
        }
    
        protected BigInteger getHigh()
        {
        	if(!hasSelection())
        		return null;
        	
        	return getStart().max(getEnd());
        }
        
        protected BigInteger getLow()
        {
        	if(!hasSelection())
        		return null;
        	
        	return getStart().min(getEnd());
        }
        
        protected BigInteger getStart()
        {
            // if there is no start, return null
            if(fStartHigh == null)
                return null;
            
            // if there is no end, return the high address of the start
            if(fEndHigh == null)
                return fStartHigh;
            
            // if Start High/Low equal End High/Low, return a low start and high end
            if(fStartHigh.equals(fEndHigh) 
                && fStartLow.equals(fEndLow))
                return fStartLow;
            
            BigInteger differenceEndToStartHigh = fEndHigh.subtract(fStartHigh).abs();
            BigInteger differenceEndToStartLow = fEndHigh.subtract(fStartLow).abs();
            
            // return the start high or start low based on which creates a larger selection
            if(differenceEndToStartHigh.compareTo(differenceEndToStartLow) > 0)
                return fStartHigh;
            else 
                return fStartLow;
        }
    
        protected BigInteger getEnd()
        {
            // if there is no end, return null
            if(fEndHigh == null)
                return null;
            
            // if Start High/Low equal End High/Low, return a low start and high end
            if(fStartHigh.equals(fEndHigh) 
                && fStartLow.equals(fEndLow))
                return fStartHigh;
            
            BigInteger differenceStartToEndHigh = fStartHigh.subtract(fEndHigh).abs();
            BigInteger differenceStartToEndLow = fStartHigh.subtract(fEndLow).abs();
            
            // return the start high or start low based on which creates a larger selection
            if(differenceStartToEndHigh.compareTo(differenceStartToEndLow) >= 0)
                return fEndHigh;
            else 
                return fEndLow;
        }
    }
    
    protected void setPaneVisible(int pane, boolean visible)
    {
        switch(pane)
        {
            case PANE_ADDRESS:
                fAddressPane.setPaneVisible(visible);
                break;
            case PANE_BINARY:
                fBinaryPane.setPaneVisible(visible);
                break;
            case PANE_TEXT:
                fTextPane.setPaneVisible(visible);
                break;
        }

        fireSettingsChanged();
        layoutPanes();
    }

    protected boolean getPaneVisible(int pane)
    {
        switch(pane)
        {
            case PANE_ADDRESS:
                return fAddressPane.isPaneVisible();
            case PANE_BINARY:
                return fBinaryPane.isPaneVisible();
            case PANE_TEXT:
                return fTextPane.isPaneVisible();
            default:
                return false;
        }
    }

    protected void packColumns()
    {
        int availableWidth = Rendering.this.getSize().x;

        if(fAddressPane.isPaneVisible())
        {
            availableWidth -= fAddressPane.computeSize(0, 0).x;
            availableWidth -= Rendering.this.getRenderSpacing() * 2;
        }

        int combinedWidth = 0;

        if(fBinaryPane.isPaneVisible())
            combinedWidth += fBinaryPane.getCellWidth();

        if(fTextPane.isPaneVisible())
            combinedWidth += fTextPane.getCellWidth();

        if(combinedWidth == 0)
            fColumnCount = 0;
        else
        {
            fColumnCount = availableWidth / combinedWidth;
            if(fColumnCount == 0)
            	fColumnCount = 1; // paint one column even if only part can show in view
        }
        
        try
        {
        // Update the number of bytes per row;
        // the max and min scroll range and the current thumb nail position.        
        fBytesPerRow = getBytesPerColumn() * getColumnCount();
        BigInteger difference = getMemoryBlockEndAddress().subtract(getMemoryBlockStartAddress()).add(BigInteger.ONE);
        BigInteger maxScrollRange = difference.divide(BigInteger.valueOf(getAddressableCellsPerRow()));
        if(maxScrollRange.multiply(BigInteger.valueOf(getAddressableCellsPerRow())).compareTo(difference) != 0)
        	maxScrollRange = maxScrollRange.add(BigInteger.ONE);
        
        // support targets with an addressable size greater than 1
        maxScrollRange = maxScrollRange.divide(BigInteger.valueOf(getAddressableSize()));
                
        fScrollBar.setMinimum(1);
        fScrollBar.setMaximum(maxScrollRange.intValue());
        fScrollBar.setIncrement(1);
        fScrollBar.setPageIncrement(this.getRowCount()-1);
    	fScrollBar.setToolTipText(Rendering.this.getAddressString(fViewportAddress));
        setCurrentScrollSelection();
        }
        catch(Exception e)
        {
        	// FIXME precautionary
        }
        
        Rendering.this.redraw();
     	Rendering.this.redrawPanes();
    }

    protected AbstractPane[] getRenderingPanes()
    {
        return new AbstractPane[] { fAddressPane, fBinaryPane,
            fTextPane };
    }

    protected int getCellPadding()
    {
        return fCellPadding;
    }

    protected int getRenderSpacing()
    {
        return fPaneSpacing;
    }

    protected void refresh()
    {
    	if(!this.isDisposed())
    	{
    		if(this.isVisible())
    		{
    			fViewportCache.refresh();
    		}
    		else
    		{
    			setDirty(true);
    			fParent.updateRenderingLabels();
    		}
    	}
    }
    
    protected void archiveDeltas()
    {
    	fViewportCache.archiveDeltas();
    }

    protected void gotoAddress(BigInteger address)
    {
    	// Ensure that the GoTo address is within the addressable range
    	if((address.compareTo(this.getMemoryBlockStartAddress())< 0) ||
    	   (address.compareTo(this.getMemoryBlockEndAddress()) > 0))
    	{
    		return;
    	}
    	
        fViewportAddress = address; // TODO update fCaretAddress
        redrawPanes();
    }

    protected void setViewportStartAddress(BigInteger newAddress)
    {
        fViewportAddress = newAddress;
    }
    
    protected BigInteger getViewportStartAddress()
    {
        return fViewportAddress;
    }

    protected BigInteger getViewportEndAddress()
    {
        return fViewportAddress.add(BigInteger.valueOf(this.getBytesPerRow() * getRowCount() / getAddressableSize()));
    }

    protected String getAddressString(BigInteger address)
    {
        StringBuffer addressString = new StringBuffer(address.toString(16)
            .toUpperCase());
        for(int chars = getAddressBytes() * 2 - addressString.length(); chars > 0; chars--)
        {
            addressString.insert(0, '0');
        }
        addressString.insert(0, "0x"); //$NON-NLS-1$

        return addressString.toString();
    }

    private int fAddressBytes = -1; // called often, cache

    protected int getAddressBytes()
    {
        if(fAddressBytes == -1)
        {
            try
            {
                IMemoryBlockExtension block = getMemoryBlock();
                fAddressBytes = block.getAddressSize();
            }
            catch(DebugException e)
            {
                fAddressBytes = 0;
                logError(
                    TraditionalRenderingMessages
                        .getString("TraditionalRendering.FAILURE_DETERMINE_ADDRESS_SIZE"), e); //$NON-NLS-1$
            }
        }

        return fAddressBytes;
    }

    protected int getColumnCount()
    {
        return fColumnCount;
    }

    protected void ensureVisible(BigInteger address)
    {
        BigInteger viewportStart = this.getViewportStartAddress();
        BigInteger viewportEnd = this.getViewportEndAddress();

        boolean isAddressBeforeViewportStart = address.compareTo(viewportStart) < 0;
        boolean isAddressAfterViewportEnd = address.compareTo(viewportEnd) > 0;

        if(isAddressBeforeViewportStart || isAddressAfterViewportEnd)
            gotoAddress(address);
    }

    protected int getRowCount()
    {
        int rowCount = 0;
        Control panes[] = getRenderingPanes();
        for(int i = 0; i < panes.length; i++)
        {
            if(panes[i] instanceof AbstractPane)
                rowCount = Math.max(rowCount,
                    ((AbstractPane) panes[i]).getRowCount());
        }

        return rowCount;
    }

    protected int getBytesPerColumn()
    {
        return fBytesPerColumn;
    }

    protected int getBytesPerRow()
    {
        return fBytesPerRow;
    }
    
    protected int getAddressableCellsPerRow()
    {
    	return getBytesPerRow() / getAddressableSize();
    }
    
    protected int getAddressesPerColumn()
    {
    	return this.getBytesPerColumn() / getAddressableSize();
    }

    /**
	 *  @return Set current scroll selection
	 */
	protected void setCurrentScrollSelection()
	{
		BigInteger selection = getViewportStartAddress().divide(
			BigInteger.valueOf(getAddressableCellsPerRow()).add(BigInteger.ONE));
		fScrollBar.setSelection(selection.intValue());
		fCurrentScrollSelection = selection.intValue();
	}
    
    /**
	 * @return start address of the memory block
	 */
	protected BigInteger getMemoryBlockStartAddress()
	{
		if (fMemoryBlockStartAddress == null)
		{
			try {
				IMemoryBlock memoryBlock = this.getMemoryBlock();
				if(memoryBlock instanceof IMemoryBlockExtension)
				{
					BigInteger startAddress = ((IMemoryBlockExtension)memoryBlock).getMemoryBlockStartAddress();
					if (startAddress != null)
						fMemoryBlockStartAddress =  startAddress;
				}
			} catch (DebugException e) {
				fMemoryBlockStartAddress =  null;			
			}
			
			// default to 0 if we have trouble getting the start address
			if (fMemoryBlockStartAddress == null)
				fMemoryBlockStartAddress =  BigInteger.valueOf(0);
		}
		return fMemoryBlockStartAddress; 
	}
	
	/**
	 * @return end address of the memory block
	 */
	protected BigInteger getMemoryBlockEndAddress()
	{
		if (fMemoryBlockEndAddress == null)
		{
			IMemoryBlock memoryBlock = this.getMemoryBlock();
			if(memoryBlock instanceof IMemoryBlockExtension)
			{
				BigInteger endAddress;
				try {
					endAddress = ((IMemoryBlockExtension)memoryBlock).getMemoryBlockEndAddress();
					if (endAddress != null)
						fMemoryBlockEndAddress = endAddress;
				} catch (DebugException e) {
					fMemoryBlockEndAddress = null;
				}
				
				if (fMemoryBlockEndAddress == null)
				{
					int addressSize;
					try {
						addressSize = ((IMemoryBlockExtension)memoryBlock).getAddressSize();
					} catch (DebugException e) {
						addressSize = 4;
					}
					
					endAddress = BigInteger.valueOf(2);
					endAddress = endAddress.pow(addressSize*8);
					endAddress = endAddress.subtract(BigInteger.valueOf(1));
					fMemoryBlockEndAddress =  endAddress;
				}
			}
			
			// default to MAX_VALUE if we have trouble getting the end address
			if (fMemoryBlockEndAddress == null)
				fMemoryBlockEndAddress = BigInteger.valueOf(Integer.MAX_VALUE);
		}
		return fMemoryBlockEndAddress;
	}

    protected int getRadix()
    {
        return fRadix;
    }

    protected int getNumericRadix(int radix)
    {
        switch(radix)
        {
            case RADIX_BINARY:
                return 2;
            case RADIX_OCTAL:
                return 8;
            case RADIX_DECIMAL_SIGNED:
            case RADIX_DECIMAL_UNSIGNED:
                return 10;
            case RADIX_HEX:
                return 16;
        }

        return -1;
    }

    protected void setRadix(int mode)
    {
        if(fRadix == mode)
            return;

        fRadix = mode;
        fireSettingsChanged();
        layoutPanes();
    }

    protected void setTextMode(int mode)
    {
    	fTextMode = mode;
    	
        fireSettingsChanged();
        layoutPanes();
    }
    
    protected int getTextMode()
    {
    	return fTextMode;
    }
    
    protected String getCharacterSet(int mode)
    {
    	switch(mode)
    	{
    		case Rendering.TEXT_UTF8:
    			return "UTF8";
    		case Rendering.TEXT_UTF16:
    			return "UTF16";
    		case Rendering.TEXT_USASCII:
    			return "US-ASCII";
    		case Rendering.TEXT_ISO_8859_1:
    		default:
    			return "ISO-8859-1";
    	}
    }

    protected int getBytesPerCharacter()
    {
    	if(fTextMode == Rendering.TEXT_UTF16)
    		return 2;
    		
        return 1;
    }

    protected boolean isLittleEndian()
    {
        return fLittleEndian;
    }

    protected void setLittleEndian(boolean enable)
    {
        if(fLittleEndian == enable)
            return;

        fLittleEndian = enable;
        fireSettingsChanged();
        layoutPanes();
    }

    protected void setBytesPerColumn(int byteCount)
    {
        if(fBytesPerColumn != byteCount)
        {
            fBytesPerColumn = byteCount;
            fireSettingsChanged();
            layoutPanes();
        }
    }

    protected void redrawPanes()
    {
    	if(this.isVisible())
    	{
	        if(fAddressPane.isPaneVisible())
	        {
	            fAddressPane.redraw();
	            fAddressPane.setRowCount();
	            if(fAddressPane.isFocusControl())
	                fAddressPane.updateCaret();
	        }
	
	        if(fBinaryPane.isPaneVisible())
	        {
	            fBinaryPane.redraw();
	            fBinaryPane.setRowCount();
	            if(fBinaryPane.isFocusControl())
	                fBinaryPane.updateCaret();
	        }
	
	        if(fTextPane.isPaneVisible())
	        {
	            fTextPane.redraw();
	            fTextPane.setRowCount();
	            if(fTextPane.isFocusControl())
	                fTextPane.updateCaret();
	        }
    	}
    	
    	fParent.updateRenderingLabels();
    }

    private void layoutPanes()
    {
        packColumns();
        layout(true);

        redraw();
        redrawPanes();
    }

    private void fireSettingsChanged()
    {
        fAddressPane.settingsChanged();
        fBinaryPane.settingsChanged();
        fTextPane.settingsChanged();
    }

    static final char[] hexdigits = { '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    
    protected String getRadixText(MemoryByte bytes[], int radix,
            boolean isLittleEndian)
    {
        boolean readable = true;
        for(int i = 0; i < bytes.length; i++)
            if(!bytes[i].isReadable())
                readable = false;

        if(readable)
        {
            // see if we need to swap the data or not.  if the bytes are BE
            // and we want to view in LE then we need to swap.  if the bytes
            // are LE and we want to view BE then we need to swap.
            boolean needsSwap = false;
            boolean bytesAreLittleEndian = !bytes[0].isBigEndian();
            if ((isLittleEndian && !bytesAreLittleEndian) || (!isLittleEndian && bytesAreLittleEndian))
            	needsSwap = true;

            switch(radix)
            {
                case Rendering.RADIX_HEX:
                case Rendering.RADIX_OCTAL:
                case Rendering.RADIX_BINARY:
                {
    				long value = 0;
                    if(needsSwap)
                    {
                        for(int i = 0; i < bytes.length; i++)
                        {
                            value = value << 8;
                            value = value
                                | (bytes[bytes.length - 1 - i].getValue() & 0xFF);
                        }
                    }
                    else
                    {
                        for(int i = 0; i < bytes.length; i++)
                        {
                            value = value << 8;
                            value = value | (bytes[i].getValue() & 0xFF);
                        }
                    }

                    char buf[] = new char[getRadixCharacterCount(radix,
                        bytes.length)];

                    switch(radix)
                    {
	                    case Rendering.RADIX_BINARY:
	                    {
	                        for(int i = buf.length - 1; i >= 0; i--)
	                        {
	                            buf[i] = hexdigits[(int) (value & 1)];
	                            value = value >>> 1;
	                        }
	                        break;
	                    }
	                    case Rendering.RADIX_OCTAL:
                        {
                            for(int i = buf.length - 1; i >= 0; i--)
                            {
                                buf[i] = hexdigits[(int) (value & 7)];
                                value = value >>> 3;
                            }
                            break;
                        }
                        case Rendering.RADIX_HEX:
                        {
                            for(int i = buf.length - 1; i >= 0; i--)
                            {
                                buf[i] = hexdigits[(int) (value & 15)];
                                value = value >>> 4;
                            }
                            break;
                        }
                    }

                    return new String(buf);
                }
                case Rendering.RADIX_DECIMAL_UNSIGNED:
                case Rendering.RADIX_DECIMAL_SIGNED:
                {
                    boolean isSignedType = radix == Rendering.RADIX_DECIMAL_SIGNED ? true
                        : false;

                    int textWidth = getRadixCharacterCount(radix, bytes.length);

                    char buf[] = new char[textWidth];

                    byte[] value = new byte[bytes.length + 1];

    				if(needsSwap)
                    {
                        for(int i = 0; i < bytes.length; i++)
                        {
                            value[bytes.length - i] = bytes[i].getValue();
                        }
                    }
                    else
                    {
                        for(int i = 0; i < bytes.length; i++)
                        {
                            value[i + 1] = bytes[i].getValue();
                        }
                    }
                    
                    BigInteger bigValue;
                    boolean isNegative = false;
                    if(isSignedType && (value[1] & 0x80) != 0)
                    {
                        value[0] = -1;
                        isNegative = true;
                        bigValue = new BigInteger(value).abs();
                    }
                    else
                    {
                        value[0] = 0;
                        bigValue = new BigInteger(value);
                    }
                    
                    for(int i = 0; i < textWidth; i++)
                    {
                        BigInteger divideRemainder[] = bigValue.divideAndRemainder(
                        	BigInteger.valueOf(10));
                        int remainder = divideRemainder[1].intValue();
                        buf[textWidth - 1 - i] = hexdigits[remainder % 10];
                        bigValue = divideRemainder[0];
                    }
                    
                    if(isSignedType)
                    {
                        buf[0] = isNegative ? '-' : ' ';
                    }

                    return new String(buf);
                }
            }
        }

        StringBuffer errorText = new StringBuffer();
        for(int i = getRadixCharacterCount(radix, bytes.length); i > 0; i--)
            errorText.append('?');

        return errorText.toString();
    }

    protected int getRadixCharacterCount(int radix, int bytes)
    {
        switch(radix)
        {
            case Rendering.RADIX_HEX:
                return bytes * 2;
            case Rendering.RADIX_BINARY:
                return bytes * 8;
            case Rendering.RADIX_OCTAL:
            {
                switch(bytes)
                {
                    case 1:
                        return 3;
                    case 2:
                        return 6;
                    case 4:
                        return 11;
                    case 8:
                        return 22;
                }
            }
            case Rendering.RADIX_DECIMAL_UNSIGNED:
            {
                switch(bytes)
                {
                    case 1:
                        return 3;
                    case 2:
                        return 5;
                    case 4:
                        return 10;
                    case 8:
                        return 20;
                }
            }
            case Rendering.RADIX_DECIMAL_SIGNED:
            {
                switch(bytes)
                {
                    case 1:
                        return 4;
                    case 2:
                        return 6;
                    case 4:
                        return 11;
                    case 8:
                        return 21;
                }
            }
        }

        return 0;
    }

    protected static boolean isValidEditCharacter(char character)
    {
        return (character >= '0' && character <= '9')
            || (character >= 'a' && character <= 'z')
            || (character >= 'A' && character <= 'Z') || character == '-'
            || character == ' ';
    }
    
    protected String formatText(MemoryByte[] memoryBytes,
        boolean isLittleEndian, int textMode)
    {
    	// check memory byte for unreadable bytes
    	boolean readable = true;
        for(int i = 0; i < memoryBytes.length; i++)
            if(!memoryBytes[i].isReadable())
                readable = false;
        
        // if any bytes are not readable, return ?'s
        if(!readable)
        {
            StringBuffer errorText = new StringBuffer();
            for(int i = memoryBytes.length; i > 0; i--)
                errorText.append('?');
            return errorText.toString();
        }

        // TODO
        // does endian mean anything for text? ah, unicode?
        
        // create byte array from MemoryByte array
        byte bytes[] = new byte[memoryBytes.length];
        for(int i = 0; i < bytes.length; i++)
        {
            bytes[i] = memoryBytes[i].getValue();
        }
        
        // replace invalid characters with '.'
        // maybe there is a way to query the character set for
        // valid characters?
        
        // replace invalid US-ASCII with '.'
        if(textMode == Rendering.TEXT_USASCII)
        {
        	for(int i = 0; i < bytes.length; i++)
        	{
        		int byteValue = bytes[i];
        		if(byteValue < 0)
        			byteValue += 256;
        		
        		if(byteValue < 0x20 || byteValue > 0x7e)
        			bytes[i] = '.';
        	}
        }
        
        // replace invalid ISO-8859-1 with '.'
        if(textMode == Rendering.TEXT_ISO_8859_1)
        {
        	for(int i = 0; i < bytes.length; i++)
        	{
        		int byteValue = bytes[i];
        		if(byteValue < 0)
        			byteValue += 256;
        		
        		if(byteValue < 0x20 || 
        				(byteValue >= 0x7f && byteValue < 0x9f))
        			bytes[i] = '.';
        	}
        }
        
        try
    	{
        	// convert bytes to string using desired character set
    		StringBuffer buf = new StringBuffer(new String(bytes, this.getCharacterSet(textMode)));
    		
    		// pad string to (byte count - string length) with spaces
    		for(int i = 0; i < memoryBytes.length - buf.length(); i++)
    			buf.append(' ');
    		return buf.toString();
    	}
    	catch(Exception e)
    	{
    		// return ?s the length of byte count
    		StringBuffer buf = new StringBuffer();
    		for(int i = 0; i < memoryBytes.length - buf.length(); i++)
    			buf.append('?');
    		return buf.toString();
    	}   
    }

}

class CopyAction extends Action
{
    // TODO for the sake of large copies, this action should probably read in
    // blocks on a Job.

    private Rendering fRendering;

    public CopyAction(Rendering rendering)
    {
        super();
        fRendering = rendering;
        setText(DebugUIMessages.CopyViewToClipboardAction_title);
        setToolTipText(DebugUIMessages.CopyViewToClipboardAction_tooltip);
        setImageDescriptor(DebugPluginImages
            .getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_COPY_VIEW_TO_CLIPBOARD));
        setHoverImageDescriptor(DebugPluginImages
            .getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_COPY_VIEW_TO_CLIPBOARD));
        setDisabledImageDescriptor(DebugPluginImages
            .getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_COPY_VIEW_TO_CLIPBOARD));
    }

    public void run()
    {
        final int COLUMNS = 80; // FIXME user preference
        final String PANE_SPACING = "  "; // preference also ? //$NON-NLS-1$

        Clipboard clip = null;
        try
        {
            clip = new Clipboard(fRendering.getDisplay());

            BigInteger start = fRendering.getSelection().getStart();
            BigInteger end = fRendering.getSelection().getEnd();
            // end will be null when there is nothing selected
            if (end == null)
            	return;
            
            if(start.compareTo(end) > 0)
            {
                // swap start and end

                BigInteger bigI = end;
                end = start;
                start = bigI;
            }

            final int radix = fRendering.getRadix();
            final int bytesPerColumn = fRendering.getBytesPerColumn();
            final boolean isLittleEndian = fRendering.isLittleEndian();
            final int bytesPerCharacter = fRendering.getBytesPerCharacter();

            final int addressWidth = fRendering.getAddressString(start)
                .length();

            final int binaryCellWidth = fRendering.getRadixCharacterCount(
                radix, bytesPerColumn) + 1;

            final int asciiCellWidth = fRendering.getBytesPerColumn()
                / fRendering.getBytesPerCharacter();

            final int combindCellWidths = (fRendering
                .getPaneVisible(Rendering.PANE_BINARY) ? binaryCellWidth : 0)
                + (fRendering.getPaneVisible(Rendering.PANE_TEXT) ? asciiCellWidth
                    : 0);

            int availableWidth = COLUMNS;

            if(fRendering.getPaneVisible(Rendering.PANE_ADDRESS))
            {
                availableWidth -= addressWidth;
                availableWidth -= PANE_SPACING.length(); // between address
                // and next
            }

            if(fRendering.getPaneVisible(Rendering.PANE_BINARY)
                && fRendering.getPaneVisible(Rendering.PANE_TEXT))
            {
                availableWidth -= PANE_SPACING.length(); // between binary
                // and text
            }

            final int columns = availableWidth / combindCellWidths;

            BigInteger lengthToRead = end.subtract(start);

            int rows = lengthToRead.divide(
                BigInteger.valueOf(columns * bytesPerColumn)).intValue();

            if(rows * columns * bytesPerColumn < lengthToRead.intValue())
                rows++;

            StringBuffer buffer = new StringBuffer();

            for(int row = 0; row < rows; row++)
            {
                BigInteger rowAddress = start.add(BigInteger.valueOf(row
                    * columns * bytesPerColumn));

                if(fRendering.getPaneVisible(Rendering.PANE_ADDRESS))
                {
                    buffer.append(fRendering.getAddressString(rowAddress));
                    buffer.append(PANE_SPACING);
                }

                if(fRendering.getPaneVisible(Rendering.PANE_BINARY))
                {
                    for(int col = 0; col < columns; col++)
                    {
                        BigInteger cellAddress = rowAddress.add(BigInteger
                            .valueOf(col * bytesPerColumn));

                        if(cellAddress.compareTo(end) < 0)
                        {
                            try
                            {
                                MemoryByte bytes[] = fRendering.getBytes(
                                    cellAddress, bytesPerColumn);
                                buffer.append(fRendering.getRadixText(bytes,
                                    radix, isLittleEndian));
                            }
                            catch(DebugException de)
                            {
                                fRendering
                                    .logError(
                                        TraditionalRenderingMessages
                                            .getString("TraditionalRendering.FAILURE_COPY_OPERATION"), de); //$NON-NLS-1$
                                return;
                            }
                        }
                        else
                        {
                            for(int i = fRendering.getRadixCharacterCount(
                                radix, bytesPerColumn); i > 0; i--)
                                buffer.append(' ');
                        }

                        if(col != columns - 1)
                            buffer.append(' ');
                    }
                }

                if(fRendering.getPaneVisible(Rendering.PANE_BINARY)
                    && fRendering.getPaneVisible(Rendering.PANE_TEXT))
                {
                    buffer.append(PANE_SPACING);
                }

                if(fRendering.getPaneVisible(Rendering.PANE_TEXT))
                {
                    for(int col = 0; col < columns; col++)
                    {
                        BigInteger cellAddress = rowAddress.add(BigInteger
                            .valueOf(col * fRendering.getBytesPerColumn()));

                        if(cellAddress.compareTo(end) < 0)
                        {
                            try
                            {
                                MemoryByte bytes[] = fRendering
                                    .getBytes(cellAddress, fRendering
                                        .getBytesPerColumn());
                                buffer.append(fRendering.formatText(bytes,
                                    isLittleEndian, fRendering.getTextMode()));
                            }
                            catch(DebugException de)
                            {
                                fRendering
                                    .logError(
                                        TraditionalRenderingMessages
                                            .getString("TraditionalRendering.FAILURE_COPY_OPERATION"), de); //$NON-NLS-1$
                                return;
                            }
                        }
                        else
                        {

                        }
                    }
                }

                buffer.append("\n"); //$NON-NLS-1$
            }

            if(buffer.length() > 0)
            {
                TextTransfer plainTextTransfer = TextTransfer.getInstance();
                clip.setContents(new Object[] { buffer.toString() },
                    new Transfer[] { plainTextTransfer });
            }
        }
        finally
        {
            if(clip != null)
            {
                clip.dispose();
            }
        }
    }
}