
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

/** This is a type design to represent the result of usecase-2 processing. It represents a fragment, but also with the data associated for category decision. The fragment annotated by this type has one or more category decision type, which annotates category id and confidence for that category.
 * Updated by JCasGen Tue Apr 16 12:56:12 CEST 2013
 * @generated */
public class CategoryAnnotation_Type extends FragmentAnnotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (CategoryAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = CategoryAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new CategoryAnnotation(addr, CategoryAnnotation_Type.this);
  			   CategoryAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new CategoryAnnotation(addr, CategoryAnnotation_Type.this);
  	  }
    };
  /** @generated */
  //@SuppressWarnings ("hiding")
  public final static int typeIndexID = CategoryAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
//  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("eu.excitement.type.tl.CategoryAnnotation");
 
  /** @generated */
  final Feature casFeat_categories;
  /** @generated */
  final int     casFeatCode_categories;
  /** @generated */ 
  public int getCategories(int addr) {
        if (featOkTst && casFeat_categories == null)
      jcas.throwFeatMissing("categories", "eu.excitement.type.tl.CategoryAnnotation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_categories);
  }
  /** @generated */    
  public void setCategories(int addr, int v) {
        if (featOkTst && casFeat_categories == null)
      jcas.throwFeatMissing("categories", "eu.excitement.type.tl.CategoryAnnotation");
    ll_cas.ll_setRefValue(addr, casFeatCode_categories, v);}
    
   /** @generated */
  public int getCategories(int addr, int i) {
        if (featOkTst && casFeat_categories == null)
      jcas.throwFeatMissing("categories", "eu.excitement.type.tl.CategoryAnnotation");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_categories), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_categories), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_categories), i);
  }
   
  /** @generated */ 
  public void setCategories(int addr, int i, int v) {
        if (featOkTst && casFeat_categories == null)
      jcas.throwFeatMissing("categories", "eu.excitement.type.tl.CategoryAnnotation");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_categories), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_categories), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_categories), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public CategoryAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_categories = jcas.getRequiredFeatureDE(casType, "categories", "uima.cas.FSArray", featOkTst);
    casFeatCode_categories  = (null == casFeat_categories) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_categories).getCode();

  }
}



    