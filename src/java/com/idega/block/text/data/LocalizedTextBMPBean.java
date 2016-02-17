//idega 2000 - Laddi
package com.idega.block.text.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import com.idega.data.IDOAddRelationshipException;
import com.idega.data.IDOEntity;
import com.idega.data.IDORelationshipException;
import com.idega.util.CoreConstants;
import com.idega.util.text.TextSoap;

public class LocalizedTextBMPBean extends com.idega.data.GenericEntity implements com.idega.block.text.data.LocalizedText {

	private static final long serialVersionUID = 8914454824558244503L;

	public static final String	TABLE_NAME = "TX_LOCALIZED_TEXT",
								COLUMN_LOCALE_ID = "IC_LOCALE_ID",
								COLUMN_HEADLINE = "HEADLINE",
								COLUMN_TITLE = "TITLE",
								COLUMN_BODY = "BODY",
								COLUMN_CREATED = "CREATED",
								COLUMN_UPDATED = "UPDATED",
								COLUMN_MARKUP_LANGUAGE = "MARKUP_LANGUAGE";

  public LocalizedTextBMPBean(){
    super();
    setBody(CoreConstants.EMPTY);
  }

  public LocalizedTextBMPBean(int id)throws SQLException{
    super(id);
    setBody(CoreConstants.EMPTY);
  }

  @Override
public void initializeAttributes(){
    addAttribute(getIDColumnName());
    addAttribute(getColumnNameLocaleId(), "Locale", true, true, java.lang.Integer.class,"many_to_one",com.idega.core.localisation.data.ICLocale.class);
    addAttribute(getColumnNameHeadline(), "Headline", true, true, java.lang.String.class);
    addAttribute(getColumnNameTitle(), "Title", true, true, java.lang.String.class);
    addAttribute(getColumnNameBody(), "Body", true, true, java.lang.String.class,30000);
    addAttribute(getColumnNameCreated(), "Created", true, true, java.sql.Timestamp.class);
    addAttribute(getColumnNameUpdated(), "Updated", true, true, java.sql.Timestamp.class);
    addAttribute(getColumnNameMarkupLanguage(), "The markup language of the text", true, true, java.lang.String.class);

    addIndex("IDX_TX_LOCALIZED_TEXT_1", getColumnNameLocaleId());
  }

  public static String getEntityTableName(){ return TABLE_NAME;}
  public static String getColumnNameLocaleId(){ return COLUMN_LOCALE_ID;}
  public static String getColumnNameHeadline(){ return COLUMN_HEADLINE;}
  public static String getColumnNameTitle(){ return COLUMN_TITLE;}
  public static String getColumnNameBody(){ return COLUMN_BODY;}
  public static String getColumnNameCreated(){ return COLUMN_CREATED;}
  public static String getColumnNameUpdated(){ return COLUMN_UPDATED;}
  public static String getColumnNameMarkupLanguage(){ return COLUMN_MARKUP_LANGUAGE;}

  @Override
public String getEntityName(){
    return getEntityTableName();
  }
  @Override
public int getLocaleId(){
    return getIntColumnValue(getColumnNameLocaleId());
  }
  @Override
public void setLocaleId(int id){
    setColumn(getColumnNameLocaleId(),id);
  }
  @Override
public void setLocaleId(Integer id){
    setColumn(getColumnNameLocaleId(),id);
  }
  @Override
public String getHeadline(){
    return getStringColumnValue(getColumnNameHeadline());
  }
  @Override
public void setHeadline(String headline){
    setColumn(getColumnNameHeadline(), headline);
  }
  @Override
public String getTitle(){
    return getStringColumnValue(getColumnNameTitle());
  }
  @Override
public void setTitle(String title){
    setColumn(getColumnNameTitle(), title);
  }
  @Override
public String getBody(){
    return getStringColumnValue(getColumnNameBody());
  }
  @Override
public void setBody(String body){
    setColumn(getColumnNameBody(), addBreaks(body));
  }
  @Override
public String getMarkupLanguage(){
    return getStringColumnValue(getColumnNameMarkupLanguage());
  }
  @Override
public void setMarkupLanguage(String markup){
    setColumn(getColumnNameMarkupLanguage(), markup);
  }
  @Override
public java.sql.Timestamp getCreated(){
    return (java.sql.Timestamp) getColumnValue(getColumnNameCreated());
  }
  @Override
public void setCreated(java.sql.Timestamp stamp){
    setColumn(getColumnNameCreated(), stamp);
  }
  @Override
public java.sql.Timestamp getUpdated(){
    return (java.sql.Timestamp) getColumnValue(getColumnNameUpdated());
  }
  @Override
public void setUpdated(java.sql.Timestamp stamp){
    setColumn(getColumnNameUpdated(), stamp);
  }

  private String addBreaks(String text){
    //replace with local bean method? and a none html specific xml
    return TextSoap.findAndReplaceOnPrefixCondition(text, "\r\n", ">","<br/>",true);
  }
  @Override
public Collection ejbFindRelatedEntities(IDOEntity entity) throws IDORelationshipException{
  	return idoGetRelatedEntities(entity);
  }
  /*
   *  (non-Javadoc)
   * @see com.idega.data.GenericEntity#idoAddTo(com.idega.data.IDOEntity)
   */
  @Override
public void idoAddTo(IDOEntity entity) throws IDOAddRelationshipException {

  	try {
  		idoAddTo(getNameOfMiddleTable(entity, this), entity.getEntityDefinition().getPrimaryKeyDefinition().getField().getSQLFieldName(), entity.getPrimaryKey());
  	} catch (Exception e) {
  		throw new IDOAddRelationshipException(e, this);
  	}
  }
  /*
   * copy and paste from GenericEntity
   */
  @Override
public void idoAddTo(String middleTableName, String sqlFieldName, Object primaryKey) throws IDOAddRelationshipException {
  	/**
  	 * @todo Change implementation
  	 */
  	try {
  		Connection conn = null;
  		Statement Stmt = null;
  		try {
  			conn = getConnection(getDatasource());
  			Stmt = conn.createStatement();
  			//String sql = "insert into "+getNameOfMiddleTable(entityToAddTo,this)+"("+getIDColumnName()+","+entityToAddTo.getIDColumnName()+") values("+getID()+","+entityToAddTo.getID()+")";
  			String sql = null;
  			//try
  			//{
  			sql = "insert into " + middleTableName + "(" + getIDColumnName() + "," + sqlFieldName + ") values(" + getPrimaryKeyValueSQLString() + "," + getKeyValueSQLString(primaryKey) + ")";
  			/*}
  			 catch (RemoteException rme)
  			 {
  			 throw new SQLException("RemoteException in addTo, message: " + rme.getMessage());
  			 }*/

  			//debug("statement: "+sql);

  			Stmt.executeUpdate(sql);
  		} finally {
  			if (Stmt != null) {
  				Stmt.close();
  			}
  			if (conn != null) {
  				freeConnection(getDatasource(), conn);
  			}
  		}

  	} catch (Exception ex) {
  		//ex.printStackTrace();
  		throw new IDOAddRelationshipException(ex, this);
  	}
  }

}
