

/* First created by JCasGen Wed Aug 29 14:17:38 CEST 2012 */
package eu.excitement.type.temporal;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** This type represents a temporal expression of type Duration. The expression describes a duration with explicit durations: 2 months, 48 hours, all last night, 20 days, etc.
 * Updated by JCasGen Wed Aug 29 14:17:38 CEST 2012
 * XML source: /home/tailblues/Dropbox/excitement_expr/ExcitementTypes/typesystem/desc/type/TemporalExpression.xml
 * @generated */
public class Duration extends TemporalExpression {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Duration.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Duration() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Duration(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Duration(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Duration(JCas jcas, int begin, int end) {
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

    