package com.att.utils.dw.writer.excel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.utils.dw.exceptions.DocumentCreationException;
import com.att.utils.dw.exceptions.DocumentDataEntryException;
import com.att.utils.dw.writer.DocWriter;

public class ExcelPoiDocWriter implements DocWriter {

	private Logger log = LoggerFactory.getLogger(ExcelPoiDocWriter.class);

	private Workbook workbook;
	private Sheet sheet;

	private int numCols = 0;

	private int colIndex = 0;
	private int rowIndex = 0;

	private ByteArrayOutputStream baos;

	@Override
	public void init(Map<String, String> attrs) throws DocumentCreationException {
		log.debug("creating new excel xlsx document");
		baos = new ByteArrayOutputStream();

		// initialize workbook
		workbook = new XSSFWorkbook();
		sheet = workbook.createSheet("Sheet1");
		log.debug("new workbook and sheet initialized");
	}

	@Override
	public void createTable(int cols, String type) throws DocumentCreationException {
		numCols = cols;
	}

	@Override
	public void createTable(float[] widths) throws DocumentCreationException {
		createTable(widths.length, null);
	}

	@Override
	public void addPageHeader(String[] headers) throws DocumentDataEntryException {
		Row row = sheet.getRow(rowIndex) == null ? sheet.createRow(rowIndex++) : sheet.getRow(rowIndex);

		Font font = workbook.createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 11);
		font.setFontName("Arial");

		CellStyle style = workbook.createCellStyle();
		style.setFont(font);

		Cell cell;
		for (int i = 0; i < headers.length; i++) {
			cell = row.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(style);
		}
	}

	@Override
	public void addCell(String value, Map<String, String> attrs) throws DocumentDataEntryException {
		Row row = sheet.getRow(rowIndex) == null ? sheet.createRow(rowIndex) : sheet.getRow(rowIndex);
		row.createCell(colIndex++).setCellValue(value);
		if (colIndex >= numCols) {
			rowIndex++;
			colIndex = 0;
		}
	}

	@Override
	public void endTable() throws DocumentCreationException {
		for (int i = 0; i < numCols; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	@Override
	public void startInnerTable(int col) {
		throw new RuntimeException("method not implemented yet");
	}

	@Override
	public void endInnerTable() {
		throw new RuntimeException("method not implemented yet");
	}

	@Override
	public void newPage() {
		throw new RuntimeException("method not implemented yet");
	}

	@Override
	public void removeRow(int count) {
		throw new RuntimeException("method not implemented yet");
	}

	@Override
	public void end() throws IOException {
		workbook.write(baos);
		workbook.close();
		baos.close();
	}

	@Override
	public ByteArrayOutputStream getOutputStream() {
		return baos;
	}

	@Override
	public void addParagraph(String titleStr, String contentStr, Map<String, String> attrs)
			throws DocumentDataEntryException {
		throw new RuntimeException("method not implemented yet");
	}

}
