package com.idega.block.text.model;

public interface LocalizedTextModel {

	String getHeadline();

	String getTitle();

	String getBody();

	int getLocaleId();

	Object getPrimaryKey();

}