

/* First created by JCasGen Tue Apr 16 12:56:12 CEST 2013 */
package eu.excitement.type.tl;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.TOP;


/** This is the metadata used for output of use-case 2 category detection case. This type is used in CategoryAnnotation, as a element of an array.   
 * Updated by JCasGen Tue Apr 16 12:56:12 CEST 2013
 * XML source: /home/tailblues/progs/tl_graphs/src/main/resources/desc/type/TLcategory.xml
 * @generated */
public class CategoryDecision extends TOP {
  /** @generated
   * @ordered 
   */
  //@SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(CategoryDecision.class);
  /** @generated
   * @ordered 
   */
  //@SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected CategoryDecision() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public CategoryDecision(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public CategoryDecision(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: categoryId

  /** getter for categoryId - gets Category ID
   * @generated */
  public String getCategoryId() {
    if (CategoryDecision_Type.featOkTst && ((CategoryDecision_Type)jcasType).casFeat_categoryId == null)
      jcasType.jcas.throwFeatMissing("categoryId", "eu.excitement.type.tl.CategoryDecision");
    return jcasType.ll_cas.ll_getStringValue(addr, ((CategoryDecision_Type)jcasType).casFeatCode_categoryId);}
    
  /** setter for categoryId - sets Category ID 
   * @generated */
  public void setCategoryId(String v) {
    if (CategoryDecision_Type.featOkTst && ((CategoryDecision_Type)jcasType).casFeat_categoryId == null)
      jcasType.jcas.throwFeatMissing("categoryId", "eu.excitement.type.tl.CategoryDecision");
    jcasType.ll_cas.ll_setStringValue(addr, ((CategoryDecision_Type)jcasType).casFeatCode_categoryId, v);}    
   
    
  //*--------------*
  //* Feature: confidence

  /** getter for confidence - gets Confidence of this category
   * @generated */
  public double getConfidence() {
    if (CategoryDecision_Type.featOkTst && ((CategoryDecision_Type)jcasType).casFeat_confidence == null)
      jcasType.jcas.throwFeatMissing("confidence", "eu.excitement.type.tl.CategoryDecision");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((CategoryDecision_Type)jcasType).casFeatCode_confidence);}
    
  /** setter for confidence - sets Confidence of this category 
   * @generated */
  public void setConfidence(double v) {
    if (CategoryDecision_Type.featOkTst && ((CategoryDecision_Type)jcasType).casFeat_confidence == null)
      jcasType.jcas.throwFeatMissing("confidence", "eu.excitement.type.tl.CategoryDecision");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((CategoryDecision_Type)jcasType).casFeatCode_confidence, v);}    
  }

    