package com.idega.block.text.data;

import javax.ejb.FinderException;


public interface LocalizedTextHome extends com.idega.data.IDOHome
{
 public LocalizedText create() throws javax.ejb.CreateException;
 public LocalizedText createLegacy();
 public LocalizedText findByPrimaryKey(int id) throws FinderException;
 public LocalizedText findByPrimaryKey(Object pk) throws FinderException;
 public LocalizedText findByPrimaryKeyLegacy(int id) throws java.sql.SQLException;
 public LocalizedText findLocalizedNameForApplication(String applicationName, int localeId) throws FinderException;

}