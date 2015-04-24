package com.att.utils.dw.writer;

import org.junit.Test;

import com.att.utils.dw.exceptions.DocumentCreationException;
import com.att.utils.dw.writer.excel.ExcelPoiDocWriter;

public class ExcelPoiWriterTest extends AbstractWriterTest {

	@Test
	public void testTablesCreation() throws Exception {
		startDocument();
		createSimpleTable(1000, 500000);
		docWriter.end();

		// indicate output file name
		out = "output/tables.xlsx";
	}

	private void startDocument() throws DocumentCreationException {
		docWriter = new ExcelPoiDocWriter();
		docWriter.init(attrs);
	}

	private void createSimpleTable(int cols, int vals) throws Exception {
		docWriter.createTable(cols, null);

		String[] headers = new String[cols];
		for (int i = 1; i <= cols; i++) {
			headers[i-1] = "Header " + i;
		}
		docWriter.addPageHeader(headers);
		for (int i = 1; i <= vals; i++) {
			docWriter.addCell("Value " + i, attrs);
		}
		docWriter.endTable();
	}
}