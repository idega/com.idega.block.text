package com.idega.block.text.data;

import java.util.Collection;

import javax.ejb.FinderException;

import com.idega.data.IDOEntity;


public class LocalizedTextHomeImpl extends com.idega.data.IDOFactory implements LocalizedTextHome
{
 protected Class getEntityInterfaceClass(){
  return LocalizedText.class;
 }

 public LocalizedText create() throws javax.ejb.CreateException{
  return (LocalizedText) super.idoCreate();
 }

 public LocalizedText createLegacy(){
	try{
		return create();
	}
	catch(javax.ejb.CreateException ce){
		throw new RuntimeException("CreateException:"+ce.getMessage());
	}

 }

 public LocalizedText findByPrimaryKey(int id) throws javax.ejb.FinderException{
  return (LocalizedText) super.idoFindByPrimaryKey(id);
 }

 public LocalizedText findByPrimaryKey(Object pk) throws javax.ejb.FinderException{
  return (LocalizedText) super.idoFindByPrimaryKey(pk);
 }

 public LocalizedText findByPrimaryKeyLegacy(int id) throws java.sql.SQLException{
	try{
		return findByPrimaryKey(id);
	}
	catch(javax.ejb.FinderException fe){
		throw new java.sql.SQLException(fe.getMessage());
	}

 }

}