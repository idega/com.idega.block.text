package com.idega.block.text.localization.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.idega.block.text.localization.dao.LocalizedStringDAO;
import com.idega.block.text.localization.data.LocalizedString;
import com.idega.core.persistence.Param;
import com.idega.core.persistence.impl.GenericDaoImpl;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.datastructures.map.MapUtil;

@Repository("localizedStringDAO")
@Transactional(readOnly = true)
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class LocalizedStringDAOImpl extends GenericDaoImpl implements LocalizedStringDAO {

	@Override
	public String getLocalizedString(String key, String identifier, String locale) {
		return getLocalizedString(key, identifier, locale, String.class);
	}

	@Override
	public LocalizedString getLocalizedStringEntity(String key, String identifier, String locale) {
		return getLocalizedString(key, identifier, locale, LocalizedString.class);
	}

	private <T> T getLocalizedString(String key, String identifier, String locale, Class<T> type) {
		if (StringUtil.isEmpty(key) || StringUtil.isEmpty(identifier) || StringUtil.isEmpty(locale)) {
			return null;
		}

		try {
			List<T> messages = getResultList(
					type.getName().equals(LocalizedString.class.getName()) ? LocalizedString.FIND_BY_IDENTIFIER_KEY_LOCALE : LocalizedString.FIND_MESSAGE_BY_IDENTIFIER_KEY_LOCALE,
					type,
					0,
					1,
					null,
					new Param(LocalizedString.PARAM_KEY, key),
					new Param(LocalizedString.PARAM_IDENTIFIER, identifier),
					new Param(LocalizedString.PARAM_LOCALE, locale)
			);
			return ListUtil.isEmpty(messages) ? null : messages.iterator().next();
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error getting localized string for " + locale + " from " + identifier + " for " + key, e);
		}

		return null;
	}

	@Override
	public List<LocalizedString> getLocalizedStrings(String identifier, String locale) {
		if (StringUtil.isEmpty(identifier) || StringUtil.isEmpty(locale)) {
			return null;
		}

		try {
			List<LocalizedString> localizedStrings = getResultList(
					LocalizedString.FIND_ALL_BY_IDENTIFIER_AND_LOCALE,
					LocalizedString.class,
					new Param(LocalizedString.PARAM_IDENTIFIER, identifier),
					new Param(LocalizedString.PARAM_LOCALE, locale)
			);
			return localizedStrings;
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error getting localized strings for " + locale + " from " + identifier, e);
		}

		return null;
	}

	@Override
	public List<LocalizedString> getLocalizedStrings(String identifier, String locale, List<String> keys) {
		if (ListUtil.isEmpty(keys)) {
			return getLocalizedStrings(identifier, locale);
		}

		if (StringUtil.isEmpty(identifier) || StringUtil.isEmpty(locale)) {
			return null;
		}

		try {
			List<LocalizedString> localizedStrings = getResultList(
					LocalizedString.FIND_BY_IDENTIFIER_LOCALE_AND_KEYS,
					LocalizedString.class,
					new Param(LocalizedString.PARAM_KEY, keys),
					new Param(LocalizedString.PARAM_IDENTIFIER, identifier),
					new Param(LocalizedString.PARAM_LOCALE, locale)
			);
			return localizedStrings;
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error getting localized strings for " + locale + " from " + identifier + " by " + keys, e);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = false)
	public void setLocalizedString(String key, String message, String identifier, String locale) {
		if (StringUtil.isEmpty(message) || StringUtil.isEmpty(key) || StringUtil.isEmpty(identifier) || StringUtil.isEmpty(locale)) {
			return;
		}

		try {
			LocalizedString latestLS = getLocalizedStringEntity(key, identifier, locale);
			setLocalizedString(latestLS, key, message, identifier, locale);
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error setting localized string '" + message + "' for " + locale + " from " + identifier + " for " + key, e);
		}
	}

	@Transactional(readOnly = false)
	private void setLocalizedString(LocalizedString latestLS, String key, String message, String identifier, String locale) {
		try {
			Integer version = null;
			if (latestLS == null) {
				version = 1;

			} else {
				String latestMessage = latestLS.getMessage();
				if (!StringUtil.isEmpty(latestMessage) && latestMessage.equals(message)) {
					return;	//	No need to make duplicates of the same message
				}

				version = latestLS.getVersion();
				version++;
			}

			LocalizedString ls = new LocalizedString(identifier, locale, key, message, version);
			persist(ls);
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error setting localized string '" + message + "' for " + locale + " from " + identifier + " for " + key + ". Latest localization: " + latestLS, e);
		}
	}

	@Override
	@Transactional(readOnly = false)
	public void setLocalizedStrings(Map<String, String> localizations, String identifier, String locale) {
		if (MapUtil.isEmpty(localizations) || StringUtil.isEmpty(identifier) || StringUtil.isEmpty(locale)) {
			return;
		}

		List<String> keys = new ArrayList<>(localizations.keySet());
		List<LocalizedString> existingLocalizations = getLocalizedStrings(identifier, locale, keys);
		Map<String, LocalizedString> groupedExistingLocalizations = new HashMap<>();
		if (!ListUtil.isEmpty(existingLocalizations)) {
			for (LocalizedString ls: existingLocalizations) {
				groupedExistingLocalizations.put(ls.getKey(), ls);
			}
		}

		Map<String, String> copy = new HashMap<>(localizations);
		for (String key: keys) {
			LocalizedString latestLS = groupedExistingLocalizations.get(key);
			setLocalizedString(latestLS, key, copy.get(key), identifier, locale);
		}
	}

}