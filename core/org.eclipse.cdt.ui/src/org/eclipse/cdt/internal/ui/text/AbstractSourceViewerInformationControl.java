/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.ICPartitions;

import org.eclipse.cdt.internal.ui.editor.CSourceViewer;
import org.eclipse.cdt.internal.ui.text.c.hover.SourceViewerInformationControl;

/**
 * Abstract class for "quick" source views in light-weight controls.
 *
 * @since 5.0
 */
public abstract class AbstractSourceViewerInformationControl extends org.eclipse.jface.text.AbstractInformationControl implements IInformationControlExtension2, DisposeListener {

	private ISourceViewer fSourceViewer;
	private Color fBackgroundColor;
	private boolean fIsSystemBackgroundColor = true;
	private Font fTextFont;
	private StyledText fText;
	private Label fTitleLabel;

	/**
	 * Creates a source viewer information control with the given shell as parent. The given
	 * styles are applied to the shell and the source viewer.
	 *
	 * @param parent  the parent shell
	 * @param statusFieldText
	 */
	public AbstractSourceViewerInformationControl(Shell parent, String statusFieldText) {
		super(parent, statusFieldText);
		// Create all controls
		create();
	}

	/**
	 * Creates a source viewer information control with the given shell as parent. The given
	 * styles are applied to the shell and the source viewer.
	 *
	 * @param parent  the parent shell
	 * @param isResizable  whether the control should be resizable
	 */
	public AbstractSourceViewerInformationControl(Shell parent, boolean isResizable) {
		super(parent, isResizable);
		// Create all controls
		create();
	}

	/**
	 * @return <code>true</code> if the control should have a title label
	 */
	protected boolean hasHeader() {
		return false;
	}

	private void initializeColors() {
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		RGB bgRGB;
		if (store.getBoolean(PreferenceConstants.EDITOR_SOURCE_HOVER_BACKGROUND_COLOR_SYSTEM_DEFAULT)) {
			bgRGB= SourceViewerInformationControl.getVisibleBackgroundColor(getShell().getDisplay());
		} else {
			bgRGB= PreferenceConverter.getColor(store, PreferenceConstants.EDITOR_SOURCE_HOVER_BACKGROUND_COLOR);
		}
		if (bgRGB != null) {
			fBackgroundColor= new Color(getShell().getDisplay(), bgRGB);
			fIsSystemBackgroundColor= false;
		} else {
			fBackgroundColor= getShell().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
			fIsSystemBackgroundColor= true;
		}
	}
	
	@Override
	public void createContent(Composite parent) {
		Composite content= new Composite(parent, SWT.NONE);
		final GridLayout gridLayout= new GridLayout();
		gridLayout.marginWidth= 0;
		gridLayout.marginHeight= 0;
		gridLayout.verticalSpacing= 0;
		content.setLayout(gridLayout);

		if (hasHeader()) {
			createTitleLabel(content);
		}
		fSourceViewer= createSourceViewer(content, SWT.NONE);

		final StyledText text= fSourceViewer.getTextWidget();
		text.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e)  {
				if (e.character == 0x1B) // ESC
					dispose();
			}
			@Override
			public void keyReleased(KeyEvent e) {
				// do nothing
			}
		});

		addDisposeListener(this);
	}
	
	protected final ISourceViewer createSourceViewer(Composite parent, int style) {
		IPreferenceStore store= CUIPlugin.getDefault().getCombinedPreferenceStore();
		SourceViewer sourceViewer= new CSourceViewer(parent, null, null, false, style, store);
		CTextTools tools= CUIPlugin.getDefault().getTextTools();
		sourceViewer.configure(new SimpleCSourceViewerConfiguration(tools.getColorManager(), store, null, ICPartitions.C_PARTITIONING, false));
		sourceViewer.setEditable(false);

		fText= sourceViewer.getTextWidget();
		GridData gd= new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
		fText.setLayoutData(gd);
		initializeColors();
		fText.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		fText.setBackground(fBackgroundColor);
		
		fTextFont= JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT);
		fText.setFont(fTextFont);

		return sourceViewer;
	}

	private void createTitleLabel(Composite parent) {
		fTitleLabel= new Label(parent, SWT.LEFT);
		fTitleLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label separator= new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fTitleLabel.setFont(JFaceResources.getDialogFont());

		Display display= parent.getDisplay();
		Color foreground= display.getSystemColor(SWT.COLOR_INFO_FOREGROUND);
		Color background= display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		fTitleLabel.setForeground(foreground);
		fTitleLabel.setBackground(background);
	}

	public void setTitleText(String titleText) {
		if (fTitleLabel != null) {
			fTitleLabel.setText(titleText);
		}
	}

	/**
	 * Returns the source viewer.
	 * 
	 * @return the source viewer.
	 */
	protected final ISourceViewer getSourceViewer() {
		return fSourceViewer;
	}

	@Override
	public void setInformation(String content) {
		if (content == null) {
			fSourceViewer.setDocument(null);
			return;
		}

		IDocument doc= new Document(content);
		CUIPlugin.getDefault().getTextTools().setupCDocument(doc);
		fSourceViewer.setDocument(doc);
	}

	@Override
	public void setInput(Object input) {
		if (input instanceof String)
			setInformation((String)input);
		else
			setInformation(null);
	}

	@Override
	public final void dispose() {
		if (!fIsSystemBackgroundColor) {
			fBackgroundColor.dispose();
		}
		super.dispose();
	}

	/**
	 * {@inheritDoc}
	 * @param event can be null
	 * <p>
	 * Subclasses may extend.
	 * </p>
	 */
	@Override
	public void widgetDisposed(DisposeEvent event) {
		fSourceViewer= null;
	}

	@Override
	public boolean hasContents() {
		return fSourceViewer != null && fSourceViewer.getDocument() != null;
	}

	@Override
	public void setFocus() {
		super.setFocus();
		fSourceViewer.getTextWidget().setFocus();
	}

	@Override
	public Point computeSizeConstraints(int widthInChars, int heightInChars) {
		GC gc= new GC(fText);
		gc.setFont(fTextFont);
		int width= gc.getFontMetrics().getAverageCharWidth();
		int height= gc.getFontMetrics().getHeight();
		gc.dispose();

		return new Point(widthInChars * width, heightInChars * height);
	}

	@Override
	public Point computeSizeHint() {
		// compute the preferred size
		int x= SWT.DEFAULT;
		int y= SWT.DEFAULT;
		Point size= getShell().computeSize(x, y);
		Point constraints= getSizeConstraints();
		if (constraints != null) {
			if (size.x > constraints.x)
				x= constraints.x;
			if (size.y > constraints.y)
				y= constraints.y;
		}
		// recompute using the constraints if the preferred size is larger than the constraints
		if (x != SWT.DEFAULT || y != SWT.DEFAULT)
			size= getShell().computeSize(x, y, false);

		return size;
	}
}
