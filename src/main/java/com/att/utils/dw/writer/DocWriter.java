package com.att.utils.dw.writer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import com.att.utils.dw.exceptions.DocumentCreationException;
import com.att.utils.dw.exceptions.DocumentDataEntryException;

public interface DocWriter {

	void init(Map<String, String> attrs) throws DocumentCreationException;

	void createTable(int cols, String type) throws DocumentCreationException;

	void createTable(float[] widths) throws DocumentCreationException;

	void startInnerTable(int col);

	void endInnerTable();

	void addCell(String value, Map<String, String> attrs) throws DocumentDataEntryException;

	void endTable() throws DocumentCreationException;

	void addParagraph(String titleStr, String contentStr, Map<String, String> attrs) throws DocumentDataEntryException;

	void newPage();

	void removeRow(int count);

	void end() throws IOException;

	ByteArrayOutputStream getOutputStream();

	void addPageHeader(String[] headers) throws DocumentDataEntryException;
}