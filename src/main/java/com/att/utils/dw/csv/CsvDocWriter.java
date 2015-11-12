package com.att.utils.dw.csv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jfree.util.Log;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import com.att.utils.dw.exceptions.DocumentCreationException;
import com.att.utils.dw.exceptions.DocumentDataEntryException;
import com.att.utils.dw.writer.DocWriter;

public class CsvDocWriter implements DocWriter {

	private int colIndex = 0;
	private int numCols = 0;

	private ByteArrayOutputStream baos;
	private OutputStreamWriter out;

	ICsvListWriter writer;
	CellProcessor[] processors;
	List<String> colValues;

	@Override
	public void init(Map<String, String> attrs) throws DocumentCreationException {
		baos = new ByteArrayOutputStream();
		out = new OutputStreamWriter(baos);
		writer = new CsvListWriter(out, CsvPreference.STANDARD_PREFERENCE);
		Log.debug("csv writer initialized");
	}

	@Override
	public void createTable(int cols, String type) throws DocumentCreationException {
		numCols = cols;
		processors = new CellProcessor[cols];
		for (int i = 0; i < cols; i++) {
			processors[i] = new Optional();
		}
		colValues = new ArrayList<String>();
	}

	@Override
	public void createTable(float[] widths) throws DocumentCreationException {
		createTable(widths.length, null);
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
	public void addCell(String value, Map<String, String> attrs) throws DocumentDataEntryException {
		colValues.add(value);
		if (++colIndex >= numCols) {
			try {
				writer.write(colValues, processors);
			} catch (IOException e) {
				throw new DocumentDataEntryException(e.getMessage());
			}
			colValues.clear();
			colIndex = 0;
		}
	}

	@Override
	public void endTable() throws DocumentCreationException {
		if (colValues.size() > 0 && colValues.size() <= numCols) {
			try {
				CellProcessor[] proc = new CellProcessor[colValues.size()];
				for (int i = 0; i < colValues.size(); i++) {
					proc[i] = processors[i];
				}
				writer.write(colValues, proc);
			} catch (IOException e) {
				throw new DocumentCreationException(e.getMessage());
			}
		}
	}

	@Override
	public void addParagraph(String titleStr, String contentStr, Map<String, String> attrs)
			throws DocumentDataEntryException {
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
		writer.close();
		baos.close();
		out.close();
	}

	@Override
	public ByteArrayOutputStream getOutputStream() {
		return baos;
	}

	@Override
	public void addPageHeader(String[] headers) throws DocumentDataEntryException {
		try {
			writer.writeHeader(headers);
		} catch (IOException e) {
			throw new DocumentDataEntryException(e.getMessage());
		}
	}

}
