package com.idega.block.text;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.idega.block.text.data.bean.LocalizedText;
import com.idega.block.text.model.LocalizedTextModel;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;

public class TextUtil {

	private TextUtil() {}

	public static final int getICLocaleId(Locale locale) {
		if (locale == null) {
			locale = ICLocaleBusiness.getLocaleFromLocaleString(Locale.ENGLISH.toString());
		}
		return ICLocaleBusiness.getLocaleId(locale);
	}

	public static final <T extends LocalizedTextModel> T getLocalizedText(Collection<T> texts, Locale locale) {
		int localeId = getICLocaleId(locale);
		return getLocalizedText(texts, localeId);
	}

	public static final <T extends LocalizedTextModel> T getLocalizedText(Collection<T> texts, int icLocaleId) {
		if (ListUtil.isEmpty(texts)) {
			return null;
		}

		for (Iterator<T> it = texts.iterator(); it.hasNext();) {
			T temp = it.next();
			if (temp.getLocaleId() == icLocaleId) {
				return temp;
			}
		}

		return null;
	}

	public static final String getLocalized(List<LocalizedText> texts, int icLocaleId) {
		LocalizedText text = getLocalizedText(texts, icLocaleId);
		if (text == null) {
			return null;
		}

		String localized = text.getBody();
		localized = StringUtil.isEmpty(localized) ? text.getHeadline() : localized;
		return StringUtil.isEmpty(localized) ? text.getTitle() : localized;
	}

	public static final String getLocalized(List<LocalizedText> texts, Locale locale) {
		return getLocalized(texts, getICLocaleId(locale));
	}

}