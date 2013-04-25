
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
import org.apache.uima.jcas.cas.TOP_Type;

/** This is the metadata used for output of use-case 2 category detection case. This type is used in CategoryAnnotation, as a element of an array.   
 * Updated by JCasGen Tue Apr 16 12:56:12 CEST 2013
 * @generated */
public class CategoryDecision_Type extends TOP_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (CategoryDecision_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = CategoryDecision_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new CategoryDecision(addr, CategoryDecision_Type.this);
  			   CategoryDecision_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new CategoryDecision(addr, CategoryDecision_Type.this);
  	  }
    };
  /** @generated */
  //@SuppressWarnings ("hiding")
  public final static int typeIndexID = CategoryDecision.typeIndexID;
  /** @generated 
     @modifiable */
  //@SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("eu.excitement.type.tl.CategoryDecision");
 
  /** @generated */
  final Feature casFeat_categoryId;
  /** @generated */
  final int     casFeatCode_categoryId;
  /** @generated */ 
  public String getCategoryId(int addr) {
        if (featOkTst && casFeat_categoryId == null)
      jcas.throwFeatMissing("categoryId", "eu.excitement.type.tl.CategoryDecision");
    return ll_cas.ll_getStringValue(addr, casFeatCode_categoryId);
  }
  /** @generated */    
  public void setCategoryId(int addr, String v) {
        if (featOkTst && casFeat_categoryId == null)
      jcas.throwFeatMissing("categoryId", "eu.excitement.type.tl.CategoryDecision");
    ll_cas.ll_setStringValue(addr, casFeatCode_categoryId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_confidence;
  /** @generated */
  final int     casFeatCode_confidence;
  /** @generated */ 
  public double getConfidence(int addr) {
        if (featOkTst && casFeat_confidence == null)
      jcas.throwFeatMissing("confidence", "eu.excitement.type.tl.CategoryDecision");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_confidence);
  }
  /** @generated */    
  public void setConfidence(int addr, double v) {
        if (featOkTst && casFeat_confidence == null)
      jcas.throwFeatMissing("confidence", "eu.excitement.type.tl.CategoryDecision");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_confidence, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public CategoryDecision_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_categoryId = jcas.getRequiredFeatureDE(casType, "categoryId", "uima.cas.String", featOkTst);
    casFeatCode_categoryId  = (null == casFeat_categoryId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_categoryId).getCode();

 
    casFeat_confidence = jcas.getRequiredFeatureDE(casType, "confidence", "uima.cas.Double", featOkTst);
    casFeatCode_confidence  = (null == casFeat_confidence) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_confidence).getCode();

  }
}



    