/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Ed Swartz (Nokia)
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IPositionConverter;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.model.ext.ICElementHandle;

import org.eclipse.cdt.internal.ui.util.EditorUtility;

/**
 * An utility to open editors for references or elements.
 * @author markus.schorn@windriver.com
 */
public class EditorOpener {
	
	/**
	 * Opens the editor selecting the given region.
	 */
	public static void open(IWorkbenchPage page, IFile file, IRegion region, long timestamp) {
        IEditorPart editor= null;
        if (timestamp == 0) {
        	timestamp= file.getLocalTimeStamp();
        }
        try {
        	editor= IDE.openEditor(page, file, false);
        } catch (PartInitException e) {
        	CUIPlugin.getDefault().log(e);
        }
        selectRegion(file.getFullPath(), region, timestamp, editor);
	}

	private static void selectRegion(IPath filebufferKey, IRegion region, long timestamp, IEditorPart editor) {
		if (editor instanceof ITextEditor) {
            ITextEditor te= (ITextEditor) editor;
            IPositionConverter pc= CCorePlugin.getPositionTrackerManager().findPositionConverter(filebufferKey, timestamp);
            if (pc != null) {
            	region= pc.historicToActual(region);
            }
            te.selectAndReveal(region.getOffset(), region.getLength());
        }
	}

	/**
	 * Opens the editor for an external location, selecting the given region.
	 */
	public static void openExternalFile(IWorkbenchPage page, IPath location, IRegion region, long timestamp) {
		IEditorPart editor= null;
		try {
			editor= EditorUtility.openInEditor(location, null);
	        if (timestamp == 0) {
	        	timestamp= location.toFile().lastModified();
	        }
	        selectRegion(location, region, timestamp, editor);
		} catch (PartInitException e) {
			CUIPlugin.getDefault().log(e);
		}
	}

	/**
	 * Opens the editor for an ICElement, selecting the id.
	 * @throws CModelException 
	 */
	public static void open(IWorkbenchPage page, ICElement element) throws CModelException {
		if (element instanceof ISourceReference) {
			ISourceReference sr= (ISourceReference) element;
			ITranslationUnit tu= sr.getTranslationUnit();
			ISourceRange range= sr.getSourceRange();
			long timestamp= 0; // last modified of file.
			if (tu.isWorkingCopy() || element instanceof ICElementHandle) {
				timestamp= -1; 
			}
			open(page, tu, new Region(range.getIdStartPos(), range.getIdLength()), timestamp);
		}
	}

	/**
	 * Opens the editor for an ICElement, selecting the given region.
	 */
	public static void open(IWorkbenchPage page, ITranslationUnit tu, Region region, long timestamp) {
		if (tu != null) {
			IResource r= tu.getResource();
			if (r instanceof IFile) {
				EditorOpener.open(page, (IFile) r, region, timestamp);
			}
			else {
				IPath location= tu.getPath();
				if (location != null) {
					EditorOpener.openExternalFile(page, location, region, timestamp);
				}
			}
		}
	}
}
