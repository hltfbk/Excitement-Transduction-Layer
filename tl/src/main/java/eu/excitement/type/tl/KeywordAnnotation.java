

/* First created by JCasGen Tue Aug 20 13:08:09 CEST 2013 */
package eu.excitement.type.tl;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** The annotation marks a keyword (or a keyphrase, no restriction on the number of terms in it), that is relevant for the  application side (TL layer user).
The annotation will be used in the TL library to extract relevant fragments. 

It does not have any specific features, and annotation itself (begin - end) is meaningful. 
 * Updated by JCasGen Tue Aug 20 13:08:09 CEST 2013
 * XML source: /home/tailblues/progs/Excitement-Transduction-Layer/tl/src/main/resources/desc/type/TLkeyword.xml
 * @generated */
public class KeywordAnnotation extends Annotation {
  /** @generated
   * @ordered 
   */
  //@SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(KeywordAnnotation.class);
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
  protected KeywordAnnotation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public KeywordAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public KeywordAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public KeywordAnnotation(JCas jcas, int begin, int end) {
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
     
}

    