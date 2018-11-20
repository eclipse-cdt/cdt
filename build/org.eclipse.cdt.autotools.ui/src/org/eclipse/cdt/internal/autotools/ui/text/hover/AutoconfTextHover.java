/*******************************************************************************
 * Copyright (c) 2006, 2016 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.text.hover;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.cdt.autotools.ui.AutotoolsUIPlugin;
import org.eclipse.cdt.autotools.ui.editors.AutoconfEditor;
import org.eclipse.cdt.autotools.ui.editors.AutoconfMacro;
import org.eclipse.cdt.autotools.ui.editors.IAutotoolEditorActionDefinitionIds;
import org.eclipse.cdt.internal.autotools.core.AutotoolsPropertyConstants;
import org.eclipse.cdt.internal.autotools.ui.CWordFinder;
import org.eclipse.cdt.internal.autotools.ui.HTMLPrinter;
import org.eclipse.cdt.internal.autotools.ui.HTMLTextPresenter;
import org.eclipse.cdt.internal.autotools.ui.preferences.AutotoolsEditorPreferenceConstants;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AutoconfTextHover implements ITextHover, ITextHoverExtension {

	public static final String LOCAL_AUTOCONF_MACROS_DOC_NAME = "macros/acmacros";
	public static final String LOCAL_AUTOMAKE_MACROS_DOC_NAME = "macros/ammacros";
	public static final String AUTOCONF_MACROS_DOC_NAME = "http://www.sourceware.org/eclipse/autotools/acmacros"; //$NON-NLS-1$
	public static final String AUTOMAKE_MACROS_DOC_NAME = "http://www.sourceware.org/eclipse/autotools/ammacros"; //$NON-NLS-1$

	private static class AutotoolsHoverDoc {
		public Document[] documents = new Document[2];

		public AutotoolsHoverDoc(Document acDocument, Document amDocument) {
			this.documents[0] = acDocument;
			this.documents[1] = amDocument;
		}

		public Document getAcDocument() {
			return documents[0];
		}

		public Document getAmDocument() {
			return documents[1];
		}

		public Document[] getDocuments() {
			return documents;
		}
	}

	private static Map<String, Document> acHoverDocs;
	private static Map<String, Document> amHoverDocs;
	private static Map<Document, ArrayList<AutoconfMacro>> acHoverMacros;
	private static String fgStyleSheet;
	private static AutoconfEditor fEditor;

	/* Mapping key to action */
	private static IBindingService fBindingService = PlatformUI.getWorkbench().getAdapter(IBindingService.class);

	public static String getAutoconfMacrosDocName(String version) {
		return AUTOCONF_MACROS_DOC_NAME + "-" //$NON-NLS-1$
				+ version + ".xml"; //$NON-NLS-1$
	}

	public static String getLocalAutoconfMacrosDocName(String version) {
		return LOCAL_AUTOCONF_MACROS_DOC_NAME + "-" //$NON-NLS-1$
				+ version + ".xml"; //$NON-NLS-1$
	}

	/* Get the preferences default for the autoconf macros document name.  */
	public static String getDefaultAutoconfMacrosVer() {
		return AutotoolsPlugin.getDefault().getPreferenceStore()
				.getString(AutotoolsEditorPreferenceConstants.AUTOCONF_VERSION);
	}

	public static String getAutomakeMacrosDocName(String version) {
		return AUTOMAKE_MACROS_DOC_NAME + "-" //$NON-NLS-1$
				+ version + ".xml"; //$NON-NLS-1$
	}

	public static String getLocalAutomakeMacrosDocName(String version) {
		return LOCAL_AUTOMAKE_MACROS_DOC_NAME + "-" //$NON-NLS-1$
				+ version + ".xml"; //$NON-NLS-1$
	}

	/* Get the preferences default for the autoconf macros document name.  */
	public static String getDefaultAutomakeMacrosVer() {
		return AutotoolsPlugin.getDefault().getPreferenceStore()
				.getString(AutotoolsEditorPreferenceConstants.AUTOMAKE_VERSION);
	}

	protected static Document getACDoc(String acDocVer) {
		Document acDocument = null;
		if (acHoverDocs == null) {
			acHoverDocs = new HashMap<>();
		}
		acDocument = acHoverDocs.get(acDocVer);
		if (acDocument == null) {
			Document doc = null;
			try {
				// see comment in initialize()
				try {
					InputStream docStream = null;
					try {
						URI uri = new URI(getLocalAutoconfMacrosDocName(acDocVer));
						IPath p = URIUtil.toPath(uri);
						// Try to open the file as local to this plug-in.
						docStream = FileLocator.openStream(AutotoolsUIPlugin.getDefault().getBundle(), p, false);
					} catch (IOException e) {
						// Local open failed.  Try normal external location.
						URI acDoc = new URI(getAutoconfMacrosDocName(acDocVer));
						IPath p = URIUtil.toPath(acDoc);
						if (p == null) {
							URL url = acDoc.toURL();
							docStream = url.openStream();
						} else {
							docStream = new FileInputStream(p.toFile());
						}
					}
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					factory.setValidating(false);
					try {
						DocumentBuilder builder = factory.newDocumentBuilder();
						doc = builder.parse(docStream);
					} catch (SAXException | ParserConfigurationException | IOException saxEx) {
						doc = null;
					} finally {
						if (docStream != null)
							docStream.close();
					}
				} catch (FileNotFoundException | MalformedURLException | URISyntaxException e) {
					AutotoolsPlugin.log(e);
				}
				acDocument = doc;
			} catch (IOException ioe) {
			}
		}
		acHoverDocs.put(acDocVer, acDocument);
		return acDocument;
	}

	protected static Document getAMDoc(String amDocVer) {
		Document amDocument = null;
		if (amHoverDocs == null) {
			amHoverDocs = new HashMap<>();
		}
		amDocument = amHoverDocs.get(amDocVer);
		if (amDocument == null) {
			Document doc = null;
			try {
				// see comment in initialize()
				try {
					InputStream docStream = null;
					try {
						URI uri = new URI(getLocalAutomakeMacrosDocName(amDocVer));
						IPath p = URIUtil.toPath(uri);
						// Try to open the file as local to this plug-in.
						docStream = FileLocator.openStream(AutotoolsUIPlugin.getDefault().getBundle(), p, false);
					} catch (IOException e) {
						// Local open failed.  Try normal external location.
						URI acDoc = new URI(getAutomakeMacrosDocName(amDocVer));
						IPath p = URIUtil.toPath(acDoc);
						if (p == null) {
							URL url = acDoc.toURL();
							docStream = url.openStream();
						} else {
							docStream = new FileInputStream(p.toFile());
						}
					}
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					factory.setValidating(false);
					try {
						DocumentBuilder builder = factory.newDocumentBuilder();
						doc = builder.parse(docStream);
					} catch (SAXException | ParserConfigurationException | IOException ex) {
						doc = null;
					} finally {
						if (docStream != null)
							docStream.close();
					}
				} catch (FileNotFoundException | MalformedURLException | URISyntaxException e) {
					AutotoolsPlugin.log(e);
				}
				amDocument = doc;
			} catch (IOException ioe) {
			}
		}
		amHoverDocs.put(amDocVer, amDocument);
		return amDocument;
	}

	protected static AutotoolsHoverDoc getHoverDoc(IEditorInput input) {
		String acDocVer = getDefaultAutoconfMacrosVer();
		String amDocVer = getDefaultAutomakeMacrosVer();
		if (input instanceof IFileEditorInput) {
			IFileEditorInput fe = (IFileEditorInput) input;
			IFile f = fe.getFile();
			IProject p = f.getProject();
			try {
				String acVer = p.getPersistentProperty(AutotoolsPropertyConstants.AUTOCONF_VERSION);
				if (acVer != null)
					acDocVer = acVer;
				else { // look for compat project properties
					acVer = p.getPersistentProperty(AutotoolsPropertyConstants.AUTOCONF_VERSION_COMPAT);
					if (acVer != null)
						acDocVer = acVer;
				}
			} catch (CoreException ce1) {
				// do nothing
			}
			try {
				String amVer = p.getPersistentProperty(AutotoolsPropertyConstants.AUTOMAKE_VERSION);
				if (amVer != null)
					amDocVer = amVer;
				else { // look for compat project properties
					amVer = p.getPersistentProperty(AutotoolsPropertyConstants.AUTOMAKE_VERSION_COMPAT);
					if (amVer != null)
						amDocVer = amVer;
				}
			} catch (CoreException ce2) {
				// do nothing
			}
		}
		Document ac_document = getACDoc(acDocVer);
		Document am_document = getAMDoc(amDocVer);
		return new AutoconfTextHover.AutotoolsHoverDoc(ac_document, am_document);
	}

	public AutoconfTextHover(AutoconfEditor editor) {
		fEditor = editor;
	}

	public static String getIndexedInfo(String name, AutoconfEditor editor) {
		AutotoolsHoverDoc h = getHoverDoc(editor.getEditorInput());
		String x = getIndexedInfoFromDocument(name, h.getAcDocument());
		if (x == null)
			x = getIndexedInfoFromDocument(name, h.getAmDocument());
		return x;
	}

	private static String getIndexedInfoFromDocument(String name, Document document) {
		StringBuilder buffer = new StringBuilder();

		if (document != null && name != null) {
			Element elem = document.getElementById(name);
			if (null != elem) {
				int prototypeCount = 0;
				buffer.append("<B>Macro:</B> ").append(name);
				NodeList nl = elem.getChildNodes();
				for (int i = 0; i < nl.getLength(); ++i) {
					Node n = nl.item(i);
					String nodeName = n.getNodeName();
					if (nodeName.equals("prototype")) { //$NON-NLS-1$
						StringBuilder prototype = new StringBuilder();
						++prototypeCount;
						if (prototypeCount == 1) {
							buffer.append(" (");
						} else {
							buffer.append("    <B>or</B> "); //$NON-NLS-2$
							buffer.append(name);
							buffer.append(" (<I>"); //$NON-NLS-2$
						}
						NodeList varList = n.getChildNodes();
						for (int j = 0; j < varList.getLength(); ++j) {
							Node v = varList.item(j);
							String vnodeName = v.getNodeName();
							if (vnodeName.equals("parameter")) { //$NON-NLS-1$
								NamedNodeMap parms = v.getAttributes();
								Node parmNode = parms.item(0);
								String parm = parmNode.getNodeValue();
								if (prototype.length() == 0)
									prototype.append(parm);
								else
									prototype.append(", ").append(parm);
							}
						}
						buffer.append(prototype).append("</I>)<br>"); //$NON-NLS-1$
					}
					if (nodeName.equals("synopsis")) { //$NON-NLS-1$
						Node textNode = n.getLastChild();
						buffer.append("<br><B>Synopsis:</B> ");
						buffer.append(textNode.getNodeValue());
					}
				}
			}
		}
		if (buffer.length() > 0) {
			HTMLPrinter.insertPageProlog(buffer, 0);
			HTMLPrinter.addPageEpilog(buffer);
			return buffer.toString();
		}

		return null;
	}

	public static AutoconfMacro[] getMacroList(AutoconfEditor editor) {
		IEditorInput input = editor.getEditorInput();
		AutotoolsHoverDoc hoverdoc = getHoverDoc(input);
		return getMacroList(hoverdoc);
	}

	private static AutoconfMacro[] getMacroList(AutotoolsHoverDoc hoverdoc) {
		if (acHoverMacros == null) {
			acHoverMacros = new HashMap<>();
		}

		ArrayList<AutoconfMacro> masterList = new ArrayList<>();
		Document[] doc = hoverdoc.getDocuments();
		for (int ix = 0; ix < doc.length; ++ix) {
			Document macroDoc = doc[ix];
			ArrayList<AutoconfMacro> list = acHoverMacros.get(macroDoc);
			if (list == null && macroDoc != null) {
				list = new ArrayList<>();
				NodeList nl = macroDoc.getElementsByTagName("macro"); //$NON-NLS-1$
				for (int i = 0; i < nl.getLength(); ++i) {
					Node macro = nl.item(i);
					NamedNodeMap macroAttrs = macro.getAttributes();
					Node n2 = macroAttrs.getNamedItem("id"); //$NON-NLS-1$
					if (n2 != null) {
						String name = n2.getNodeValue();
						StringBuilder parms = new StringBuilder();
						NodeList macroChildren = macro.getChildNodes();
						for (int j = 0; j < macroChildren.getLength(); ++j) {
							Node x = macroChildren.item(j);
							if (x.getNodeName().equals("prototype")) { //$NON-NLS-1$
								// Use parameters for context info.
								NodeList parmList = x.getChildNodes();
								int parmCount = 0;
								for (int k = 0; k < parmList.getLength(); ++k) {
									Node n3 = parmList.item(k);
									if (n3.getNodeName().equals("parameter")) { //$NON-NLS-1$
										NamedNodeMap parmVals = n3.getAttributes();
										Node parmVal = parmVals.item(0);
										if (parmCount > 0)
											parms = parms.append(", "); //$NON-NLS-1$
										parms.append(parmVal.getNodeValue());
										++parmCount;
									}
								}
							}
						}
						AutoconfMacro m = new AutoconfMacro(name, parms.toString());
						list.add(m);
					}
				}
				// Cache the arraylist of macros for later usage.
				acHoverMacros.put(macroDoc, list);
			}
			if (list != null)
				masterList.addAll(list);
		}
		// Convert to a sorted array of macros and return result.
		AutoconfMacro[] macros = new AutoconfMacro[masterList.size()];
		masterList.toArray(macros);
		Arrays.sort(macros);
		return macros;
	}

	public static AutoconfPrototype getPrototype(String name, AutoconfEditor editor) {
		IEditorInput input = editor.getEditorInput();
		AutotoolsHoverDoc hoverdoc = getHoverDoc(input);
		AutoconfPrototype x = getPrototype(name, hoverdoc.getAcDocument());
		if (x == null)
			x = getPrototype(name, hoverdoc.getAmDocument());
		return x;
	}

	private static AutoconfPrototype getPrototype(String name, Document document) {
		AutoconfPrototype p = null;
		if (document != null && name != null) {
			Element elem = document.getElementById(name);
			if (null != elem) {
				int prototypeCount = -1;
				p = new AutoconfPrototype();
				p.setName(name);
				NodeList nl = elem.getChildNodes();
				for (int i = 0; i < nl.getLength(); ++i) {
					Node n = nl.item(i);
					String nodeName = n.getNodeName();
					if (nodeName.equals("prototype")) { //$NON-NLS-1$
						++prototypeCount;
						int parmCount = 0;
						int minParmCount = -1;
						p.setNumPrototypes(prototypeCount + 1);
						NodeList varList = n.getChildNodes();
						for (int j = 0; j < varList.getLength(); ++j) {
							Node v = varList.item(j);
							String vnodeName = v.getNodeName();
							if (vnodeName.equals("parameter")) { //$NON-NLS-1$
								++parmCount;
								NamedNodeMap parms = v.getAttributes();
								Node parmNode = parms.item(0);
								String parm = parmNode.getNodeValue();
								// Check for first optional parameter which means
								// we know the minimum number of parameters needed.
								if (minParmCount < 0 && (parm.charAt(0) == '[' || parm.startsWith("...")))
									minParmCount = parmCount - 1;
								// Old style documentation sometimes had '[' in
								// prototypes so look for one at end of a parm too.
								else if (minParmCount < 0 && parm.endsWith("["))
									minParmCount = parmCount;
								p.setParmName(prototypeCount, parmCount - 1, parm);
							}
						}
						p.setMaxParms(prototypeCount, parmCount);
						// If we see no evidence of optional parameters, then
						// the min and max number of parameters are equal.
						if (minParmCount < 0)
							minParmCount = parmCount;
						p.setMinParms(prototypeCount, minParmCount);
					}
				}
			}
		}
		return p;
	}

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {

		String hoverInfo = null;

		IDocument d = textViewer.getDocument();

		try {
			String name = d.get(hoverRegion.getOffset(), hoverRegion.getLength());
			hoverInfo = getIndexedInfo(name, fEditor);
		} catch (BadLocationException e) {
			// do nothing
		}
		return hoverInfo;
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {

		if (textViewer != null) {
			/*
			 * If the hover offset falls within the selection range return the
			 * region for the whole selection.
			 */
			Point selectedRange = textViewer.getSelectedRange();
			if (selectedRange.x >= 0 && selectedRange.y > 0 && offset >= selectedRange.x
					&& offset <= selectedRange.x + selectedRange.y)
				return new Region(selectedRange.x, selectedRange.y);
			else {
				return CWordFinder.findWord(textViewer.getDocument(), offset);
			}
		}
		return null;
	}

	/*
	 * @see ITextHoverExtension#getHoverControlCreator()
	 * @since 3.0
	 */
	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return parent -> new DefaultInformationControl(parent, getTooltipAffordanceString(),
				new HTMLTextPresenter(false));
	}

	/*
	 * Static member function to allow content assist to add hover help.
	 */
	public static IInformationControlCreator getInformationControlCreator() {
		return parent -> new DefaultInformationControl(parent, getTooltipAffordanceString(),
				new HTMLTextPresenter(false));
	}

	protected static String getTooltipAffordanceString() {
		if (fBindingService == null)
			return null;

		String keySequence = fBindingService
				.getBestActiveBindingFormattedFor(IAutotoolEditorActionDefinitionIds.SHOW_TOOLTIP);
		if (keySequence == null)
			return null;

		return HoverMessages.getFormattedString("ToolTipFocus", keySequence); //$NON-NLS-1$
	}

	/**
	 * Returns the style sheet.
	 *
	 * @since 3.2
	 */
	protected static String getStyleSheet() {
		if (fgStyleSheet == null) {
			Bundle bundle = Platform.getBundle(AutotoolsUIPlugin.PLUGIN_ID);
			URL styleSheetURL = bundle.getEntry("/AutoconfHoverStyleSheet.css"); //$NON-NLS-1$
			if (styleSheetURL != null) {
				try {
					styleSheetURL = FileLocator.toFileURL(styleSheetURL);
					try (BufferedReader reader = new BufferedReader(
							new InputStreamReader(styleSheetURL.openStream()))) {
						StringBuilder buffer = new StringBuilder(200);
						String line = reader.readLine();
						while (line != null) {
							buffer.append(line);
							buffer.append('\n');
							line = reader.readLine();
						}
						fgStyleSheet = buffer.toString();
					}
				} catch (IOException ex) {
					AutotoolsUIPlugin.log(ex);
					fgStyleSheet = ""; //$NON-NLS-1$
				}
			}
		}
		return fgStyleSheet;
	}
}
