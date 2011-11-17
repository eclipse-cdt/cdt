/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     IBM Corporation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.parser.CodeReaderCache;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;

/**
 * This OptionPage is used in the IndexerPreference page to allow for adjusting
 * various parsing related caches.
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CacheSizeBlock extends AbstractCOptionPage {
	private IntegerFieldEditor fDBLimitPct;
	private IntegerFieldEditor fDBAbsoluteLimit;
	private IntegerFieldEditor fCodeReaderLimit;

    private IPropertyChangeListener validityChangeListener = new IPropertyChangeListener() {
        @Override
		public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().equals(FieldEditor.IS_VALID)) {
				updateValidState();
			}
        }
    };

    public CacheSizeBlock(ICOptionContainer container){
    	setContainer(container);
    }

    @Override
	public void createControl(Composite parent) {
    	PixelConverter pixelConverter =  new PixelConverter(parent);
    	GridData gd;
    	GridLayout gl;
        Composite composite = ControlFactory.createComposite(parent, 1);
		gl=  (GridLayout)composite.getLayout();
		gl.marginWidth= 0;

		gd= (GridData) composite.getLayoutData();
		gd.grabExcessHorizontalSpace= false;
		gd.horizontalAlignment= GridData.FILL;

		setControl(composite);

		Group group= ControlFactory.createGroup(composite, DialogsMessages.CacheSizeBlock_cacheLimitGroup, 1);
		gd= (GridData) group.getLayoutData();
		gd.grabExcessHorizontalSpace= true;
		gd.horizontalAlignment= GridData.FILL;

		Composite cacheComp= ControlFactory.createComposite(group, 3);

		Label dbCacheLabel= ControlFactory.createLabel(cacheComp, DialogsMessages.CacheSizeBlock_indexDatabaseCache);
		fDBLimitPct= new IntegerFieldEditor(CCorePreferenceConstants.INDEX_DB_CACHE_SIZE_PCT, DialogsMessages.CacheSizeBlock_limitRelativeToMaxHeapSize, cacheComp, 3);
		fDBLimitPct.setValidRange(1, 75);
		Text control = fDBLimitPct.getTextControl(cacheComp);
		LayoutUtil.setWidthHint(control, pixelConverter.convertWidthInCharsToPixels(10));
		LayoutUtil.setHorizontalGrabbing(control, false);
		ControlFactory.createLabel(cacheComp, "%"); //$NON-NLS-1$

		fDBAbsoluteLimit= new IntegerFieldEditor(CCorePreferenceConstants.MAX_INDEX_DB_CACHE_SIZE_MB, DialogsMessages.CacheSizeBlock_absoluteLimit, cacheComp, 4);
		fDBAbsoluteLimit.setValidRange(1, 10000);
		control = fDBAbsoluteLimit.getTextControl(cacheComp);
		LayoutUtil.setWidthHint(control, pixelConverter.convertWidthInCharsToPixels(10));
		LayoutUtil.setHorizontalGrabbing(control, false);
		ControlFactory.createLabel(cacheComp, DialogsMessages.CacheSizeBlock_MB);

		Label codeReaderLabel= ControlFactory.createLabel(cacheComp, DialogsMessages.CacheSizeBlock_headerFileCache);
		fCodeReaderLimit= new IntegerFieldEditor(CodeReaderCache.CODE_READER_BUFFER, DialogsMessages.CacheSizeBlock_absoluteLimit, cacheComp, 4);
		fCodeReaderLimit.setValidRange(1, 10000);
		control = fCodeReaderLimit.getTextControl(cacheComp);
		LayoutUtil.setWidthHint(control, pixelConverter.convertWidthInCharsToPixels(10));
		LayoutUtil.setHorizontalGrabbing(control, false);
		ControlFactory.createLabel(cacheComp, DialogsMessages.CacheSizeBlock_MB);

		gl= (GridLayout) cacheComp.getLayout();
		gl.numColumns= 3;
		gl.makeColumnsEqualWidth= false;
		gl.marginLeft= 0;
		gl.verticalSpacing= 2;

		gd= (GridData) dbCacheLabel.getLayoutData();
		gd.horizontalSpan= 3;

		int vindent= pixelConverter.convertHeightInCharsToPixels(1) / 2;
		gd= (GridData) codeReaderLabel.getLayoutData();
		gd.verticalIndent= vindent;
		gd.horizontalSpan= 3;

		int hindent= pixelConverter.convertWidthInCharsToPixels(2);
		gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.horizontalIndent= hindent;
		fDBLimitPct.getLabelControl(cacheComp).setLayoutData(gd);

		gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.horizontalIndent= hindent;
		fDBAbsoluteLimit.getLabelControl(cacheComp).setLayoutData(gd);

		gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.horizontalIndent= hindent;
		fCodeReaderLimit.getLabelControl(cacheComp).setLayoutData(gd);

		initializeValues();
    }

    private void initializeValues() {
    	ScopedPreferenceStore prefStore= new ScopedPreferenceStore(InstanceScope.INSTANCE, CCorePlugin.PLUGIN_ID);

    	fDBLimitPct.setPreferenceStore(prefStore);
    	fDBLimitPct.setPropertyChangeListener(validityChangeListener);
    	fDBAbsoluteLimit.setPreferenceStore(prefStore);
    	fDBAbsoluteLimit.setPropertyChangeListener(validityChangeListener);
    	fCodeReaderLimit.setPreferenceStore(prefStore);
    	fCodeReaderLimit.setPropertyChangeListener(validityChangeListener);

		fDBLimitPct.load();
		fDBAbsoluteLimit.load();
		fCodeReaderLimit.load();
	}

	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
		fDBLimitPct.store();
		fDBAbsoluteLimit.store();
		fCodeReaderLimit.store();

		// code reader cache does not listen for pref-changes, help out:
		ICodeReaderCache cache = CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_SAVED_RESOURCES).getCodeReaderCache();
		if (cache instanceof CodeReaderCache) {
			((CodeReaderCache) cache).setCacheSize(fCodeReaderLimit.getIntValue());
		}
    }

    @Override
	public void performDefaults() {
    	fDBLimitPct.loadDefault();
		fDBAbsoluteLimit.loadDefault();
		fCodeReaderLimit.loadDefault();
    }

    private void updateValidState() {
    	if (!fDBLimitPct.isValid()) {
    		setErrorMessage(fDBLimitPct.getErrorMessage());
    		setValid(false);
    	} else if (!fDBAbsoluteLimit.isValid()) {
    		setErrorMessage(fDBAbsoluteLimit.getErrorMessage());
    		setValid(false);
    	} else if (!fCodeReaderLimit.isValid()) {
    		setErrorMessage(fCodeReaderLimit.getErrorMessage());
    		setValid(false);
    	} else {
    		setValid(true);
    	}
        getContainer().updateContainer();
    }
}
