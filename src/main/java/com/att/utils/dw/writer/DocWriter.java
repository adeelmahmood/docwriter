/*
 * 
 */
package com.att.utils.dw.writer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import com.att.utils.dw.exceptions.DocumentCreationException;
import com.att.utils.dw.exceptions.DocumentDataEntryException;

/**
 * The Interface DocWriter.
 *
 * @author m057188
 * 
 * DocWriter interface
 * Provides interface for document creation methods
 */
public interface DocWriter {
	
	/**
	 * Inits the document.
	 *
	 * @param attrs the attrs
	 * @throws DocumentCreationException the document creation exception
	 */
	public void init(Map<String, String> attrs) throws DocumentCreationException;
	
	/**
	 * Creates the table using number and type of columns.
	 *
	 * @param cols the cols
	 * @param type the type
	 * @throws DocumentCreationException the document creation exception
	 */
	public void createTable(int cols, String type) throws DocumentCreationException;
	
	/**
	 * Creates the table using given widths array.
	 *
	 * @param widths the widths
	 * @throws DocumentCreationException the document creation exception
	 */
	public void createTable(float[] widths) throws DocumentCreationException;
	
	/**
	 * Start inner table.
	 *
	 * @param col the col
	 */
	public void startInnerTable(int col);
	
	/**
	 * End inner table.
	 */
	public void endInnerTable();

	/**
	 * Adds the cell.
	 *
	 * @param value the value
	 * @param attrs the attrs
	 * @throws DocumentCreationException the document creation exception
	 */
	public void addCell(String value, Map<String, String> attrs) throws DocumentDataEntryException;
	
	/**
	 * End table.
	 *
	 * @throws DocumentCreationException the document creation exception
	 */
	public void endTable() throws DocumentCreationException;
	
	/**
	 * Adds the paragraph.
	 *
	 * @param titleStr the title str
	 * @param contentStr the content str
	 * @param attrs the attrs
	 * @throws DocumentDataEntryException the document data entry exception
	 */
	public void addParagraph(String titleStr, String contentStr, Map<String, String> attrs) throws DocumentDataEntryException;
	
	/**
	 * New page.
	 */
	public void newPage();
	
	
	/**
	 * Removes the row.
	 *
	 * @param count the count
	 */
	public void removeRow(int count);
	
	/**
	 * End.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void end() throws IOException;
	
	/**
	 * Gets the output stream.
	 *
	 * @return the output stream
	 */
	public ByteArrayOutputStream getOutputStream();
	
	/**
	 * Adds the page header.
	 *
	 * @param headers the headers
	 * @throws DocumentDataEntryException the document data entry exception
	 */
	public void addPageHeader(String[] headers) throws DocumentDataEntryException;
}