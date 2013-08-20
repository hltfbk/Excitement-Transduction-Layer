
/* First created by JCasGen Tue Aug 20 13:08:09 CEST 2013 */
package eu.excitement.type.tl;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** The annotation marks a keyword (or a keyphrase, no restriction on the number of terms in it), that is relevant for the  application side (TL layer user).
The annotation will be used in the TL library to extract relevant fragments. 

It does not have any specific features, and annotation itself (begin - end) is meaningful. 
 * Updated by JCasGen Tue Aug 20 13:08:09 CEST 2013
 * @generated */
public class KeywordAnnotation_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (KeywordAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = KeywordAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new KeywordAnnotation(addr, KeywordAnnotation_Type.this);
  			   KeywordAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new KeywordAnnotation(addr, KeywordAnnotation_Type.this);
  	  }
    };
  /** @generated */
  //@SuppressWarnings ("hiding")
  public final static int typeIndexID = KeywordAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  //@SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("eu.excitement.type.tl.KeywordAnnotation");



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public KeywordAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    