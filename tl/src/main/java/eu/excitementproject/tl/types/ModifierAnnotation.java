

/* First created by JCasGen Tue Apr 16 12:56:22 CEST 2013 */
package eu.excitementproject.tl.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;


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
 * XML source: /home/tailblues/progs/tl_graphs/src/main/resources/desc/type/TLmodifier.xml
 * @generated */
public class ModifierAnnotation extends Annotation {
  /** @generated
   * @ordered 
   */
  //@SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(ModifierAnnotation.class);
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
  protected ModifierAnnotation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public ModifierAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public ModifierAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public ModifierAnnotation(JCas jcas, int begin, int end) {
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
     
 
    
  //*--------------*
  //* Feature: modifierParts

  /** getter for modifierParts - gets this holds one or more ModifierPart type in an array. Thus, it can actually map non-continous regions. If the modifier is continous, this array will only have one item.
   * @generated */
  public FSArray getModifierParts() {
    if (ModifierAnnotation_Type.featOkTst && ((ModifierAnnotation_Type)jcasType).casFeat_modifierParts == null)
      jcasType.jcas.throwFeatMissing("modifierParts", "eu.excitement.type.tl.ModifierAnnotation");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((ModifierAnnotation_Type)jcasType).casFeatCode_modifierParts)));}
    
  /** setter for modifierParts - sets this holds one or more ModifierPart type in an array. Thus, it can actually map non-continous regions. If the modifier is continous, this array will only have one item. 
   * @generated */
  public void setModifierParts(FSArray v) {
    if (ModifierAnnotation_Type.featOkTst && ((ModifierAnnotation_Type)jcasType).casFeat_modifierParts == null)
      jcasType.jcas.throwFeatMissing("modifierParts", "eu.excitement.type.tl.ModifierAnnotation");
    jcasType.ll_cas.ll_setRefValue(addr, ((ModifierAnnotation_Type)jcasType).casFeatCode_modifierParts, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for modifierParts - gets an indexed value - this holds one or more ModifierPart type in an array. Thus, it can actually map non-continous regions. If the modifier is continous, this array will only have one item.
   * @generated */
  public ModifierPart getModifierParts(int i) {
    if (ModifierAnnotation_Type.featOkTst && ((ModifierAnnotation_Type)jcasType).casFeat_modifierParts == null)
      jcasType.jcas.throwFeatMissing("modifierParts", "eu.excitement.type.tl.ModifierAnnotation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((ModifierAnnotation_Type)jcasType).casFeatCode_modifierParts), i);
    return (ModifierPart)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((ModifierAnnotation_Type)jcasType).casFeatCode_modifierParts), i)));}

  /** indexed setter for modifierParts - sets an indexed value - this holds one or more ModifierPart type in an array. Thus, it can actually map non-continous regions. If the modifier is continous, this array will only have one item.
   * @generated */
  public void setModifierParts(int i, ModifierPart v) { 
    if (ModifierAnnotation_Type.featOkTst && ((ModifierAnnotation_Type)jcasType).casFeat_modifierParts == null)
      jcasType.jcas.throwFeatMissing("modifierParts", "eu.excitement.type.tl.ModifierAnnotation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((ModifierAnnotation_Type)jcasType).casFeatCode_modifierParts), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((ModifierAnnotation_Type)jcasType).casFeatCode_modifierParts), i, jcasType.ll_cas.ll_getFSRef(v));}
   
    
  //*--------------*
  //* Feature: dependsOn

  /** getter for dependsOn - gets If this modifier depends on some other modifier, this feature points that modifier. This modifier dependsOn the pointed modifier. (Thus, if that modifier pointed by this feature does not exist, this modifier is not grammatical / meaningless.).
   * @generated */
  public ModifierAnnotation getDependsOn() {
    if (ModifierAnnotation_Type.featOkTst && ((ModifierAnnotation_Type)jcasType).casFeat_dependsOn == null)
      jcasType.jcas.throwFeatMissing("dependsOn", "eu.excitement.type.tl.ModifierAnnotation");
    return (ModifierAnnotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((ModifierAnnotation_Type)jcasType).casFeatCode_dependsOn)));}
    
  /** setter for dependsOn - sets If this modifier depends on some other modifier, this feature points that modifier. This modifier dependsOn the pointed modifier. (Thus, if that modifier pointed by this feature does not exist, this modifier is not grammatical / meaningless.). 
   * @generated */
  public void setDependsOn(ModifierAnnotation v) {
    if (ModifierAnnotation_Type.featOkTst && ((ModifierAnnotation_Type)jcasType).casFeat_dependsOn == null)
      jcasType.jcas.throwFeatMissing("dependsOn", "eu.excitement.type.tl.ModifierAnnotation");
    jcasType.ll_cas.ll_setRefValue(addr, ((ModifierAnnotation_Type)jcasType).casFeatCode_dependsOn, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    