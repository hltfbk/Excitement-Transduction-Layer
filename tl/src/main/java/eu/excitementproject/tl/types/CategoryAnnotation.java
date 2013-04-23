

/* First created by JCasGen Tue Apr 16 12:56:12 CEST 2013 */
package eu.excitementproject.tl.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSArray;


/** This is a type design to represent the result of usecase-2 processing. It represents a fragment, but also with the data associated for category decision. The fragment annotated by this type has one or more category decision type, which annotates category id and confidence for that category.
 * Updated by JCasGen Tue Apr 16 12:56:12 CEST 2013
 * XML source: /home/tailblues/progs/tl_graphs/src/main/resources/desc/type/TLcategory.xml
 * @generated */
public class CategoryAnnotation extends FragmentAnnotation {
  /** @generated
   * @ordered 
   */
  //@SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(CategoryAnnotation.class);
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
  protected CategoryAnnotation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public CategoryAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public CategoryAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public CategoryAnnotation(JCas jcas, int begin, int end) {
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
  //* Feature: categories

  /** getter for categories - gets at least one or more category decision data associated with this fragment.
   * @generated */
  public FSArray getCategories() {
    if (CategoryAnnotation_Type.featOkTst && ((CategoryAnnotation_Type)jcasType).casFeat_categories == null)
      jcasType.jcas.throwFeatMissing("categories", "eu.excitement.type.tl.CategoryAnnotation");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((CategoryAnnotation_Type)jcasType).casFeatCode_categories)));}
    
  /** setter for categories - sets at least one or more category decision data associated with this fragment. 
   * @generated */
  public void setCategories(FSArray v) {
    if (CategoryAnnotation_Type.featOkTst && ((CategoryAnnotation_Type)jcasType).casFeat_categories == null)
      jcasType.jcas.throwFeatMissing("categories", "eu.excitement.type.tl.CategoryAnnotation");
    jcasType.ll_cas.ll_setRefValue(addr, ((CategoryAnnotation_Type)jcasType).casFeatCode_categories, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for categories - gets an indexed value - at least one or more category decision data associated with this fragment.
   * @generated */
  public CategoryDecision getCategories(int i) {
    if (CategoryAnnotation_Type.featOkTst && ((CategoryAnnotation_Type)jcasType).casFeat_categories == null)
      jcasType.jcas.throwFeatMissing("categories", "eu.excitement.type.tl.CategoryAnnotation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((CategoryAnnotation_Type)jcasType).casFeatCode_categories), i);
    return (CategoryDecision)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((CategoryAnnotation_Type)jcasType).casFeatCode_categories), i)));}

  /** indexed setter for categories - sets an indexed value - at least one or more category decision data associated with this fragment.
   * @generated */
  public void setCategories(int i, CategoryDecision v) { 
    if (CategoryAnnotation_Type.featOkTst && ((CategoryAnnotation_Type)jcasType).casFeat_categories == null)
      jcasType.jcas.throwFeatMissing("categories", "eu.excitement.type.tl.CategoryAnnotation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((CategoryAnnotation_Type)jcasType).casFeatCode_categories), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((CategoryAnnotation_Type)jcasType).casFeatCode_categories), i, jcasType.ll_cas.ll_getFSRef(v));}
  }

    