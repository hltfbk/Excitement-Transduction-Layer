

/* First created by JCasGen Tue Apr 16 12:56:12 CEST 2013 */
package eu.excitementproject.tl.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** This is a fragmentation annotation that is used to note the "assumed" fragment. WP7 application layer will use this annotation to mark what WP7 application thinks possible fragment. This might be not really accurate, and WP6 may / may not trust this annotation.
 * Updated by JCasGen Tue Apr 16 12:56:16 CEST 2013
 * XML source: /home/tailblues/progs/tl_graphs/src/main/resources/desc/type/TLfragment.xml
 * @generated */
public class AssumedFragment extends FragmentAnnotation {
  /** @generated
   * @ordered 
   */
//  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(AssumedFragment.class);
  /** @generated
   * @ordered 
   */
 // @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected AssumedFragment() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public AssumedFragment(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public AssumedFragment(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public AssumedFragment(JCas jcas, int begin, int end) {
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

    