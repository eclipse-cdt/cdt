/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlExtension;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.ICPartitions;

import org.eclipse.cdt.internal.ui.editor.CSourceViewer;

/**
 * Abstract class for "quick" views in light-weight controls.
 *
 * @since 4.0
 */
public abstract class AbstractSourceViewerInformationControl extends PopupDialog implements IInformationControl, IInformationControlExtension, IInformationControlExtension2, DisposeListener {

	private int fTextStyle;
	
	private ISourceViewer fSourceViewer;

	private Color fBackgroundColor;

	private boolean fIsSystemBackgroundColor;

	private int fMaxWidth;

	private int fMaxHeight;

	private List fColorExclusionControls= new ArrayList();
	
	/**
	 * Creates a source viewer information control with the given shell as parent. The given
	 * styles are applied to the shell and the source viewer.
	 *
	 * @param parent  the parent shell
	 * @param shellStyle  the additional styles for the shell
	 * @param textStyle  the additional styles for the source viewer
	 * @param takeFocus  flag indicating whether to take the focus
	 * @param showViewMenu  flag indicating whether to show the "view" menu
	 * @param persistBounds  flag indicating whether control size and location should be persisted
	 */
	public AbstractSourceViewerInformationControl(Shell parent, int shellStyle, int textStyle, boolean takeFocus, boolean showViewMenu, boolean persistBounds) {
		super(parent, shellStyle, takeFocus, persistBounds, showViewMenu, false, null, null);
		fTextStyle= textStyle;
		// Title and status text must be set to get the title label created, so force empty values here. 
		if (hasHeader())
			setTitleText(""); //$NON-NLS-1$
		setInfoText(""); //  //$NON-NLS-1$

		// Create all controls
		create();
	}

	private void initializeColors() {
		RGB bgRGB= getHoverBackgroundColorRGB();
		if (bgRGB != null) {
			fBackgroundColor= new Color(getShell().getDisplay(), bgRGB);
			fIsSystemBackgroundColor= false;
		} else {
			fBackgroundColor= getShell().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
			fIsSystemBackgroundColor= true;
		}
	}
	
	private RGB getHoverBackgroundColorRGB() {
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(PreferenceConstants.EDITOR_SOURCE_HOVER_BACKGROUND_COLOR_SYSTEM_DEFAULT)
			? null
			: PreferenceConverter.getColor(store, PreferenceConstants.EDITOR_SOURCE_HOVER_BACKGROUND_COLOR);
	}

	/*
	 * @see org.eclipse.jface.dialogs.PopupDialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Control contents= super.createContents(parent);
		for (Iterator it= fColorExclusionControls.iterator(); it.hasNext(); ) {
			Control ctrl = (Control) it.next();
			ctrl.setBackground(fBackgroundColor);
		}
		return contents;
	}
	
	/**
	 * Create the main content for this information control.
	 * 
	 * @param parent The parent composite
	 * @return The control representing the main content.
	 * 
	 */
	protected Control createDialogArea(Composite parent) {
		fSourceViewer= createSourceViewer(parent, fTextStyle);

		final StyledText text= fSourceViewer.getTextWidget();
		text.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e)  {
				if (e.character == 0x1B) // ESC
					dispose();
			}
			public void keyReleased(KeyEvent e) {
				// do nothing
			}
		});

		addDisposeListener(this);
		return ((Viewer)fSourceViewer).getControl();
	}
	
	protected ISourceViewer createSourceViewer(Composite parent, int style) {
		IPreferenceStore store= CUIPlugin.getDefault().getCombinedPreferenceStore();
		SourceViewer sourceViewer= new CSourceViewer(parent, null, null, false, style, store);
		CTextTools tools= CUIPlugin.getDefault().getTextTools();
		sourceViewer.configure(new SimpleCSourceViewerConfiguration(tools.getColorManager(), store, null, ICPartitions.C_PARTITIONING, false));
		sourceViewer.setEditable(false);

		StyledText styledText= sourceViewer.getTextWidget();
		GridData gd= new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
		styledText.setLayoutData(gd);
		initializeColors();
		styledText.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		styledText.setBackground(fBackgroundColor);
		fColorExclusionControls.add(styledText);
		
		Font font= JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT);
		styledText.setFont(font);

		return sourceViewer;
	}

	/*
	 * @see org.eclipse.jface.dialogs.PopupDialog#createInfoTextArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createInfoTextArea(Composite parent) {
		Control infoText= super.createInfoTextArea(parent);
		fColorExclusionControls.add(infoText);
		return infoText;
	}
	
	/*
	 * @see org.eclipse.jface.dialogs.PopupDialog#createTitleControl(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createTitleControl(Composite parent) {
		Control titleText= super.createTitleControl(parent);
		fColorExclusionControls.add(titleText);
		return titleText;
	}

	/*
	 * @see org.eclipse.jface.dialogs.PopupDialog#getBackgroundColorExclusions()
	 */
	protected List getBackgroundColorExclusions() {
		List exclusions= super.getBackgroundColorExclusions();
		exclusions.addAll(fColorExclusionControls);
		return exclusions;
	}

	/*
	 * @see org.eclipse.jface.dialogs.PopupDialog#getForegroundColorExclusions()
	 */
	protected List getForegroundColorExclusions() {
		List exclusions= super.getForegroundColorExclusions();
		exclusions.addAll(fColorExclusionControls);
		return exclusions;
	}
	
	/**
	 * Returns the name of the dialog settings section.
	 * <p>
	 * The default is to return <code>null</code>.
	 * </p>
	 * @return the name of the dialog settings section or <code>null</code> if
	 *         nothing should be persisted
	 */
	protected String getId() {
		return null;
	}

	/**
	 * Returns the source viewer.
	 * 
	 * @return the source viewer.
	 */
	protected final ISourceViewer getSourceViewer() {
		return fSourceViewer;
	}

	/**
	 * Returns <code>true</code> if the control has a header, <code>false</code> otherwise.
	 * <p>
	 * The default is to return <code>false</code>.
	 * </p>
	 * 
	 * @return <code>true</code> if the control has a header
	 */
	protected boolean hasHeader() {
		// default is to have no header
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInformation(String content) {
		if (content == null) {
			fSourceViewer.setDocument(null);
			return;
		}

		IDocument doc= new Document(content);
		CUIPlugin.getDefault().getTextTools().setupCDocument(doc);
		fSourceViewer.setDocument(doc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInput(Object input) {
		if (input instanceof String)
			setInformation((String)input);
		else
			setInformation(null);
	}

	/**
	 * Fills the view menu.
	 * Clients can extend or override.
	 *
	 * @param viewMenu the menu manager that manages the menu
	 */
	protected void fillViewMenu(IMenuManager viewMenu) {
	}

	/*
	 * @see org.eclipse.jface.dialogs.PopupDialog#fillDialogMenu(IMenuManager)
	 */
	protected void fillDialogMenu(IMenuManager dialogMenu) {
		super.fillDialogMenu(dialogMenu);
		fillViewMenu(dialogMenu);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setVisible(boolean visible) {
		if (visible) {
			open();
		} else {
			saveDialogBounds(getShell());
			getShell().setVisible(false);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void dispose() {
		if (!fIsSystemBackgroundColor) {
			fBackgroundColor.dispose();
		}
		close();
	}

	protected Point getInitialLocation(Point initialSize) {
		if (!getPersistBounds()) {
			Point size = new Point(400, 400);
			Rectangle parentBounds = getParentShell().getBounds();
			int x = parentBounds.x + parentBounds.width / 2 - size.x / 2;
			int y = parentBounds.y + parentBounds.height / 2 - size.y / 2;
			return new Point(x, y);
		}
		return super.getInitialLocation(initialSize);
	}

	/**
	 * {@inheritDoc}
	 * @param event can be null
	 * <p>
	 * Subclasses may extend.
	 * </p>
	 */
	public void widgetDisposed(DisposeEvent event) {
		fSourceViewer= null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasContents() {
		return fSourceViewer != null && fSourceViewer.getDocument() != null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSizeConstraints(int maxWidth, int maxHeight) {
		fMaxWidth= maxWidth;
		fMaxHeight= maxHeight;
	}

	/**
	 * {@inheritDoc}
	 */
	public Point computeSizeHint() {
		// compute the preferred size
		int x= SWT.DEFAULT;
		int y= SWT.DEFAULT;
		Point size= getShell().computeSize(x, y);
		if (size.x > fMaxWidth)
			x= fMaxWidth;
		if (size.y > fMaxHeight)
			y= fMaxHeight;

		// recompute using the constraints if the preferred size is larger than the constraints
		if (x != SWT.DEFAULT || y != SWT.DEFAULT)
			size= getShell().computeSize(x, y, false);

		return size;
//		return getShell().getSize();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLocation(Point location) {
		if (!getPersistBounds() || getDialogSettings() == null)
			getShell().setLocation(location);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSize(int width, int height) {
		if (!getPersistBounds() || getDialogSettings() == null) {
			getShell().setSize(width, height);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addDisposeListener(DisposeListener listener) {
		getShell().addDisposeListener(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeDisposeListener(DisposeListener listener) {
		getShell().removeDisposeListener(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setForegroundColor(Color foreground) {
		applyForegroundColor(foreground, getContents());
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBackgroundColor(Color background) {
		applyBackgroundColor(background, getContents());
	}

	/*
	 * @see org.eclipse.jface.dialogs.PopupDialog#getFocusControl()
	 */
	protected Control getFocusControl() {
		return fSourceViewer.getTextWidget();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isFocusControl() {
		return fSourceViewer.getTextWidget().isFocusControl();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFocus() {
		getShell().forceFocus();
		fSourceViewer.getTextWidget().setFocus();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addFocusListener(FocusListener listener) {
		getShell().addFocusListener(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeFocusListener(FocusListener listener) {
		getShell().removeFocusListener(listener);
	}

	/*
	 * @see org.eclipse.jface.dialogs.PopupDialog#getDialogSettings()
	 */
	protected IDialogSettings getDialogSettings() {
		String sectionName= getId();
		if (sectionName == null) {
			return null;
		}
		IDialogSettings settings= CUIPlugin.getDefault().getDialogSettings().getSection(sectionName);
		if (settings == null)
			settings= CUIPlugin.getDefault().getDialogSettings().addNewSection(sectionName);

		return settings;
	}
}
