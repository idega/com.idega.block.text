package com.idega.block.text.presentation;


import java.sql.*;
import java.util.*;
import java.io.*;
import com.idega.util.*;
import com.idega.jmodule.object.textObject.*;
import com.idega.jmodule.object.*;
import com.idega.jmodule.object.interfaceobject.*;
import com.idega.core.localisation.presentation.ICLocalePresentation;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.block.text.data.*;
import com.idega.block.text.business.*;
import com.idega.core.user.data.User;
import com.idega.jmodule.image.presentation.ImageInserter;
import com.idega.core.accesscontrol.business.AccessControl;
import com.idega.util.text.*;
import com.idega.idegaweb.presentation.IWAdminWindow;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;

public class TextEditorWindow extends IWAdminWindow{

  private final static String IW_BUNDLE_IDENTIFIER="com.idega.block.text";
  private boolean isAdmin = false;
  private boolean update = false;
  private boolean save = false;
  private int iUserId = -1;
  private static String prmAttribute = "txe.attribute";
  public static String prmTextId = "txep.txtextid";
  public static String prmDelete = "txep.txdeleteid";
  public static String prmLocale = "txep.localedrp";
  public static String prmObjInstId = "txep.icobjinstid";
  private static String prmHeadline = "txep.headline",prmBody = "txep.body",
    prmImageId = "txep.imageid",
    prmTxTextId = "txep.txtextid",prmLocalizedTextId = "txep.loctextid",
    prmUseImage = "txep.useimage",prmOldLocale = "txep.oldlocale";
  private static String prmMode = "txep.mode",prmAction = "txep.action";
  private static String actNone = "txea.none",actDelete = "txea.delete",
    actSave = "txea.save",actUpdate = "txea.update" ,actNew = "txea.new";
  private static String modeNew = "txem.new",modeUpdate ="txem.update",
    modeSave = "txem.save",modeDelete = "txea.delete";
  private TextHelper textHelper;

  private IWBundle iwb;
  private IWResourceBundle iwrb;

  public TextEditorWindow(){
    setWidth(570);
    setHeight(430);
    setUnMerged();
  }

  private void control(ModuleInfo modinfo)throws Exception{
    boolean doView = true;
    Locale currentLocale = modinfo.getCurrentLocale(),chosenLocale;

    String sLocaleId = modinfo.getParameter(prmLocale);

    int iLocaleId = -1;
    if(sLocaleId!= null){
      iLocaleId = Integer.parseInt(sLocaleId);
      chosenLocale = TextFinder.getLocale(iLocaleId);
    }
    else{
      chosenLocale = currentLocale;
      iLocaleId = ICLocaleBusiness.getLocaleId(chosenLocale);
    }

    add(sLocaleId+ " ");

    if ( isAdmin ) {
      String sAction;

    String sTextId = null,sAttribute = null;
    String sLocTextId = modinfo.getParameter(prmLocalizedTextId);
    if(modinfo.getParameter(prmTxTextId) != null){
      sTextId = modinfo.getParameter(prmTxTextId);
      add(sTextId);
    }
    else if(modinfo.getParameter(prmAttribute)!=null){
      sAttribute = modinfo.getParameter(actSave);
    }
    else if(modinfo.getParameter(prmDelete)!=null){
      confirmDelete(modinfo);
      doView = false;
    }


    if(modinfo.getParameter(actSave)!=null || modinfo.getParameter(actSave+".x")!=null ){
      saveText(modinfo,sTextId,sLocTextId);
    }
    else if(modinfo.getParameter( actDelete )!=null || modinfo.getParameter(actDelete+".x")!=null){
      deleteText(modinfo,sTextId,sLocTextId);
    }
    else if(modinfo.getParameter( actNew ) != null || modinfo.getParameter(actNew+".x")!= null){
      sTextId = null;sAttribute = null;
    }
    if(doView)
      doViewText(sTextId,sAttribute,chosenLocale,iLocaleId);
    }
    else {
      noAccess();
    }

  }

  private void doViewText(String sTextId,String sAttribute,Locale locale,int iLocaleId){
    TextHelper textHelper = null;
    if(sTextId != null){

      int iTextId = Integer.parseInt(sTextId);
      if(iLocaleId > 0)
        textHelper = TextFinder.getTextHelper(iTextId,iLocaleId);
      else
        textHelper = TextFinder.getTextHelper(iTextId,locale);
    }
    else if(sAttribute != null){
      textHelper = TextFinder.getTextHelper(sAttribute,iLocaleId);
    }
    LocalizedText LocTx = null;
    TxText  TxTx = null;
    if(textHelper != null){
      LocTx = textHelper.getLocalizedText();
      TxTx = textHelper.getTxText();
    }

    addLocalizedTextFields(LocTx,TxTx,iLocaleId);
  }

  private void addLocalizedTextFields(LocalizedText locText,TxText txText, int iLocaleId){
    boolean hasTxText = ( txText != null ) ? true: false;
    boolean hasLocalizedText = ( locText != null ) ? true: false;

    TextInput tiHeadline = new TextInput(prmHeadline);
    tiHeadline.setLength(40);
    tiHeadline.setMaxlength(255);
    List L = TextFinder.listOfLocales();
    DropdownMenu LocaleDrop = ICLocalePresentation.getLocaleDropdownIdKeyed(prmLocale);
    LocaleDrop.setToSubmit();
    LocaleDrop.setSelectedElement(Integer.toString(iLocaleId));

    TextArea taBody = new TextArea(prmBody,65,19);
    if ( hasLocalizedText ) {
      if ( locText.getHeadline() != null ) {
        tiHeadline.setContent(locText.getHeadline());
      }
      if ( locText.getBody() != null ) {
        taBody.setContent(locText.getBody());
      }
      addHiddenInput(new HiddenInput(prmLocalizedTextId,String.valueOf(locText.getID())));
    }

    if( hasTxText )
      addHiddenInput(new HiddenInput(prmTxTextId,Integer.toString(txText.getID())));

    SubmitButton save = new SubmitButton(iwrb.getImage("save.gif"),actSave);

    ImageInserter imageInsert = new ImageInserter();
    imageInsert.setImSessionImageName(prmImageId);
    imageInsert.setSelected(false);
      if ( update ) {
        if ( txText.getIncludeImage()) {
          imageInsert.setImageId(txText.getImageId());
          imageInsert.setSelected(true);
        }
      }

    addLeft(iwrb.getLocalizedString("title","Title"),tiHeadline,true);
    addLeft(iwrb.getLocalizedString("locale","Locale"), LocaleDrop,true);
    addLeft(iwrb.getLocalizedString("body","Text"),taBody,true);
    addRight(iwrb.getLocalizedString("image","Image"),imageInsert,true);
    addSubmitButton(save);
  }

  private void noAccess() throws IOException,SQLException {
    addLeft(iwrb.getLocalizedString("no_access","Login first!"));
    this.addSubmitButton(new CloseButton(iwrb.getLocalizedString("close","Closee")));
  }

  private void confirmDelete(ModuleInfo modinfo) throws IOException,SQLException {
    TextModule text = TextBusiness.getTextModule(modinfo);
    String textHeadline = text.getTextHeadline();

    if ( textHeadline != null ) {
      addLeft(iwrb.getLocalizedString("text_to_delete","Text to delete")+": "+textHeadline);
      addLeft(iwrb.getLocalizedString("confirm_delete","Are you sure?"));
      addHiddenInput(new HiddenInput("text_id",Integer.toString(text.getID())));
      addHiddenInput(new HiddenInput(prmAction,actDelete));
      addHiddenInput(new HiddenInput(prmMode,modeDelete));
      addSubmitButton(new SubmitButton(iwrb.getImage("delete.gif")));
    }
    else {
      addLeft(iwrb.getLocalizedString("not_exists","Text already deleted or not available."));
      addSubmitButton(new CloseButton(iwrb.getImage("close.gif")));
    }
  }

  private void saveText(ModuleInfo modinfo,String sTxTextId,String sLocalizedTextId){
    String sHeadline = modinfo.getParameter( prmHeadline );
    String sBody = modinfo.getParameter(prmBody );
    String sImageId = modinfo.getParameter(prmImageId);
    String sLocaleId = modinfo.getParameter(prmLocale);
    String sUseImage = modinfo.getParameter(prmUseImage);
    if(sHeadline != null || sBody != null){
      int iTxTextId = sTxTextId!=null?Integer.parseInt(sTxTextId): -1;
      int iLocalizedTextId = sLocalizedTextId != null ? Integer.parseInt(sLocalizedTextId): -1;
      int iLocaleId = sLocaleId != null ? Integer.parseInt(sLocaleId):-1;
      int iImageId = sImageId != null ? Integer.parseInt(sImageId):-1;
      boolean bUseImage = sUseImage!= null?true:false;
      TextBusiness.saveText(iTxTextId,iLocalizedTextId,sHeadline,"",sBody,iImageId,bUseImage,iLocaleId,iUserId);
    }
    setParentToReload();
    close();
  }

  private void deleteText(ModuleInfo modinfo,String sTxTextId,String sLocalizedTextId ) {
    TextBusiness.deleteText(modinfo);
    setParentToReload();
    close();
  }

  public void main(ModuleInfo modinfo) throws Exception {
    super.main(modinfo);
    isAdmin = AccessControl.hasEditPermission(new TextReader(),modinfo);
    User u= com.idega.block.login.business.LoginBusiness.getUser(modinfo);
    iUserId = u != null?u.getID():-1;
    isAdmin = true;
    iwb = getBundle(modinfo);
    iwrb = getResourceBundle(modinfo);
    addTitle(iwrb.getLocalizedString("text_editor","Text Editor"));
    control(modinfo);
  }

  public String getBundleIdentifier(){
    return IW_BUNDLE_IDENTIFIER;
  }
}