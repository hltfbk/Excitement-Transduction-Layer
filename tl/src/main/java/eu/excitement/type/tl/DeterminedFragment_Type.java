
/* First created by JCasGen Tue Apr 16 12:56:12 CEST 2013 */
package eu.excitement.type.tl;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;

/** This is a fragmentation annotation that is "determined" by WP6 internal modules. Unlike "assumed fragment", this is actual fragment that will be treated as the real fragment.
 * Updated by JCasGen Tue Apr 16 12:56:16 CEST 2013
 * @generated */
public class DeterminedFragment_Type extends FragmentAnnotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (DeterminedFragment_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = DeterminedFragment_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new DeterminedFragment(addr, DeterminedFragment_Type.this);
  			   DeterminedFragment_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new DeterminedFragment(addr, DeterminedFragment_Type.this);
  	  }
    };
  /** @generated */
  //@SuppressWarnings ("hiding")
  public final static int typeIndexID = DeterminedFragment.typeIndexID;
  /** @generated 
     @modifiable */
  //@SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("eu.excitement.type.tl.DeterminedFragment");



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public DeterminedFragment_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    