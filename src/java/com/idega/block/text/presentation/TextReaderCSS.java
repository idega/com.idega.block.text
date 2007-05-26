package com.idega.block.text.presentation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.idega.block.text.business.ContentHelper;
import com.idega.block.text.business.TextFormatter;
import com.idega.block.text.data.LocalizedText;
import com.idega.block.text.data.TxText;
import com.idega.core.file.data.ICFile;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.util.text.TextSoap;

public class TextReaderCSS extends TextReader {

	private final static String ATTRIBUTE_HEADLINE = "headline";
	private final static String ATTRIBUTE_BODY = "body";
	private final static String ATTRIBUTE_IMAGE = "image";
	private final static String styleClassPrefix = "article_";
	private final static String DEFAULT_STYLE_CLASS = styleClassPrefix + "item";

	protected PresentationObject getTextPresentation(TxText txText, LocalizedText locText, ContentHelper ch, boolean hasId) throws IOException, SQLException {
		Layer layer = new Layer();
		layer.setStyleClass("newText");
		if (ch != null && locText != null) {
			layer.add(getTextTable(txText, locText, ch));

		}
		if (this.isAdmin) {
			layer.add(getAdminPart(this.iTextId, this.enableDelete, this.newobjinst, this.newWithAttribute, hasId));
		}

		return layer;
	}

	protected PresentationObject getTextTable(TxText txText, LocalizedText locText, ContentHelper contentHelper) throws IOException, SQLException {
		Layer layer = new Layer();
		layer.setStyleClass(DEFAULT_STYLE_CLASS);
		layer.setStyleClass("article_item_first");
		layer.setStyleClass("article_item_odd");

		String sHeadline = locText.getHeadline() != null ? locText.getHeadline() : "";
		String textBody = locText.getBody() != null ? locText.getBody() : "";

		if (this.reverse) {
			textBody = TextFormatter.textReverse(textBody);
		}
		if (this.crazy) {
			textBody = TextFormatter.textCrazy(textBody);
		}

		textBody = TextSoap.formatText(textBody);

		Layer headlineL = new Layer();
		headlineL.setStyleClass(styleClassPrefix + ATTRIBUTE_HEADLINE);
		Text headLine = new Text(sHeadline);
		headlineL.add(headLine);
		layer.add(headlineL);
		
		Layer bodyL = new Layer();
		bodyL.setStyleClass(styleClassPrefix + ATTRIBUTE_BODY);
		bodyL.add(new Text(textBody));
		layer.add(bodyL);

		List files = contentHelper.getFiles();
		if (files != null && files.size()>0) {
			try {
				ICFile imagefile = (ICFile) files.get(0);
				int imid = ((Integer)imagefile.getPrimaryKey()).intValue();
				String att = imagefile.getMetaData(TextEditorWindow.imageAttributeKey);

				Image textImage = new Image(imid);
				if (att != null) {
					textImage.addMarkupAttributes(getAttributeMap(att));
				}
				Layer imageL = new Layer();
				imageL.setStyleClass(styleClassPrefix + ATTRIBUTE_IMAGE);
				imageL.add(textImage);
				layer.add(imageL);
			}
			catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		
		return layer;
	}
	protected PresentationObject getAdminPart(int iTextId, boolean enableDelete, boolean newObjInst, boolean newWithAttribute, boolean hasId) {
		Layer layer = new Layer();
		layer.setStyleClass(styleClassPrefix + "ADMIN");

		if (iTextId > 0) {
			Link breyta = new Link(this.iwcb.getImage("/shared/edit.gif"));
			breyta.setWindowToOpen(TextEditorWindow.class);
			breyta.addParameter(TextEditorWindow.prmTextId, iTextId);
			breyta.addParameter(TextEditorWindow.prmObjInstId, getICObjectInstanceID());
			layer.add(breyta);

			if (enableDelete) {
				Link delete = new Link(this.iwcb.getImage("/shared/delete.gif"));
				delete.setWindowToOpen(TextEditorWindow.class);
				delete.addParameter(TextEditorWindow.prmDelete, iTextId);
				layer.add(delete);
			}
		}
		if (this.createInstance && newObjInst && !hasId) {
			Link newLink = new Link(this.iwcb.getImage("/shared/create.gif"));
			newLink.setWindowToOpen(TextEditorWindow.class);
			if (newObjInst) {
				newLink.addParameter(TextEditorWindow.prmObjInstId, getICObjectInstanceID());
			}
			else if (newWithAttribute) {
				newLink.addParameter(TextEditorWindow.prmAttribute, this.sAttribute);
			}

			layer.add(newLink);
		}

		return layer;
	}
}
