package org.steps.solr;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @program: panda
 * @description: 描述
 * @author: 泡海椒
 * @create: 2020-04-25 07:02
 **/
public class SolrInData extends BaseStepData implements StepDataInterface {
    public int argnrs[];
    public Database db;

    /**
     *
     */
    public SolrInData()
    {
        super();

        db=null;
    }
}
