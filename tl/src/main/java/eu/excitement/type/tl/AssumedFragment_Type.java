
/* First created by JCasGen Tue Apr 16 12:56:12 CEST 2013 */
package  eu.excitement.type.tl;

import org.apache.uima.jcas.JCas;

import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;

/** This is a fragmentation annotation that is used to note the "assumed" fragment. WP7 application layer will use this annotation to mark what WP7 application thinks possible fragment. This might be not really accurate, and WP6 may / may not trust this annotation.
 * Updated by JCasGen Tue Apr 16 12:56:16 CEST 2013
 * @generated */
public class AssumedFragment_Type extends FragmentAnnotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (AssumedFragment_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = AssumedFragment_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new AssumedFragment(addr, AssumedFragment_Type.this);
  			   AssumedFragment_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new AssumedFragment(addr, AssumedFragment_Type.this);
  	  }
    };
  /** @generated */
//  @SuppressWarnings ("hiding")
  public final static int typeIndexID = AssumedFragment.typeIndexID;
  /** @generated 
     @modifiable */
 // @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("eu.excitement.type.tl.AssumedFragment");



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public AssumedFragment_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    