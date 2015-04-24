package com.att.utils.dw.writer.form;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfAnnotation;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfFormField;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfWriter;

public class FieldCell implements PdfPCellEvent {
	private Log log = LogFactory.getLog(FieldCell.class);

	PdfFormField formField;
	PdfWriter writer;
	int width;

	public FieldCell(PdfFormField formField, int width, PdfWriter writer) {
		this.formField = formField;
		this.width = width;
		this.writer = writer;
	}

	public void cellLayout(PdfPCell cell, Rectangle rect, PdfContentByte[] canvas) {
		try {
			formField.setWidget(new Rectangle(rect.getLeft(), rect.getBottom(), rect.getLeft() + width, rect.getTop()),
					PdfAnnotation.HIGHLIGHT_NONE);

			writer.addAnnotation(formField);
		} catch (Exception e) {
			log.error("error in field cell event handler", e);
		}
	}
}
