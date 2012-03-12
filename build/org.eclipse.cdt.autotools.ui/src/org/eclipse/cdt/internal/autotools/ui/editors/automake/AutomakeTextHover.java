/*******************************************************************************
 * Copyright (c) 2006, 2007 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Point;


public class AutomakeTextHover implements ITextHover, ITextHoverExtension {

	AutomakeEditor editor;
	
	public AutomakeTextHover(AutomakeEditor editor) {
		this.editor = editor;
	}
	
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		TargetRule target = null;
		String[] preReqs = null;
		
		if (hoverRegion == null || hoverRegion.getLength() == 0)
			return null;
		Automakefile makefile = (Automakefile) editor.getMakefile();
		if (makefile == null)
			return null;
		
		String hoverText;
		int hoverLine;
		try {
			hoverText = textViewer.getDocument().get(hoverRegion.getOffset(), hoverRegion.getLength());
			hoverLine = textViewer.getDocument().getLineOfOffset(hoverRegion.getOffset());
		} catch (BadLocationException e) {
			return null;
		}
		
		// Automatic variables
		if (hoverText.startsWith("$")) {
			IDirective containingDirective = makefile.getDirectiveContainingLine(hoverLine);
			if (containingDirective instanceof TargetRule) {
				target = (TargetRule) containingDirective;
			}
			if (target == null)
				return "";
			switch (hoverText.charAt(1)) {
			case '@':
				return target.getTarget().toString();
			case '<':
				preReqs = target.getPrerequisites();
				if (preReqs != null && preReqs.length > 0)
					return preReqs[0];
				break;
			// FIXME:  implement $* ?
//			case '*':
//				break;
			case '?':
				preReqs = target.getPrerequisites();
				if (preReqs != null && preReqs.length > 0) {
					StringBuffer toReturn = new StringBuffer();
					toReturn.append(preReqs[0]);
					for (int i = 1; i < preReqs.length; i++) {
						toReturn.append(" " + preReqs[i]);
					}
					return toReturn.toString();
				}
				break;
			case '%':
//				if (target instanceOf ArchiveTarget) {
//					return target.getMember();
//				} else {
//					error;
//				}
//				break;
			default:
				break;
			}
		} else {
			// Macros
			IMacroDefinition[] macroDefinitions = makefile.getMacroDefinitions(hoverText);
			for (int i = 0; i < macroDefinitions.length; i++) {
				IMacroDefinition definition = macroDefinitions[i];
				if (definition.getName().equals(hoverText))
					return definition.getValue().toString();
			}
		}
		
//		IRule[] rules = makefile.getRules();
//		for (int i = 0; i < rules.length; i++) {
//			rule = rules[i];
//			System.out.println("rule:  " + rule);
//			System.out.println("target:  " + rule.getTarget());
//			ICommand[] commands = rule.getCommands();
//			for (int j = 0; j < commands.length; j++) {
//				ICommand command = commands[j];
//				System.out.println("command:  " + command);
//			}
//		}
		return "";
	}

	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		
		if (textViewer != null) {
			/*
			 * If the hover offset falls within the selection range return the
			 * region for the whole selection.
			 */
			Point selectedRange = textViewer.getSelectedRange();
			if (selectedRange.x >= 0 && selectedRange.y > 0
					&& offset >= selectedRange.x
					&& offset <= selectedRange.x + selectedRange.y)
				return new Region(selectedRange.x, selectedRange.y);
			else {
				return findWord(textViewer.getDocument(), offset);
			}
		}
		return null;
	}

	private IRegion findWord(IDocument document, int offset) {
		int start = -1;
		int end = -1;

		try {
			int pos = offset;
			char c;

			while (pos >= 0) {
				c = document.getChar(pos);
				if (!Character.isJavaIdentifierPart(c) &&
						(c != '@') && (c != '<') && (c != '*') && (c != '?') && (c != '%'))
					break;
				--pos;
			}

			start = pos;

			pos = offset;
			int length = document.getLength();

			while (pos < length) {
				c = document.getChar(pos);
				if (!Character.isJavaIdentifierPart(c) &&
						(c != '@') && (c != '<') && (c != '*') && (c != '?') && (c != '%'))
					break;
				++pos;
			}

			end = pos;

		} catch (BadLocationException x) {
		}

		if (start > -1 && end > -1) {
			if (start == offset && end == offset)
				return new Region(offset, 0);
			else if (start == offset)
				return new Region(start, end - start);
			else
				return new Region(start + 1, end - start - 1);
		}

		return null;
	}

	public IInformationControlCreator getHoverControlCreator() {
		// TODO Auto-generated method stub
		return null;
	}

}
