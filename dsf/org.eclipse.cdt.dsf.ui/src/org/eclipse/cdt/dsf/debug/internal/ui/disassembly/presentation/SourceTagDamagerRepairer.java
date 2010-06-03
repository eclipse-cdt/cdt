/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.presentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.internal.ui.editor.SemanticHighlightings;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.IColorManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 *
 */
@SuppressWarnings("restriction")
public class SourceTagDamagerRepairer extends DefaultDamagerRepairer implements ISourceTagListener {

	private ISourceTagProvider fSourceTagProvider;
	private Map<String, ITokenScanner> fScannerMap= new HashMap<String, ITokenScanner>();
	private List<ISourceTag> fSourceTags = new ArrayList<ISourceTag>();
	private IColorManager fColorManager;
	private IPreferenceStore fPreferenceStore;
	private Map<String, TextAttribute> fAttributeMap= new HashMap<String, TextAttribute>();

	private final static String[] KEYS= {
		SemanticHighlightings.CLASS,
		SemanticHighlightings.METHOD_DECLARATION,
		SemanticHighlightings.FUNCTION_DECLARATION,
		SemanticHighlightings.FIELD,
		SemanticHighlightings.GLOBAL_VARIABLE,
		SemanticHighlightings.TYPEDEF,
		SemanticHighlightings.MACRO_DEFINITION,
		SemanticHighlightings.ENUMERATOR,
		SemanticHighlightings.ENUM,
	};

	/**
	 * @param scanner
	 * @param sourceTagProvider
	 */
	public SourceTagDamagerRepairer(ITokenScanner scanner, ISourceTagProvider sourceTagProvider, IColorManager colorManager, IPreferenceStore store) {
		super(scanner);
		fSourceTagProvider= sourceTagProvider;
		fColorManager= colorManager;
		fPreferenceStore= store;
		fDefaultTextAttribute = new TextAttribute(null, null, SWT.NORMAL);
		if (fSourceTagProvider != null) {
			fSourceTagProvider.addSourceTagListener(this);
			sourceTagsChanged(fSourceTagProvider);
		}
	}

	private void initTextAttributes() {
		boolean shEnabled= fPreferenceStore.getBoolean(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED);
		for (int i= 0; i < KEYS.length; i++) {
			String enabledKey= PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + KEYS[i] + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED_SUFFIX;
			String colorKey= PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + KEYS[i] + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_COLOR_SUFFIX;
			boolean enabled= shEnabled && fPreferenceStore.getBoolean(enabledKey);
			if (enabled) {
				String boldKey= PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + KEYS[i] + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_BOLD_SUFFIX;
				String italicKey= PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + KEYS[i] + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ITALIC_SUFFIX;
				String strikethroughKey= PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + KEYS[i] + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_STRIKETHROUGH_SUFFIX;
				String underlineKey= PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + KEYS[i] + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_UNDERLINE_SUFFIX;
				addTextAttribute(KEYS[i], colorKey, boldKey, italicKey, strikethroughKey, underlineKey);
			} else {
				removeTextAttribute(KEYS[i], colorKey);
			}
		}
	}

	private void removeTextAttribute(String key, String colorKey) {
		if (fColorManager != null && colorKey != null) {
			Color color= fColorManager.getColor(colorKey);
			if (color != null) {
				fColorManager.unbindColor(colorKey);
			}
		}

		fAttributeMap.remove(key);
	}

	private void addTextAttribute(String key, String colorKey, String boldKey, String italicKey, String strikethroughKey, String underlineKey) {
		if (fColorManager != null && colorKey != null) {
			RGB rgb= PreferenceConverter.getColor(fPreferenceStore, colorKey);
			Color color= fColorManager.getColor(colorKey);
			if (color == null || !rgb.equals(color.getRGB())) {
				fColorManager.unbindColor(colorKey);
				fColorManager.bindColor(colorKey, rgb);
			}
		}

		TextAttribute textAttribute= createTextAttribute(colorKey, boldKey, italicKey, strikethroughKey, underlineKey);
		fAttributeMap.put(key, textAttribute);
	}

	private TextAttribute createTextAttribute(String colorKey, String boldKey, String italicKey, String strikethroughKey, String underlineKey) {
		Color color= null;
		if (colorKey != null)
			color= fColorManager.getColor(colorKey);

		int style= fPreferenceStore.getBoolean(boldKey) ? SWT.BOLD : SWT.NORMAL;
		if (fPreferenceStore.getBoolean(italicKey))
			style |= SWT.ITALIC;

		if (fPreferenceStore.getBoolean(strikethroughKey))
			style |= TextAttribute.STRIKETHROUGH;

		if (fPreferenceStore.getBoolean(underlineKey))
			style |= TextAttribute.UNDERLINE;

		return new TextAttribute(color, null, style);
	}

	/**
	 * Set scanner for contentType.
	 * @param contentType
	 * @param scanner
	 */
	public void setScanner(String contentType, ITokenScanner scanner) {
		fScannerMap.put(contentType, scanner);
	}

	/*
	 * @see org.eclipse.jface.text.presentation.IPresentationRepairer#createPresentation(org.eclipse.jface.text.TextPresentation, org.eclipse.jface.text.ITypedRegion)
	 */
	@Override
	public void createPresentation(TextPresentation presentation, ITypedRegion region) {
		if (fAttributeMap.isEmpty()) {
			initTextAttributes();
		}
		String contentType= region.getType();
		fScanner = fScannerMap.get(contentType);
		if (!contentType.equals(IDocument.DEFAULT_CONTENT_TYPE) && !contentType.equals(ICPartitions.C_PREPROCESSOR)) {
			super.createPresentation(presentation, region);
			return;
		}
		if (fScanner == null) {
			return;
		}

		int lastStart = region.getOffset();
		int regionEnd = lastStart + region.getLength();
		int length = 0;

		int sourceTagCount = fSourceTags.size();
		int sourceTagIdx = 0;

		ISourceTag sourceTag = null;
		ISourceRange range = null;
		int sourceTagStart = 0;
		int sourceTagEnd = 0;

		if (sourceTagCount > 0 && fDocument.getLength() > 0) {
			int left = 0;
			int mid = (int) (sourceTagCount * ((float)lastStart / fDocument.getLength()));
			int right = sourceTagCount - 1;
			while (true) {
				sourceTag = fSourceTags.get(mid);
				range = sourceTag.getRangeOfIdentifier();
				sourceTagStart = range.getBeginOffset();
				sourceTagEnd = range.getEndOffset() + 1;
				if (mid == left) {
					break;
				} else if (mid < right && sourceTagEnd < lastStart) {
					left = mid;
					mid = (mid + right) / 2;
				} else if (sourceTagStart >= regionEnd) {
					right = mid;
					mid = (left + mid) / 2;
				} else if (sourceTagStart > lastStart) {
					--mid;
					right = mid;
				} else {
					break;
				}
			}
			// set to next index
			sourceTagIdx = mid + 1;
		}

		TextAttribute lastAttribute = fDefaultTextAttribute;

		fScanner.setRange(fDocument, lastStart, region.getLength());

		while (true) {
			IToken token = fScanner.nextToken();

			// if the attribute is the same as the previous, extend range and continue
			TextAttribute attribute = getTokenTextAttribute(token);
			int tokenLength = fScanner.getTokenLength();
			if (tokenLength > 0
				&& (lastAttribute == attribute || lastAttribute != null && lastAttribute.equals(attribute))) {
				length += tokenLength;
				continue;
			}
			// attribute has changed, now add the style range
			while (sourceTag != null && length > 0) {
				if (sourceTagStart >= regionEnd) {
					// we are past the region boundary -> no more source tags
					sourceTag = null;
					break;
				}
				if (sourceTagStart >= lastStart) {
					if (sourceTagStart < lastStart + length) {
						String sourceTagStyle = getSourceTagStyle(sourceTag.getStyleCode());
						if (sourceTagStyle != null) {
							if (sourceTagStart > lastStart) {
								addRange(presentation, lastStart, Math.min(sourceTagStart - lastStart, length), lastAttribute);
							}
							int rangeEnd = Math.min(sourceTagEnd, regionEnd);
							addRange(
								presentation,
								sourceTagStart,
								rangeEnd - sourceTagStart,
								getSourceTagTextAttribute(sourceTagStyle));
							length = lastStart + length - rangeEnd;
							lastStart = rangeEnd;
						} else {
							fSourceTags.remove(--sourceTagIdx);
							--sourceTagCount;
						}
					} else {
						break;
					}
				}
				sourceTag = sourceTagIdx < sourceTagCount ? fSourceTags.get(sourceTagIdx++) : null;
				if (sourceTag != null) {
					range = sourceTag.getRangeOfIdentifier();
					sourceTagStart = range.getBeginOffset();
					sourceTagEnd = range.getEndOffset() + 1;
				}
			}
			if (token.isEOF()) {
				break;
			}

			if (length > 0) {
				addRange(presentation, lastStart, length, lastAttribute);
				lastAttribute = attribute;
				lastStart = fScanner.getTokenOffset();
				length = tokenLength;
			} else {
				lastAttribute = attribute;
				length = fScanner.getTokenOffset() - lastStart + tokenLength;
			}

		}

		addRange(presentation, lastStart, length, lastAttribute);
	}

	/**
	 * @param sourceTagStyle
	 * @return
	 */
	private TextAttribute getSourceTagTextAttribute(String sourceTagStyle) {
		return fAttributeMap.get(sourceTagStyle);
	}

	/**
	 * Get the style id for a source tag style code.
	 * @param styleCode
	 * @return the associated style id or <code>null</code>
	 */
	private String getSourceTagStyle(int styleCode) {
		switch (styleCode) {
			case ISourceTag.STYLE_None :
				return null;
			case ISourceTag.STYLE_Class :
				return SemanticHighlightings.CLASS;
			case ISourceTag.STYLE_Struct :
				return SemanticHighlightings.CLASS;
			case ISourceTag.STYLE_Union :
				return SemanticHighlightings.CLASS;
			case ISourceTag.STYLE_Function :
				return SemanticHighlightings.FUNCTION_DECLARATION;
			case ISourceTag.STYLE_Method :
				return SemanticHighlightings.METHOD_DECLARATION;
			case ISourceTag.STYLE_Variable :
				return SemanticHighlightings.GLOBAL_VARIABLE;
			case ISourceTag.STYLE_MemberVariable :
				return SemanticHighlightings.FIELD;
			case ISourceTag.STYLE_Enumerator :
				return SemanticHighlightings.ENUMERATOR;
			case ISourceTag.STYLE_Macro :
				return SemanticHighlightings.MACRO_DEFINITION;
			case ISourceTag.STYLE_Include :
				// include is colored by the scanner
				return null;
			case ISourceTag.STYLE_Enumeration :
				return SemanticHighlightings.ENUM;
			case ISourceTag.STYLE_Undefined :
				return null;
			case ISourceTag.STYLE_Typedef :
				return SemanticHighlightings.TYPEDEF;
			default :
				return null;
		}
	}

	public void sourceTagsChanged(ISourceTagProvider sourceTagProvider) {
		fSourceTags.clear();
		if (sourceTagProvider != null) {
			sourceTagProvider.getSourceTags(fSourceTags);
			Collections.sort(fSourceTags, new Comparator<Object>() {
				public int compare(Object o1, Object o2) {
					ISourceRange sr1 = ((ISourceTag)o1).getRangeOfIdentifier();
					ISourceRange sr2 = ((ISourceTag)o2).getRangeOfIdentifier();
					return (sr1.getBeginOffset() - sr2.getBeginOffset());
				}
			});
		}
	}

	/*
	 * @see org.eclipse.jface.text.rules.DefaultDamagerRepairer#addRange(org.eclipse.jface.text.TextPresentation, int, int, org.eclipse.jface.text.TextAttribute)
	 */
	@Override
	protected void addRange(TextPresentation presentation, int offset, int length, TextAttribute attr) {
		if (length > 0 && attr != null) {
			presentation.addStyleRange(
				new StyleRange(offset, length, attr.getForeground(), attr.getBackground(), attr.getStyle()));
		}
	}

	/**
	 * Test whether the given preference change affects us.
	 * 
	 * @param event
	 * @return <code>true</code> if the given event affects the behavior.
	 */
	public boolean affectsBahvior(PropertyChangeEvent event) {
		return event.getProperty().startsWith(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX);
	}

	/**
	 * Adapt to changes in the preferences.
	 * 
	 * @param event
	 */
	public void handlePropertyChangeEvent(PropertyChangeEvent event) {
		initTextAttributes();
	}

}
