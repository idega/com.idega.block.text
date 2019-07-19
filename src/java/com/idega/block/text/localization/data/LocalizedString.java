package com.idega.block.text.localization.data;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = LocalizedString.ENTITY_NAME, indexes = {
		@Index(name = "loc_str_identifier_index", columnList = LocalizedString.COLUMN_IDENTIFIER),
		@Index(name = "loc_str_locale_index", columnList = LocalizedString.COLUMN_LOCALE),
		@Index(name = "loc_str_key_index", columnList = LocalizedString.COLUMN_KEY),
		@Index(name = "loc_str_message_index", columnList = LocalizedString.COLUMN_MESSAGE),
		@Index(name = "loc_str_key_id_loc_index", columnList = LocalizedString.COLUMN_KEY + "," + LocalizedString.COLUMN_IDENTIFIER + "," + LocalizedString.COLUMN_LOCALE),
		@Index(name = "loc_str_id_loc_index", columnList = LocalizedString.COLUMN_IDENTIFIER + "," + LocalizedString.COLUMN_LOCALE)
})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries({
	@NamedQuery(
			name = LocalizedString.FIND_BY_IDENTIFIER_KEY_LOCALE,
			query = "select t from LocalizedString t where t.key = :" + LocalizedString.PARAM_KEY + " and t.identifier = :" + LocalizedString.PARAM_IDENTIFIER + " and t.locale = :" + LocalizedString.PARAM_LOCALE +
			" order by t.version desc"
	),
	@NamedQuery(
			name = LocalizedString.FIND_MESSAGE_BY_IDENTIFIER_KEY_LOCALE,
			query = "select t.message from LocalizedString t where t.key = :" + LocalizedString.PARAM_KEY + " and t.identifier = :" + LocalizedString.PARAM_IDENTIFIER + " and t.locale = :" + LocalizedString.PARAM_LOCALE +
			" order by t.version desc"
	),
	@NamedQuery(
			name = LocalizedString.FIND_LATEST_VERSION_NR,
			query = "select max(t.version) from LocalizedString t where t.key = :" + LocalizedString.PARAM_KEY + " and t.identifier = :" + LocalizedString.PARAM_IDENTIFIER + " and t.locale = :" + LocalizedString.PARAM_LOCALE
	),
	@NamedQuery(
			name = LocalizedString.FIND_ALL_BY_IDENTIFIER_AND_LOCALE,
			query = "select t from LocalizedString t where t.identifier = :" + LocalizedString.PARAM_IDENTIFIER + " and t.locale = :" + LocalizedString.PARAM_LOCALE + " and t.version in " +
			"(select max(tt.version) from LocalizedString tt where t.key = tt.key and tt.identifier = :" + LocalizedString.PARAM_IDENTIFIER + " and tt.locale = :" + LocalizedString.PARAM_LOCALE + ") group by t.key"
	),
	@NamedQuery(
			name = LocalizedString.FIND_BY_IDENTIFIER_LOCALE_AND_KEYS,
			query = "select t from LocalizedString t where t.key in (:" + LocalizedString.PARAM_KEY + ") and t.identifier = :" + LocalizedString.PARAM_IDENTIFIER + " and t.locale = :" + LocalizedString.PARAM_LOCALE +
			" and t.version in (select max(tt.version) from LocalizedString tt where t.key = tt.key and tt.identifier = :" + LocalizedString.PARAM_IDENTIFIER + " and tt.locale = :" + LocalizedString.PARAM_LOCALE +
			") group by t.key"
	)
})
public class LocalizedString implements Serializable {

	private static final long serialVersionUID = 5059815671821875072L;

	public static final String	ENTITY_NAME = "tx_localized_string",

								COLUMN_IDENTIFIER = "identifier",
								COLUMN_KEY = "loc_key",
								COLUMN_LOCALE = "locale",
								COLUMN_MESSAGE = "message",
								COLUMN_VERSION = "version",
								COLUMN_MODIFIED = "modified",

								FIND_BY_IDENTIFIER_KEY_LOCALE = "LocalizedString.findByIdentifierKeyLocale",
								FIND_MESSAGE_BY_IDENTIFIER_KEY_LOCALE = "LocalizedString.findMessageByIdentifierKeyLocale",
								FIND_LATEST_VERSION_NR = "LocalizedString.findLatestVersionNr",
								FIND_ALL_BY_IDENTIFIER_AND_LOCALE = "LocalizedString.findAllByIdentifierAndLocale",
								FIND_BY_IDENTIFIER_LOCALE_AND_KEYS = "LocalizedString.findByIdentifierLocaleAndKeys",

								PARAM_KEY = "locKey",
								PARAM_IDENTIFIER = "identifier",
								PARAM_LOCALE = "locale";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = COLUMN_IDENTIFIER, nullable = false)
	private String identifier;

	@Column(name = COLUMN_LOCALE, nullable = false)
	private String locale;

	@Column(name = COLUMN_KEY, length = 2000, nullable = false)
	private String key;

	@Column(name = COLUMN_MESSAGE, length = 21844, nullable = false)
	private String message;

	@Column(name = COLUMN_VERSION, nullable = false)
	private Integer version;

	@Column(name = COLUMN_MODIFIED)
	private Timestamp modified;

	public LocalizedString() {
		super();
	}

	public LocalizedString(String identifier, String locale, String key, String message, Integer version) {
		this();

		this.identifier = identifier;
		this.locale = locale;
		this.key = key;
		this.message = message;
		this.version = version;
	}

	@PrePersist
	public void prePersist() {
		if (modified == null) {
			modified = new Timestamp(System.currentTimeMillis());
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Timestamp getModified() {
		return modified;
	}

	public void setModified(Timestamp modified) {
		this.modified = modified;
	}

	@Override
	public String toString() {
		return getLocale() + ": " + getKey() + "=" + getMessage() + ". Version: " + getVersion() + ". ID: " + getId();
	}

}