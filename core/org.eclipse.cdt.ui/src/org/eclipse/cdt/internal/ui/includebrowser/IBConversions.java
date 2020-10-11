/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.includebrowser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.editors.text.ILocationProvider;

public class IBConversions {

	public static IBNode selectionToNode(ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) sel;
			for (Iterator<?> iter = ssel.iterator(); iter.hasNext();) {
				Object o = iter.next();
				if (o instanceof IBNode) {
					IBNode node = (IBNode) o;
					return node;
				}
			}
		}
		return null;
	}

	public static ITranslationUnit selectionToTU(ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) sel;
			for (Iterator<?> iter = ssel.iterator(); iter.hasNext();) {
				ITranslationUnit tu = objectToTU(iter.next());
				if (tu != null) {
					return tu;
				}
			}
		}
		return null;
	}

	public static ISelection nodeSelectionToRepresentedTUSelection(ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) sel;
			ArrayList<ITranslationUnit> tus = new ArrayList<>();
			for (Iterator<?> iter = ssel.iterator(); iter.hasNext();) {
				Object obj = iter.next();
				if (obj instanceof IBNode) {
					ITranslationUnit tu = ((IBNode) obj).getRepresentedTranslationUnit();
					if (tu != null) {
						tus.add(tu);
					}
				}
			}
			return new StructuredSelection(tus);
		}
		return StructuredSelection.EMPTY;
	}

	/**
	 * Returns an optional ITranslationUnit for the given IFile.
	 * The ITranslationUnit can be outside a source folder.
	 */
	public static Optional<ITranslationUnit> fileToTU(IFile file) {
		ITranslationUnit tu = CoreModelUtil.findTranslationUnit(file);
		if (tu == null) {
			// Handle header not under a source folder
			ICProject cproject = CoreModel.getDefault().create(file.getProject());
			if (cproject != null) {
				String contentTypeId = CoreModel.getRegistedContentTypeId(file.getProject(), file.getName());
				if (contentTypeId != null) {
					tu = new TranslationUnit(cproject, file, contentTypeId);
				}
			}
		}
		return Optional.ofNullable(tu);
	}

	public static ITranslationUnit objectToTU(Object object) {
		if (object instanceof ITranslationUnit) {
			return (ITranslationUnit) object;
		}
		if (object instanceof IFile) {
			return fileToTU((IFile) object).orElse(null);
		}
		if (object instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) object;
			ITranslationUnit result = adaptable.getAdapter(ITranslationUnit.class);
			if (result != null) {
				return result;
			}
			IFile file = adaptable.getAdapter(IFile.class);
			if (file != null) {
				return fileToTU(file).orElse(null);
			}

			ILocationProvider locProvider = adaptable.getAdapter(ILocationProvider.class);
			if (locProvider != null) {
				IPath path = locProvider.getPath(locProvider);
				if (path != null) {
					try {
						return CoreModelUtil.findTranslationUnitForLocation(path, null);
					} catch (CModelException e) {
						CUIPlugin.log(e);
					}
				}
			}
		}
		return null;
	}
}
