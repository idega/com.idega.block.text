package com.idega.block.text.localization.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.text.localization.dao.LocalizedStringDAO;
import com.idega.block.text.localization.data.LocalizedString;
import com.idega.block.text.localization.service.DatabaseResourceBundle;
import com.idega.block.text.localization.service.LocalizedStringService;
import com.idega.core.business.DefaultSpringBean;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.core.localisation.data.ICLocale;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWMainApplicationStartedEvent;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.JarLoadedResourceBundle;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.LocaleUtil;
import com.idega.util.StringUtil;
import com.idega.util.datastructures.map.MapUtil;
import com.idega.util.messages.MessageResource;
import com.idega.util.messages.MessageResourceFactory;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class LocalizedStringServiceImpl extends DefaultSpringBean implements LocalizedStringService, ApplicationListener<IWMainApplicationStartedEvent> {

	@Autowired
	private MessageResourceFactory messageResourceFactory;

	@Autowired
	private LocalizedStringDAO localizedStringDAO;

	@Override
	public Set<String> getKeys(String identifier, String locale, List<String> sources) {
		if (StringUtil.isEmpty(identifier) || StringUtil.isEmpty(locale)) {
			return null;
		}

		try {
			List<MessageResource> resources = messageResourceFactory.getResourceListByBundleAndLocale(identifier, LocaleUtil.getLocale(locale));
			if (ListUtil.isEmpty(resources)) {
				return null;
			}

			Set<String> results = new HashSet<>();
			for (MessageResource resource: resources) {
				if (resource instanceof DatabaseResourceBundle) {
					continue;
				}
				if (!ListUtil.isEmpty(sources) && !sources.contains(resource.getIdentifier())) {
					continue;
				}

				Set<String> keys = resource.getAllLocalizedKeys();
				if (!ListUtil.isEmpty(keys)) {
					results.addAll(keys);
				}
			}
			return results;
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error getting keys for " + identifier + " and locale " + locale, e);
		}

		return null;
	}

	@Override
	public Map<String, String> getLocalizations(Set<String> keys, String identifier, String locale, List<String> sources) {
		if (ListUtil.isEmpty(keys) || StringUtil.isEmpty(identifier) || StringUtil.isEmpty(locale)) {
			return null;
		}

		try {
			List<MessageResource> resources = messageResourceFactory.getResourceListByBundleAndLocale(identifier, LocaleUtil.getLocale(locale));
			if (ListUtil.isEmpty(resources)) {
				return null;
			}

			Map<String, String> results = new HashMap<>();
			Map<String, Boolean> keysToSkip = new HashMap<>();
			boolean checkFilesLastModifiedDate = getApplication().getSettings().getBoolean("localizations.check_file_date", false);

			for (MessageResource resource: resources) {
				if (resource instanceof DatabaseResourceBundle) {
					continue;
				}
				if (!ListUtil.isEmpty(sources) && !sources.contains(resource.getIdentifier())) {
					continue;
				}

				boolean skip = false;
				long lastModified = resource.lastModified();
				if (checkFilesLastModifiedDate) {
					Timestamp lastModifiedString = null;
					if (lastModified >= 0) {
						lastModifiedString = localizedStringDAO.getLastModificationDate(identifier, locale);
						if (lastModifiedString != null && lastModifiedString.getTime() >= lastModified) {
							skip = true;	//	Changes in localization file where made before changes in DB
						}
					}
					if (skip) {
						getLogger().info("Localization file in " + identifier + " for " + locale + " was changed at " + new Timestamp(lastModified) + ". Last change in DB: " + lastModifiedString +
								" - skiping localization file with identifier " + resource.getIdentifier());
						continue;
					}
				}

				Map<String, LocalizedString> lastModifications = null;
				List<LocalizedString> lastModifiedStrings = localizedStringDAO.getLastModifiedStrings(new ArrayList<>(keys), identifier, locale);
				Map<String, Boolean> skipKeys = new HashMap<>();
				if (!ListUtil.isEmpty(lastModifiedStrings)) {
					lastModifications = new HashMap<>();
					for (LocalizedString ls: lastModifiedStrings) {
						Timestamp lastModifiedLocalizedString = ls.getModified();
						if (lastModifiedLocalizedString != null && lastModified >= 0 && lastModified > lastModifiedLocalizedString.getTime()) {
							//	Current string was changed in DB before localization file was changed - will need to compare translations to make sure latest version is used
							lastModifications.put(ls.getKey(), ls);
						} else {
							//	Current string was changed in DB after localization file was changes - no need to update localization in DB
							skipKeys.put(ls.getKey(), Boolean.TRUE);
						}
					}
				}

				for (String key: keys) {
					if (StringUtil.isEmpty(key)) {
						continue;
					}
					if (results.containsKey(key) || keysToSkip.containsKey(key)) {
						continue;
					}
					String message = resource.getMessage(key, false);
					if (StringUtil.isEmpty(message) || key.equals(message)) {
						continue;
					}

					boolean skipKey = false;
					LocalizedString lastModifiedLS = lastModifications == null ? null : lastModifications.get(key);
					Timestamp lastModifiedLocalizedString = lastModifiedLS == null ? null : lastModifiedLS.getModified();
					if (lastModifiedLocalizedString != null) {
						if (message.equals(lastModifiedLS.getMessage())) {
							skipKey = true;

						} else {
							LocalizedString existingVersion = localizedStringDAO.getLocalizedString(key, identifier, locale, message);
							if (existingVersion != null) {
								lastModifiedLocalizedString = existingVersion.getModified();
								skipKey = true;
							}
						}
					} else {
						if (skipKeys.containsKey(key)) {
							skipKey = true;
						}
					}
					if (skipKey) {
						results.remove(key);
						keysToSkip.put(key, Boolean.TRUE);
						continue;
					}

					results.put(key, message);
				}
			}
			return results;
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error getting localizations for keys " + keys + " and " + identifier + " and locale " + locale, e);
		}

		return null;
	}

	@Override
	public void onApplicationEvent(IWMainApplicationStartedEvent event) {
		doImportLocalizations(event.getIWMA());
	}

	private void doImportLocalizations(IWMainApplication iwma) {
		try {
			List<ICLocale> locales = ICLocaleBusiness.listOfLocales(true);
			if (ListUtil.isEmpty(locales)) {
				return;
			}

			Map<String, IWBundle> bundles = iwma.getLoadedBundles();
			if (MapUtil.isEmpty(bundles)) {
				return;
			}

			List<String> sources = null;
			String propName = "localizations.sources";
			String propValue = iwma.getSettings().getProperty(propName);
			if (!StringUtil.isEmpty(propValue)) {
				sources = Arrays.asList(propValue.split(CoreConstants.COMMA));
			}

			for (IWBundle bundle: bundles.values()) {
				String identifier = bundle.getBundleIdentifier();
				for (ICLocale locale: locales) {
					String localeKey = locale.getLocale();

					Set<String> keys = getKeys(identifier, localeKey, sources);
					if (ListUtil.isEmpty(keys)) {
						getLogger().info("No keywords to import from " + identifier + " for " + localeKey + ". Sources: " + (ListUtil.isEmpty(sources) ? "all" : sources));
						continue;
					}

					Map<String, String> localizations = getLocalizations(keys, identifier, localeKey, sources);
					if (MapUtil.isEmpty(localizations)) {
						getLogger().info("No localizations to import from " + identifier + " for " + localeKey + ". Sources: " + (ListUtil.isEmpty(sources) ? "all" : sources));
						continue;
					}

					getLogger().info("Importing localizations (" + localizations.size() + ") from " + identifier + " for " + localeKey + ". Sources: " + (ListUtil.isEmpty(sources) ? "all" : sources));
					localizedStringDAO.setLocalizedStrings(localizations, identifier, localeKey);
					getLogger().info("Imported " + localizations.size() + " localization(s) from " + identifier + " for " + localeKey + ". Sources: " + (ListUtil.isEmpty(sources) ? "all" : sources));

					IWResourceBundle iwrb = bundle.getResourceBundle(LocaleUtil.getLocale(localeKey));
					Set<String> foundKeys = iwrb.getAllLocalizedKeys();
					getLogger().info("Found " + (foundKeys == null ? 0 : foundKeys.size()) + " key(s) for localization(s) from " + identifier + " for " + localeKey + ". Sources: " +
							(ListUtil.isEmpty(sources) ? "all" : sources));
				}
			}

			if (propValue == null) {
				propValue = IWResourceBundle.RESOURCE_IDENTIFIER.concat(CoreConstants.COMMA).concat(JarLoadedResourceBundle.RESOURCE_IDENTIFIER);
				iwma.getSettings().setProperty(propName, propValue);
			}
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error importing all localizations into " + DatabaseResourceBundle.class.getName(), e);
		}
	}

}