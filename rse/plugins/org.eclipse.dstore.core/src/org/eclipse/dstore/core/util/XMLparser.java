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

package org.eclipse.dstore.core.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Stack;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreResources;

/**
 * <p>
 * This class is used to deserialize data received from a file or a socket.  
 * </p>
 * <p>
 * When tags indicate that data is being received, the byte stream is deserialized
 * as a DataStore tree.  When deserialized data maps to existing DataElements in 
 * the DataStore, those elements are updated directly.  Any deserialized data 
 * that maps to within an existing DataElement, that does not already exist, gets
 * created under the existing DataElement.  When parsing DataElement XML, there is
 * no intermediate DOM - rather the DOM is the DataStore itself.  
 * </p>
 * <p>
 * When tags indicate that a byte stream or file is being received, bytes are
 * sent the the current DataStore <code>ByteStreamHandler</code> to be saved on disk.
 * </p>
 */
public class XMLparser
{
	
	public static final int IO_SOCKET_READ_TIMEOUT = 3600000;
	public static final long KEEPALIVE_RESPONSE_TIMEOUT = 60000;
	public static final boolean VERBOSE_KEEPALIVE = false;
	
	private DataStore _dataStore;
	private DataElement _rootDataElement;
	private Stack _tagStack;
	private Stack _objStack;

	private boolean _isFile;
	private boolean _isClass;
	private boolean _isRequestClass;
	private boolean _isKeepAlive;
	private boolean _isKeepAliveConfirm;
	private boolean _isSerialized;
	
	private String _tagType;

	private byte[] _byteBuffer;
	private byte[] _fileByteBuffer;
	private int _maxBuffer;

	private boolean _panic = false;
	private Throwable _panicException = null;
	
	private boolean _isKeepAliveCompatible = false;
	private boolean _firstTime = true;
	
	private KeepAliveRequestThread _kart = null;
	private KeepAliveRequestThread _initialKart = null;
	
	public static String STR_DATAELEMENT = "DataElement";

	public static String STR_BUFFER_START = "<Buffer>";
	public static String STR_BUFFER_END   = "</Buffer>";
	public static String STR_BUFFER       = "Buffer";

	public static String STR_STATUS       = "status";
	public static String STR_STATUS_DONE  = "done";
	public static String STR_STATUS_ALMOST_DONE = "almost done";
	
	public static String STR_FILE         = "File";
	public static String STR_CLASS		   = "Class";
	public static String STR_REQUEST_CLASS= "RequestClass";
	public static String STR_SERIALIZED   = "Serialized";
	
	public static String STR_AMP = "&amp;";
	public static String STR_QUOTE = "&quot;";
	public static String STR_APOS = "&apos;";
	public static String STR_LT = "&lt;";
	public static String STR_GT = "&gt;";
	public static String STR_SEMI = "&#59;";
	
	
	/**
	 * Constructor
	 * @param dataStore the associated DataStore
	 */
	public XMLparser(DataStore dataStore)
	{
		_dataStore = dataStore;
		_tagStack = new Stack();
		_objStack = new Stack();
		_maxBuffer = 100000;
		_byteBuffer = new byte[_maxBuffer];
	}

	/**
	 * Read a file from the pipe
	 * @param reader the pipe reader
	 * @param size the number of bytes to read
	 * @param path the path of the file where the received bytes should be inserted
	 */
	public void readFile(BufferedInputStream reader, int size, String path, String byteStreamHandlerId)
	{
	
		Runtime rt = Runtime.getRuntime();
		//long totalMem = rt.totalMemory();
		long freeMem = rt.freeMemory();
	
		if (size * 100 > freeMem)
		{
			rt.gc();
		}
		
		if (_fileByteBuffer == null || _fileByteBuffer.length < size)
		{
			try 
			{
				_fileByteBuffer = new byte[size];
			}
			catch (OutOfMemoryError e)
			{
				System.exit(-1);
			}
		}

		int written = 0;
		
//		// hack to deal with platform inconsistencies
//		// only needed on the server side
//		if (!_dataStore.isVirtual())
//		{
//			try
//			{
//				synchronized (reader)
//				{
//					int first = reader.read();
//					
//					if (first != 10) {
//						written = 1;
//						buffer[0] = (byte) first;
//					}
//					else {
//						System.out.println("First byte is 10!");
//					}
//				}
//			}
//			catch (IOException e)
//			{
//				_dataStore.trace(e);
//			}
//		}

		while (written < size)
		{
			try
			{
				int read = reader.read(_fileByteBuffer, written, size - written);
				written += read;
			}
			catch (SocketException se)
			{
			    // DKM- socket exception means connection is gone
			    //  need bail now!
			    _dataStore.trace(se);
				handlePanic(se);
				return;
			}
			catch (IOException e)
			{
				_dataStore.trace(e);
				handlePanic(e);
			}
			catch (Error err)
			{
				System.out.println("error!"); 
				handlePanic(err);
			}
		}

		if (_tagType.startsWith("File.Append"))
		{
			boolean binary = _tagType.equals("File.Append.Binary");
			_dataStore.appendToFile(path, _fileByteBuffer, size, binary, byteStreamHandlerId);
		}
		else
		{
			boolean binary = _tagType.equals("File.Binary");
			_dataStore.saveFile(path, _fileByteBuffer, size, binary, byteStreamHandlerId);
		}
	}

	public boolean readInstance(BufferedInputStream reader, int size, String classbyteStreamHandlerId)
	{
		byte[] buffer = new byte[size];
		int written = 0;

		while (written < size)
		{
			try
			{
				int read = reader.read(buffer, written, size - written);
				written += read;
			}
			catch (SocketException se)
			{
			    // DKM- socket exception means connection is gone
			    //  need bail now!
			    _dataStore.trace(se);
				handlePanic(se);
				return false;
			}
			catch (IOException e)
			{
				_dataStore.trace(e);
				handlePanic(e);
				return false;
			}
		}
		_dataStore.saveClassInstance(buffer, size, classbyteStreamHandlerId);
		return true;
	}
	
	/**
	 * Read a class file from the pipe
	 * @param reader the pipe reader
	 * @param size the number of bytes to read
	 * @param className the name of the class defined by the byte array.
	 * @param classbyteStreamHandlerId the name of the classByteStreamHandler that will receive the bytes of the file.
	 * @return whether the operation is successful
	 */
	public boolean readClass(BufferedInputStream reader, int size, String className, String classbyteStreamHandlerId)
	{
		byte[] buffer = new byte[size];
		int written = 0;

		while (written < size)
		{
			try
			{
				int read = reader.read(buffer, written, size - written);
				written += read;
			}
			catch (SocketException se)
			{
			    // DKM- socket exception means connection is gone
			    //  need bail now!
			    _dataStore.trace(se);
				handlePanic(se);
				return false;
			}
			catch (IOException e)
			{
				_dataStore.trace(e);
				handlePanic(e);
				return false;
			}
		}
		_dataStore.saveClass(className, buffer, size, classbyteStreamHandlerId);
		return true;
	}
	
	/**
	 * Reads a line from the pipe
	 * 
	 * @param reader the pipe reader
	 * @return the line received
	 */
	public String readLine(BufferedInputStream reader, Socket socket)
	{
		boolean done = false;
		int offset = 0;

		try
		{
		    boolean inquotes = false;
			while (!done)
			{

				if (_firstTime)
				{
					_initialKart = new KeepAliveRequestThread(KEEPALIVE_RESPONSE_TIMEOUT, _dataStore);
					_firstTime = false;
					if (VERBOSE_KEEPALIVE) System.out.println("Starting initial KeepAlive thread.");
					_initialKart.start();
					continue;
				}
				else if (_initialKart != null && !_initialKart.isAlive())
				{
					if (!_initialKart.failed())
					{
						_isKeepAliveCompatible = true;
						if (VERBOSE_KEEPALIVE) System.out.println("KeepAlive compatible.");
						_initialKart = null;
					}			
					else
					{
						_isKeepAliveCompatible = false;
						if (VERBOSE_KEEPALIVE) System.out.println("KeepAlive incompatible.");
						_initialKart = null;
					}
				}

				int in = -1;
				
				if (_isKeepAliveCompatible)
				{	
					socket.setSoTimeout(IO_SOCKET_READ_TIMEOUT);
					try
					{
						in = reader.read();
					}
					catch (InterruptedIOException e)
					{
						if ((_kart != null) && _kart.failed())
						{
							done = true;
							if (_dataStore.isVirtual()) handlePanic(new Exception("KeepAlive request to server wasnt answered in time."));
							else handlePanic(new Exception("KeepAlive request to client wasnt answered in time."));
							continue;
						}
						else
						{
							_kart = new KeepAliveRequestThread(KEEPALIVE_RESPONSE_TIMEOUT, _dataStore);
							if (VERBOSE_KEEPALIVE) System.out.println("No activity on socket. KeepAlive thread started.");
							_kart.start();
							continue;
						}
					}
				}
				else
				{
					in = reader.read();
				}

				if (in == -1)
				{
					done = true;
					Exception e = new Exception("The connection to the server has been lost.");
					handlePanic(e);
				}
				else
				{
				    if (in <= 0)
				    {
				        done = true;
				    }
				    else
				    {
				    	if (_kart != null) _kart.interrupt();
				    }
					byte aByte = (byte) in;
					switch (aByte)
					{
						case '"':
						    inquotes = !inquotes;
						    break;
						case '\n':
						case '\r':
						case '\0':
						    if (!inquotes)
						        done = true;
						    break;
						default:				       
							break;
					}
					
					if (offset >= _maxBuffer)
					{
						done = true;
					}

					_byteBuffer[offset] = aByte;
					offset++;  
				}
			}
		}
		catch (IOException e)
		{
			_dataStore.trace(e);
			done = true;

			handlePanic(e);

			return null;
		}

		if (offset > 0)
		{
			String result = null;
			try
			{
				result = new String(_byteBuffer, 0, offset, DE.ENCODING_UTF_8);
			}
			catch (IOException e)
			{
				_dataStore.trace(e);
			}
			return result;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Called if an exception occurs during reading of the pipe
	 * @param e the Exception
	 */
	private void handlePanic(Throwable e)
	{
		_panic = true;
		_panicException = e;
	}

	/**
	 * Returns the communications exception if one occurred
	 * @return a exception
	 */
	public Throwable getPanicException()
	{
		return _panicException;
	}

	/**
	 * This method gets called to receive data from the pipe.  It deserializes
	 * DataStore XML documents, creating the appropriate DataElements in appropriate
	 * places in the DataStore tree.  If files are being transmitted it creates
	 * the appropriate files using the DataStore <code>ByteStreamHandler</code>.
	 * 
	 * @param reader the pipe reader
	 * @return the root DataElement of the parsed document
	 * @throws IOException
	 */
	public DataElement parseDocument(BufferedInputStream reader, Socket socket) throws IOException
	{
		_tagStack.clear();
		_objStack.clear();

		_rootDataElement = null;
		_isFile = false;
		_isClass = false;
		_isRequestClass = false;
		_isKeepAlive = false;
		_isKeepAliveConfirm = false;
		_isSerialized = false;
		_tagType = STR_DATAELEMENT;

		DataElement parent = null;
		String matchTag = null;

		boolean done = false;
		while (!done)
		{
			String xmlTag = readLine(reader, socket);
			
			if (xmlTag != null)
			{
				String trimmedTag = xmlTag.trim();
				
				if (_dataStore.getReferenceTag() == null)
				{
					if (trimmedTag.indexOf(DE.P_ISREF + "=") > -1) _dataStore.setReferenceTag(DE.P_ISREF);
					else if (trimmedTag.indexOf(DE.P_REF_TYPE + "=") > -1) _dataStore.setReferenceTag(DE.P_REF_TYPE);
				}

				if (!_tagStack.empty())
				{
					matchTag = (String) _tagStack.peek();
				}
				if (trimmedTag.equals(STR_BUFFER_START))
				{
					_tagType = STR_BUFFER;
					_tagStack.push(STR_BUFFER_END);
				}
				else if (trimmedTag.equals(STR_BUFFER_END))
				{
					_tagType = STR_DATAELEMENT;
					_tagStack.pop();
				}
				else if (_tagType.equals(STR_BUFFER))
				{
					String buffer = convertStringFromXML(xmlTag);
					parent.appendToBuffer(buffer);
				}
				else if ((matchTag != null) && trimmedTag.equals(matchTag))
				{
					if (parent.getType().equals(STR_STATUS))
					{
						if (parent.getName().equals(STR_STATUS_ALMOST_DONE))
						{
							
							parent.setAttribute(DE.A_NAME, STR_STATUS_DONE);
							if (parent.getValue().equals(STR_STATUS_ALMOST_DONE))
							{
								parent.setAttribute(DE.A_VALUE,STR_STATUS_DONE);
							}
							if (_dataStore.isWaiting(parent))
							{
								_dataStore.stopWaiting(parent);
								parent.notifyUpdate();
							}
						}
					}

					_tagStack.pop();
					if (_tagStack.empty())
					{
						done = true;
					}
					else if (_tagStack.size() == 1)
					{
						parent = _rootDataElement;
					}
					else
					{
						parent = (DataElement) _objStack.pop();
					}

				}
				else
				{
					xmlTag = xmlTag.trim();

					if (xmlTag.length() > 3)
					{

						try
						{
							if (parent != null)
							{
								if (_objStack.contains(parent))
								{
								}
								else
								{
									_objStack.push(parent);
								}
							}

							DataElement result = parseTag(xmlTag, parent);

							if (_panic)
							{
								return null;
							}

							if (result != null)
							{
							    result.setUpdated(true);

								if (parent == null && _rootDataElement == null)
								{
									_rootDataElement = result;
									_rootDataElement.setParent(null);
								}

								parent = result;

								if (_isFile && (result != null))
								{
									int size = result.depth();
									String path = result.getSource();
									
									String  byteStreamHandler = result.getName();
									if (path.equals(byteStreamHandler))
									{
									    // older client or server, fall back to default
									    byteStreamHandler = DataStoreResources.DEFAULT_BYTESTREAMHANDLER;
									}
									if (path != null)
									{
										readFile(reader, size, path, byteStreamHandler);
									}
									_isFile = false;
									//_dataStore.deleteObject(parent, result);
								}
								else if (_isClass && (result != null))
								{
									int size = result.depth();
									
									String classbyteStreamHandler = result.getSource();

									if (result.getName() != null)
									{
										boolean success = readClass(reader, size, result.getName(), classbyteStreamHandler);
									}
									_isClass = false;
								}
								else if (_isRequestClass && (result != null))
								{
									result.getDataStore().sendClass(result.getName());
									_isRequestClass = false;
								}
								else if (_isKeepAlive && (result != null))
								{
									if (VERBOSE_KEEPALIVE) System.out.println("KeepAlive request received, sending confirmation.");
									result.getDataStore().sendKeepAliveConfirmation();
									_isKeepAlive = false;
								}
								else if (_isKeepAliveConfirm && (result != null))
								{
									if (VERBOSE_KEEPALIVE) System.out.println("KeepAlive confirmation received.");
									if (_initialKart != null) _initialKart.interrupt();
									_isKeepAliveConfirm = false;
								}
								else if (_isSerialized && (result != null))
								{
									int size = result.depth();
									String classbyteStreamHandler = result.getSource();
									if (result.getName() != null)
									{
										boolean success = readInstance(reader, size, classbyteStreamHandler);
									}
									_isSerialized = false;
								}

								StringBuffer endTag = new StringBuffer("</");
								endTag.append(_tagType);
								endTag.append('>');
								_tagStack.push(endTag.toString());
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
							_dataStore.trace(e);
							return _rootDataElement;
						}
					}
				}
			}

			if (_panic)
				return null;
		}

		DataElement result = _rootDataElement;
		_rootDataElement.setParent(null); // this root is transient

		_rootDataElement = null;
		return result;
	}

	/**
	 * Deserializes a single DataElement from the XML stream.
	 * 
	 * @param fullTag the DataElement XML tag
	 * @param parent the DataElement that container for the deserialized DataElement
	 * @return the parsed DataElement
	 */
	protected synchronized DataElement parseTag(String fullTag, DataElement parent)
	{
		if (!fullTag.startsWith("<"))
			return null;

		try
		{
			fullTag = fullTag.substring(1, fullTag.length() - 1);
		}
		catch (Exception e)
		{
			return null;
		}

		// get type
		int nextSpace = fullTag.indexOf(' ');
		if (nextSpace > 0)
		{
			String[] attributes = new String[DE.A_SIZE];

			// tag type
			String tagType = fullTag.substring(0, nextSpace);
			if (tagType.startsWith(STR_FILE))
			{
				_isFile = true;
				_tagType = tagType;
			}
			else if (tagType.startsWith(STR_CLASS))
			{
				_isClass = true;
				_tagType = tagType;
			}
			else if (tagType.startsWith(STR_REQUEST_CLASS))
			{
				_isRequestClass = true;
				_tagType = tagType;
			}
			else if (tagType.startsWith(STR_SERIALIZED))
			{
				_isSerialized = true;
				_tagType = tagType;
			}

			int index = 0;
			int nextQuote = 0;
			int nextnextQuote = nextSpace;
			while ((index < DE.A_SIZE) && (nextQuote >= 0))
			{
				nextQuote = fullTag.indexOf('\"', nextnextQuote + 1);
				nextnextQuote = fullTag.indexOf('\"', nextQuote + 1);

				if ((nextQuote >= 0) && (nextnextQuote > nextQuote) && (fullTag.length() > nextnextQuote))
				{
					String attribute = fullTag.substring(nextQuote + 1, nextnextQuote);

					attributes[index] = convertStringFromXML(attribute);
					index++;
				}
			}

			DataElement result = null;
			if (attributes.length == DE.A_SIZE)
			{
				String type = attributes[DE.A_TYPE];
				if (type.equals(DataStoreResources.KEEPALIVE_TYPE))
				{
					_isKeepAlive= true;
					result = _dataStore.createTransientObject(attributes);
				}
				else if (type.equals(DataStoreResources.KEEPALIVECONFIRM_TYPE))
				{
					_isKeepAliveConfirm = true;
					result = _dataStore.createTransientObject(attributes);
				}
				
				else if (type.equals(DataStoreResources.DOCUMENT_TYPE))
				{
					String id = attributes[DE.A_ID];
					if (_dataStore.contains(id))
					{
						result = _dataStore.find(id);
						result.removeNestedData();
					}
					else
					{						
						result = _dataStore.createObject(null, attributes);
					}
				}

				else if (_isFile || _isClass || _isSerialized || parent == null)
				{
					result = _dataStore.createTransientObject(attributes);
				}			
				else
				{
					String refType = attributes[DE.A_REF_TYPE];
					boolean isSpirit = false;
					if (refType != null) isSpirit = refType.equals(DataStoreResources.SPIRIT);
					
					if ((refType != null) && (refType.equals(DataStoreResources.TRUE) || refType.equals(DataStoreResources.REFERENCE)))
					{
						// new reference
						String origId = attributes[DE.A_NAME];
						if (_dataStore.contains(origId))
						{

							DataElement to = _dataStore.find(origId);
							if (parent != null)
							{
								result = _dataStore.createReference(parent, to, attributes[DE.A_TYPE], false);

							}
							else
							{
								_dataStore.trace("NULL2!");
							}
						}
						else
						{
							// creating reference to unknown object
							result = _dataStore.createObject(parent, attributes);
						}
					}
					else
					{
						String id = attributes[DE.A_ID];
						if (id == null)
						{
							handlePanic(new Exception(fullTag));
							return null;
						}
	
						if (parent != null && _dataStore.contains(id))
						{
							result = _dataStore.find(id);
	
							// treat status special test
							String name = attributes[DE.A_NAME];
							String value = attributes[DE.A_VALUE];
							if (type.equals(STR_STATUS) && name.equals(STR_STATUS_DONE))
							{
								attributes[DE.A_NAME] = STR_STATUS_ALMOST_DONE;
								if (value.equals(STR_STATUS_DONE))
								{
									attributes[DE.A_VALUE] = STR_STATUS_ALMOST_DONE;
								}
								
								result.setAttributes(attributes);
							}
							else
							{
								if (isSpirit)
								{
									if (!_dataStore.isVirtual()) attributes[DE.A_REF_TYPE] = DataStoreResources.VALUE;
									result.setSpirit(_dataStore.isVirtual());
								}
								else
								{
									result.setSpirit(false);
								}
								result.setAttributes(attributes);
							}
	
							if (parent == null)
							{
								return result;
							}
							else if (parent == _rootDataElement)
							{
								DataElement rParent = result.getParent();
								parent = rParent;
	
								_rootDataElement.addNestedData(result, false);
							}
							else
							{
								if (result.getParent() == null)
								{
									if (result != _dataStore.getRoot())
									{
										result.setParent(parent);
									}
								}
							}
	
							if (parent != null)
							{
								parent.addNestedData(result, true);
							}
							else
							{
								if (result != _dataStore.getRoot())
								{
									_dataStore.trace("parent of " + result.getName() + " is NULL!");
								}
								else
								{
									result.setParent(null);
								}
							}
							if (result.isDeleted())
								//_dataStore.deleteObject(result.getParent(), result);
								result.delete();
						}
						else
						{
							// new object
							if (_dataStore.isVirtual() && parent != null) 
							{
								result = _dataStore.find(parent, DE.A_NAME, attributes[DE.A_NAME], 1);
								if (result != null && result.getValue().equals(attributes[DE.A_VALUE]) && result.isSpirit())
									_dataStore.deleteObject(parent, result);
							}
							if (isSpirit)
							{
								if (!_dataStore.isVirtual()) attributes[DE.A_REF_TYPE] = DataStoreResources.VALUE;
								result = _dataStore.createObject(parent, attributes);
								result.setSpirit(_dataStore.isVirtual());
							}
							else
							{
								result = _dataStore.createObject(parent, attributes);
								result.setSpirit(false);
							}

					
						}

					}
				}
			}

			if (result != null && result.isDeleted())
			{
				_dataStore.deleteObject(parent, result);
			}

			return result;
		}

		return null;
	}

	
	public static String replaceSpecial(String input)
	{
		int indexOfAmp = input.indexOf('&');
		int indexOfSemi = input.indexOf(';');
		if (indexOfAmp >= 0 && indexOfSemi > indexOfAmp)
		{
			String converted = input.replaceAll(STR_AMP, "&")
									.replaceAll(STR_SEMI, ";")
									.replaceAll(STR_QUOTE, "\"")
									.replaceAll(STR_APOS, "\'")
									.replaceAll(STR_LT, "<")
									.replaceAll(STR_GT, ">");
			return converted;						
		}
		else
		{
			return input;
		}
	}
	
	/**
	 * Converts XML special character representations to the appropriate characters
	 * @param input buffer to convert
	 * @return the converted buffer
	 */
	public static String convertStringFromXML(String input)
	{
		if (input.indexOf('&') > -1)
		{
			return replaceSpecial(input);
			/*
			StringBuffer result = new StringBuffer();
			
			String[] tokens = splitString(input);
			for (int i = 0; i < tokens.length; i++)
			{
				String token = tokens[i];
				if (token.equals(STR_AMP_TRIMMED))
				{
					result.append('&');
				}
				else if (token.equals(STR_SEMI_TRIMMED))
				{
					result.append(';');
				}
				else if (token.equals(STR_QUOTE_TRIMMED))
				{
					result.append('"');
				}
				else if (token.equals(STR_APOS_TRIMMED))
				{
					result.append('\'');
				}
				else if (token.equals(STR_LT_TRIMMED))
				{
					result.append('<');
				}
				else if (token.equals(STR_GT_TRIMMED))
				{
					result.append('>');
				}
				else
					result.append(token); 
			}
			
	
			return result.toString();
			*/
		}
		else
		{
			return input;
		}
	}
	
	public class KeepAliveRequestThread extends Thread
	{
		private long _timeout;
		private DataStore _dataStore;
		private boolean _failed;
		
		public KeepAliveRequestThread(long timeout, DataStore datastore)
		{
			_timeout = timeout;
			_dataStore = datastore;
			_failed = false;
		}
		
		public void run()
		{
			_dataStore.sendKeepAliveRequest();
			try
			{
				sleep(_timeout);
			}
			catch (InterruptedException e)
			{
				if (VERBOSE_KEEPALIVE) System.out.println("KeepAlive thread interrupted.");
				return;
			}
			if (VERBOSE_KEEPALIVE) System.out.println("KeepAlive thread failed to be interrupted.");
			_failed = true;			
		}
		
		public boolean failed()
		{
			return _failed;
		}
	}

}