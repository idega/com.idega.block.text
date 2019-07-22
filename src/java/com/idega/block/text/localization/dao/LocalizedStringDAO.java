package com.idega.block.text.localization.dao;

import java.util.List;
import java.util.Map;

import com.idega.block.text.localization.data.LocalizedString;
import com.idega.core.persistence.GenericDao;

public interface LocalizedStringDAO extends GenericDao {

	public String getLocalizedString(String key, String identifier, String locale);

	public LocalizedString getLocalizedStringEntity(String key, String identifier, String locale);

	public void setLocalizedString(String key, String message, String identifier, String locale);

	public void setLocalizedStrings(Map<String, String> localizations, String identifier, String locale);

	public List<LocalizedString> getLocalizedStrings(String identifier, String locale);

	public List<LocalizedString> getLocalizedStrings(String identifier, String locale, List<String> keys);

	public void deleteLocalizedString(String key, String identifier, String locale);

}