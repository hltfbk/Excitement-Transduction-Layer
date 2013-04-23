

/* First created by JCasGen Tue Apr 16 12:56:22 CEST 2013 */
package eu.excitementproject.tl.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** This is a type that is designed to represent one of non-continous region of a modifier. Only used for that purpose, and does not have any additional feature.
 * Updated by JCasGen Tue Apr 16 12:56:22 CEST 2013
 * XML source: /home/tailblues/progs/tl_graphs/src/main/resources/desc/type/TLmodifier.xml
 * @generated */
public class ModifierPart extends Annotation {
  /** @generated
   * @ordered 
   */
  //@SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(ModifierPart.class);
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
  protected ModifierPart() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public ModifierPart(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public ModifierPart(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public ModifierPart(JCas jcas, int begin, int end) {
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

    