

/* First created by JCasGen Wed Jun 12 11:06:43 CEST 2013 */
package eu.excitement.type.tl;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** This type description file defines InputCasMetadata type, which records various metadata related to the Interaction and the input CAS. 
Note that one CAS should have only one metadata (only the first one should be considered, if more than one), and each CAS should have one metadata, even if all of its fields are null. 

Note that language ID is not recorded in this metadata type. It is directly recorded in CAS. Also note that all of the metadata are simply strings, and can be null if that metadata is missing. 

The type  includes: (all strings) 
- interactionId 
- channel 
- provider 
- date (string as YYYY-MM-DD)  
- businessScenario 
- author
 * Updated by JCasGen Wed Jun 12 11:06:43 CEST 2013
 * XML source: /home/tailblues/progs/Excitement-Transduction-Layer/tl/src/main/resources/desc/type/TLMetadata.xml
 * @generated */
public class Metadata extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Metadata.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Metadata() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Metadata(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Metadata(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Metadata(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: interactionId

  /** getter for interactionId - gets interaction ID of the interaction, contained in the CAS.
   * @generated */
  public String getInteractionId() {
    if (Metadata_Type.featOkTst && ((Metadata_Type)jcasType).casFeat_interactionId == null)
      jcasType.jcas.throwFeatMissing("interactionId", "eu.excitement.type.tl.Metadata");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Metadata_Type)jcasType).casFeatCode_interactionId);}
    
  /** setter for interactionId - sets interaction ID of the interaction, contained in the CAS. 
   * @generated */
  public void setInteractionId(String v) {
    if (Metadata_Type.featOkTst && ((Metadata_Type)jcasType).casFeat_interactionId == null)
      jcasType.jcas.throwFeatMissing("interactionId", "eu.excitement.type.tl.Metadata");
    jcasType.ll_cas.ll_setStringValue(addr, ((Metadata_Type)jcasType).casFeatCode_interactionId, v);}    
   
    
  //*--------------*
  //* Feature: channel

  /** getter for channel - gets channel of the interaction (e.g. e-mail, web forum, speech, telephone transcript, etc)
   * @generated */
  public String getChannel() {
    if (Metadata_Type.featOkTst && ((Metadata_Type)jcasType).casFeat_channel == null)
      jcasType.jcas.throwFeatMissing("channel", "eu.excitement.type.tl.Metadata");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Metadata_Type)jcasType).casFeatCode_channel);}
    
  /** setter for channel - sets channel of the interaction (e.g. e-mail, web forum, speech, telephone transcript, etc) 
   * @generated */
  public void setChannel(String v) {
    if (Metadata_Type.featOkTst && ((Metadata_Type)jcasType).casFeat_channel == null)
      jcasType.jcas.throwFeatMissing("channel", "eu.excitement.type.tl.Metadata");
    jcasType.ll_cas.ll_setStringValue(addr, ((Metadata_Type)jcasType).casFeatCode_channel, v);}    
   
    
  //*--------------*
  //* Feature: provider

  /** getter for provider - gets This value holds the provider as string (ALMA, OMQ or NICE, etc)
   * @generated */
  public String getProvider() {
    if (Metadata_Type.featOkTst && ((Metadata_Type)jcasType).casFeat_provider == null)
      jcasType.jcas.throwFeatMissing("provider", "eu.excitement.type.tl.Metadata");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Metadata_Type)jcasType).casFeatCode_provider);}
    
  /** setter for provider - sets This value holds the provider as string (ALMA, OMQ or NICE, etc) 
   * @generated */
  public void setProvider(String v) {
    if (Metadata_Type.featOkTst && ((Metadata_Type)jcasType).casFeat_provider == null)
      jcasType.jcas.throwFeatMissing("provider", "eu.excitement.type.tl.Metadata");
    jcasType.ll_cas.ll_setStringValue(addr, ((Metadata_Type)jcasType).casFeatCode_provider, v);}    
   
    
  //*--------------*
  //* Feature: date

  /** getter for date - gets The date of the interaction: as ISO format of Year-Month-Day (YYYY-MM-DD)
   * @generated */
  public String getDate() {
    if (Metadata_Type.featOkTst && ((Metadata_Type)jcasType).casFeat_date == null)
      jcasType.jcas.throwFeatMissing("date", "eu.excitement.type.tl.Metadata");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Metadata_Type)jcasType).casFeatCode_date);}
    
  /** setter for date - sets The date of the interaction: as ISO format of Year-Month-Day (YYYY-MM-DD) 
   * @generated */
  public void setDate(String v) {
    if (Metadata_Type.featOkTst && ((Metadata_Type)jcasType).casFeat_date == null)
      jcasType.jcas.throwFeatMissing("date", "eu.excitement.type.tl.Metadata");
    jcasType.ll_cas.ll_setStringValue(addr, ((Metadata_Type)jcasType).casFeatCode_date, v);}    
   
    
  //*--------------*
  //* Feature: businessScenario

  /** getter for businessScenario - gets This string holds the business scenario of the interaction (like coffeeshop, internet shopping, train claims, etc)
   * @generated */
  public String getBusinessScenario() {
    if (Metadata_Type.featOkTst && ((Metadata_Type)jcasType).casFeat_businessScenario == null)
      jcasType.jcas.throwFeatMissing("businessScenario", "eu.excitement.type.tl.Metadata");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Metadata_Type)jcasType).casFeatCode_businessScenario);}
    
  /** setter for businessScenario - sets This string holds the business scenario of the interaction (like coffeeshop, internet shopping, train claims, etc) 
   * @generated */
  public void setBusinessScenario(String v) {
    if (Metadata_Type.featOkTst && ((Metadata_Type)jcasType).casFeat_businessScenario == null)
      jcasType.jcas.throwFeatMissing("businessScenario", "eu.excitement.type.tl.Metadata");
    jcasType.ll_cas.ll_setStringValue(addr, ((Metadata_Type)jcasType).casFeatCode_businessScenario, v);}    
   
    
  //*--------------*
  //* Feature: author

  /** getter for author - gets this field holds the name of the author, if it is applicable (e.g. web forums)
   * @generated */
  public String getAuthor() {
    if (Metadata_Type.featOkTst && ((Metadata_Type)jcasType).casFeat_author == null)
      jcasType.jcas.throwFeatMissing("author", "eu.excitement.type.tl.Metadata");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Metadata_Type)jcasType).casFeatCode_author);}
    
  /** setter for author - sets this field holds the name of the author, if it is applicable (e.g. web forums) 
   * @generated */
  public void setAuthor(String v) {
    if (Metadata_Type.featOkTst && ((Metadata_Type)jcasType).casFeat_author == null)
      jcasType.jcas.throwFeatMissing("author", "eu.excitement.type.tl.Metadata");
    jcasType.ll_cas.ll_setStringValue(addr, ((Metadata_Type)jcasType).casFeatCode_author, v);}    
  }

    