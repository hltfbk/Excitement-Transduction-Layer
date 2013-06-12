
/* First created by JCasGen Wed Jun 12 11:06:43 CEST 2013 */
package eu.excitement.type.tl;

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

/** This type description file defines InputCasMetadata type, which records various metadata related to the Interaction and the input CAS. 
Note that one CAS should have only one metadata (only the first one should be considered, if more than one), and each CAS should have one metadata, even if all of its fields are null. 

Note that language ID is not recorded in this metadata type. It is directly recorded in CAS. Also note that all of the metadata are simply strings, and can be null if that metadata is missing. 

The type  includes: (all strings) 
- interactionId 
- channel 
- provider 
- date (string as YYYY-MM-DD)  
- businessScenario 
- author
 * Updated by JCasGen Wed Jun 12 11:06:43 CEST 2013
 * @generated */
public class Metadata_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Metadata_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Metadata_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Metadata(addr, Metadata_Type.this);
  			   Metadata_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Metadata(addr, Metadata_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Metadata.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("eu.excitement.type.tl.Metadata");
 
  /** @generated */
  final Feature casFeat_interactionId;
  /** @generated */
  final int     casFeatCode_interactionId;
  /** @generated */ 
  public String getInteractionId(int addr) {
        if (featOkTst && casFeat_interactionId == null)
      jcas.throwFeatMissing("interactionId", "eu.excitement.type.tl.Metadata");
    return ll_cas.ll_getStringValue(addr, casFeatCode_interactionId);
  }
  /** @generated */    
  public void setInteractionId(int addr, String v) {
        if (featOkTst && casFeat_interactionId == null)
      jcas.throwFeatMissing("interactionId", "eu.excitement.type.tl.Metadata");
    ll_cas.ll_setStringValue(addr, casFeatCode_interactionId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_channel;
  /** @generated */
  final int     casFeatCode_channel;
  /** @generated */ 
  public String getChannel(int addr) {
        if (featOkTst && casFeat_channel == null)
      jcas.throwFeatMissing("channel", "eu.excitement.type.tl.Metadata");
    return ll_cas.ll_getStringValue(addr, casFeatCode_channel);
  }
  /** @generated */    
  public void setChannel(int addr, String v) {
        if (featOkTst && casFeat_channel == null)
      jcas.throwFeatMissing("channel", "eu.excitement.type.tl.Metadata");
    ll_cas.ll_setStringValue(addr, casFeatCode_channel, v);}
    
  
 
  /** @generated */
  final Feature casFeat_provider;
  /** @generated */
  final int     casFeatCode_provider;
  /** @generated */ 
  public String getProvider(int addr) {
        if (featOkTst && casFeat_provider == null)
      jcas.throwFeatMissing("provider", "eu.excitement.type.tl.Metadata");
    return ll_cas.ll_getStringValue(addr, casFeatCode_provider);
  }
  /** @generated */    
  public void setProvider(int addr, String v) {
        if (featOkTst && casFeat_provider == null)
      jcas.throwFeatMissing("provider", "eu.excitement.type.tl.Metadata");
    ll_cas.ll_setStringValue(addr, casFeatCode_provider, v);}
    
  
 
  /** @generated */
  final Feature casFeat_date;
  /** @generated */
  final int     casFeatCode_date;
  /** @generated */ 
  public String getDate(int addr) {
        if (featOkTst && casFeat_date == null)
      jcas.throwFeatMissing("date", "eu.excitement.type.tl.Metadata");
    return ll_cas.ll_getStringValue(addr, casFeatCode_date);
  }
  /** @generated */    
  public void setDate(int addr, String v) {
        if (featOkTst && casFeat_date == null)
      jcas.throwFeatMissing("date", "eu.excitement.type.tl.Metadata");
    ll_cas.ll_setStringValue(addr, casFeatCode_date, v);}
    
  
 
  /** @generated */
  final Feature casFeat_businessScenario;
  /** @generated */
  final int     casFeatCode_businessScenario;
  /** @generated */ 
  public String getBusinessScenario(int addr) {
        if (featOkTst && casFeat_businessScenario == null)
      jcas.throwFeatMissing("businessScenario", "eu.excitement.type.tl.Metadata");
    return ll_cas.ll_getStringValue(addr, casFeatCode_businessScenario);
  }
  /** @generated */    
  public void setBusinessScenario(int addr, String v) {
        if (featOkTst && casFeat_businessScenario == null)
      jcas.throwFeatMissing("businessScenario", "eu.excitement.type.tl.Metadata");
    ll_cas.ll_setStringValue(addr, casFeatCode_businessScenario, v);}
    
  
 
  /** @generated */
  final Feature casFeat_author;
  /** @generated */
  final int     casFeatCode_author;
  /** @generated */ 
  public String getAuthor(int addr) {
        if (featOkTst && casFeat_author == null)
      jcas.throwFeatMissing("author", "eu.excitement.type.tl.Metadata");
    return ll_cas.ll_getStringValue(addr, casFeatCode_author);
  }
  /** @generated */    
  public void setAuthor(int addr, String v) {
        if (featOkTst && casFeat_author == null)
      jcas.throwFeatMissing("author", "eu.excitement.type.tl.Metadata");
    ll_cas.ll_setStringValue(addr, casFeatCode_author, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Metadata_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_interactionId = jcas.getRequiredFeatureDE(casType, "interactionId", "uima.cas.String", featOkTst);
    casFeatCode_interactionId  = (null == casFeat_interactionId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_interactionId).getCode();

 
    casFeat_channel = jcas.getRequiredFeatureDE(casType, "channel", "uima.cas.String", featOkTst);
    casFeatCode_channel  = (null == casFeat_channel) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_channel).getCode();

 
    casFeat_provider = jcas.getRequiredFeatureDE(casType, "provider", "uima.cas.String", featOkTst);
    casFeatCode_provider  = (null == casFeat_provider) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_provider).getCode();

 
    casFeat_date = jcas.getRequiredFeatureDE(casType, "date", "uima.cas.String", featOkTst);
    casFeatCode_date  = (null == casFeat_date) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_date).getCode();

 
    casFeat_businessScenario = jcas.getRequiredFeatureDE(casType, "businessScenario", "uima.cas.String", featOkTst);
    casFeatCode_businessScenario  = (null == casFeat_businessScenario) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_businessScenario).getCode();

 
    casFeat_author = jcas.getRequiredFeatureDE(casType, "author", "uima.cas.String", featOkTst);
    casFeatCode_author  = (null == casFeat_author) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_author).getCode();

  }
}



    