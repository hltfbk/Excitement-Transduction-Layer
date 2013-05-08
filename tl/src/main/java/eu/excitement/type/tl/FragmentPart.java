

/* First created by JCasGen Tue Apr 16 12:56:12 CEST 2013 */
package eu.excitement.type.tl;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** This is a type that is designed to represent one of non-continous region of a fragment. Only used for that purpose, and does not have any additional feature.
 * Updated by JCasGen Tue Apr 16 12:56:17 CEST 2013
 * XML source: /home/tailblues/progs/tl_graphs/src/main/resources/desc/type/TLfragment.xml
 * @generated */
public class FragmentPart extends Annotation {
  /** @generated
   * @ordered 
   */
  //@SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(FragmentPart.class);
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
  protected FragmentPart() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public FragmentPart(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public FragmentPart(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public FragmentPart(JCas jcas, int begin, int end) {
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

    