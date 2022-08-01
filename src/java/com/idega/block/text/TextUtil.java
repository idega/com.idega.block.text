package com.idega.block.text;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import com.idega.block.text.model.LocalizedTextModel;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.util.ListUtil;

public class TextUtil {

	private TextUtil() {}

	public static final <T extends LocalizedTextModel> T getLocalizedText(Collection<T> texts, Locale locale) {
		if (locale == null) {
			locale = ICLocaleBusiness.getLocaleFromLocaleString(Locale.ENGLISH.toString());
		}
		int localeId = ICLocaleBusiness.getLocaleId(locale);
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

}