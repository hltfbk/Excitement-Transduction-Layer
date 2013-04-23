
/* First created by JCasGen Tue Apr 16 12:56:12 CEST 2013 */
package eu.excitementproject.tl.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** This type annotates a "fragment", as defined in EXCITEMENT WP6 (and WP2). This is the base type of two different fragments: AssumedFragment type and DeterminedFragment type.

Example.
"The connection was slow. I was on vacation. GPRS was specially slow."

-begin:0
-end:67

-text: The connection was slow. GPRS was specially slow.

-fragParts(0): FragmentParts -begin:0  -end:23
-fragParts(1): FragmentParts -begin:44 -end:67
 * Updated by JCasGen Tue Apr 16 12:56:17 CEST 2013
 * @generated */
public class FragmentAnnotation_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (FragmentAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = FragmentAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new FragmentAnnotation(addr, FragmentAnnotation_Type.this);
  			   FragmentAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new FragmentAnnotation(addr, FragmentAnnotation_Type.this);
  	  }
    };
  /** @generated */
  //@SuppressWarnings ("hiding")
  public final static int typeIndexID = FragmentAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  //@SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("eu.excitement.type.tl.FragmentAnnotation");
 
  /** @generated */
  final Feature casFeat_text;
  /** @generated */
  final int     casFeatCode_text;
  /** @generated */ 
  public String getText(int addr) {
        if (featOkTst && casFeat_text == null)
      jcas.throwFeatMissing("text", "eu.excitement.type.tl.FragmentAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_text);
  }
  /** @generated */    
  public void setText(int addr, String v) {
        if (featOkTst && casFeat_text == null)
      jcas.throwFeatMissing("text", "eu.excitement.type.tl.FragmentAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_text, v);}
    
  
 
  /** @generated */
  final Feature casFeat_fragParts;
  /** @generated */
  final int     casFeatCode_fragParts;
  /** @generated */ 
  public int getFragParts(int addr) {
        if (featOkTst && casFeat_fragParts == null)
      jcas.throwFeatMissing("fragParts", "eu.excitement.type.tl.FragmentAnnotation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_fragParts);
  }
  /** @generated */    
  public void setFragParts(int addr, int v) {
        if (featOkTst && casFeat_fragParts == null)
      jcas.throwFeatMissing("fragParts", "eu.excitement.type.tl.FragmentAnnotation");
    ll_cas.ll_setRefValue(addr, casFeatCode_fragParts, v);}
    
   /** @generated */
  public int getFragParts(int addr, int i) {
        if (featOkTst && casFeat_fragParts == null)
      jcas.throwFeatMissing("fragParts", "eu.excitement.type.tl.FragmentAnnotation");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_fragParts), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_fragParts), i);
  return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_fragParts), i);
  }
   
  /** @generated */ 
  public void setFragParts(int addr, int i, int v) {
        if (featOkTst && casFeat_fragParts == null)
      jcas.throwFeatMissing("fragParts", "eu.excitement.type.tl.FragmentAnnotation");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_fragParts), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_fragParts), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_fragParts), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public FragmentAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_text = jcas.getRequiredFeatureDE(casType, "text", "uima.cas.String", featOkTst);
    casFeatCode_text  = (null == casFeat_text) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_text).getCode();

 
    casFeat_fragParts = jcas.getRequiredFeatureDE(casType, "fragParts", "uima.cas.FSArray", featOkTst);
    casFeatCode_fragParts  = (null == casFeat_fragParts) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_fragParts).getCode();

  }
}



    