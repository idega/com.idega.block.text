package com.idega.block.text.localization.dao.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.idega.block.text.localization.dao.LocalizedStringDAO;
import com.idega.block.text.localization.data.LocalizedString;
import com.idega.core.persistence.Param;
import com.idega.core.persistence.impl.GenericDaoImpl;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.user.data.bean.User;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.datastructures.map.MapUtil;

@Repository("localizedStringDAO")
@Transactional(readOnly = true)
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class LocalizedStringDAOImpl extends GenericDaoImpl implements LocalizedStringDAO {

	private Map<String, Map<String, Map<String, String>>> CACHE = new ConcurrentHashMap<>();

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

		T result = null;
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
			if (ListUtil.isEmpty(messages)) {
				return null;
			}

			result = messages.iterator().next();
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error getting localized string for " + locale + " from " + identifier + " for " + key, e);
		} finally {
			if (result != null) {
				addToCache(
						identifier,
						locale,
						key,
						result instanceof LocalizedString ?
								((LocalizedString) result).getMessage() :
								result.toString()
				);
			}
		}

		return result;
	}

	private Map<String, String> getCachedLocalizations(String identifier, String locale) {
		if (StringUtil.isEmpty(identifier) || StringUtil.isEmpty(locale) || !IWMainApplication.getDefaultIWMainApplication().getSettings().getBoolean("db_loc_strings.cache", true)) {
			return null;
		}

		Map<String, Map<String, String>> cachesByLocaleAndKey = CACHE.get(identifier);
		if (cachesByLocaleAndKey == null) {
			cachesByLocaleAndKey = new ConcurrentHashMap<>();
			CACHE.put(identifier, cachesByLocaleAndKey);
		}

		Map<String, String> cacheByKeys = cachesByLocaleAndKey.get(locale);
		if (cacheByKeys == null) {
			cacheByKeys = new TreeMap<>();
			cachesByLocaleAndKey.put(locale, cacheByKeys);
		}

		return cacheByKeys;
	}

	private void addToCache(String identifier, String locale, List<LocalizedString> localizations) {
		if (ListUtil.isEmpty(localizations)) {
			return;
		}

		Map<String, String> toCache = new HashMap<>();
		for (LocalizedString ls: localizations) {
			String key = ls.getKey();
			if (StringUtil.isEmpty(key)) {
				continue;
			}

			toCache.put(key, ls.getMessage());
		}

		addToCache(identifier, locale, toCache);
	}

	private void addToCache(String identifier, String locale, String key, String message) {
		if (StringUtil.isEmpty(key)) {
			return;
		}

		Map<String, String> localization = new HashMap<>();
		localization.put(key, message);
		addToCache(identifier, locale, localization);
	}

	private void addToCache(String identifier, String locale, Map<String, String> localizations) {
		if (MapUtil.isEmpty(localizations)) {
			return;
		}

		Map<String, String> currentLocalizations = getCachedLocalizations(identifier, locale);
		if (currentLocalizations == null) {
			return;
		}

		for (String key: localizations.keySet()) {
			String message = localizations.get(key);
			currentLocalizations.put(key, message);
		}
	}

	private void removeFromCache(String identifier, String locale, String key) {
		if (StringUtil.isEmpty(key)) {
			return;
		}

		Map<String, String> currentLocalizations = getCachedLocalizations(identifier, locale);
		if (currentLocalizations == null) {
			return;
		}

		currentLocalizations.remove(key);
	}

	private Map<String, String> getConverted(List<LocalizedString> localizedStrings) {
		if (ListUtil.isEmpty(localizedStrings)) {
			return null;
		}

		Map<String, String> converted = new TreeMap<>();
		try {
			for (LocalizedString ls: localizedStrings) {
				String key = ls.getKey();
				if (StringUtil.isEmpty(key)) {
					continue;
				}

				converted.put(key, ls.getMessage());
			}
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error converting " + localizedStrings + " to " + converted.getClass().getName(), e);
		}

		return converted;
	}

	@Override
	public Map<String, String> getLocalizedStrings(String identifier, String locale) {
		Map<String, String> cached = getCachedLocalizations(identifier, locale);
		if (!MapUtil.isEmpty(cached)) {
			return cached;
		}

		List<LocalizedString> localizedStrings = getLocalizedStringsEntities(identifier, locale);
		return getConverted(localizedStrings);
	}

	private List<LocalizedString> getLocalizedStringsEntities(String identifier, String locale) {
		if (StringUtil.isEmpty(identifier) || StringUtil.isEmpty(locale)) {
			return null;
		}

		List<LocalizedString> localizedStrings = null;
		try {
			localizedStrings = getResultList(
					LocalizedString.FIND_ALL_LATEST_BY_IDENTIFIER_AND_LOCALE,
					LocalizedString.class,
					new Param(LocalizedString.PARAM_IDENTIFIER, identifier),
					new Param(LocalizedString.PARAM_LOCALE, locale)
			);
			return localizedStrings;
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error getting localized strings for " + locale + " from " + identifier, e);
		} finally {
			addToCache(identifier, locale, localizedStrings);
		}

		return null;
	}

	@Override
	public Map<String, String> getLocalizedStrings(String identifier, String locale, List<String> keys) {
		if (ListUtil.isEmpty(keys)) {
			return getLocalizedStrings(identifier, locale);
		}

		Map<String, String> cached = getCachedLocalizations(identifier, locale);
		if (!MapUtil.isEmpty(cached)) {
			Map<String, String> results = new TreeMap<>();
			for (String key: keys) {
				results.put(key, cached.get(key));
			}
			if (!MapUtil.isEmpty(results)) {
				return results;
			}
		}

		List<LocalizedString> localizedStrings = getLocalizedStringsEntities(identifier, locale, keys);
		return getConverted(localizedStrings);
	}

	private List<LocalizedString> getLocalizedStringsEntities(String identifier, String locale, List<String> keys) {
		if (StringUtil.isEmpty(identifier) || StringUtil.isEmpty(locale)) {
			return null;
		}

		List<LocalizedString> localizedStrings = null;
		try {
			localizedStrings = getResultList(
					LocalizedString.FIND_BY_IDENTIFIER_LOCALE_AND_KEYS,
					LocalizedString.class,
					new Param(LocalizedString.PARAM_KEY, keys),
					new Param(LocalizedString.PARAM_IDENTIFIER, identifier),
					new Param(LocalizedString.PARAM_LOCALE, locale)
			);
			return localizedStrings;
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error getting localized strings for " + locale + " from " + identifier + " by " + keys, e);
		} finally {
			addToCache(identifier, locale, localizedStrings);
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
		LocalizedString ls = null;
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

			ls = new LocalizedString(identifier, locale, key, message, version);
			persist(ls);
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error setting localized string '" + message + "' for " + locale + " from " + identifier + " for " + key + ". Latest localization: " + latestLS, e);
		} finally {
			if (ls != null) {
				addToCache(identifier, locale, key, message);
			}
		}
	}

	@Override
	@Transactional(readOnly = false)
	public void setLocalizedStrings(Map<String, String> localizations, String identifier, String locale) {
		if (MapUtil.isEmpty(localizations) || StringUtil.isEmpty(identifier) || StringUtil.isEmpty(locale)) {
			return;
		}

		List<String> keys = new ArrayList<>(localizations.keySet());
		List<LocalizedString> existingLocalizations = getLocalizedStringsEntities(identifier, locale, keys);
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

		addToCache(identifier, locale, localizations);
	}

	@Override
	@Transactional(readOnly = false)
	public void deleteLocalizedString(String key, String identifier, String locale) {
		if (StringUtil.isEmpty(key) || StringUtil.isEmpty(identifier) || StringUtil.isEmpty(locale)) {
			return;
		}

		try {
			List<LocalizedString> toDelete = getResultList(
					LocalizedString.FIND_ALL_BY_IDENTIFIER_AND_LOCALE_AND_KEY,
					LocalizedString.class,
					new Param(LocalizedString.PARAM_KEY, key),
					new Param(LocalizedString.PARAM_IDENTIFIER, identifier),
					new Param(LocalizedString.PARAM_LOCALE, locale)
			);
			if (ListUtil.isEmpty(toDelete)) {
				return;
			}

			Integer deletedBy = null;
			IWContext iwc = CoreUtil.getIWContext();
			if (iwc != null && iwc.isLoggedOn()) {
				User user = iwc.getLoggedInUser();
				if (user != null) {
					deletedBy = user.getId();
				}
			}
			for (LocalizedString locStrToDelete: toDelete) {
				locStrToDelete.setDeleted(Boolean.TRUE);
				locStrToDelete.setDeletedWhen(new Timestamp(System.currentTimeMillis()));
				locStrToDelete.setDeletedBy(deletedBy);
				merge(locStrToDelete);
			}
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error deleting " + key + " from " + identifier + " by " + locale, e);
		} finally {
			removeFromCache(identifier, locale, key);
		}
	}

	@Override
	public Timestamp getLastModificationDate(String identifier, String locale) {
		if (StringUtil.isEmpty(identifier) || StringUtil.isEmpty(locale)) {
			return null;
		}

		try {
			Timestamp timestamp = getSingleResult(
					LocalizedString.FIND_LATEST_MODIFIED_DATE,
					Timestamp.class,
					new Param(LocalizedString.PARAM_IDENTIFIER, identifier),
					new Param(LocalizedString.PARAM_LOCALE, locale)
			);
			return timestamp;
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error getting last modification date for " + identifier + " by " + locale, e);
		}

		return null;
	}

	@Override
	public List<LocalizedString> getLastModifiedStrings(List<String> keys, String identifier, String locale) {
		if (ListUtil.isEmpty(keys) || StringUtil.isEmpty(identifier) || StringUtil.isEmpty(locale)) {
			return null;
		}

		try {
			List<LocalizedString> strings = getResultList(
					LocalizedString.FIND_LATEST_MODIFIED_DATE_BY_KEYS,
					LocalizedString.class,
					new Param(LocalizedString.PARAM_KEY, keys),
					new Param(LocalizedString.PARAM_IDENTIFIER, identifier),
					new Param(LocalizedString.PARAM_LOCALE, locale)
			);
			return strings;
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error getting last modified strings by keys " + keys + " from " + identifier + " by " + locale, e);
		}

		return null;
	}

	@Override
	public List<LocalizedString> getAllVersionsOfLocalizedString(String key, String identifier, String locale) {
		if (StringUtil.isEmpty(key) || StringUtil.isEmpty(identifier) || StringUtil.isEmpty(locale)) {
			return null;
		}

		try {
			List<LocalizedString> strings = getResultList(
					LocalizedString.GET_ALL_VERSIONS,
					LocalizedString.class,
					new Param(LocalizedString.PARAM_KEY, key),
					new Param(LocalizedString.PARAM_IDENTIFIER, identifier),
					new Param(LocalizedString.PARAM_LOCALE, locale)
			);
			return strings;
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error getting all versions for key " + key + " from " + identifier + " by " + locale, e);
		}

		return null;
	}

	@Override
	public LocalizedString getLocalizedString(String key, String identifier, String locale, String message) {
		if (StringUtil.isEmpty(key) || StringUtil.isEmpty(identifier) || StringUtil.isEmpty(locale) || StringUtil.isEmpty(message)) {
			return null;
		}

		try {
			List<LocalizedString> results = getResultList(
					LocalizedString.GET_LOCALIZED_STRING,
					LocalizedString.class,
					0,
					1,
					null,
					new Param(LocalizedString.PARAM_KEY, key),
					new Param(LocalizedString.PARAM_IDENTIFIER, identifier),
					new Param(LocalizedString.PARAM_LOCALE, locale),
					new Param(LocalizedString.PARAM_MESSAGE, message)
			);
			return ListUtil.isEmpty(results) ? null : results.iterator().next();
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error getting localized string for key " + key + " from " + identifier + " by " + locale + " and message " + message, e);
		}

		return null;
	}

}