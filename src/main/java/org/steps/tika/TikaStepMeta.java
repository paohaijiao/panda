/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
*
*******************************************************************************
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
******************************************************************************/

package org.steps.tika;

import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;


@Step(
  id = "TikaStep",
  name = "TikaStep.Name",
  description = "TikaStep.TooltipDesc",
  image = "org/steps/tika/resources/demo.svg",
  categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Transform",
  i18nPackageName = "org.pentaho.di.sdk.steps.tika",
  documentationUrl = "TikaStep.DocumentationURL",
  casesUrl = "TikaStep.CasesURL",
  forumUrl = "TikaStep.ForumURL"
  )
@InjectionSupported( localizationPrefix = "TikaStepMeta.Injection." )
public class TikaStepMeta extends BaseStepMeta implements StepMetaInterface {

  private static final Class<?> PKG =TikaStepMeta.class; // for i18n purposes
  /**
   * tika容器
   */
  @Injection( name = "OUTPUT_FIELD" )
  private TikaBean tika;

  /**
   * 默认构造方法
   */
  public TikaStepMeta() {
    super();
  }

  /**
   *  获取对话框
   */
  public StepDialogInterface getDialog( Shell shell, StepMetaInterface meta, TransMeta transMeta, String name ) {
    return new TikaStepDialog( shell, meta, transMeta, name );
  }

  /**
   * Called by PDI to get a new instance of the step implementation. 
   * A standard implementation passing the arguments to the constructor of the step class is recommended.
   * 
   * @param stepMeta        description of the step
   * @param stepDataInterface    instance of a step data class
   * @param cnr          copy number
   * @param transMeta        description of the transformation
   * @param disp          runtime implementation of the transformation
   * @return            the new instance of a step implementation 
   */
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
      Trans disp ) {
    return new TikaStep( stepMeta, stepDataInterface, cnr, transMeta, disp );
  }

  /**
   * Called by PDI to get a new instance of the step data class.
   */
  public StepDataInterface getStepData() {
    return new TikaStepData();
  }

  /**
   * This method is called every time a new step is created and should allocate/set the step configuration
   * to sensible defaults. The values set here will be used by Spoon when a new step is created.    
   */
  public void setDefault() {
          tika=new TikaBean();
          tika.setFieldName("tika_field");
          tika.setFilepath("file_path");
  }

  public TikaBean getTika() {
    return tika;
  }

  public void setTika(TikaBean tika) {
    this.tika = tika;
  }


  public Object clone() {
    Object retval = super.clone();
    return retval;
  }


  public String getXML() throws KettleValueException {
    StringBuilder xml = new StringBuilder();

    // only one field to serialize
    xml.append( XMLHandler.addTagValue( "fieldName", tika.getFieldName() ) );
    xml.append( XMLHandler.addTagValue( "filePath", tika.getFilepath() ) );
    return xml.toString();
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    try {
      tika.setFieldName( XMLHandler.getNodeValue( XMLHandler.getSubNode( stepnode, "fieldName" ) ) );
      tika.setFilepath( XMLHandler.getNodeValue( XMLHandler.getSubNode( stepnode, "filePath" ) ) );
    } catch ( Exception e ) {
      throw new KettleXMLException( "Tika plugin unable to read step info from XML node", e );
    }
  }
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
      throws KettleException {
    try {
      if(null==tika){
        tika=new TikaBean();
      }
      rep.saveStepAttribute( id_transformation, id_step, "fieldName", tika.getFieldName() ); //$NON-NLS-1$
      rep.saveStepAttribute( id_transformation, id_step, "filePath", tika.getFilepath() ); //$NON-NLS-1$
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step into repository: " + id_step, e );
    }
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
      throws KettleException {
    try {
      if(null==tika){
        tika=new TikaBean();
      }
      tika.setFieldName(rep.getStepAttributeString( id_step, "fieldName" ));
      tika.setFilepath(rep.getStepAttributeString( id_step, "filePath" ));  ; //$NON-NLS-1$
    } catch ( Exception e ) {
      throw new KettleException( "Unable to load step from repository", e );
    }
  }

  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    ValueMetaInterface v = new ValueMetaString( tika.getFieldName() );
    v.setTrimType( ValueMetaInterface.TRIM_TYPE_BOTH );
    v.setOrigin( name );
    inputRowMeta.addValueMeta( v );
//    ValueMetaInterface v1 = new ValueMetaString( tika.getFilepath() );
//    v1.setTrimType( ValueMetaInterface.TRIM_TYPE_BOTH );
//    v1.setOrigin( name );
//    inputRowMeta.addValueMeta( v1 );

  }

  /**
   * This method is called when the user selects the "Verify Transformation" option in Spoon. 
   * A list of remarks is passed in that this method should add to. Each remark is a comment, warning, error, or ok.
   * The method should perform as many checks as necessary to catch design-time errors.
   * 
   * Typical checks include:
   * - verify that all mandatory configuration is given
   * - verify that the step receives any input, unless it's a row generating step
   * - verify that the step does not receive any input if it does not take them into account
   * - verify that the step finds fields it relies on in the row-stream
   * 
   *   @param remarks    the list of remarks to append to
   *   @param transMeta  the description of the transformation
   *   @param stepMeta  the description of the step
   *   @param prev      the structure of the incoming row-stream
   *   @param input      names of steps sending input to the step
   *   @param output    names of steps this step is sending output to
   *   @param info      fields coming in from info steps 
   *   @param metaStore  metaStore to optionally read from
   */
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {
    CheckResult cr;

    // See if there are input streams leading to this step!
    if ( input != null && input.length > 0 ) {
      cr = new CheckResult( CheckResult.TYPE_RESULT_OK,
        BaseMessages.getString( PKG, "Tika.CheckResult.ReceivingRows.OK" ), stepMeta );
      remarks.add( cr );
    } else {
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR,
        BaseMessages.getString( PKG, "Tika.CheckResult.ReceivingRows.ERROR" ), stepMeta );
      remarks.add( cr );
    }
  }
}
