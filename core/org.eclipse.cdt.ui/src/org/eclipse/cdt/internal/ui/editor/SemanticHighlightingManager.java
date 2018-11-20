/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - Adapted for CDT
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.parser.scanner.CharArray;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingReconciler.AbstractPositionCollector;
import org.eclipse.cdt.internal.ui.text.CPresentationReconciler;
import org.eclipse.cdt.internal.ui.text.CSourceViewerScalableConfiguration;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.CSourceViewerConfiguration;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.IColorManager;
import org.eclipse.cdt.ui.text.ISemanticToken;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * Semantic highlighting manager.
 * Cloned from JDT.
 *
 * @since 4.0
 */
public class SemanticHighlightingManager implements IPropertyChangeListener {
	/**
	 * Highlighting style.
	 */
	public static class HighlightingStyle {
		/** Text attribute */
		private TextAttribute fTextAttribute;
		/** Enabled state */
		private boolean fIsEnabled;

		/**
		 * Initialize with the given text attribute.
		 * @param textAttribute The text attribute
		 * @param isEnabled the enabled state
		 */
		public HighlightingStyle(TextAttribute textAttribute, boolean isEnabled) {
			setTextAttribute(textAttribute);
			setEnabled(isEnabled);
		}

		/**
		 * @return Returns the text attribute.
		 */
		public TextAttribute getTextAttribute() {
			return fTextAttribute;
		}

		/**
		 * @param textAttribute The background to set.
		 */
		public void setTextAttribute(TextAttribute textAttribute) {
			fTextAttribute = textAttribute;
		}

		/**
		 * @return the enabled state
		 */
		public boolean isEnabled() {
			return fIsEnabled;
		}

		/**
		 * @param isEnabled the new enabled state
		 */
		public void setEnabled(boolean isEnabled) {
			fIsEnabled = isEnabled;
		}
	}

	/**
	 * Highlighted Positions.
	 */
	public static class HighlightedPosition extends Position {
		/** Highlighting of the position */
		private HighlightingStyle fStyle;

		/** Lock object */
		private Object fLock;

		/**
		 * Initialize the styled positions with the given offset, length and foreground color.
		 *
		 * @param offset The position offset
		 * @param length The position length
		 * @param highlighting The position's highlighting
		 * @param lock The lock object
		 */
		public HighlightedPosition(int offset, int length, HighlightingStyle highlighting, Object lock) {
			super(offset, length);
			fStyle = highlighting;
			fLock = lock;
		}

		/**
		 * @return Returns a corresponding style range.
		 */
		public StyleRange createStyleRange() {
			int len = 0;
			if (fStyle.isEnabled())
				len = getLength();

			TextAttribute textAttribute = fStyle.getTextAttribute();
			int style = textAttribute.getStyle();
			int fontStyle = style & (SWT.ITALIC | SWT.BOLD | SWT.NORMAL);
			StyleRange styleRange = new StyleRange(getOffset(), len, textAttribute.getForeground(),
					textAttribute.getBackground(), fontStyle);
			styleRange.strikeout = (style & TextAttribute.STRIKETHROUGH) != 0;
			styleRange.underline = (style & TextAttribute.UNDERLINE) != 0;

			return styleRange;
		}

		/**
		 * Uses reference equality for the highlighting.
		 *
		 * @param off The offset
		 * @param len The length
		 * @param highlighting The highlighting
		 * @return <code>true</code> iff the given offset, length and highlighting are equal to the internal ones.
		 */
		public boolean isEqual(int off, int len, HighlightingStyle highlighting) {
			synchronized (fLock) {
				return !isDeleted() && getOffset() == off && getLength() == len && fStyle == highlighting;
			}
		}

		/**
		 * Is this position contained in the given range (inclusive)? Synchronizes on position updater.
		 *
		 * @param off The range offset
		 * @param len The range length
		 * @return <code>true</code> iff this position is not delete and contained in the given range.
		 */
		public boolean isContained(int off, int len) {
			synchronized (fLock) {
				return !isDeleted() && off <= getOffset() && off + len >= getOffset() + getLength();
			}
		}

		public void update(int off, int len) {
			synchronized (fLock) {
				super.setOffset(off);
				super.setLength(len);
			}
		}

		@Override
		public void setLength(int length) {
			synchronized (fLock) {
				super.setLength(length);
			}
		}

		@Override
		public void setOffset(int offset) {
			synchronized (fLock) {
				super.setOffset(offset);
			}
		}

		@Override
		public void delete() {
			synchronized (fLock) {
				super.delete();
			}
		}

		@Override
		public void undelete() {
			synchronized (fLock) {
				super.undelete();
			}
		}

		/**
		 * @return Returns the highlighting.
		 */
		public HighlightingStyle getHighlighting() {
			return fStyle;
		}
	}

	/**
	 * Highlighted ranges.
	 */
	public static class HighlightedRange extends Region {
		/** The highlighting key as returned by {@link SemanticHighlighting#getPreferenceKey()}. */
		private String fKey;

		/**
		 * Initialize with the given offset, length and highlighting key.
		 *
		 * @param offset
		 * @param length
		 * @param key the highlighting key as returned by {@link SemanticHighlighting#getPreferenceKey()}
		 */
		public HighlightedRange(int offset, int length, String key) {
			super(offset, length);
			fKey = key;
		}

		/**
		 * @return the highlighting key as returned by {@link SemanticHighlighting#getPreferenceKey()}
		 */
		public String getKey() {
			return fKey;
		}

		/*
		 * @see org.eclipse.jface.text.Region#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			return super.equals(o) && o instanceof HighlightedRange && fKey.equals(((HighlightedRange) o).getKey());
		}

		/*
		 * @see org.eclipse.jface.text.Region#hashCode()
		 */
		@Override
		public int hashCode() {
			return super.hashCode() | fKey.hashCode();
		}
	}

	/** Semantic highlighting presenter */
	protected SemanticHighlightingPresenter fPresenter;
	/** Semantic highlighting reconciler */
	private SemanticHighlightingReconciler fReconciler;

	/** Semantic highlightings */
	protected SemanticHighlighting[] fSemanticHighlightings;
	/** Highlightings */
	protected HighlightingStyle[] fHighlightings;

	/** The editor */
	private CEditor fEditor;
	/** The source viewer */
	protected CSourceViewer fSourceViewer;
	/** The color manager */
	protected IColorManager fColorManager;
	/** The preference store */
	protected IPreferenceStore fPreferenceStore;
	/** The source viewer configuration */
	protected CSourceViewerConfiguration fConfiguration;
	/** The presentation reconciler */
	protected CPresentationReconciler fPresentationReconciler;

	/** Library declarations used by the previewer widget in the preferences */
	private String fPreviewerLibraryDecls;

	/**
	 * Install the semantic highlighting on the given editor infrastructure
	 *
	 * @param editor The C editor
	 * @param sourceViewer The source viewer
	 * @param colorManager The color manager
	 * @param preferenceStore The preference store
	 */
	public void install(CEditor editor, CSourceViewer sourceViewer, IColorManager colorManager,
			IPreferenceStore preferenceStore) {
		fEditor = editor;
		fSourceViewer = sourceViewer;
		fColorManager = colorManager;
		fPreferenceStore = preferenceStore;
		if (fEditor != null) {
			fConfiguration = new CSourceViewerScalableConfiguration(colorManager, preferenceStore, editor,
					ICPartitions.C_PARTITIONING);
			fPresentationReconciler = (CPresentationReconciler) fConfiguration.getPresentationReconciler(sourceViewer);
		} else {
			fConfiguration = null;
			fPresentationReconciler = null;
		}

		fPreferenceStore.addPropertyChangeListener(this);

		if (isEnabled())
			enable();
	}

	/**
	 * Installs the semantic highlighting on the given source viewer infrastructure.
	 * No reconciliation will be performed.
	 *
	 * This is used for highlighting the code in the previewer window in the preferences.
	 *
	 * @param sourceViewer the source viewer
	 * @param colorManager the color manager
	 * @param preferenceStore the preference store
	 * @param previewerLibraryDecls library declarations required for the previewer code to be valid
	 */
	public void install(CSourceViewer sourceViewer, IColorManager colorManager, IPreferenceStore preferenceStore,
			String previewerLibraryDecls) {
		fPreviewerLibraryDecls = previewerLibraryDecls;
		install(null, sourceViewer, colorManager, preferenceStore);
	}

	/**
	 * Enable semantic highlighting.
	 */
	private void enable() {
		initializeHighlightings();

		fPresenter = new SemanticHighlightingPresenter();
		fPresenter.install(fSourceViewer, fPresentationReconciler);

		if (fEditor != null) {
			fReconciler = new SemanticHighlightingReconciler();
			fReconciler.install(fEditor, fSourceViewer, fPresenter, fSemanticHighlightings, fHighlightings);
		} else {
			fPresenter.updatePresentation(null, computePreviewerPositions(), new HighlightedPosition[0]);
		}
	}

	/**
	 * Computes the positions for the preview code.
	 */
	protected HighlightedPosition[] computePreviewerPositions() {
		// Before parsing and coloring the preview code, prepend library declarations
		// required to make it valid.
		CharArray previewCode = new CharArray(fPreviewerLibraryDecls + fSourceViewer.getDocument().get());

		// Parse the preview code.
		ILanguage language = GPPLanguage.getDefault();
		FileContent content = new InternalFileContent("<previewer>", previewCode); //$NON-NLS-1$
		IScannerInfo scanInfo = new ScannerInfo();
		IncludeFileContentProvider fileCreator = IncludeFileContentProvider.getEmptyFilesProvider();
		IParserLogService log = ParserUtil.getParserLogService();
		IASTTranslationUnit tu = null;
		try {
			tu = language.getASTTranslationUnit(content, scanInfo, fileCreator, null, 0, log);
		} catch (CoreException e) {
			CUIPlugin.log(e);
			return new HighlightedPosition[] {};
		}

		// Correct highlighting of external SDK references requires an index-based AST.
		// Since we don't have an index-based AST here, we swap out the external SDK
		// highlighting with a custom one that recognizes certain hardcoded functions
		// that are present in the preview code.
		List<SemanticHighlighting> highlightings = new ArrayList<>();
		for (SemanticHighlighting highlighting : fSemanticHighlightings) {
			if (SemanticHighlightings.isExternalSDKHighlighting(highlighting)) {
				highlightings.add(new PreviewerExternalSDKHighlighting());
			} else {
				highlightings.add(highlighting);
			}
		}

		// Compute the highlighted positions for the preview code.
		PreviewerPositionCollector collector = new PreviewerPositionCollector(
				highlightings.toArray(new SemanticHighlighting[highlightings.size()]), fHighlightings);
		tu.accept(collector);
		List<HighlightedPosition> positions = collector.getPositions();

		// Since the code that was parsed and colored included library declarations as
		// a prefix, the offsets in the highlighted positions reflect offsets in the
		// library declarations + preview code. Since what we're actually showing is
		// the preview code without the library declarations, adjust the offsets
		// accordingly.
		int libraryDeclsLen = fPreviewerLibraryDecls.length();
		List<HighlightedPosition> adjustedPositions = new ArrayList<>();
		for (HighlightedPosition position : positions) {
			if (position.offset >= libraryDeclsLen) {
				position.offset -= libraryDeclsLen;
				adjustedPositions.add(position);
			}
		}

		return adjustedPositions.toArray(new HighlightedPosition[adjustedPositions.size()]);
	}

	// A custom version of the highlighting for external SDK functions, for use
	// by the previewer. Just highlights names that match a hardcoded list of
	// SDK functions that appear in the previewer code.
	private static class PreviewerExternalSDKHighlighting extends SemanticHighlightingWithOwnPreference {
		static private final Set<String> fHarcodedSDKFunctions;
		static {
			fHarcodedSDKFunctions = new HashSet<>();
			fHarcodedSDKFunctions.add("fprintf"); //$NON-NLS-1$
			// add others as necessary
		}

		@Override
		public boolean consumes(ISemanticToken token) {
			IASTNode node = token.getNode();
			if (!(node instanceof IASTName)) {
				return false;
			}
			String name = new String(((IASTName) node).getSimpleID());
			return fHarcodedSDKFunctions.contains(name);
		}

		// These methods aren't used by PositionCollector.
		@Override
		public RGB getDefaultDefaultTextColor() {
			return null;
		}

		@Override
		public String getDisplayName() {
			return null;
		}

		@Override
		public String getPreferenceKey() {
			return null;
		}

		@Override
		public boolean isEnabledByDefault() {
			return false;
		}
	}

	// Simple implementation of AbstractPositionCollector for the previewer.
	private class PreviewerPositionCollector extends AbstractPositionCollector {
		private List<HighlightedPosition> fPositions = new ArrayList<>();

		public PreviewerPositionCollector(SemanticHighlighting[] highlightings,
				HighlightingStyle[] highlightingStyles) {
			super(highlightings, highlightingStyles);
		}

		@Override
		protected void addPosition(int offset, int length, HighlightingStyle highlightingStyle) {
			fPositions.add(fPresenter.createHighlightedPosition(offset, length, highlightingStyle));
		}

		public List<HighlightedPosition> getPositions() {
			return fPositions;
		}
	}

	/**
	 * Uninstalls the semantic highlighting
	 */
	public void uninstall() {
		disable();

		if (fPreferenceStore != null) {
			fPreferenceStore.removePropertyChangeListener(this);
			fPreferenceStore = null;
		}

		fEditor = null;
		fSourceViewer = null;
		fColorManager = null;
		fConfiguration = null;
		fPresentationReconciler = null;
	}

	/**
	 * Disables semantic highlighting.
	 */
	private void disable() {
		if (fReconciler != null) {
			fReconciler.uninstall();
			fReconciler = null;
		}

		if (fPresenter != null) {
			fPresenter.uninstall();
			fPresenter = null;
		}

		if (fSemanticHighlightings != null)
			disposeHighlightings();
	}

	/**
	 * @return <code>true</code> iff semantic highlighting is enabled in the preferences
	 */
	protected boolean isEnabled() {
		return SemanticHighlightings.isEnabled(fPreferenceStore);
	}

	/**
	 * Initializes semantic highlightings.
	 */
	protected void initializeHighlightings() {
		fSemanticHighlightings = SemanticHighlightings.getSemanticHighlightings();
		fHighlightings = new HighlightingStyle[fSemanticHighlightings.length];

		for (int i = 0, n = fSemanticHighlightings.length; i < n; i++) {
			SemanticHighlighting semanticHighlighting = fSemanticHighlightings[i];
			String colorKey = SemanticHighlightings.getColorPreferenceKey(semanticHighlighting);
			addColor(colorKey);

			String boldKey = SemanticHighlightings.getBoldPreferenceKey(semanticHighlighting);
			int style = fPreferenceStore.getBoolean(boldKey) ? SWT.BOLD : SWT.NORMAL;

			String italicKey = SemanticHighlightings.getItalicPreferenceKey(semanticHighlighting);
			if (fPreferenceStore.getBoolean(italicKey))
				style |= SWT.ITALIC;

			String strikethroughKey = SemanticHighlightings.getStrikethroughPreferenceKey(semanticHighlighting);
			if (fPreferenceStore.getBoolean(strikethroughKey))
				style |= TextAttribute.STRIKETHROUGH;

			String underlineKey = SemanticHighlightings.getUnderlinePreferenceKey(semanticHighlighting);
			if (fPreferenceStore.getBoolean(underlineKey))
				style |= TextAttribute.UNDERLINE;

			boolean isEnabled = fPreferenceStore
					.getBoolean(SemanticHighlightings.getEnabledPreferenceKey(semanticHighlighting));

			fHighlightings[i] = new HighlightingStyle(
					new TextAttribute(fColorManager.getColor(PreferenceConverter.getColor(fPreferenceStore, colorKey)),
							null, style),
					isEnabled);
		}
	}

	/**
	 * Disposes the semantic highlightings.
	 */
	protected void disposeHighlightings() {
		for (int i = 0, n = fSemanticHighlightings.length; i < n; i++)
			removeColor(SemanticHighlightings.getColorPreferenceKey(fSemanticHighlightings[i]));

		fSemanticHighlightings = null;
		fHighlightings = null;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		handlePropertyChangeEvent(event);
	}

	/**
	 * Handle the given property change event
	 *
	 * @param event The event
	 * @return whether a refresh is needed
	 */
	protected boolean handlePropertyChangeEvent(PropertyChangeEvent event) {
		if (fPreferenceStore == null)
			return false; // Uninstalled during event notification

		if (fConfiguration != null)
			fConfiguration.handlePropertyChangeEvent(event);

		if (SemanticHighlightings.affectsEnablement(fPreferenceStore, event)) {
			if (isEnabled())
				enable();
			else
				disable();
		}

		if (!isEnabled())
			return false;

		boolean refreshNeeded = false;

		for (int i = 0, n = fSemanticHighlightings.length; i < n; i++) {
			SemanticHighlighting semanticHighlighting = fSemanticHighlightings[i];

			String colorKey = SemanticHighlightings.getColorPreferenceKey(semanticHighlighting);
			if (colorKey.equals(event.getProperty())) {
				adaptToTextForegroundChange(fHighlightings[i], event);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded = true;
				continue;
			}

			String boldKey = SemanticHighlightings.getBoldPreferenceKey(semanticHighlighting);
			if (boldKey.equals(event.getProperty())) {
				adaptToTextStyleChange(fHighlightings[i], event, SWT.BOLD);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded = true;
				continue;
			}

			String italicKey = SemanticHighlightings.getItalicPreferenceKey(semanticHighlighting);
			if (italicKey.equals(event.getProperty())) {
				adaptToTextStyleChange(fHighlightings[i], event, SWT.ITALIC);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded = true;
				continue;
			}

			String strikethroughKey = SemanticHighlightings.getStrikethroughPreferenceKey(semanticHighlighting);
			if (strikethroughKey.equals(event.getProperty())) {
				adaptToTextStyleChange(fHighlightings[i], event, TextAttribute.STRIKETHROUGH);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded = true;
				continue;
			}

			String underlineKey = SemanticHighlightings.getUnderlinePreferenceKey(semanticHighlighting);
			if (underlineKey.equals(event.getProperty())) {
				adaptToTextStyleChange(fHighlightings[i], event, TextAttribute.UNDERLINE);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded = true;
				continue;
			}

			String enabledKey = SemanticHighlightings.getEnabledPreferenceKey(semanticHighlighting);
			if (enabledKey.equals(event.getProperty())) {
				adaptToEnablementChange(fHighlightings[i], event);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded = true;
				continue;
			}
		}

		if (refreshNeeded && fReconciler != null)
			fReconciler.refresh();

		return refreshNeeded;
	}

	protected void adaptToEnablementChange(HighlightingStyle highlighting, PropertyChangeEvent event) {
		Object value = event.getNewValue();
		boolean eventValue;
		if (value instanceof Boolean)
			eventValue = ((Boolean) value).booleanValue();
		else if (IPreferenceStore.TRUE.equals(value))
			eventValue = true;
		else
			eventValue = false;
		highlighting.setEnabled(eventValue);
	}

	protected void adaptToTextForegroundChange(HighlightingStyle highlighting, PropertyChangeEvent event) {
		RGB rgb = null;

		Object value = event.getNewValue();
		if (value instanceof RGB)
			rgb = (RGB) value;
		else if (value instanceof String)
			rgb = StringConverter.asRGB((String) value);

		if (rgb != null) {

			String property = event.getProperty();
			Color color = fColorManager.getColor(property);

			if ((color == null || !rgb.equals(color.getRGB()))) {
				fColorManager.unbindColor(property);
				fColorManager.bindColor(property, rgb);
				color = fColorManager.getColor(property);
			}

			TextAttribute oldAttr = highlighting.getTextAttribute();
			highlighting.setTextAttribute(new TextAttribute(color, oldAttr.getBackground(), oldAttr.getStyle()));
		}
	}

	protected void adaptToTextStyleChange(HighlightingStyle highlighting, PropertyChangeEvent event,
			int styleAttribute) {
		boolean eventValue = false;
		Object value = event.getNewValue();
		if (value instanceof Boolean)
			eventValue = ((Boolean) value).booleanValue();
		else if (IPreferenceStore.TRUE.equals(value))
			eventValue = true;

		TextAttribute oldAttr = highlighting.getTextAttribute();
		boolean activeValue = (oldAttr.getStyle() & styleAttribute) == styleAttribute;

		if (activeValue != eventValue)
			highlighting.setTextAttribute(new TextAttribute(oldAttr.getForeground(), oldAttr.getBackground(),
					eventValue ? oldAttr.getStyle() | styleAttribute : oldAttr.getStyle() & ~styleAttribute));
	}

	private void addColor(String colorKey) {
		if (fColorManager != null && colorKey != null && fColorManager.getColor(colorKey) == null) {
			RGB rgb = PreferenceConverter.getColor(fPreferenceStore, colorKey);
			fColorManager.unbindColor(colorKey);
			fColorManager.bindColor(colorKey, rgb);
		}
	}

	private void removeColor(String colorKey) {
		fColorManager.unbindColor(colorKey);
	}

	/**
	 * Forces refresh of highlighting.
	 */
	public void refresh() {
		if (fReconciler != null) {
			fReconciler.refresh();
		}
	}
}
