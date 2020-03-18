package com.idega.block.text.localization.service;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.text.localization.dao.LocalizedStringDAO;
import com.idega.idegaweb.DefaultIWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.util.StringUtil;
import com.idega.util.datastructures.map.MapUtil;
import com.idega.util.expression.ELUtil;
import com.idega.util.messages.MessageResource;
import com.idega.util.messages.MessageResourceImportanceLevel;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DatabaseResourceBundle extends IWResourceBundle implements MessageResource, Serializable {

	private static final long serialVersionUID = -2321119189214977991L;

	private static final String	RESOURCE_IDENTIFIER = "database_resource";

	@Autowired
	private LocalizedStringDAO localizedStringDAO;

	private long lastModified;

	private Map<String, String> recentChanges = new HashMap<>();

	private LocalizedStringDAO getLocalizedStringDAO() {
		if (localizedStringDAO == null) {
			ELUtil.getInstance().autowire(this);
		}

		return localizedStringDAO;
	}

	public DatabaseResourceBundle() throws IOException {
		super();
	}

	@Override
	protected void initProperities() {
		setIdentifier(RESOURCE_IDENTIFIER);
		setLevel(DefaultIWBundle.isProductionEnvironment() ? MessageResourceImportanceLevel.FIRST_ORDER : MessageResourceImportanceLevel.OFF);
		setAutoInsert(true);
	}

	@Override
	public void initialize(String bundleIdentifier, Locale locale, long lastModified) throws IOException {
		setLocale(locale);
		setBundleIdentifier(bundleIdentifier);
		if (DefaultIWBundle.isProductionEnvironment()) {
			getLookup();
		}
		this.lastModified = lastModified;
	}

	@Override
	protected Map<String, String> getLookup() {
		if (MapUtil.isEmpty(super.getLookup())) {
			Properties localizationProps = new Properties();

			Map<String, String> localizedStrings = getLocalizedStringDAO().getLocalizedStrings(getBundleIdentifier(), getLocale().toString());
			if (!MapUtil.isEmpty(localizedStrings)) {
				Map<String, Boolean> inited = new HashMap<>();
				for (String key: localizedStrings.keySet()) {
					if (StringUtil.isEmpty(key) || inited.containsKey(key)) {
						continue;
					}

					inited.put(key, Boolean.TRUE);
					String message = localizedStrings.get(key);
					localizationProps.setProperty(key, message);
				}
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			Map<String, String> props = new TreeMap(localizationProps);
			setLookup(props);
		}

		Map<String, String> props = super.getLookup();
		if (props == null) {
			props = new TreeMap<>();
		}
		return props;
	}

	@Override
	public void storeState() {
		if (MapUtil.isEmpty(recentChanges)) {
			return;
		}

		Map<String, String> toSave = recentChanges;
		getLocalizedStringDAO().setLocalizedStrings(toSave, getBundleIdentifier(), getLocale().toString());
		recentChanges.clear();
	}

	@Override
	public String getLocalizedString(String key) {
		if (StringUtil.isEmpty(key)) {
			return null;
		}

		String localizedString = getLookup().get(key);
		if (localizedString != null && !"null".equals(localizedString)) {
			return localizedString;
		}

		localizedString = getLocalizedStringDAO().getLocalizedString(key, getBundleIdentifier(), getLocale().toString());
		if (localizedString != null && !"null".equals(localizedString)) {
			setString(key, localizedString);
			return localizedString;
		}

		return null;
	}

	@Override
	public void setString(String key, String value) {
		getLookup().put(key, value);
		recentChanges.put(key, value);
	}

	/**
	 * @return <code>true</code> - if the value presents in repository bundle. <code>false</code> - in other case
	 *
	 */
	@Override
	protected boolean checkBundleLocalizedString(String key, String value) {
		return !StringUtil.isEmpty((String) handleGetObject(key));
	}

	@Override
	public String getMessage(String key) {
		return getMessage(key, true);
	}

	@Override
	public String getMessage(String key, boolean loadDefaultLocalization) {
		return getLocalizedString(key);
	}

	@Override
	public String setMessage(String key, String value) {
		String currentValue = getLookup().get(key);
		if (!StringUtil.isEmpty(currentValue) && currentValue.equals(value)) {
			return value;
		}

		if (key != null && value != null) {
			getLookup().put(key, value);
			recentChanges.put(key, value);
		}

		if (IWMainApplication.getDefaultIWMainApplication().getSettings().getBoolean("flush_each_localization_prop", Boolean.FALSE)) {
			getLocalizedStringDAO().setLocalizedString(key, value, getIdentifier(), getLocale().toString());
		}

		return value;
	}

	@Override
	public void setMessages(Map<String, String> values) {
		for (Object key : values.keySet()) {
			setString(String.valueOf(key), String.valueOf(values.get(key)));
		}

		storeState();
	}

	@Override
	public Set<String> getAllLocalizedKeys() {
		return getLookup().keySet();
	}

	@Override
	public void removeMessage(String key) {
		getLookup().remove(key);
		recentChanges.remove(key);
		getLocalizedStringDAO().deleteLocalizedString(key, getBundleIdentifier(), getLocale().toString());
	}

	@Override
	public long lastModified() {
		return lastModified;
	}

	@Override
	public String toString() {
		return "Database resource: " + getBundleIdentifier() + " for " + getLocale();
	}

}