package com.idega.block.text.data.bean;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.idega.block.text.data.LocalizedTextBMPBean;
import com.idega.block.text.model.LocalizedTextModel;
import com.idega.core.localisation.data.bean.ICLocale;
import com.idega.util.DBUtil;
import com.idega.util.IWTimestamp;

@Entity
@Table(name = LocalizedText.TABLE_NAME)
@Cacheable
public class LocalizedText implements Serializable, LocalizedTextModel {

	private static final long serialVersionUID = 5009199779076471842L;

	public static final String TABLE_NAME = LocalizedTextBMPBean.TABLE_NAME;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = TABLE_NAME + "_ID")
	private Integer id;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = LocalizedTextBMPBean.COLUMN_LOCALE_ID)
	private ICLocale locale;

	@Column(name = LocalizedTextBMPBean.COLUMN_HEADLINE)
	private String headline;

	@Column(name = LocalizedTextBMPBean.COLUMN_TITLE)
	private String title;

	@Column(name = LocalizedTextBMPBean.COLUMN_BODY, length = 30000)
	private String body;

	@Column(name = LocalizedTextBMPBean.COLUMN_CREATED)
	private Timestamp created;

	@Column(name = LocalizedTextBMPBean.COLUMN_UPDATED)
	private Timestamp updated;

	@Column(name = LocalizedTextBMPBean.COLUMN_MARKUP_LANGUAGE)
	private String markupLanguage;

	public LocalizedText() {
		super();
	}

	public LocalizedText(ICLocale locale, String body) {
		this();

		this.locale = locale;
		this.body = body;
	}

	@PrePersist
	@PreUpdate
	public void prePersist() {
		Timestamp now = IWTimestamp.RightNow().getTimestamp();
		if (created == null) {
			created = now;
		}
		updated = now;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public ICLocale getLocale() {
		locale = DBUtil.getInstance().lazyLoad(locale);
		return locale;
	}

	public void setLocale(ICLocale locale) {
		this.locale = locale;
	}

	@Override
	public String getHeadline() {
		return headline;
	}

	public void setHeadline(String headline) {
		this.headline = headline;
	}

	@Override
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	public Timestamp getUpdated() {
		return updated;
	}

	public void setUpdated(Timestamp updated) {
		this.updated = updated;
	}

	public String getMarkupLanguage() {
		return markupLanguage;
	}

	public void setMarkupLanguage(String markupLanguage) {
		this.markupLanguage = markupLanguage;
	}

	@Override
	public int getLocaleId() {
		ICLocale locale = getLocale();
		return locale == null ? -1 : locale.getId();
	}

	@Override
	public Object getPrimaryKey() {
		return getId();
	}

}