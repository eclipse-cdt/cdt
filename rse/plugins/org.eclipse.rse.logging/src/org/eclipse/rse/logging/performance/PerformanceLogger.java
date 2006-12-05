/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.logging.performance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A performance measurement class for benchmarking.
 * This performance framework provides stopwatch functions
 * for calculating elapsed time for an operation.
 * <p>
 * This class should be used only at development time since
 * it prints to System.out
 * and contains non-translated strings.
 * <p>
 * It is highly likely that this class will be deprecated in 2.0.
 * Use TPTPs tools for performance monitoring and logging.
 * <pre>
 * Usage example
 *	Method_A { 
 *		String key = PerformanceLogger.register("RSE","WDSC","5120");
 *		PerformanceLogger.start(key, "OP1"); //CallerID is OP1
 *		Method_B();
 *		PerformanceLogger.stop(key);
 *	}
 * 
 *	Method_B {
 *		PerformanceLogger.start("RSE"); //"RSE" component, CalleID="class.method"
 *		// Do something
 *		PerformanceLogger.stop("RSE");
 *	}
 * 
 *	Method_C {
 *		PerformanceLogger.start(); //Use the default component for recording
 *		// Do something
 *		PerformanceLogger.stop();
 *	}
 *</pre>			
 */

public class PerformanceLogger {

	public final static boolean _ENABLE_PERFORMANCE_LOGGING_IBM_INTERNAL_ = false;
	public final static int OPTION_GET_ALL = 1;
	public final static int OPTION_GET_FEATURE = 2;
	public final static int OPTION_GET_VERSION = 3;
	private final static Object[] EMPTY = {};
	private final static String ELEMENT_TASK = "Task"; //$NON-NLS-1$
	private final static String ATTRIBUTE_NAME_TASKID = "CallerID"; //$NON-NLS-1$
	private final static String DEFAULT_COMPONENT = "_PERFORMANCELOGGER_"; //$NON-NLS-1$
	private static boolean ENABLE_PERFORMANCE_LOGGING = false; /*for user logging enabling */
	//static long currentAssignedID = -1;
	private static long samplingTime = -1; /* Elapsed time for normalization operation */
	private static boolean _initialized = false;
	private static HashMap perfLogRegistry = new HashMap();

	/*
	 * Static initializer to normalize this logger.
	 */
	static {
		normalize();
	}

	static class StartData {
		long startTime = -1;
		long stopTime = -1;
		String userID = null;
		String startThread = null;
		String startMethod = null;
		String stopThread = null;
		String stopMethod = null;
		Element node = null;
	}

	class ComponentData {
		String component = null;
		String timeStamp = null;
		String feature = null;
		String version = null;
		String XMLFileID = null;
		File XMLFile = null;
		Document doc = null;
		Stack taskStack = new Stack();

		ComponentData(String comp_id) {
			component = comp_id;
		}
	}

	private static void printMessage(String message, Object[] data) {
		System.out.println(MessageFormat.format(message, data));
	}

	private static void printMessage(String message, Object value) {
		printMessage(message, new Object[] {value});
	}
	
	private static void printMessage(String message) {
		printMessage(message, EMPTY);
	}

	/**
	 * Enable performance logging
	 * The flag ENABLE_PERFORMANCE_LOGGING is enabled(true or false)
	 * @param enable true or false
	 */
	public static void enablePerformanceLogging(boolean enable) {
		ENABLE_PERFORMANCE_LOGGING = enable;
	}

	/**
	 * Check if logging enabled.
	 * @return boolean ENABLE_PERFORMANCE_LOGGING
	 */
	public static boolean isPerformanceLoggingEnabled() {
		return ENABLE_PERFORMANCE_LOGGING;
	}

	/** public static String register(String comp_id) : registering a component using default
	 * @param comp_id Component to be registered
	 * @return component registered with no product info
	 */
	public static String register(String comp_id) {
		return register(comp_id, "", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** Registers component using default component id
	 * @param feature Identifier for Product Feature attribute in XML
	 * @param version Identifier for Product Version attribute in XML
	 * @return default component key 
	 */
	public static String register(String feature, String version) {
		return register(DEFAULT_COMPONENT, feature, version);
	}

	/**
	 * Registering a component.
	 * An XML file is created by concatenating comp_id, feature and version with time stamp appended
	 * @param comp_id Component to be registered
	 * @param feature Identifier for Product Feature attribute in XML
	 * @param version Identifier for Product Version attribute in XML
	 * @return comp_id as the registered key
	 */
	public static String register(String comp_id, String feature, String version) {

		if ((comp_id == null) || (comp_id.length() == 0)) {
			printMessage("PerformanceLogger:register(): Cannot register null or empty component id."); //$NON-NLS-1$
			return comp_id;
		}

		if (perfLogRegistry.containsKey(comp_id)) {
			printMessage("PerformanceLogger:register(): component {0} already registered", comp_id); //$NON-NLS-1$
			return comp_id;
		}

		ComponentData compData = new PerformanceLogger().new ComponentData(comp_id);

		Calendar time = Calendar.getInstance();
		compData.timeStamp = time.getTime().toString();
		String userID = System.getProperty("user.name"); //$NON-NLS-1$
		String idTemplate = "{0}_{1}_{2}_{3}_perf.{4}.xml"; //$NON-NLS-1$
		String id = MessageFormat.format(idTemplate, new Object[] {comp_id, userID, feature, version, compData.timeStamp});
		compData.XMLFileID = id.replace(' ', '_').replace(':', '_');
		compData.XMLFile = new File(compData.XMLFileID);
		compData.feature = feature;
		compData.version = version;
		generateXMLFile(compData);
		compData.taskStack = new Stack();
		perfLogRegistry.put(comp_id, compData);
		_initialized = true;
		printMessage("PerformanceLogger: XML file created is {0}.", compData.XMLFile.getAbsolutePath()); //$NON-NLS-1$
		return comp_id;
	}

	/**
	 * De-register the default component.
	 * 	Default component "_PERFORMANCELOGGER_" removed,
	 * 	start() will be disabled.
	 */
	public static void deRegister() {
		perfLogRegistry.remove(DEFAULT_COMPONENT);
		printMessage("SystemPerformanceLogger: default component de-registered"); //$NON-NLS-1$
	}

	/**
	 * De-register a component.
	 * 	component identified by key removed,
	 * 	start(comp_id) will be disabled.
	 * @param key component to be removed
	 */
	public static void deRegister(String key) {
		perfLogRegistry.remove(key);
		printMessage("SystemPerformanceLogger: component {0} de-registered", key); //$NON-NLS-1$
	}

	/**
	 * Set the normalization unit for this run. Based on a standard method for class instance initialization.
	 * @return a string containing the unit.
	 */
	public static String normalize() {
		/*
		 * Execute some standard code and time it to generate our normalization interval.
		 * Return the value to attempt to make sure it is not optimized by the compiler.
		 */
		long startTime = System.currentTimeMillis();
		Double q = new Double(0);
		int i = 0;
		int n = 1000000;
		for (i = 0; i < n; i++) {
			Double dd = new Double(n);
			Double dr = new Double(n + i);
			q = new Double(dd.doubleValue() / dr.doubleValue());
		}
		double val = q.doubleValue() / i;
		long stopTime = System.currentTimeMillis();
		samplingTime = stopTime - startTime;
		String template = "SystemPerformanceLogger::Normalization Elapsed time = {0} {1}"; //$NON-NLS-1$
		String result = MessageFormat.format(template, new Object[] {new Long(samplingTime), new Double(val)});
		return result;
	}

	/**
	 * public static long start(): start timer using default component
	 * 	The Task values will be recorded in the default component XML file
	 * @return
	 * 	- started time milliseconds
	 */
	public static long start() {
		if (_initialized == false) {
			register(DEFAULT_COMPONENT, "", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		/*Use the class method name for CallerID*/
		String methodPath = getMethodName(true);
		return start(DEFAULT_COMPONENT, methodPath);
	}

	/**
	 * public static long start(String comp_id): start timer for component comp_id using default TaskID
	 * @param comp_id : component registered previously by register(comp_id,..)
	 * @return started time in milliseconds.
	 */
	public static long start(String comp_id) {
		String methodPath = getMethodName(true);
		return start(comp_id, methodPath);
	}

	/**
	 * public long start(String comp_id, String call_id): start the timer for registered component comp_id
	 * @param
	 * 	comp_id is the registered component
	 * 	call_id is the "CallID" attribute value for the XML tag "Task"
	 * @return
	 * 	- started time in milliseconds.
	 */
	public static long start(String comp_id, String call_id) {

		if (perfLogRegistry.containsKey(comp_id) == false) {
			printMessage("PerformanceLogger:start(): component {0} not registered", comp_id); //$NON-NLS-1$
			return -1;
		}

		ComponentData cd = (ComponentData) perfLogRegistry.get(comp_id);
		StartData td = new StartData();

		td.userID = call_id;
		td.startThread = Thread.currentThread().toString();
		td.startMethod = getMethodName(false);

		/* Create the new Task Element in the DOC */
		try {

			//BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cd.XMLFile), "UTF8"));
			Element root = cd.doc.getDocumentElement();
			Element task = cd.doc.createElement(ELEMENT_TASK);
			task.setAttribute(ATTRIBUTE_NAME_TASKID, td.userID);
			task.setAttribute("StartAt", td.startMethod); //$NON-NLS-1$
			task.setAttribute("StartThread", td.startThread); //$NON-NLS-1$

			td.node = task;

			/* Check if start() is nested by checking if the TaskStack is empty */
			if (cd.taskStack.isEmpty()) { /*Empty==>not a nested start()*/
				root.appendChild(task);
			} else { /*Not empty ==> this start() is nested*/

				StartData sd = (StartData) cd.taskStack.peek(); /*Peek the parent CallID on the stack*/
				sd.node.appendChild(task);
			}
		} catch (DOMException e) {
			printMessage("PerformanceLogger::updateXMLFileatStart DOM Error: {0}", e); //$NON-NLS-1$
		}

		/*Read the current time save it on stack */
		td.startTime = System.currentTimeMillis();
		cd.taskStack.push(td);
		return td.startTime;
	}

	/**
	 * public static long stop(): stop timer for default component
	 * 	The Task values will be recorded in the default component XML file
	 * @return
	 * 	- started time milliseconds
	 */
	public static long stop() {
		return stop(DEFAULT_COMPONENT);
	}

	/**
	 * public long stop(String comp_id): Stopping the timer for component comp_id
	 * @return
	 * 	- stopped time in milliseconds.
	 */
	public static long stop(String comp_id) {

		long st = System.currentTimeMillis();
		ComponentData cd = (ComponentData) perfLogRegistry.get(comp_id);
		if (cd == null) {
			printMessage("SystemPerformanceLogger::stop(): invalid registration key"); //$NON-NLS-1$
			return 0;
		}
		long result = 0;
		try {
			StartData td = (StartData) cd.taskStack.pop();
			td.stopTime = st;
			td.stopThread = Thread.currentThread().toString();
			td.stopMethod = getMethodName(false);
			updateXMLFileAtStop(cd, td);
			result = td.stopTime;
		} catch (EmptyStackException e) {
			printMessage("SystemPerformanceLogger:: Probably too many stop() function calls. - {0}", e); //$NON-NLS-1$
		}
		return result;
	}

	/**
	 * Create an XML file with "Product" and "System" tags.
	 * @param cd component data for creating the XML 
	 */
	private static void generateXMLFile(ComponentData cd) {
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cd.XMLFile), "UTF8")); //$NON-NLS-1$
			//				DOMImplementation impl = new DOMImplementationImpl();
			//				cd.doc = impl.createDocument(null, "Benchmark", null);		
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			DOMImplementation impl = builder.getDOMImplementation();
			cd.doc = impl.createDocument(null, "Benchmark", null); //$NON-NLS-1$
			// get root element and set attributes
			Element root = cd.doc.getDocumentElement();
			root.setAttribute("BenchmarkID", cd.XMLFileID); //$NON-NLS-1$
			root.setAttribute("TimeStamp", cd.timeStamp); //$NON-NLS-1$

			Element system = cd.doc.createElement("System"); //$NON-NLS-1$
			Element product = cd.doc.createElement("Product"); //$NON-NLS-1$

			product.setAttribute("Feature", cd.feature); //$NON-NLS-1$
			product.setAttribute("Version", cd.version); //$NON-NLS-1$
			root.appendChild(product);

			system.setAttribute("OSName", System.getProperty("os.name")); //$NON-NLS-1$ //$NON-NLS-2$
			system.setAttribute("OSVersion", System.getProperty("os.version")); //$NON-NLS-1$ //$NON-NLS-2$
			system.setAttribute("JavaVersion", System.getProperty("java.version")); //$NON-NLS-1$ //$NON-NLS-2$
			system.setAttribute("JavaVMVersion", System.getProperty("java.vm.version")); //$NON-NLS-1$ //$NON-NLS-2$
			system.setAttribute("JavaClassPath", System.getProperty("java.class.path")); //$NON-NLS-1$ //$NON-NLS-2$
			system.setAttribute("JavaLibraryPath", System.getProperty("java.library.path")); //$NON-NLS-1$ //$NON-NLS-2$
			root.appendChild(system);

			Element norm = cd.doc.createElement("_NORMALIZATION_VALUES"); //$NON-NLS-1$
			Long ems = new Long(samplingTime);
			norm.setAttribute("ElapsedTime", ems.toString()); //$NON-NLS-1$
			root.appendChild(norm);

			/* Insert comments for Task tag */
			Comment cmt1 = cd.doc.createComment("Each Task element represents one start/stop timer operation"); //$NON-NLS-1$
			Comment cmt2 = cd.doc.createComment("Time recorded is in milliseconds"); //$NON-NLS-1$
			Comment cmt3 = cd.doc.createComment("NormalizedFactor is the performance indicator. A larger value than the previous run might indicate performance degradation."); //$NON-NLS-1$
			root.appendChild(cmt1);
			root.appendChild(cmt2);
			root.appendChild(cmt3);

			try {
				Source source = new DOMSource(cd.doc);
				Result result = new StreamResult(writer);
				Transformer t = TransformerFactory.newInstance().newTransformer();
				t.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
				t.transform(source, result);
			} catch (TransformerConfigurationException e2) {
			} catch (TransformerFactoryConfigurationError e2) {
			} catch (TransformerException e2) {
			}

			//				OutputFormat fmt = new OutputFormat(cd.doc);
			//				fmt.setLineSeparator(LineSeparator.Windows);
			//				fmt.setIndenting(true);
			//				fmt.setPreserveSpace(false);
			//				//writer.flush();
			//				XMLSerializer serializer = new XMLSerializer(writer, fmt);
			//				serializer.serialize(cd.doc);
			//				writer.close();

		} catch (IOException e) {
			printMessage("PerformanceLogger::updateXML IO Error: {0}", e); //$NON-NLS-1$
		} catch (ParserConfigurationException e) {
			printMessage("PerformanceLogger::updateXML Parser Configuration Error: {0}", e); //$NON-NLS-1$
		} catch (DOMException e) {
			printMessage("PerformanceLogger::updateXML DOM Error: {0}", e); //$NON-NLS-1$
		}
	}

	/**
	 * Update XML file with performance measurement info
	 * 	- A "Task" tag is created in the XML file with the current start and stop timer information.
	 */
	private static void updateXMLFileAtStop(ComponentData cd, StartData td) {

		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cd.XMLFile), "UTF8")); //$NON-NLS-1$
			cd.doc.getDocumentElement();
			Element task = td.node;

			/* Construct Long class insatnce for string manipulation  */
			Long ems = new Long(td.stopTime - td.startTime);
			Long sms = new Long(td.startTime);
			Long tms = new Long(td.stopTime);
			/*Calculate the normalization factor*/
			String normalizedFactor = "invalid"; //$NON-NLS-1$
			if (samplingTime > 0) {
				Long sam = new Long(samplingTime);
				Double val = new Double(ems.doubleValue() / sam.doubleValue());
				normalizedFactor = val.toString();
			}

			/* Update the document */
			task.setAttribute("ElapsedTime", ems.toString()); //$NON-NLS-1$
			task.setAttribute("NormalizedFactor", normalizedFactor); //$NON-NLS-1$
			task.setAttribute("StartTime", sms.toString()); //$NON-NLS-1$
			task.setAttribute("StopTime", tms.toString()); //$NON-NLS-1$
			task.setAttribute("StartAt", td.startMethod); //$NON-NLS-1$
			task.setAttribute("StartThread", td.startThread); //$NON-NLS-1$
			task.setAttribute("StopAt", td.stopMethod); //$NON-NLS-1$
			task.setAttribute("StopThread", td.stopThread); //$NON-NLS-1$

			try {
				Source source = new DOMSource(cd.doc);
				Result result = new StreamResult(writer);
				Transformer t = TransformerFactory.newInstance().newTransformer();
				t.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
				t.transform(source, result);
			} catch (TransformerConfigurationException e2) {
			} catch (TransformerFactoryConfigurationError e2) {
			} catch (TransformerException e2) {
			}

			/*Now save the DOM*/
			//				OutputFormat fmt = new OutputFormat(cd.doc);
			//				fmt.setLineSeparator(LineSeparator.Windows);
			//				fmt.setIndenting(true);
			//				fmt.setPreserveSpace(false);
			//				//writer.flush();
			//				XMLSerializer serializer = new XMLSerializer(writer, fmt);
			//				serializer.serialize(cd.doc);
			//				writer.close();
		} catch (java.io.IOException e) {
			printMessage("PerformanceLogger::updateXMLFileAtStop IO Error: {0}", e); //$NON-NLS-1$
		} catch (DOMException e) {
			printMessage("PerformanceLogger::updateXMLFileAtStop DOM Error: {0}", e); //$NON-NLS-1$
		}

	}

	/**
	 * public String geCurrentProductInfo(int req, String comp_id) : retrieve the product information.
	 * @param 
	 * 	req : OPTION_GET_FEATURE/OPTION_GET_VERSION
	 * 	comp_id : the component id
	 * @return
	 *   "OPTION_GET_FEATURE":product feature as specified in register()
	 *	"OPTION_GET_VERSION": product version as specified in register()
	 *   no match: null
	 */
	public static String getCurrentProductInfo(int req, String comp_id) {
		ComponentData cd = (ComponentData) perfLogRegistry.get(comp_id);
		if (cd == null) {
			printMessage("PerformanceLogger::getCurrentProductInfo invalid comp_id"); //$NON-NLS-1$
			return null;
		}

		if (req == OPTION_GET_FEATURE)
			return cd.feature;
		else if (req == OPTION_GET_VERSION) return cd.version;
		return null;
	}

	/**
	 * public String getXMLFileName(String comp_id) : get the XML file pathname
	 * @return
	 * 	The XML file fullpath name.
	 */
	public static String getXMLFileName(String comp_id) {
		ComponentData cd = (ComponentData) perfLogRegistry.get(comp_id);
		return cd.XMLFile.getAbsolutePath();
	}

	/**
	 * public String getMethodName(boolean parsed) : get the method name
	 * @param
	 * 	parsed : true or false
	 * @return
	 * 	if true method name is returned as class.method.
	 */
	private static String getMethodName(boolean parsed) {

		String methodPath = null;
		Throwable e = new Throwable();
		StringWriter strwriter = new StringWriter(100);
		e.printStackTrace(new java.io.PrintWriter(strwriter));
		String stack = strwriter.toString();
		StringTokenizer tokenizer = new StringTokenizer(stack, "\r\n"); //$NON-NLS-1$
		/*
		 * Here to parse the exception string to get the caller which is the current method location
		 * to be obtained. The Exception stack should show the PerformanceLogger start() in the stack first:
		 * 	-java.lang.Exception
		 * 	-at org.eclipse.rse.logging.performance.PerformanceLogger.start(PerformanceLogger.java:151)
		 *	-at org.eclipse.rse.logging.performance.PerformanceLogger.start(PerformanceLogger.java:135)
		 * Depending if the caller is using default task ID or not, so the caller is the 3rd or the 4th in the stack.
		 */

		for (int i = 0; tokenizer.hasMoreTokens(); i++) {
			methodPath = tokenizer.nextToken();
			if ((methodPath.indexOf("java.lang.Throwable") == -1) && (methodPath.indexOf("logging.performance.PerformanceLogger") == -1)) break; //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (methodPath != null) {
			methodPath = methodPath.substring(4);
			if (parsed) {
				try {
					int i = methodPath.indexOf('(');
					if (i != -1) methodPath = methodPath.substring(0, i); //strip of the substring enclosed in ()
					//Now we have "org.eclipse.rse.logging.performance.PerformanceLogger.start"						  
					i = methodPath.lastIndexOf('.'); //Get the method name after the last period (.)			
					String methodName = methodPath.substring(i + 1); //Now we have the method name "start"
					String className = methodPath.substring(0, i); //remove method name from the string
					//We are left with "org.eclipse.rse.logging.performance.PerformanceLogger"
					i = className.lastIndexOf('.');
					if (i != -1) className = className.substring(i + 1); //Now we have the class name "PerformanceLogger"
					methodPath = className + "." + methodName; //$NON-NLS-1$
				} catch (IndexOutOfBoundsException ex) {
					printMessage("PerformanceLogger:getMethodName exception {0}", ex); //$NON-NLS-1$
				}
			}
		}
		return methodPath; /*delete " at" in the beginning of the string */
	}

	/**
	 * Retrieve the system information.
	 * These values will be retrieved and printed in stdout:
	 * <ul>
	 *    <li>java.version</li>
	 *	  <li>java.vm.version</li>
	 *    <li>java.class.version</li>
	 *    <li>java.class.path</li>
	 *    <li>java.library.path</li>
	 *    <li>os.name</li>
	 *    <li>os.version</li>
	 * </ul>
	 */
	public static void listSystemProfile() {
		System.out.println("java version : " + System.getProperty("java.version")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println("OS name : " + System.getProperty("os.name")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println("OS version : " + System.getProperty("os.version")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println("working dir : " + System.getProperty("user.dir")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println("home dir : " + System.getProperty("home.dir")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * public static void main() : This main is used for testing this PerformanceLogger functions.
	 * 	The objective is to check the XML output format for nested start() calls. 
	 */
	public static void main(String[] args) {

		int i = 0;

		if (isPerformanceLoggingEnabled()) {
			PerformanceLogger.start(); //Start timer using default component
		}
		for (i = 0; i < 1000000; i++) {
			//empty performance test loop
		}
		if (isPerformanceLoggingEnabled()) {
			PerformanceLogger.stop();
		}

		PerformanceLogger.enablePerformanceLogging(true);
		String key = PerformanceLogger.register("", "WDSC", "5120"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		key = PerformanceLogger.register("RSE", "WDSC", "5120"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		PerformanceLogger.deRegister("XXX"); //not registered key previously //$NON-NLS-1$
		System.out.println("Product info : " + PerformanceLogger.getCurrentProductInfo(PerformanceLogger.OPTION_GET_FEATURE, key) + " " //$NON-NLS-1$ //$NON-NLS-2$
				+ PerformanceLogger.getCurrentProductInfo(PerformanceLogger.OPTION_GET_VERSION, key));
		PerformanceLogger.start(key, "NOT_NESTED_1"); //$NON-NLS-1$
		for (i = 0; i < 1000000; i++) {
			//empty performance test loop
		}
		PerformanceLogger.stop(key);

		PerformanceLogger.start(key, "NESTED_ONE"); //$NON-NLS-1$
		for (i = 0; i < 500; i++) {
			//empty performance test loop
		}
		PerformanceLogger.start(key, "NESTED_ONE_CHILD"); //$NON-NLS-1$
		for (i = 0; i < 300; i++) {
			//empty performance test loop
		}
		PerformanceLogger.stop(key);
		PerformanceLogger.stop(key);

		PerformanceLogger.start(key, "NOT_NESTED_2"); //$NON-NLS-1$
		for (i = 0; i < 2000000; i++) {
			//empty performance test loop
		}
		PerformanceLogger.stop(key);

		PerformanceLogger.start(key, "NESTED_THREE"); //$NON-NLS-1$
		for (i = 0; i < 300; i++) {
			//empty performance test loop
		}
		PerformanceLogger.start(key, "NESTED_TWO_CHILD1"); //$NON-NLS-1$
		PerformanceLogger.start(key, "NESTED_TWO_CHILD2"); //$NON-NLS-1$
		for (i = 0; i < 4000; i++) {
			//empty performance test loop
		}
		PerformanceLogger.start(key, "NESTED_TWO_CHILD3"); //$NON-NLS-1$
		for (i = 0; i < 6000; i++) {
			//empty performance test loop
		}
		PerformanceLogger.stop(key);
		PerformanceLogger.stop(key);
		PerformanceLogger.stop(key);
		PerformanceLogger.stop(key);

		PerformanceLogger.start("ABC"); //Expect error: not registered		 //$NON-NLS-1$
		PerformanceLogger.start(key); //record timer in the previous registered component
		for (i = 0; i < 3000000; i++) {
			//empty performance test loop
		}
		PerformanceLogger.stop(key);
		key = PerformanceLogger.register(key); // Expect error: already registered
		PerformanceLogger.deRegister(key);
		key = PerformanceLogger.register(key);
	}

}