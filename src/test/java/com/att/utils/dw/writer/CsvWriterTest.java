package com.att.utils.dw.writer;

import org.junit.Test;

import com.att.utils.dw.csv.CsvDocWriter;
import com.att.utils.dw.exceptions.DocumentCreationException;

public class CsvWriterTest extends AbstractWriterTest {

	@Test
	public void testTablesCreation() throws Exception {
		startDocument();
		createSimpleTable(10, 500);
		docWriter.end();

		// indicate output file name
		out = "output/tables.csv";
	}

	private void startDocument() throws DocumentCreationException {
		docWriter = new CsvDocWriter();
		docWriter.init(attrs);
	}

	private void createSimpleTable(int cols, int vals) throws Exception {
		docWriter.createTable(cols, null);

		String[] headers = new String[cols];
		for (int i = 1; i <= cols; i++) {
			headers[i - 1] = "Header " + i;
		}
		docWriter.addPageHeader(headers);
		for (int i = 1; i <= vals; i++) {
			docWriter.addCell("Value " + i, attrs);
		}
		
		docWriter.addCell("TotalRecords", attrs);
		docWriter.addCell("=", attrs);
		docWriter.addCell("100", attrs);
		
		docWriter.endTable();
	}
}