package com.att.utils.dw.writer.pdf;

import java.awt.Color;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfPageEvent;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

public class PdfEventHandler implements PdfPageEvent {
	private Log log = LogFactory.getLog(PdfEventHandler.class);

	private String watermark;
	private String layout;

	private BaseFont helv;
	private PdfGState gState;

	private PdfTemplate footer;

	private Date printed;
	private SimpleDateFormat dateFormat;

	private Image image = null;

	public PdfEventHandler() {
		watermark = "";
		dateFormat = new SimpleDateFormat("MM/dd/yy - h:mm a z");
	}

	public void onChapter(PdfWriter arg0, Document arg1, float arg2, Paragraph arg3) {
	}

	public void onChapterEnd(PdfWriter arg0, Document arg1, float arg2) {
	}

	public void onCloseDocument(PdfWriter arg0, Document arg1) {
		// write contents of footer template
		footer.beginText();
		footer.setFontAndSize(helv, 9);
		footer.setTextMatrix(0, 0);
		footer.showText(String.valueOf(arg0.getPageNumber() - 1));
		footer.endText();
	}

	public void onEndPage(PdfWriter writer, Document document) {
		PdfContentByte contentUnder = writer.getDirectContent();
		contentUnder.saveState();
		contentUnder.setFontAndSize(helv, 9);

		// add footer
		contentUnder.beginText();

		// show page numbers footer
		String text = "Page " + writer.getPageNumber() + " of ";
		float textBase = document.bottom() - 20;
		float textSize = helv.getWidthPoint(text, 9);

		// place footer
		contentUnder.setTextMatrix(document.right() - textSize, textBase);
		contentUnder.showText(text);
		contentUnder.endText();
		contentUnder.addTemplate(footer, document.right(), textBase);

		// add printer on
		contentUnder.beginText();

		// show printed on date
		text = "Printed on " + dateFormat.format(printed);

		// place printer on text
		contentUnder.setTextMatrix(document.left(), textBase);
		contentUnder.showText(text);
		contentUnder.endText();

		// add watermark
		if (watermark != null && watermark.length() > 0) {
			// text watermark
			if (watermark.startsWith("text:")) {
				String watermarkText = watermark.split("text:")[1];
				// for text watermark there can be three lines
				String[] watermarks = watermarkText.split(",");
				// use text watermark instead
				contentUnder.beginText();
				contentUnder.setColorFill(Color.GRAY);
				contentUnder.setFontAndSize(helv, 40);
				// show text
				if (watermarks.length > 0) {
					contentUnder
							.showTextAligned(Element.ALIGN_CENTER, watermarks[0],
									document.getPageSize().getWidth() / 2 - 38,
									document.getPageSize().getHeight() / 2 + 60, 45);
				}
				if (watermarks.length > 1) {
					contentUnder.showTextAligned(Element.ALIGN_CENTER, watermarks[1],
							document.getPageSize().getWidth() / 2, document.getPageSize().getHeight() / 2, 45);
				}
				if (watermarks.length > 2) {
					contentUnder
							.showTextAligned(Element.ALIGN_CENTER, watermarks[2],
									document.getPageSize().getWidth() / 2 + 60,
									document.getPageSize().getHeight() / 2 - 40, 45);
				}
				contentUnder.endText();
				// contentUnder.setColorFill(BaseColor.BLACK);
			}
			// image watermark
			else if (watermark.startsWith("image:")) {
				try {
					contentUnder.setGState(gState);
					String watermarkPath = watermark.split("image:")[1];
					// initialize watermark image
					image = Image.getInstance(watermarkPath);

					// adjust image positioning based on report layout
					int offsetX = -80;
					int offsetY = -80;
					if ("portrait".equalsIgnoreCase(layout)) {
						offsetX = 20;
						offsetY = -150;
					}

					// add image to page
					contentUnder.addImage(image, image.getWidth(), 0, 0, image.getHeight(), document.getPageSize()
							.getWidth() - image.getWidth() + offsetX,
							document.getPageSize().getHeight() - image.getHeight() + offsetY);
				} catch (Exception e) {
					log.error("error in adding watermark image " + e);
				}
			}
		}

		// close editing
		contentUnder.restoreState();
	}

	public void onGenericTag(PdfWriter arg0, Document arg1, Rectangle arg2, String arg3) {
		// TODO Auto-generated method stub
	}

	public void onOpenDocument(PdfWriter arg0, Document arg1) {
		printed = Calendar.getInstance().getTime();

		// set footer page stamping template
		footer = arg0.getDirectContent().createTemplate(100, 100);
		footer.setBoundingBox(new Rectangle(-20, -20, 100, 100));

		try {
			// initialize watermark font
			helv = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
		} catch (DocumentException e) {
			log.error("error in open document " + e);
		} catch (IOException e) {
			log.error("error in open document " + e);
		}

		// set the gstate
		gState = new PdfGState();
		gState.setFillOpacity(0.2f);
		gState.setStrokeOpacity(0.2f);
	}

	public void onParagraph(PdfWriter arg0, Document arg1, float arg2) {
	}

	public void onParagraphEnd(PdfWriter arg0, Document arg1, float arg2) {
	}

	public void onSection(PdfWriter arg0, Document arg1, float arg2, int arg3, Paragraph arg4) {
	}

	public void onSectionEnd(PdfWriter arg0, Document arg1, float arg2) {
	}

	public void onStartPage(PdfWriter arg0, Document arg1) {
		// set watermark color
		/*
		 * if(arg0.getPageNumber() % 2 == 1){ color = BaseColor.BLUE; } else{
		 */
		// color = BaseColor.BLUE;
		// }
	}

	public void setWatermark(String watermark) {
		this.watermark = watermark;
	}

	public String getWatermark() {
		return watermark;
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}

	public String getLayout() {
		return layout;
	}
}