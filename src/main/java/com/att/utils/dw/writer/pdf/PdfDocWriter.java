package com.att.utils.dw.writer.pdf;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.utils.dw.exceptions.DocumentCreationException;
import com.att.utils.dw.exceptions.DocumentDataEntryException;
import com.att.utils.dw.utils.Constants;
import com.att.utils.dw.writer.DocWriter;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class PdfDocWriter implements DocWriter {
	private Logger log = LoggerFactory.getLogger(PdfDocWriter.class);

	private Document document;
	private PdfPTable table, innerTable;
	private PdfPCell cell;
	private Font font;

	private PdfWriter writer;
	private PdfEventHandler pdfEventHandler;

	private boolean usingInnerTable = false;

	private int colIndex = 0;
	private int rowIndex = 0;

	private ByteArrayOutputStream baos;

	public void init(Map<String, String> reportAttrs) throws DocumentCreationException {
		log.debug("creating new pdf document");
		// reset local vars
		colIndex = 0;
		rowIndex = 0;

		// initialize the output stream
		baos = new ByteArrayOutputStream();

		// create new pdf document
		document = new Document(getPaperRectangle(reportAttrs));

		// create pdf event handler for watermark
		try {
			writer = PdfWriter.getInstance(document, baos);

			// set page event handler
			pdfEventHandler = new PdfEventHandler();
			writer.setPageEvent(pdfEventHandler);

			// set event handler based preferences
			pdfEventHandler.setLayout(reportAttrs.containsKey("landscape") ? "landscape" : "portrait");
			// secured
			if (reportAttrs.containsKey("secured")) {
				writer.setEncryption(null, "owner".getBytes(), PdfWriter.ALLOW_PRINTING, PdfWriter.ENCRYPTION_AES_128);
			}
		} catch (DocumentException e) {
			log.error("error in setting up pdf event handler", e);
		}

		// open the document
		document.open();

		// set watermark image
		if (reportAttrs.containsKey("watermark")) {
			// pass watermark info to pdf event handler class
			pdfEventHandler.setWatermark(reportAttrs.get("watermark"));
			log.debug("applied watermark");
		}
		log.debug("pdf document initialized");
	}

	public void createTable(int col, String type) {
		// create new table
		if (type != null && type.equals(Constants.FIRST_COL_BIG_LAST_COL_SMALL)) {
			float[] w = new float[col];
			for (int i = 0; i < col; i++) {
				if (i == 0)
					w[i] = 1.5f;
				else if (i == col - 1)
					w[i] = 0.5f;
				else
					w[i] = 1f;
			}
			table = new PdfPTable(w);
		} else if (type != null && type.equals(Constants.FIRST_COL_BIG)) {
			float[] w = new float[col];
			for (int i = 0; i < col; i++) {
				if (i == 0)
					w[i] = 1.75f;
				else
					w[i] = 1f;
			}
			table = new PdfPTable(w);
		} else {
			table = new PdfPTable(col);
		}
		// table display settings
		table.setWidthPercentage(100);
		table.setSpacingAfter(10);
		log.debug("new table with " + table.getNumberOfColumns() + " cols created");
	}

	public void createTable(float[] widths) {
		// create new table
		table = new PdfPTable(widths);
		// table display settings
		table.setWidthPercentage(100);
		table.setSpacingAfter(10);
		rowIndex = 0;
		log.debug("new table with " + widths.length + " cols created using widths");
	}

	public void startInnerTable(int col) {
		// create new inner table
		innerTable = new PdfPTable(col);
		this.setUsingInnerTable(true);
		log.debug("started inner table with " + col + " cols");
	}

	public void addPageHeader(String[] headers) throws DocumentDataEntryException {
		Paragraph para;
		Chunk chunk;
		for (int i = 0; i < headers.length; i++) {
			// create new paragraph
			para = new Paragraph();
			chunk = new Chunk(headers[i]);
			chunk.setFont(new Font(Font.TIMES_ROMAN, 12, Font.BOLD));
			// header
			if (i == 0)
				chunk.setUnderline(1f, -2f);
			para.add(chunk);
			para.setAlignment(Paragraph.ALIGN_CENTER);
			para.setSpacingBefore(0);
			if (i == 0 && i < headers.length - 1)
				para.setSpacingAfter(5);
			else
				para.setSpacingAfter(10);
			// add to document
			try {
				document.add(para);
			} catch (DocumentException e) {
				log.error("error in adding page header. " + e);
				throw new DocumentDataEntryException(e.toString());
			}
		}
	}

	public void addCell(String value, Map<String, String> attrs) {
		int fontSize = 10;
		if (attrs.containsKey("fontSize")) {
			fontSize = Integer.parseInt(attrs.get("fontSize"));
		}

		// set font
		if (attrs.containsKey("bold") || "bold".equals(attrs.get("format"))) {
			font = new Font(Font.TIMES_ROMAN, fontSize, Font.BOLD);
		} else if (attrs.containsKey("underline") || "underline".equals(attrs.get("format"))) {
			font = new Font(Font.TIMES_ROMAN, fontSize, Font.UNDERLINE);
		} else {
			font = new Font(Font.TIMES_ROMAN, fontSize, Font.NORMAL);
		}

		// editable cells
		/*
		 * if(attrs.containsKey("editable")){ //create a new empty cell cell =
		 * new PdfPCell();
		 * 
		 * String editable = attrs.get("editable"); //textfield
		 * if(editable.equals("text")){ //create new text field TextField
		 * textfield = pdfFormHelper.createTextField(writer, attrs.get("name"),
		 * attrs.containsKey("required"));
		 * 
		 * //use cell event to include textfield in the cell
		 * cell.setMinimumHeight(10); try { cell.setCellEvent(new
		 * FieldCell(textfield.getTextField(), 200, writer)); } catch
		 * (IOException e) { log.error("error in adding textfield", e); } catch
		 * (DocumentException e) { log.error("error in adding textfield", e); }
		 * } } else{
		 */
		// create a label cell
		cell = new PdfPCell(new Phrase(value, font));
		// }

		// set column attrs
		cell.setPadding(4);
		// colspan
		int cols = 1;
		if (attrs.containsKey("colspan")) {
			cols = Integer.parseInt(attrs.get("colspan"));
			cell.setColspan(cols);
		}
		// background color
		if (attrs.containsKey("grey")) {
			cell.setGrayFill(0.7f);
		} else if (attrs.containsKey("lightgrey")) {
			cell.setGrayFill(0.9f);
		}
		// aligment
		if (attrs.containsKey("align")) {
			if (attrs.get("align").equals("center")) {
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			} else if (attrs.get("align").equals("right")) {
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			}
		}
		// highlighting
		if (attrs.containsKey("highlight") || attrs.containsKey("highlighted")) {
			cell.setBackgroundColor(Color.YELLOW);
		}

		// add cell
		if (this.isUsingInnerTable()) {
			// add to inner table
			innerTable.addCell(cell);
		} else {
			// add to table
			table.addCell(cell);
		}

		// handle header row add behaviro
		colIndex += cols;
		// increment row and col indexes (this is only used for setting header
		// rows)
		if (colIndex >= table.getNumberOfColumns()) {
			colIndex = 0;
			rowIndex++;
			if (attrs.containsKey("header")) {
				// set header rows
				table.setHeaderRows(rowIndex);
				log.debug("setting header rows to : " + rowIndex);
			}
		}
	}

	public void endInnerTable() {
		// make sure inner table was started
		if (this.isUsingInnerTable()) {
			// create new cell for the inner table
			cell = new PdfPCell(innerTable);
			cell.setPadding(0);
			table.addCell(cell);

			// update local flag for inner table
			this.setUsingInnerTable(false);
			log.debug("completed inner table");
		}
	}

	public void endTable() throws DocumentCreationException {
		try {
			document.add(table);
			rowIndex = 0;
			log.debug("completed table");
		} catch (DocumentException e) {
			log.error("error in report creation. " + e);
			throw new DocumentCreationException(e.toString());
		}
	}

	public void end() throws IOException {
		document.close();
		baos.close();
		baos.flush();
		log.debug("completed document");
	}

	public ByteArrayOutputStream getOutputStream() {
		return baos;
	}

	public void removeRow(int count) {
		log.warn("removeRow method in pdfDocWriter is not implemented yet");
	}

	public void newPage() {
		log.debug("switching to new page");
		document.newPage();
	}

	public void addParagraph(String titleStr, String contentStr, Map<String, String> attrs)
			throws DocumentDataEntryException {
		log.debug("adding paragraph to document");
		// title for paragraph
		Paragraph title = new Paragraph();
		Chunk titleChunk = new Chunk(titleStr);
		Font font = new Font(Font.TIMES_ROMAN, 11, Font.BOLD);
		titleChunk.setFont(font);
		titleChunk.setUnderline(1f, -2f);
		title.add(titleChunk);
		title.setSpacingAfter(10);
		log.debug("paragraph title '" + titleStr + "' created");

		// paragraph content
		Chunk body = new Chunk(contentStr);
		body.setFont(new Font(Font.TIMES_ROMAN, 10, Font.NORMAL));

		// add all to paragraph
		Paragraph para = new Paragraph();
		para.add(title);
		para.add(body);
		para.setSpacingAfter(10);
		log.debug("paragraph content added");

		try {
			// add to document
			document.add(para);
			log.debug("added paragraph to doc");
		} catch (DocumentException e) {
			log.error("error adding paragraph element to document: " + e);
			throw new DocumentDataEntryException(e.toString());
		}
	}

	private Rectangle getPaperRectangle(Map<String, String> attrs) {
		Rectangle rect = PageSize.LETTER;
		// paper type
		if (attrs.containsKey("paper") && "letter".equals(attrs.get("paper"))) {
			rect = PageSize.LETTER;
		} else if (attrs.containsKey("paper") && "legal".equals(attrs.get("paper"))) {
			rect = PageSize.LEGAL;
		}
		// paper rotation
		if (attrs.containsKey("landscape") && "true".equals(attrs.get("landscape"))) {
			rect = rect.rotate();
		}
		return rect;
	}

	public void setUsingInnerTable(boolean usingInnerTable) {
		this.usingInnerTable = usingInnerTable;
	}

	public boolean isUsingInnerTable() {
		return usingInnerTable;
	}
}