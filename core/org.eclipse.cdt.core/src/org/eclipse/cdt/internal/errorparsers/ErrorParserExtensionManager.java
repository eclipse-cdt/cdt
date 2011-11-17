/*******************************************************************************
 * Copyright (c) 2009, 2011 Andrew Gvozdev (Quoin Inc.) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.errorparsers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IErrorParserNamed;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.errorparsers.ErrorParserNamedWrapper;
import org.eclipse.cdt.core.errorparsers.RegexErrorParser;
import org.eclipse.cdt.core.errorparsers.RegexErrorPattern;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * ErrorParserExtensionManager manages error parser extensions, serialization and preferences
 *
 */
public class ErrorParserExtensionManager {
	private static final String STORAGE_ERRORPARSER_EXTENSIONS = "model.extensions.xml"; //$NON-NLS-1$
	private static final String PREFERENCE_ERRORPARSER_DEFAULT_IDS = "errorparser.default.ids"; //$NON-NLS-1$
	private static final String NONE = ""; //$NON-NLS-1$

	private static final String EXTENSION_POINT_ERROR_PARSER = "org.eclipse.cdt.core.ErrorParser"; //$NON-NLS-1$
	private static final String ELEM_PLUGIN = "plugin"; //$NON-NLS-1$
	private static final String ELEM_EXTENSION = "extension"; //$NON-NLS-1$
	private static final String ELEM_ERRORPARSER = "errorparser"; //$NON-NLS-1$
	private static final String ELEM_PATTERN = "pattern"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_POINT = "point"; //$NON-NLS-1$

	private static final String ATTR_REGEX = "regex"; //$NON-NLS-1$
	private static final String ATTR_SEVERITY = "severity"; //$NON-NLS-1$
	private static final String ATTR_FILE = "file-expr"; //$NON-NLS-1$
	private static final String ATTR_LINE = "line-expr"; //$NON-NLS-1$
	private static final String ATTR_DESCRIPTION = "description-expr"; //$NON-NLS-1$
	private static final String ATTR_VARIABLE = "variable-expr"; //$NON-NLS-1$
	private static final String ATTR_EAT_LINE = "eat-processed-line"; //$NON-NLS-1$

	private static final String ATTR_VALUE_WARNING = "Warning"; //$NON-NLS-1$
	private static final String ATTR_VALUE_ERROR = "Error"; //$NON-NLS-1$
	private static final String ATTR_VALUE_INFO = "Info"; //$NON-NLS-1$
	private static final String ATTR_VALUE_IGNORE = "Ignore"; //$NON-NLS-1$

	private static final LinkedHashMap<String, IErrorParserNamed> fExtensionErrorParsers = new LinkedHashMap<String, IErrorParserNamed>();
	private static final LinkedHashMap<String, IErrorParserNamed> fAvailableErrorParsers = new LinkedHashMap<String, IErrorParserNamed>();
	private static LinkedHashMap<String, IErrorParserNamed> fUserDefinedErrorParsers = null;
	private static List<String> fDefaultErrorParserIds = null;

	private static class ErrorParserComparator implements Comparator<IErrorParserNamed> {
		// For the error parsers taken from platform extensions following sorting order applies:
		// - first regular error parsers
		// - then deprecated ones
		// - then contributed by test plugin
		// inside the same category sort by parser name
		@Override
		public int compare(IErrorParserNamed errorParser1, IErrorParserNamed errorParser2) {
			final String TEST_PLUGIN_ID="org.eclipse.cdt.core.tests"; //$NON-NLS-1$
			final String DEPRECATED=CCorePlugin.getResourceString("CCorePlugin.Deprecated"); //$NON-NLS-1$

			boolean isTestPlugin1 = errorParser1.getId().startsWith(TEST_PLUGIN_ID);
			boolean isTestPlugin2 = errorParser2.getId().startsWith(TEST_PLUGIN_ID);
			if (isTestPlugin1==true && isTestPlugin2==false)
				return 1;
			if (isTestPlugin1==false && isTestPlugin2==true)
				return -1;

			boolean isDeprecated1 = errorParser1.getName().contains(DEPRECATED);
			boolean isDeprecated2 = errorParser2.getName().contains(DEPRECATED);
			if (isDeprecated1==true && isDeprecated2==false)
				return 1;
			if (isDeprecated1==false && isDeprecated2==true)
				return -1;

			return errorParser1.getName().compareTo(errorParser2.getName());
		}
	}

	static {
		loadUserDefinedErrorParsers();
		loadDefaultErrorParserIds();
		loadErrorParserExtensions();
	}

	/**
	 * Load user defined error parsers from workspace preference storage.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	synchronized public static void loadUserDefinedErrorParsers() {
		fUserDefinedErrorParsers = null;
		Document doc = null;
		try {
			doc = XmlUtil.loadXml(getStoreURI(STORAGE_ERRORPARSER_EXTENSIONS));
		} catch (Exception e) {
			CCorePlugin.log("Can't load preferences from "+STORAGE_ERRORPARSER_EXTENSIONS, e); //$NON-NLS-1$
		}

		if (doc!=null) {
			Set<IErrorParserNamed> sortedErrorParsers = new TreeSet<IErrorParserNamed>(new ErrorParserComparator());
			loadErrorParserExtensions(doc, sortedErrorParsers);

			if (sortedErrorParsers.size()>0) {
				fUserDefinedErrorParsers = new LinkedHashMap<String, IErrorParserNamed>();
				for (IErrorParserNamed errorParser : sortedErrorParsers) {
					fUserDefinedErrorParsers.put(errorParser.getId(), errorParser);
				}
			}
		}
		recalculateAvailableErrorParsers();
	}

	/**
	 * Parse error parser contributed extensions from XML document.
	 *
	 * @param doc - source XML
	 * @param errorParsers - resulting list of error parsers
	 */
	private static void loadErrorParserExtensions(Document doc, Set<IErrorParserNamed> errorParsers) {
		errorParsers.clear();
		NodeList extensionNodes = doc.getElementsByTagName(ELEM_EXTENSION);
		for (int iext=0;iext<extensionNodes.getLength();iext++) {
			Node extensionNode = extensionNodes.item(iext);
			if(extensionNode.getNodeType() != Node.ELEMENT_NODE)
				continue;

			NodeList errorparserNodes = extensionNode.getChildNodes();
			for (int ierp=0;ierp<errorparserNodes.getLength();ierp++) {
				Node errorparserNode = errorparserNodes.item(ierp);
				if(errorparserNode.getNodeType() != Node.ELEMENT_NODE || ! ELEM_ERRORPARSER.equals(errorparserNode.getNodeName()))
					continue;

				String className = XmlUtil.determineAttributeValue(errorparserNode, ATTR_CLASS);
				try {
					IErrorParserNamed errorParser = createErrorParserCarcass(className, Platform.getExtensionRegistry());
					if (errorParser!=null) {
						configureErrorParser(errorParser, errorparserNode);
						errorParsers.add(errorParser);
					}
				} catch (Exception e) {
					CCorePlugin.log("Can't create class ["+className+"] while trying to load error parser extension", e); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
	}

	/**
	 * Load workspace default error parser IDs to be used if no error parsers specified.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	synchronized public static void loadDefaultErrorParserIds() {
		fDefaultErrorParserIds = null;
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID);
		String ids = preferences.get(PREFERENCE_ERRORPARSER_DEFAULT_IDS, NONE);
		if (ids.equals(NONE)) {
			return;
		}

		fDefaultErrorParserIds = Arrays.asList(ids.split(String.valueOf(ErrorParserManager.ERROR_PARSER_DELIMITER)));
	}

	/**
	 * Load error parser contributed extensions.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	synchronized public static void loadErrorParserExtensions() {
		Set<IErrorParserNamed> sortedErrorParsers = new TreeSet<IErrorParserNamed>(new ErrorParserComparator());
		loadErrorParserExtensions(Platform.getExtensionRegistry(), sortedErrorParsers);

		fExtensionErrorParsers.clear();
		for (IErrorParserNamed errorParser : sortedErrorParsers) {
			fExtensionErrorParsers.put(errorParser.getId(), errorParser);
		}
		recalculateAvailableErrorParsers();
	}

	/**
	 * Load error parser contributed extensions from extension registry.
	 *
	 * @param registry - extension registry
	 * @param errorParsers - resulting set of error parsers
	 */
	private static void loadErrorParserExtensions(IExtensionRegistry registry, Set<IErrorParserNamed> errorParsers) {
		errorParsers.clear();
		IExtensionPoint extension = registry.getExtensionPoint(CCorePlugin.PLUGIN_ID, CCorePlugin.ERROR_PARSER_SIMPLE_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (IExtension ext : extensions) {
				try {
					String extensionID = ext.getUniqueIdentifier();
					String oldStyleId = extensionID;
					String oldStyleName = ext.getLabel();
					for (IConfigurationElement cfgEl : ext.getConfigurationElements()) {
						if (cfgEl.getName().equals(ELEM_ERRORPARSER)) {
							IErrorParserNamed errorParser = createErrorParserCarcass(oldStyleId, oldStyleName, cfgEl);
							if (errorParser!=null) {
								configureErrorParser(errorParser, cfgEl);
								errorParsers.add(errorParser);
							}
						}
					}
				} catch (Exception e) {
					CCorePlugin.log("Cannot load ErrorParser extension " + ext.getUniqueIdentifier(), e); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * Populate the list of available error parsers where workspace level user defined parsers
	 * overwrite contributed through error parser extension point.
	 */
	private static void recalculateAvailableErrorParsers() {
		fAvailableErrorParsers.clear();
		// put default parsers on top of the list
		List<String> ids = new ArrayList<String>();
		if (fDefaultErrorParserIds!=null) {
			for (String id : fDefaultErrorParserIds) {
				IErrorParserNamed errorParser = null;
				if (fUserDefinedErrorParsers!=null) {
					errorParser = fUserDefinedErrorParsers.get(id);
				}
				if (errorParser==null) {
					errorParser = fExtensionErrorParsers.get(id);
				}
				if (errorParser!=null) {
					fAvailableErrorParsers.put(id, errorParser);
					ids.add(id);
				}
			}
		}
		// then the rest in the order defined by comparator
		Set<IErrorParserNamed> sortedErrorParsers = new TreeSet<IErrorParserNamed>(new ErrorParserComparator());

		if (fUserDefinedErrorParsers!=null) {
			for (String id : fUserDefinedErrorParsers.keySet()) {
				if (!ids.contains(id)) {
					IErrorParserNamed errorParser = fUserDefinedErrorParsers.get(id);
					sortedErrorParsers.add(errorParser);
				}
			}
		}
		for (String id : fExtensionErrorParsers.keySet()) {
			if (!ids.contains(id)) {
				IErrorParserNamed errorParser = fExtensionErrorParsers.get(id);
				sortedErrorParsers.add(errorParser);
			}
		}

		for (IErrorParserNamed errorParser : sortedErrorParsers) {
			fAvailableErrorParsers.put(errorParser.getId(), errorParser);
		}
	}

	/**
	 * Serialize error parsers in workspace level storage.
	 *
	 * @throws CoreException if something goes wrong
	 */
	public static void serializeUserDefinedErrorParsers() throws CoreException {
		try {
			Document doc = XmlUtil.newDocument();
			Element elementPlugin = XmlUtil.appendElement(doc, ELEM_PLUGIN);

			if (fUserDefinedErrorParsers!=null) {
				for (Entry<String, IErrorParserNamed> entry: fUserDefinedErrorParsers.entrySet()) {
					IErrorParserNamed errorParser = entry.getValue();
					addErrorParserExtension(elementPlugin, errorParser);
				}
			}

			XmlUtil.serializeXml(doc, getStoreURI(STORAGE_ERRORPARSER_EXTENSIONS));

		} catch (Exception e) {
			throw new CoreException(CCorePlugin.createStatus("Failed serializing to file " + STORAGE_ERRORPARSER_EXTENSIONS, e)); //$NON-NLS-1$
		}
	}

	/**
	 * Utility method to convert severity to string for the purpose of serializing in XML.
	 *
	 * @param severity - severity
	 * @return string representation
	 */
	private static String severityToString(int severity) {
		switch (severity) {
		case IMarkerGenerator.SEVERITY_INFO:
			return ATTR_VALUE_INFO;
		case IMarkerGenerator.SEVERITY_WARNING:
			return ATTR_VALUE_WARNING;
		case IMarkerGenerator.SEVERITY_ERROR_BUILD:
		case IMarkerGenerator.SEVERITY_ERROR_RESOURCE:
			return ATTR_VALUE_ERROR;
		}
		return ATTR_VALUE_IGNORE;
	}

	/**
	 * Utility method to de-serialize severity from XML.
	 *
	 * @param attrSeverity - string representation of the severity
	 * @return severity
	 */
	private static int stringToSeverity(String attrSeverity) {
		if (ATTR_VALUE_ERROR.equals(attrSeverity))
			return IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
		if (ATTR_VALUE_WARNING.equals(attrSeverity))
			return IMarkerGenerator.SEVERITY_WARNING;
		if (ATTR_VALUE_INFO.equals(attrSeverity))
			return IMarkerGenerator.SEVERITY_INFO;

		return RegexErrorPattern.SEVERITY_SKIP;
	}

	/**
	 * Add error parser extension to XML fragment, normally under <plugin/> element.
	 *
	 * @param elementPlugin - element where to add error parser extension
	 * @param errorParserNamed - error parser to add
	 */
	private static void addErrorParserExtension(Element elementPlugin, IErrorParserNamed errorParserNamed) {
		String id = errorParserNamed.getId();
		String name = errorParserNamed.getName();
		String simpleId = getSimpleId(id);

		IErrorParser errorParser = errorParserNamed;
		if (errorParser instanceof ErrorParserNamedWrapper)
			errorParser = ((ErrorParserNamedWrapper)errorParser).getErrorParser();

		// <extension/>
		Element elementExtension = XmlUtil.appendElement(elementPlugin, ELEM_EXTENSION, new String[] {
				ATTR_ID, simpleId,
				ATTR_NAME, name,
				ATTR_POINT, EXTENSION_POINT_ERROR_PARSER,
			});

		// <errorparser/>
		Element elementErrorParser = XmlUtil.appendElement(elementExtension, ELEM_ERRORPARSER, new String[] {
				ATTR_ID, id,
				ATTR_NAME, name,
				ATTR_CLASS, errorParser.getClass().getCanonicalName(),
			});

		if (errorParserNamed instanceof RegexErrorParser) {
			RegexErrorParser regexErrorParser = (RegexErrorParser)errorParserNamed;
			RegexErrorPattern[] patterns = regexErrorParser.getPatterns();

			for (RegexErrorPattern pattern : patterns) {
				// <pattern/>
				@SuppressWarnings("unused")
				Element elementPattern = XmlUtil.appendElement(elementErrorParser, ELEM_PATTERN, new String[] {
						ATTR_SEVERITY, severityToString(pattern.getSeverity()),
						ATTR_REGEX, pattern.getPattern(),
						ATTR_FILE, pattern.getFileExpression(),
						ATTR_LINE, pattern.getLineExpression(),
						ATTR_DESCRIPTION, pattern.getDescriptionExpression(),
						ATTR_EAT_LINE, String.valueOf(pattern.isEatProcessedLine()),
					});
			}

		}
	}

	/**
	 * Determine simple ID of error parser as last segment of full or unique ID.
	 *
	 * @param uniqueId - full ID of error parser
	 * @return simple ID of error parser
	 */
	private static String getSimpleId(String uniqueId) {
		String simpleId = uniqueId;
		int dot = uniqueId.lastIndexOf('.');
		if (dot>=0) {
			simpleId = uniqueId.substring(dot+1);
		}
		return simpleId;
	}

	/**
	 * Save the list of default error parsers in preferences.
	 *
	 * @throws BackingStoreException in case of problem storing
	 */
	public static void serializeDefaultErrorParserIds() throws BackingStoreException {
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID);
		String ids = NONE;
		if (fDefaultErrorParserIds!=null) {
			ids = ErrorParserManager.toDelimitedString(fDefaultErrorParserIds.toArray(new String[0]));
		}

		preferences.put(PREFERENCE_ERRORPARSER_DEFAULT_IDS, ids);
		preferences.flush();
	}

	/**
	 * @param store - name of the store
	 * @return URI of the store in the plug-in state area to keep plug-in specific data.
	 */
	private static URI getStoreURI(String store) {
		IPath location = CCorePlugin.getDefault().getStateLocation().append(store);
		URI uri = URIUtil.toURI(location);
		return uri;
	}

	/**
	 * Creates empty non-configured error parser from extension point definition looking at "class" attribute.
	 * ID and name of error parser are assigned from first extension point encountered.
	 *
	 * @param className - full qualified class name of error parser.
	 * @param registry - extension registry
	 * @return new non-configured error parser
	 */
	private static IErrorParserNamed createErrorParserCarcass(String className, IExtensionRegistry registry) {
		if (className==null || className.length()==0 || className.equals(RegexErrorParser.class.getName()))
			return new RegexErrorParser();

		try {
			IExtensionPoint extension = registry.getExtensionPoint(CCorePlugin.PLUGIN_ID, CCorePlugin.ERROR_PARSER_SIMPLE_ID);
			if (extension != null) {
				IExtension[] extensions = extension.getExtensions();
				for (IExtension ext : extensions) {
					String extensionID = ext.getUniqueIdentifier();
					String oldStyleId = extensionID;
					String oldStyleName = ext.getLabel();
					for (IConfigurationElement cfgEl : ext.getConfigurationElements()) {
						if (cfgEl.getName().equals(ELEM_ERRORPARSER) && className.equals(cfgEl.getAttribute(ATTR_CLASS))) {
							return createErrorParserCarcass(oldStyleId, oldStyleName, cfgEl);
						}
					}
				}
			}
		} catch (Exception e) {
			CCorePlugin.log("Error creating error parser", e); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Creates empty non-configured error parser as executable extension from extension point definition.
	 * If "class" attribute is empty RegexErrorParser is created.
	 *
	 * @param initialId - nominal ID of error parser
	 * @param initialName - nominal name of error parser
	 * @param ce - configuration element with error parser definition
	 * @return new non-configured error parser
	 * @throws CoreException in case of failure
	 */
	private static IErrorParserNamed createErrorParserCarcass(String initialId, String initialName, IConfigurationElement ce) throws CoreException {
		IErrorParserNamed errorParser = null;
		if (ce.getAttribute(ATTR_CLASS)!=null) {
			IErrorParser ep = (IErrorParser)ce.createExecutableExtension(ATTR_CLASS);
			if (ep instanceof IErrorParserNamed) {
				errorParser = (IErrorParserNamed)ep;
				errorParser.setId(initialId);
				errorParser.setName(initialName);
			} else if (ep!=null) {
				errorParser = new ErrorParserNamedWrapper(initialId, initialName, ep);
			}
		}
		if (errorParser==null) {
			errorParser = new RegexErrorParser(initialId, initialName);
		}
		return errorParser;
	}

	/**
	 * Configure error parser from XML error parser node.
	 *
	 * @param errorParser - error parser to configure
	 * @param errorparserNode - XML error parser node
	 */
	private static void configureErrorParser(IErrorParserNamed errorParser, Node errorparserNode) {
		String id = XmlUtil.determineAttributeValue(errorparserNode, ATTR_ID);
		String name = XmlUtil.determineAttributeValue(errorparserNode, ATTR_NAME);
		errorParser.setId(id);
		errorParser.setName(name);
		if (errorParser instanceof RegexErrorParser) {
			RegexErrorParser regexErrorParser = (RegexErrorParser)errorParser;

			NodeList patternNodes = errorparserNode.getChildNodes();
			for (int ipat=0;ipat<patternNodes.getLength();ipat++) {
				Node patternNode = patternNodes.item(ipat);
				if(patternNode.getNodeType() != Node.ELEMENT_NODE || ! ELEM_PATTERN.equals(patternNode.getNodeName()))
					continue;

				String attrSeverity = XmlUtil.determineAttributeValue(patternNode, ATTR_SEVERITY);
				String regex = XmlUtil.determineAttributeValue(patternNode, ATTR_REGEX);
				String fileExpr = XmlUtil.determineAttributeValue(patternNode, ATTR_FILE);
				String lineExpr = XmlUtil.determineAttributeValue(patternNode, ATTR_LINE);
				String DescExpr = XmlUtil.determineAttributeValue(patternNode, ATTR_DESCRIPTION);
				String attrEatLine = XmlUtil.determineAttributeValue(patternNode, ATTR_EAT_LINE);

				int severity = stringToSeverity(attrSeverity);

				boolean eatLine = ! Boolean.FALSE.toString().equals(attrEatLine); // if null default to true
				regexErrorParser.addPattern(new RegexErrorPattern(regex, fileExpr, lineExpr, DescExpr, null,
						severity, eatLine));
			}
		}
	}

	/**
	 * Configure error parser from extension configuration element.
	 *
	 * @param errorParser - error parser to configure
	 * @param cfgEl - extension configuration element
	 * @throws CoreException
	 */
	private static void configureErrorParser(IErrorParserNamed errorParser, IConfigurationElement cfgEl) throws CoreException {
		String id = cfgEl.getAttribute(ATTR_ID);
		if (id!=null && id.length()>0)
			errorParser.setId(id);
		String name = cfgEl.getAttribute(ATTR_NAME);
		if (name!=null && name.length()>0)
			errorParser.setName(name);

		if (errorParser instanceof RegexErrorParser) {
			RegexErrorParser regexErrorParser = (RegexErrorParser)errorParser;

			for (IConfigurationElement cepat : cfgEl.getChildren()) {
				if (cepat.getName().equals(ELEM_PATTERN)) {

					boolean eat = ! Boolean.FALSE.toString().equals(cepat.getAttribute(ATTR_EAT_LINE));
					regexErrorParser.addPattern(new RegexErrorPattern(cepat.getAttribute(ATTR_REGEX),
							cepat.getAttribute(ATTR_FILE),
							cepat.getAttribute(ATTR_LINE),
							cepat.getAttribute(ATTR_DESCRIPTION),
							cepat.getAttribute(ATTR_VARIABLE),
							stringToSeverity(cepat.getAttribute(ATTR_SEVERITY)),
							eat));
				}
			}
		}
	}

	/**
	 * Return error parser as stored in internal list.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 * Use {@link #getErrorParserCopy(String, boolean)} instead.
	 *
	 * @param id - ID of error parser
	 * @return internal instance of error parser
	 */
	public static IErrorParser getErrorParserInternal(String id) {
		IErrorParserNamed errorParser = fAvailableErrorParsers.get(id);
		if (errorParser instanceof ErrorParserNamedWrapper)
			return ((ErrorParserNamedWrapper)errorParser).getErrorParser();
		return errorParser;
	}

	/**
	 * Set and store in workspace area user defined error parsers.
	 *
	 * @param errorParsers - array of user defined error parsers
	 * @throws CoreException in case of problems
	 */
	public static void setUserDefinedErrorParsers(IErrorParserNamed[] errorParsers) throws CoreException {
		setUserDefinedErrorParsersInternal(errorParsers);
		serializeUserDefinedErrorParsers();
	}

	/**
	 * Internal method to set user defined error parsers in memory.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 * Use {@link #setUserDefinedErrorParsers(IErrorParserNamed[])}.
	 *
	 * @param errorParsers - array of user defined error parsers
	 */
	public static void setUserDefinedErrorParsersInternal(IErrorParserNamed[] errorParsers) {
		if (errorParsers==null) {
			fUserDefinedErrorParsers = null;
		} else {
			Set<IErrorParserNamed> sortedErrorParsers = new TreeSet<IErrorParserNamed>(new ErrorParserComparator());
			sortedErrorParsers.addAll(Arrays.asList(errorParsers));
			fUserDefinedErrorParsers= new LinkedHashMap<String, IErrorParserNamed>();
			// set customized list
			for (IErrorParserNamed errorParser : sortedErrorParsers) {
				fUserDefinedErrorParsers.put(errorParser.getId(), errorParser);
			}
		}
		recalculateAvailableErrorParsers();
	}

	/**
	 * @return available error parsers IDs which include contributed through extension + user defined ones
	 * from workspace
	 */
	public static String[] getErrorParserAvailableIds() {
		return fAvailableErrorParsers.keySet().toArray(new String[0]);
	}

	/**
	 * @return IDs of error parsers contributed through error parser extension point.
	 */
	public static String[] getErrorParserExtensionIds() {
		return fExtensionErrorParsers.keySet().toArray(new String[0]);
	}

	/**
	 * @return default error parsers IDs to be used if error parser list is empty.
	 */
	public static String[] getUserDefinedErrorParserIds() {
		if (fUserDefinedErrorParsers!=null)
			return fUserDefinedErrorParsers.keySet().toArray(new String[0]);
		return null;
	}

	/**
	 * Set and store default error parsers IDs to be used if error parser list is empty.
	 *
	 * @param ids - default error parsers IDs
	 * @throws BackingStoreException in case of problem with storing
	 */
	public static void setDefaultErrorParserIds(String[] ids) throws BackingStoreException {
		setDefaultErrorParserIdsInternal(ids);
		serializeDefaultErrorParserIds();
	}

	/**
	 * Set default error parsers IDs in internal list.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 * Use {@link #setDefaultErrorParserIds(String[])}.
	 *
	 * @param ids - default error parsers IDs
	 */
	public static void setDefaultErrorParserIdsInternal(String[] ids) {
		if (ids==null) {
			fDefaultErrorParserIds = null;
		} else {
			fDefaultErrorParserIds = new ArrayList<String>(Arrays.asList(ids));
		}
		recalculateAvailableErrorParsers();
	}

	/**
	 * @return default error parsers IDs to be used if error parser list is empty.
	 */
	public static String[] getDefaultErrorParserIds() {
		if (fDefaultErrorParserIds==null) {
			return fAvailableErrorParsers.keySet().toArray(new String[0]);
		}
		return fDefaultErrorParserIds.toArray(new String[0]);
	}

	/**
	 * @param id - ID of error parser
	 * @param isExtension - if {@code true} get unmodified copy of error parser defined as extension
	 * @return cloned copy of error parser. Note that {@link ErrorParserNamedWrapper} returns
	 * shallow copy with the same instance of underlying error parser.
	 */
	public static IErrorParserNamed getErrorParserCopy(String id, boolean isExtension) {
		IErrorParserNamed errorParser = isExtension ? fExtensionErrorParsers.get(id) : fAvailableErrorParsers.get(id);

		try {
			if (errorParser instanceof RegexErrorParser) {
				return (RegexErrorParser) ((RegexErrorParser)errorParser).clone();
			} else if (errorParser instanceof ErrorParserNamedWrapper) {
				return (ErrorParserNamedWrapper) ((ErrorParserNamedWrapper)errorParser).clone();
			}
		} catch (CloneNotSupportedException e) {
			CCorePlugin.log(e);
		}
		return errorParser;
	}

}
