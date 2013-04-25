

/* First created by JCasGen Tue Apr 16 12:56:12 CEST 2013 */
package eu.excitementproject.tl.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** This is a fragmentation annotation that is "determined" by WP6 internal modules. Unlike "assumed fragment", this is actual fragment that will be treated as the real fragment.
 * Updated by JCasGen Tue Apr 16 12:56:16 CEST 2013
 * XML source: /home/tailblues/progs/tl_graphs/src/main/resources/desc/type/TLfragment.xml
 * @generated */
public class DeterminedFragment extends FragmentAnnotation {
  /** @generated
   * @ordered 
   */
  //@SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(DeterminedFragment.class);
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
  protected DeterminedFragment() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public DeterminedFragment(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public DeterminedFragment(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public DeterminedFragment(JCas jcas, int begin, int end) {
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

    