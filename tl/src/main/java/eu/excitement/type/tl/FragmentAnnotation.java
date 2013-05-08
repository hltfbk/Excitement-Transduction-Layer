

/* First created by JCasGen Tue Apr 16 12:56:12 CEST 2013 */
package eu.excitement.type.tl;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;


/** This type annotates a "fragment", as defined in EXCITEMENT WP6 (and WP2). This is the base type of two different fragments: AssumedFragment type and DeterminedFragment type.

Example.
"The connection was slow. I was on vacation. GPRS was specially slow."

-begin:0
-end:67

-text: The connection was slow. GPRS was specially slow.

-fragParts(0): FragmentParts -begin:0  -end:23
-fragParts(1): FragmentParts -begin:44 -end:67
 * Updated by JCasGen Tue Apr 16 12:56:16 CEST 2013
 * XML source: /home/tailblues/progs/tl_graphs/src/main/resources/desc/type/TLfragment.xml
 * @generated */
public class FragmentAnnotation extends Annotation {
  /** @generated
   * @ordered 
   */
  //@SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(FragmentAnnotation.class);
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
  protected FragmentAnnotation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public FragmentAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public FragmentAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public FragmentAnnotation(JCas jcas, int begin, int end) {
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
  //* Feature: text

  /** getter for text - gets this holds the text that this fragmentation represents.
   * @generated */
  public String getText() {
    if (FragmentAnnotation_Type.featOkTst && ((FragmentAnnotation_Type)jcasType).casFeat_text == null)
      jcasType.jcas.throwFeatMissing("text", "eu.excitement.type.tl.FragmentAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((FragmentAnnotation_Type)jcasType).casFeatCode_text);}
    
  /** setter for text - sets this holds the text that this fragmentation represents. 
   * @generated */
  public void setText(String v) {
    if (FragmentAnnotation_Type.featOkTst && ((FragmentAnnotation_Type)jcasType).casFeat_text == null)
      jcasType.jcas.throwFeatMissing("text", "eu.excitement.type.tl.FragmentAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((FragmentAnnotation_Type)jcasType).casFeatCode_text, v);}    
   
    
  //*--------------*
  //* Feature: fragParts

  /** getter for fragParts - gets this holds one or more FragmentsParts type in an array. Thus, it can actually map non-continous regions. If the fragmentation is continous, this array will only have one item.
   * @generated */
  public FSArray getFragParts() {
    if (FragmentAnnotation_Type.featOkTst && ((FragmentAnnotation_Type)jcasType).casFeat_fragParts == null)
      jcasType.jcas.throwFeatMissing("fragParts", "eu.excitement.type.tl.FragmentAnnotation");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((FragmentAnnotation_Type)jcasType).casFeatCode_fragParts)));}
    
  /** setter for fragParts - sets this holds one or more FragmentsParts type in an array. Thus, it can actually map non-continous regions. If the fragmentation is continous, this array will only have one item. 
   * @generated */
  public void setFragParts(FSArray v) {
    if (FragmentAnnotation_Type.featOkTst && ((FragmentAnnotation_Type)jcasType).casFeat_fragParts == null)
      jcasType.jcas.throwFeatMissing("fragParts", "eu.excitement.type.tl.FragmentAnnotation");
    jcasType.ll_cas.ll_setRefValue(addr, ((FragmentAnnotation_Type)jcasType).casFeatCode_fragParts, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for fragParts - gets an indexed value - this holds one or more FragmentsParts type in an array. Thus, it can actually map non-continous regions. If the fragmentation is continous, this array will only have one item.
   * @generated */
  public FragmentPart getFragParts(int i) {
    if (FragmentAnnotation_Type.featOkTst && ((FragmentAnnotation_Type)jcasType).casFeat_fragParts == null)
      jcasType.jcas.throwFeatMissing("fragParts", "eu.excitement.type.tl.FragmentAnnotation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((FragmentAnnotation_Type)jcasType).casFeatCode_fragParts), i);
    return (FragmentPart)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((FragmentAnnotation_Type)jcasType).casFeatCode_fragParts), i)));}

  /** indexed setter for fragParts - sets an indexed value - this holds one or more FragmentsParts type in an array. Thus, it can actually map non-continous regions. If the fragmentation is continous, this array will only have one item.
   * @generated */
  public void setFragParts(int i, FragmentPart v) { 
    if (FragmentAnnotation_Type.featOkTst && ((FragmentAnnotation_Type)jcasType).casFeat_fragParts == null)
      jcasType.jcas.throwFeatMissing("fragParts", "eu.excitement.type.tl.FragmentAnnotation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((FragmentAnnotation_Type)jcasType).casFeatCode_fragParts), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((FragmentAnnotation_Type)jcasType).casFeatCode_fragParts), i, jcasType.ll_cas.ll_getFSRef(v));}
  }

    