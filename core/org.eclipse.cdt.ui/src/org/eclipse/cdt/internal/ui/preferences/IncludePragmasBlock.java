/*******************************************************************************
 * Copyright (c) 2013, 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * The preference block for configuring Organize Includes command.
 */
public class IncludePragmasBlock extends OptionsConfigurationBlock {
	private static final Key KEY_EXPORT_PATTERN = getCDTCoreKey(CCorePreferenceConstants.INCLUDE_EXPORT_PATTERN);
	private static final Key KEY_BEGIN_EXPORTS_PATTERN = getCDTCoreKey(
			CCorePreferenceConstants.INCLUDE_BEGIN_EXPORTS_PATTERN);
	private static final Key KEY_END_EXPORTS_PATTERN = getCDTCoreKey(
			CCorePreferenceConstants.INCLUDE_END_EXPORTS_PATTERN);
	private static final Key KEY_PRIVATE_PATTERN = getCDTCoreKey(CCorePreferenceConstants.INCLUDE_PRIVATE_PATTERN);
	private static final Key KEY_KEEP_PATTERN = getCDTCoreKey(CCorePreferenceConstants.INCLUDE_KEEP_PATTERN);

	private static Key[] ALL_KEYS = { KEY_EXPORT_PATTERN, KEY_BEGIN_EXPORTS_PATTERN, KEY_END_EXPORTS_PATTERN,
			KEY_PRIVATE_PATTERN, KEY_KEEP_PATTERN, };
	private PixelConverter pixelConverter;

	public IncludePragmasBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		super(context, project, ALL_KEYS, container);
	}

	@Override
	protected Control createContents(Composite parent) {
		setShell(parent.getShell());
		pixelConverter = new PixelConverter(parent);

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		Control control = createHeader(composite);
		LayoutUtil.setHorizontalSpan(control, 3);

		control = addTextField(composite, PreferencesMessages.IncludePragmasBlock_export_pattern, KEY_EXPORT_PATTERN, 0,
				pixelConverter.convertWidthInCharsToPixels(40));
		LayoutUtil.setHorizontalGrabbing(control, true);
		control = addTextField(composite, PreferencesMessages.IncludePragmasBlock_begin_exports_pattern,
				KEY_BEGIN_EXPORTS_PATTERN, 0, pixelConverter.convertWidthInCharsToPixels(40));
		LayoutUtil.setHorizontalGrabbing(control, true);
		control = addTextField(composite, PreferencesMessages.IncludePragmasBlock_end_exports_pattern,
				KEY_END_EXPORTS_PATTERN, 0, pixelConverter.convertWidthInCharsToPixels(40));
		LayoutUtil.setHorizontalGrabbing(control, true);
		control = addTextField(composite, PreferencesMessages.IncludePragmasBlock_private_pattern, KEY_PRIVATE_PATTERN,
				0, pixelConverter.convertWidthInCharsToPixels(40));
		LayoutUtil.setHorizontalGrabbing(control, true);
		control = addTextField(composite, PreferencesMessages.IncludePragmasBlock_keep_pattern, KEY_KEEP_PATTERN, 0,
				pixelConverter.convertWidthInCharsToPixels(40));
		LayoutUtil.setHorizontalGrabbing(control, true);

		updateControls();
		return composite;
	}

	private Control createHeader(Composite parent) {
		String text = PreferencesMessages.IncludePragmasBlock_description;
		Link link = new Link(parent, SWT.NONE);
		link.setText(text);
		link.addListener(SWT.Selection, event -> BusyIndicator.showWhile(null, () -> {
			try {
				URL url = new URL(event.text);
				IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
				IWebBrowser browser = browserSupport.getExternalBrowser();
				browser.openURL(url);
			} catch (PartInitException e1) {
				// TODO(sprigogin): Should we show an error dialog?
				CUIPlugin.log(e1.getStatus());
			} catch (MalformedURLException e2) {
				CUIPlugin.log(e2);
			}
		}));
		// TODO replace by link-specific tooltips when
		// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=88866 is fixed
		link.setToolTipText(PreferencesMessages.IncludePragmasBlock_link_tooltip);

		GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		// Only expand further if anyone else requires it
		gridData.widthHint = pixelConverter.convertWidthInCharsToPixels(40);
		link.setLayoutData(gridData);
		return link;
	}

	@Override
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
		StatusInfo status = new StatusInfo();
		fContext.statusChanged(status);
	}
}
