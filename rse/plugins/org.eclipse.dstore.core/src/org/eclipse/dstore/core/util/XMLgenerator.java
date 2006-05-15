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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Stack;

import org.eclipse.dstore.core.java.IRemoteClassInstance;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreResources;

/**
 * <p>
 * This class is used to serialize data and send it either
 * to a file or a socket.  
 * </p>
 * <p>
 * When a DataStore tree needs to be transmitted, it's DataElements are 
 * converted to XML before sending.  Only those elements which have changed and
 * are thus out of synch with the DataStore on the other end of the socket are
 * actually transferred.
 * </p>
 * <p>
 * When a byte stream or file needs to be transmitted, bytes are
 * either send as is if binary transfer is requested or as unicode if 
 * binary transfer is not requested.
 * </p>
 */
public class XMLgenerator
{

	private int _state;

	private StringBuffer _document;

	private int _indent;
	private Stack _tagStack;

	private PrintStream _fileWriter;
	private BufferedWriter _dataWriter;

	private int _bufferSize;
	private boolean _generateBuffer;
	private boolean _ignoreDeleted;

	private DataStore _dataStore;

	public static final int EMPTY = 0;
	public static final int OPEN = 1;
	public static final int CLOSE = 2;
	public static final int BODY = 3;

	/**
	 * Constructor
	 * @param dataStore the associated DataStore
	 */
	public XMLgenerator(DataStore dataStore)
	{
		_dataStore = dataStore;
		_state = EMPTY;
		_bufferSize = 100000;

		_document = new StringBuffer(_bufferSize);

		_indent = 0;
		_generateBuffer = true;
		_ignoreDeleted = false;
		_tagStack = new Stack();
	}

	/**
	 * Indicate whether DataElements marked as deleted should be sent.
	 * @param flag whether deleted elements should be sent
	 */
	public void setIgnoreDeleted(boolean flag)
	{
		_ignoreDeleted = flag;
	}

	/**
	 * Sets the file writer used for file transfer
	 * @param writer the file writer used for file transfer
	 */
	public void setFileWriter(PrintStream writer)
	{
		_fileWriter = writer;
	}

	/**
	 * Sets the data writer used for XML transfer
	 * @param writer the data writer used for XML transfer
	 */
	public void setDataWriter(BufferedWriter writer)
	{
		_dataWriter = writer;
	}

	/**
	 * Set the buffer size
	 * @param size of the buffer used for transmitting packets
	 */
	public void setBufferSize(int size)
	{
		_bufferSize = size;
	}

	/**
	 * Indicate whether the buffer attribute of each DataElement should be
	 * transferred
	 * @param flag whether the buffer should be transferred
	 */
	public void setGenerateBuffer(boolean flag)
	{
		_generateBuffer = flag;
	}

	private void append(char c)
	{
		_document.append(c);
	}
	
	private void append(String buffer)
	{
		_document.append(buffer);
	}

	private void append(StringBuffer buffer)
	{
		_document.append(buffer);
	}

	private void nextLine()
	{
		if (_dataWriter != null)
		{
			_document.append('\n');

			int length = _document.length();
			if (length > _bufferSize)
			{
				flushData();
			}
		}
	}

	/**
	 * Send all buffered data through the pipe.
	 */
	public void flushData()
	{
		if (_document.length() > 0 && _dataWriter != null)
		{
			try
			{
				_dataWriter.write(_document.toString(), 0, _document.length());
				_dataWriter.write('\n');
				_dataWriter.flush();
				_document.setLength(0);
			}
			catch (Exception e)
			{
				_dataStore.trace(e);
				_dataWriter = null;
			}
		}
	}

	private void indent()
	{
		for (int i = 0; i < _indent; i++)
		{
			append(' ');
		}
	}

	private void startTag(String name)
	{
		if (_state == OPEN)
		{
			append('>');
			_indent++;
		}
		if (_state == CLOSE)
		{
			_indent--;
		}
		if (_state == BODY)
		{
			nextLine();
		}
		indent();
		if (_document == null)
		{
			append('<');
			append(name);
		}
		else
		{
			append('<');
			append(name);
		}
		_tagStack.push(name);
		_state = OPEN;
	}

	private void endTag(String name)
	{
		String top = (String) _tagStack.pop();
		if (_state == CLOSE)
		{
		}
		else if (_state == OPEN)
		{
			if (top == name)
			{
				append("/>");
				if (_tagStack.empty())
				{
					_state = CLOSE;
				}
				else
				{
					_state = BODY;
				}
			}
		}
		else if (_state == BODY)
		{
			if (top == name)
			{
				nextLine();
				_indent--;
				indent();
				append("</");
				append(name);
				append('>');
				if (_tagStack.empty())
				{
					_state = CLOSE;
				}
			}
		}
	}

	private void addAttribute(String name, String value)
	{
		if (_state != OPEN)
		{
		}

		StringBuffer niceValue = null;
		if (value != null)
		{
			niceValue = prepareStringForXML(value);

			append(' ');
			append(name);
			append("=\"");
			append(niceValue);
			append('"');
		}
		else
		{
			append(' ');
			append(name);
			append("=\"\"");
		}

	}
	
	private void addReferenceTypeAttribute(DataElement object)
	{
		if (object.isSpirit())
		{
			addAttribute(DE.P_REF_TYPE, DataStoreResources.SPIRIT);
		}
		else
		{
			if (object.isReference())
			{
				if (_dataStore.getReferenceTag() != null && _dataStore.getReferenceTag().equals(DE.P_REF_TYPE)) addAttribute(DE.P_REF_TYPE, DataStoreResources.REFERENCE);
				else addAttribute(DE.P_REF_TYPE, DataStoreResources.TRUE);
			}
			else 
			{
				if (_dataStore.getReferenceTag() != null && _dataStore.getReferenceTag().equals(DE.P_REF_TYPE)) addAttribute(DE.P_REF_TYPE, DataStoreResources.VALUE);
				else addAttribute(DE.P_REF_TYPE, DataStoreResources.FALSE);
			}
		}
	}

	private void addFile(byte[] bytes, int size, boolean binary)
	{
		if (_state == OPEN)
		{
			append('>');

			_indent++;
			_state = BODY;
		}
		if (_state == BODY)
		{
			flushData();

			// send everything across
			if (binary)
			{
				_fileWriter.write(bytes, 0, size);
				_fileWriter.flush();
			}
			else
			{
				try
				{
					_dataWriter.write(new String(bytes), 0, size);
					_dataWriter.flush();
				}
				catch (IOException e)
				{
					_dataStore.trace(e);
				}
			}
		}
		else if (_state == EMPTY)
		{
		}
		else if (_state == CLOSE)
		{
		}
	}

	private void addData(StringBuffer data)
	{
		if (_state == OPEN)
		{
			append('>');

			_indent++;
			_state = BODY;
		}
		if (_state == BODY)
		{
			if (_generateBuffer && data != null && (data.length() > 0))
			{
				StringBuffer text = prepareStringForXML(data);
				if (text != null && text.length() > 0)
				{
					nextLine();
					indent();
					append("<Buffer>");
					nextLine();
					indent();
					append(text.toString());
					nextLine();
					indent();
					append("</Buffer>");
				}
			}
			else
			{
				append("");
			}
		}
		else if (_state == EMPTY)
		{
		}
		else if (_state == CLOSE)
		{
		}
	}

	/**
	 * Returns the current serialized document
	 * @return the current document
	 */
	public StringBuffer document()
	{
		return _document;
	}

	/**
	 * Clears the current serlized document
	 */
	public void empty()
	{
		_indent = 0;
		_document.delete(0, _document.length());
	}

	/**
	 * Converts special characters to appropriate representation in XML
	 * @param input buffer to convert
	 * @return the converted buffer
	 */
	public static StringBuffer prepareStringForXML(StringBuffer input)
	{
		StringBuffer output = new StringBuffer();

		for (int idx = 0; idx < input.length(); idx++)
		{
			char currChar = input.charAt(idx);
			switch (currChar)
			{
				case '&' :
					output.append(XMLparser.STR_AMP);
					break;
				case '"' :
					output.append(XMLparser.STR_QUOTE);
					break;
				case '\'' :
					output.append(XMLparser.STR_APOS);
					break;
				case '<' :
					output.append(XMLparser.STR_LT);
					break;
				case '>' :
					output.append(XMLparser.STR_GT);
					break;
				case ';' :
					output.append(XMLparser.STR_SEMI);
					break;
				default :
					output.append(currChar);
					break;
			}
		}

		return output;
	}

	/**
	 * Converts special characters to appropriate representation in XML
	 * @param input buffer to convert
	 * @return the converted buffer
	 */
	public static StringBuffer prepareStringForXML(String input)
	{
		StringBuffer output = new StringBuffer();

		for (int idx = 0; idx < input.length(); idx++)
		{
			char currChar = input.charAt(idx);
			switch (currChar)
			{
			case '&' :
				output.append(XMLparser.STR_AMP);
				break;
			case '"' :
				output.append(XMLparser.STR_QUOTE);
				break;
			case '\'' :
				output.append(XMLparser.STR_APOS);
				break;
			case '<' :
				output.append(XMLparser.STR_LT);
				break;
			case '>' :
				output.append(XMLparser.STR_GT);
				break;
			case ';' :
				output.append(XMLparser.STR_SEMI);
				break;
				default :
					output.append(currChar);
					break;
			}
		}

		return output;
	}

	/**
	 * Generate an tags for a file transfer and send bytes over the pipe.
	 * 
	 * @param object the element representing the file transfer
	 * @param bytes the bytes to send
	 * @param size the number of bytes to send
	 * @param isAppend indicates whether bytes should be appended or not to a file on the other end of the pipe
	 * @param binary indicates whether the bytes should be sent as binary or text
	 */
	public synchronized void generate(DataElement object, byte[] bytes, int size, boolean isAppend, boolean binary)
	{
		String tagType = XMLparser.STR_FILE;
		if (isAppend)
		{
			tagType += ".Append";
		}
		if (binary)
		{
			tagType += ".Binary";
		}

		if (object != null)
		{
			startTag(tagType);
			addAttribute(DE.P_TYPE, object.getAttribute(DE.A_TYPE));
			addAttribute(DE.P_ID, object.getAttribute(DE.A_ID));
			addAttribute(DE.P_NAME, object.getAttribute(DE.A_NAME));
			addAttribute(DE.P_VALUE, object.getAttribute(DE.A_VALUE));
			addAttribute(DE.P_SOURCE, object.getAttribute(DE.A_SOURCE));
			addAttribute(DE.P_SOURCE_LOCATION, object.getAttribute(DE.A_SOURCE_LOCATION));

			addReferenceTypeAttribute(object);

			addAttribute(DE.P_DEPTH, "" + size);
			addFile(bytes, size, binary);

			endTag(tagType);
		}
	}
	
	/**
	 * Generate tags for class transfer and send bytes over the pipe.
	 * 
	 * @param object the element representing the class transfer
	 * @param bytes the bytes to send
	 * @param size the number of bytes to send
	 */
	public synchronized void generate(DataElement object, byte[] bytes, int size)
	{
		String tagType = XMLparser.STR_CLASS;

		if (object != null)
		{
			startTag(tagType);
			addAttribute(DE.P_TYPE, object.getAttribute(DE.A_TYPE));
			addAttribute(DE.P_ID, object.getAttribute(DE.A_ID));
			addAttribute(DE.P_NAME, object.getAttribute(DE.A_NAME));
			addAttribute(DE.P_VALUE, object.getAttribute(DE.A_VALUE));
			addAttribute(DE.P_SOURCE, object.getAttribute(DE.A_SOURCE));
			addAttribute(DE.P_SOURCE_LOCATION, object.getAttribute(DE.A_SOURCE_LOCATION));

			addReferenceTypeAttribute(object);

			addAttribute(DE.P_DEPTH, "" + size);
			addFile(bytes, size, true);

			endTag(tagType);
		}
	}

	/**
	 * Serializes and sends a DataStore tree through the pipe
	 * 
	 * @param object the root of the DataStore tree to send
	 * @param depth the depth of the tree to send
	 */
	public void generate(DataElement object, int depth)
	{
		if ((object != null) && (depth >= 0))
		{
			String tagType = XMLparser.STR_DATAELEMENT;

			if (object.isUpdated() && !object.isPendingTransfer() && !_generateBuffer)
			{
			}
			else
			{
				if (object.isDeleted() && _ignoreDeleted)
				{
				}
				else
				{
					object.setPendingTransfer(false);
					
					startTag(tagType);
					addAttribute(DE.P_TYPE, object.getAttribute(DE.A_TYPE));
					addAttribute(DE.P_ID, object.getAttribute(DE.A_ID));
					addAttribute(DE.P_NAME, object.getAttribute(DE.A_NAME));
					addAttribute(DE.P_VALUE, object.getAttribute(DE.A_VALUE));
					addAttribute(DE.P_SOURCE, object.getAttribute(DE.A_SOURCE));
					addAttribute(DE.P_SOURCE_LOCATION, object.getAttribute(DE.A_SOURCE_LOCATION));

					addReferenceTypeAttribute(object);

					addAttribute(DE.P_DEPTH, "" + object.depth());
					addData(object.getBuffer());
					object.setUpdated(true);

					if (!object.isReference() && depth >= 0)
					{
						for (int i = 0; i < object.getNestedSize(); i++)
						{
							generate(object.get(i), depth - 1);
						}
					}

					// end generation
					endTag(tagType);
				}
			}
		}
	}

	public void generateClassRequest(DataElement object)
	{
		String tagType = XMLparser.STR_REQUEST_CLASS;
		if (object != null)
		{
			startTag(tagType);
			addAttribute(DE.P_TYPE, object.getAttribute(DE.A_TYPE));
			addAttribute(DE.P_ID, object.getAttribute(DE.A_ID));
			addAttribute(DE.P_NAME, object.getAttribute(DE.A_NAME));
			addAttribute(DE.P_VALUE, object.getAttribute(DE.A_VALUE));
			addAttribute(DE.P_SOURCE, object.getAttribute(DE.A_SOURCE));
			addAttribute(DE.P_SOURCE_LOCATION, object.getAttribute(DE.A_SOURCE_LOCATION));

			addReferenceTypeAttribute(object);
			_state = BODY;
			endTag(tagType);
		}
		
	}

	public void generateSerializedObject(DataElement object, IRemoteClassInstance runnable)
	{
		String tagType = XMLparser.STR_SERIALIZED;
		if (object != null)
		{
			startTag(tagType);
			addAttribute(DE.P_TYPE, object.getAttribute(DE.A_TYPE));
			addAttribute(DE.P_ID, object.getAttribute(DE.A_ID));
			addAttribute(DE.P_NAME, object.getAttribute(DE.A_NAME));
			addAttribute(DE.P_VALUE, object.getAttribute(DE.A_VALUE));
			addAttribute(DE.P_SOURCE, object.getAttribute(DE.A_SOURCE));
			addAttribute(DE.P_SOURCE_LOCATION, object.getAttribute(DE.A_SOURCE_LOCATION));

			addReferenceTypeAttribute(object);
			
			try
			{
				PipedInputStream pin = new PipedInputStream();
				PipedOutputStream pout = new PipedOutputStream(pin);
				ObjectOutputStream outStream = new ObjectOutputStream(pout);
				outStream.writeObject(runnable);
					
				
				int size = pin.available();
				byte[] bytes = new byte[size];
				int nRead = pin.read(bytes, 0, size);
				addAttribute(DE.P_DEPTH, "" + nRead);
				addFile(bytes, nRead, true);
				
				outStream.close();
				pin.close();
				
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			endTag(tagType);
		}		
	}
}