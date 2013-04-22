
/* First created by JCasGen Tue Apr 16 12:56:22 CEST 2013 */
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

/** This annotation type simply annotates a region as a "modifier", and usage of this type will WP6 modules to create various fragment graph nodes by removing those modifiers. While this could be simple, it gets a bit complicated by "dependsOn" and "non-continous" regions.

See the following example:
"Seats are uncomfortable as too old."

Here we have two modifiers.

ModifierAnnotation #1 "too"
-begin: 27
-end: 29
-modifierParts: (0) -begin:27 -end:29
-dependsOn: ModifierAnnotation #2

ModifierAnnotation #2 "as ... old"
-begin: 24
-end: 34
-modifierParts(0) -begin:24 -end:25
-modifierParts(1) -begin:31 -end:33
-dependsOn: (null)

The above example shows two modifiers that one ("too") depends on the other ("as old"). If "as old" is removed, the modifier "too" is not meaningful. Thus, removing only modifier #2, is not possible. This is marked in #1 that it depends on #2. (#1 is not a valid modifier, and removed too, if #2 is removed).
 * Updated by JCasGen Tue Apr 16 12:56:22 CEST 2013
 * @generated */
public class ModifierAnnotation_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (ModifierAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = ModifierAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new ModifierAnnotation(addr, ModifierAnnotation_Type.this);
  			   ModifierAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new ModifierAnnotation(addr, ModifierAnnotation_Type.this);
  	  }
    };
  /** @generated */
  //@SuppressWarnings ("hiding")
  public final static int typeIndexID = ModifierAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  //@SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("eu.excitement.type.tl.ModifierAnnotation");
 
  /** @generated */
  final Feature casFeat_modifierParts;
  /** @generated */
  final int     casFeatCode_modifierParts;
  /** @generated */ 
  public int getModifierParts(int addr) {
        if (featOkTst && casFeat_modifierParts == null)
      jcas.throwFeatMissing("modifierParts", "eu.excitement.type.tl.ModifierAnnotation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_modifierParts);
  }
  /** @generated */    
  public void setModifierParts(int addr, int v) {
        if (featOkTst && casFeat_modifierParts == null)
      jcas.throwFeatMissing("modifierParts", "eu.excitement.type.tl.ModifierAnnotation");
    ll_cas.ll_setRefValue(addr, casFeatCode_modifierParts, v);}
    
   /** @generated */
  public int getModifierParts(int addr, int i) {
        if (featOkTst && casFeat_modifierParts == null)
      jcas.throwFeatMissing("modifierParts", "eu.excitement.type.tl.ModifierAnnotation");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_modifierParts), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_modifierParts), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_modifierParts), i);
  }
   
  /** @generated */ 
  public void setModifierParts(int addr, int i, int v) {
        if (featOkTst && casFeat_modifierParts == null)
      jcas.throwFeatMissing("modifierParts", "eu.excitement.type.tl.ModifierAnnotation");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_modifierParts), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_modifierParts), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_modifierParts), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_dependsOn;
  /** @generated */
  final int     casFeatCode_dependsOn;
  /** @generated */ 
  public int getDependsOn(int addr) {
        if (featOkTst && casFeat_dependsOn == null)
      jcas.throwFeatMissing("dependsOn", "eu.excitement.type.tl.ModifierAnnotation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_dependsOn);
  }
  /** @generated */    
  public void setDependsOn(int addr, int v) {
        if (featOkTst && casFeat_dependsOn == null)
      jcas.throwFeatMissing("dependsOn", "eu.excitement.type.tl.ModifierAnnotation");
    ll_cas.ll_setRefValue(addr, casFeatCode_dependsOn, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public ModifierAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_modifierParts = jcas.getRequiredFeatureDE(casType, "modifierParts", "uima.cas.FSArray", featOkTst);
    casFeatCode_modifierParts  = (null == casFeat_modifierParts) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_modifierParts).getCode();

 
    casFeat_dependsOn = jcas.getRequiredFeatureDE(casType, "dependsOn", "eu.excitement.type.tl.ModifierAnnotation", featOkTst);
    casFeatCode_dependsOn  = (null == casFeat_dependsOn) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_dependsOn).getCode();

  }
}



    