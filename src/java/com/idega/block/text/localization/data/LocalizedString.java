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
import javax.persistence.Lob;
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
		@Index(name = "loc_str_version_index", columnList = LocalizedString.COLUMN_VERSION),
		@Index(name = "loc_str_key_id_loc_index", columnList = LocalizedString.COLUMN_KEY + "," + LocalizedString.COLUMN_IDENTIFIER + "," + LocalizedString.COLUMN_LOCALE),
		@Index(name = "loc_str_key_id_loc_del_index", columnList = LocalizedString.COLUMN_KEY + "," + LocalizedString.COLUMN_IDENTIFIER + "," + LocalizedString.COLUMN_LOCALE + "," + LocalizedString.COLUMN_DELETED),
		@Index(name = "loc_str_id_loc_index", columnList = LocalizedString.COLUMN_IDENTIFIER + "," + LocalizedString.COLUMN_LOCALE),
		@Index(name = "loc_str_key_id_loc_ver_index", columnList = LocalizedString.COLUMN_KEY + "," + LocalizedString.COLUMN_IDENTIFIER + "," + LocalizedString.COLUMN_LOCALE + "," + LocalizedString.COLUMN_VERSION),
		@Index(name = "loc_str_id_loc_ver_index", columnList = LocalizedString.COLUMN_IDENTIFIER + "," + LocalizedString.COLUMN_LOCALE + "," + LocalizedString.COLUMN_VERSION),
		@Index(
				name = "loc_str_key_id_loc_ver_del_index",
				columnList = LocalizedString.COLUMN_KEY + "," + LocalizedString.COLUMN_IDENTIFIER + "," + LocalizedString.COLUMN_LOCALE + "," + LocalizedString.COLUMN_VERSION + "," + LocalizedString.COLUMN_DELETED
		),
		@Index(name = "loc_str_id_loc_ver_del_index", columnList = LocalizedString.COLUMN_IDENTIFIER + "," + LocalizedString.COLUMN_LOCALE + "," + LocalizedString.COLUMN_VERSION + "," + LocalizedString.COLUMN_DELETED)
})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries({
	@NamedQuery(
			name = LocalizedString.FIND_BY_IDENTIFIER_KEY_LOCALE,
			query = "select t from LocalizedString t where t.key = :" + LocalizedString.PARAM_KEY + " and t.identifier = :" + LocalizedString.PARAM_IDENTIFIER + " and t.locale = :" + LocalizedString.PARAM_LOCALE +
			" and t.deleted = 0 order by t.version desc"
	),
	@NamedQuery(
			name = LocalizedString.FIND_MESSAGE_BY_IDENTIFIER_KEY_LOCALE,
			query = "select t.message from LocalizedString t where t.key = :" + LocalizedString.PARAM_KEY + " and t.identifier = :" + LocalizedString.PARAM_IDENTIFIER + " and t.locale = :" + LocalizedString.PARAM_LOCALE +
			" and t.deleted = 0 order by t.version desc"
	),
	@NamedQuery(
			name = LocalizedString.FIND_LATEST_VERSION_NR,
			query = "select max(t.version) from LocalizedString t where t.key = :" + LocalizedString.PARAM_KEY + " and t.identifier = :" + LocalizedString.PARAM_IDENTIFIER + " and t.locale = :" + LocalizedString.PARAM_LOCALE +
			" and t.deleted = 0"
	),
	@NamedQuery(
			name = LocalizedString.FIND_ALL_LATEST_BY_IDENTIFIER_AND_LOCALE,
			query = "select t from LocalizedString t where t.identifier = :" + LocalizedString.PARAM_IDENTIFIER + " and t.locale = :" + LocalizedString.PARAM_LOCALE + " and t.version in " +
			"(select max(tt.version) from LocalizedString tt where t.key = tt.key and tt.identifier = :" + LocalizedString.PARAM_IDENTIFIER + " and tt.locale = :" + LocalizedString.PARAM_LOCALE +
			" and tt.deleted = 0) and t.deleted = 0 group by t.key"
	),
	@NamedQuery(
			name = LocalizedString.FIND_ALL_BY_IDENTIFIER_AND_LOCALE_AND_KEY,
			query = "select t from LocalizedString t where t.key = :" + LocalizedString.PARAM_KEY + " and t.identifier = :" + LocalizedString.PARAM_IDENTIFIER + " and t.locale = :" + LocalizedString.PARAM_LOCALE +
			" and t.deleted = 0"
	),
	@NamedQuery(
			name = LocalizedString.FIND_BY_IDENTIFIER_LOCALE_AND_KEYS,
			query = "select t from LocalizedString t where t.key in (:" + LocalizedString.PARAM_KEY + ") and t.identifier = :" + LocalizedString.PARAM_IDENTIFIER + " and t.locale = :" + LocalizedString.PARAM_LOCALE +
			" and t.version in (select max(tt.version) from LocalizedString tt where t.key = tt.key and tt.identifier = :" + LocalizedString.PARAM_IDENTIFIER + " and tt.locale = :" + LocalizedString.PARAM_LOCALE +
			" and tt.deleted = 0) and t.deleted = 0 group by t.key"
	),
	@NamedQuery(
			name = LocalizedString.FIND_LATEST_MODIFIED_DATE,
			query = "select max(t.modified) from LocalizedString t where t.identifier = :" + LocalizedString.PARAM_IDENTIFIER + " and t.locale = :" + LocalizedString.PARAM_LOCALE + " and t.deleted = 0"
	),
	@NamedQuery(
			name = LocalizedString.FIND_LATEST_MODIFIED_DATE_BY_KEYS,
			query = "select t from LocalizedString t where t.key in (:" + LocalizedString.PARAM_KEY + ") and t.identifier = :" + LocalizedString.PARAM_IDENTIFIER + " and t.locale = :" + LocalizedString.PARAM_LOCALE +
			" and t.modified in (select max(tt.modified) from LocalizedString tt where t.key = tt.key and tt.identifier = :" + LocalizedString.PARAM_IDENTIFIER + " and tt.locale = :" + LocalizedString.PARAM_LOCALE +
			" and tt.deleted = 0) and t.deleted = 0 group by t.key"
	),
	@NamedQuery(
			name = LocalizedString.GET_ALL_VERSIONS,
			query = "select t from LocalizedString t where t.key = :" + LocalizedString.PARAM_KEY + " and t.identifier = :" + LocalizedString.PARAM_IDENTIFIER + " and t.locale = :" + LocalizedString.PARAM_LOCALE +
			" and t.deleted = 0 order by t.version desc"
	),
	@NamedQuery(
			name = LocalizedString.GET_LOCALIZED_STRING,
			query = "select t from LocalizedString t where t.key = :" + LocalizedString.PARAM_KEY + " and t.identifier = :" + LocalizedString.PARAM_IDENTIFIER + " and t.locale = :" + LocalizedString.PARAM_LOCALE +
			" and t.message = :" + LocalizedString.PARAM_MESSAGE + " and t.deleted = 0 order by t.version desc"
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
								COLUMN_DELETED = "deleted",
								COLUMN_DELETED_WHEN = "deleted_when",
								COLUMN_DELETED_BY = "deleted_by",

								FIND_BY_IDENTIFIER_KEY_LOCALE = "LocalizedString.findByIdentifierKeyLocale",
								FIND_MESSAGE_BY_IDENTIFIER_KEY_LOCALE = "LocalizedString.findMessageByIdentifierKeyLocale",
								FIND_LATEST_VERSION_NR = "LocalizedString.findLatestVersionNr",
								FIND_ALL_LATEST_BY_IDENTIFIER_AND_LOCALE = "LocalizedString.findAllLatestByIdentifierAndLocale",
								FIND_ALL_BY_IDENTIFIER_AND_LOCALE_AND_KEY = "LocalizedString.findAllByIdentifierAndLocaleAndKey",
								FIND_BY_IDENTIFIER_LOCALE_AND_KEYS = "LocalizedString.findByIdentifierLocaleAndKeys",
								FIND_LATEST_MODIFIED_DATE = "LocalizedString.findLatestModifiedDate",
								FIND_LATEST_MODIFIED_DATE_BY_KEYS = "LocalizedString.findLatestModifiedStringsByKeys",
								GET_ALL_VERSIONS = "LocalizedString.getAllVersions",
								GET_LOCALIZED_STRING = "LocalizedString.getLocalizedString",

								PARAM_KEY = "locKey",
								PARAM_IDENTIFIER = "identifier",
								PARAM_LOCALE = "locale",
								PARAM_MESSAGE = "message";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = COLUMN_IDENTIFIER, nullable = false)
	private String identifier;

	@Column(name = COLUMN_LOCALE, nullable = false)
	private String locale;

	@Column(name = COLUMN_KEY, length = 255, nullable = false)
	private String key;

	@Lob
	@Column(name = COLUMN_MESSAGE, length = 16383, nullable = false)
	private String message;

	@Column(name = COLUMN_VERSION, nullable = false)
	private Integer version;

	@Column(name = COLUMN_MODIFIED)
	private Timestamp modified;

	@Column(name = COLUMN_DELETED)
	private Boolean deleted;

	@Column(name = COLUMN_DELETED_WHEN)
	private Timestamp deletedWhen;

	@Column(name = COLUMN_DELETED_BY)
	private Integer deletedBy;

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
		if (deleted == null) {
			deleted = Boolean.FALSE;
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

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Timestamp getDeletedWhen() {
		return deletedWhen;
	}

	public void setDeletedWhen(Timestamp deletedWhen) {
		this.deletedWhen = deletedWhen;
	}

	public Integer getDeletedBy() {
		return deletedBy;
	}

	public void setDeletedBy(Integer deletedBy) {
		this.deletedBy = deletedBy;
	}

	@Override
	public String toString() {
		return getLocale() + ": " + getKey() + "=" + getMessage() + ". Version: " + getVersion() + ". ID: " + getId();
	}

}