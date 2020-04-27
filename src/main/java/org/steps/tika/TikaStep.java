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

import org.apache.tika.Tika;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.steps.util.LoadExt;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is part of the demo step plug-in implementation.
 * It demonstrates the basics of developing a plug-in step for PDI. 
 * 
 * The demo step adds a new string field to the row stream and sets its
 * value to "Hello World!". The user may select the name of the new field.
 *   
 * This class is the implementation of StepInterface.
 * Classes implementing this interface need to:
 * 
 * - initialize the step
 * - execute the row processing logic
 * - dispose of the step 
 * 
 * Please do not create any local fields in a StepInterface class. Store any
 * information related to the processing logic in the supplied step data interface
 * instead.  
 * 
 */

public class TikaStep extends BaseStep implements StepInterface {

  private static final Class<?> PKG = TikaStepMeta.class; // for i18n purposes
    Integer index=0;

  public TikaStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis ) {
    super( s, stepDataInterface, c, t, dis );
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    TikaStepMeta meta = (TikaStepMeta) smi;
    TikaStepData data = (TikaStepData) sdi;
    if ( !super.init( meta, data ) ) {
      return false;
    }
    return true;
  }
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    TikaStepMeta meta = (TikaStepMeta) smi;
    TikaStepData data = (TikaStepData) sdi;
    Object[] r = getRow();
    if ( r == null ) {
      setOutputDone();
      return false;
    }
    if ( first ) {
      first = false;
      data.outputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, null, null );
      data.outputFieldIndex = data.outputRowMeta.indexOfValue( meta.getTika().getFieldName() );
      index=data.outputRowMeta.indexOfValue( meta.getTika().getFilepath() );
      if ( data.outputFieldIndex < 0 ) {
        log.logError( BaseMessages.getString( PKG, "TikaStep.Error.NoOutputField" ) );
        setErrors( 1L );
        setOutputDone();
        return false;
      }
    }

    Object[] outputRow = RowDataUtil.resizeArray( r, data.outputRowMeta.size() );
    try{
        String str= System.getProperty("KETTLE_HOME");
        if(null==str||"".equals(str)){
            log.logError( BaseMessages.getString( PKG, "TikaStep.Error.KETTLE_HOME" ) );
            throw new Exception("加载第三方jar包出错了");
        }
        List<String> list=new ArrayList<>();
        list.add(str+"/libext");
        LoadExt.loadExtLib(list);
        Object filepath=outputRow[index];
        if(!(filepath instanceof String)){
            throw new Exception("请确保这是文件路径字段");
        }else{
            String fileString=(String)filepath;
            File file = new File(fileString);
            Tika tikaConfig = new Tika();
            String filecontent = tikaConfig.parseToString(file);
            System.out.println("Extracted Content: " + filecontent);
            outputRow[data.outputFieldIndex] =filecontent;
            putRow( data.outputRowMeta, outputRow );
        }

    }catch (Exception e){
        log.logError(e.getMessage());
    }

    if ( checkFeedback( getLinesRead() ) ) {
      logBasic( BaseMessages.getString( PKG, "TikaStep.Linenr", getLinesRead() ) ); // Some basic logging
    }
    return true;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    TikaStepMeta meta = (TikaStepMeta) smi;
    TikaStepData data = (TikaStepData) sdi;
    super.dispose( meta, data );
  }
}
