package org.steps.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import org.w3c.dom.Node;

import kafka.consumer.ConsumerConfig;

/**
 * Kafka Consumer step definitions and serializer to/from XML and to/from Kettle
 * repository.
 *
 * @author Michael Spector
 */
@Step(
		id = "KafkaConsumerStep",
		name = "KafkaConsumerStep.Name",
		description = "KafkaConsumerStep.TooltipDesc",
		image = "org/steps/kafka/resources/demo.svg",
		categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Transform",
		i18nPackageName = "org.pentaho.di.sdk.steps.kafka",
		documentationUrl = "KafkaStep.DocumentationURL",
		casesUrl = "KafkaStep.CasesURL",
		forumUrl = "KafkaStep.ForumURL"
)
@InjectionSupported( localizationPrefix = "KafkaConsumerMeta.Injection." )
public class KafkaConsumerMeta extends BaseStepMeta implements StepMetaInterface {

	public static final String[] KAFKA_PROPERTIES_NAMES = new String[] { "zookeeper.connect", "group.id", "consumer.id",
			"socket.timeout.ms", "socket.receive.buffer.bytes", "fetch.message.max.bytes", "auto.commit.interval.ms",
			"queued.max.message.chunks", "rebalance.max.retries", "fetch.min.bytes", "fetch.wait.max.ms",
			"rebalance.backoff.ms", "refresh.leader.backoff.ms", "auto.commit.enable", "auto.offset.reset",
			"consumer.timeout.ms", "client.id", "zookeeper.session.timeout.ms", "zookeeper.connection.timeout.ms",
			"zookeeper.sync.time.ms" };
	public static final Map<String, String> KAFKA_PROPERTIES_DEFAULTS = new HashMap<String, String>();
	static {
		KAFKA_PROPERTIES_DEFAULTS.put("zookeeper.connect", "localhost:2181");
		KAFKA_PROPERTIES_DEFAULTS.put("group.id", "group");
	}

	private Properties kafkaProperties = new Properties();
	private String topic;
	private String field;
	private String keyField;
	private String limit;
	private String timeout;
	private boolean stopOnEmptyTopic;

	Properties getKafkaProperties() {
		return kafkaProperties;
	}

	/**
	 * @return Kafka topic name
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * @param topic
	 *            Kafka topic name
	 */
	public void setTopic(String topic) {
		this.topic = topic;
	}

	/**
	 * @return Target field name in Kettle stream
	 */
	public String getField() {
		return field;
	}

	/**
	 * @param field
	 *            Target field name in Kettle stream
	 */
	public void setField(String field) {
		this.field = field;
	}

	/**
	 * @return Target key field name in Kettle stream
	 */
	public String getKeyField() {
		return keyField;
	}

	/**
	 * @param keyField
	 *            Target key field name in Kettle stream
	 */
	public void setKeyField(String keyField) {
		this.keyField = keyField;
	}

	/**
	 * @return Limit number of entries to read from Kafka queue
	 */
	public String getLimit() {
		return limit;
	}

	/**
	 * @param limit
	 *            Limit number of entries to read from Kafka queue
	 */
	public void setLimit(String limit) {
		this.limit = limit;
	}

	/**
	 * @return Time limit for reading entries from Kafka queue (in ms)
	 */
	public String getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout
	 *            Time limit for reading entries from Kafka queue (in ms)
	 */
	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	/**
	 * @return 'true' if the consumer should stop when no more messages are
	 *         available
	 */
	public boolean isStopOnEmptyTopic() {
		return stopOnEmptyTopic;
	}

	/**
	 * @param stopOnEmptyTopic
	 *            If 'true', stop the consumer when no more messages are
	 *            available on the topic
	 */
	public void setStopOnEmptyTopic(boolean stopOnEmptyTopic) {
		this.stopOnEmptyTopic = stopOnEmptyTopic;
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
			String input[], String output[], RowMetaInterface info) {

		if (topic == null) {
			remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR,
					Messages.getString("KafkaConsumerMeta.Check.InvalidTopic"), stepMeta));
		}
		if (field == null) {
			remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR,
					Messages.getString("KafkaConsumerMeta.Check.InvalidField"), stepMeta));
		}
		if (keyField == null) {
			remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR,
					Messages.getString("KafkaConsumerMeta.Check.InvalidKeyField"), stepMeta));
		}
		try {
			new ConsumerConfig(kafkaProperties);
		} catch (IllegalArgumentException e) {
			remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, e.getMessage(), stepMeta));
		}
	}
	public KafkaConsumerMeta() {
		super();
	}

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name ) {
		return new KafkaConsumerDialog( shell, meta, transMeta, name );
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
			Trans trans) {
		return new KafkaConsumerStep(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData() {
		return new KafkaConsumerData();
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
			throws KettleXMLException {

		try {
			topic = XMLHandler.getTagValue(stepnode, "TOPIC");
			field = XMLHandler.getTagValue(stepnode, "FIELD");
			keyField = XMLHandler.getTagValue(stepnode, "KEY_FIELD");
			limit = XMLHandler.getTagValue(stepnode, "LIMIT");
			timeout = XMLHandler.getTagValue(stepnode, "TIMEOUT");
			// This tag only exists if the value is "true", so we can directly
			// populate the field
			stopOnEmptyTopic = XMLHandler.getTagValue(stepnode, "STOPONEMPTYTOPIC") != null;
			Node kafkaNode = XMLHandler.getSubNode(stepnode, "KAFKA");
			for (String name : KAFKA_PROPERTIES_NAMES) {
				String value = XMLHandler.getTagValue(kafkaNode, name);
				if (value != null) {
					kafkaProperties.put(name, value);
				}
			}
		} catch (Exception e) {
			throw new KettleXMLException(Messages.getString("KafkaConsumerMeta.Exception.loadXml"), e);
		}
	}

	public String getXML() throws KettleException {
		StringBuilder retval = new StringBuilder();
		if (topic != null) {
			retval.append("    ").append(XMLHandler.addTagValue("TOPIC", topic));
		}
		if (field != null) {
			retval.append("    ").append(XMLHandler.addTagValue("FIELD", field));
		}
		if (keyField != null) {
			retval.append("    ").append(XMLHandler.addTagValue("KEY_FIELD", keyField));
		}
		if (limit != null) {
			retval.append("    ").append(XMLHandler.addTagValue("LIMIT", limit));
		}
		if (timeout != null) {
			retval.append("    ").append(XMLHandler.addTagValue("TIMEOUT", timeout));
		}
		if (stopOnEmptyTopic) {
			retval.append("    ").append(XMLHandler.addTagValue("STOPONEMPTYTOPIC", "true"));
		}
		retval.append("    ").append(XMLHandler.openTag("KAFKA")).append(Const.CR);
		for (String name : KAFKA_PROPERTIES_NAMES) {
			String value = kafkaProperties.getProperty(name);
			if (value != null) {
				retval.append("      " + XMLHandler.addTagValue(name, value));
			}
		}
		retval.append("    ").append(XMLHandler.closeTag("KAFKA")).append(Const.CR);
		return retval.toString();
	}

	public void readRep(Repository rep, ObjectId stepId, List<DatabaseMeta> databases, Map<String, Counter> counters)
			throws KettleException {
		try {
			topic = rep.getStepAttributeString(stepId, "TOPIC");
			field = rep.getStepAttributeString(stepId, "FIELD");
			keyField = rep.getStepAttributeString(stepId, "KEY_FIELD");
			limit = rep.getStepAttributeString(stepId, "LIMIT");
			timeout = rep.getStepAttributeString(stepId, "TIMEOUT");
			stopOnEmptyTopic = rep.getStepAttributeBoolean(stepId, "STOPONEMPTYTOPIC");
			for (String name : KAFKA_PROPERTIES_NAMES) {
				String value = rep.getStepAttributeString(stepId, name);
				if (value != null) {
					kafkaProperties.put(name, value);
				}
			}
		} catch (Exception e) {
			throw new KettleException("KafkaConsumerMeta.Exception.loadRep", e);
		}
	}

	public void saveRep(Repository rep, ObjectId transformationId, ObjectId stepId) throws KettleException {
		try {
			if (topic != null) {
				rep.saveStepAttribute(transformationId, stepId, "TOPIC", topic);
			}
			if (field != null) {
				rep.saveStepAttribute(transformationId, stepId, "FIELD", field);
			}
			if (keyField != null) {
				rep.saveStepAttribute(transformationId, stepId, "KEY_FIELD", keyField);
			}
			if (limit != null) {
				rep.saveStepAttribute(transformationId, stepId, "LIMIT", limit);
			}
			if (timeout != null) {
				rep.saveStepAttribute(transformationId, stepId, "TIMEOUT", timeout);
			}
			rep.saveStepAttribute(transformationId, stepId, "STOPONEMPTYTOPIC", stopOnEmptyTopic);
			for (String name : KAFKA_PROPERTIES_NAMES) {
				String value = kafkaProperties.getProperty(name);
				if (value != null) {
					rep.saveStepAttribute(transformationId, stepId, name, value);
				}
			}
		} catch (Exception e) {
			throw new KettleException("KafkaConsumerMeta.Exception.saveRep", e);
		}
	}

	public void setDefault() {
	}

	public void getFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep,
			VariableSpace space) throws KettleStepException {

		ValueMetaInterface fieldValueMeta = new ValueMeta(getField(), ValueMetaInterface.TYPE_BINARY);
		fieldValueMeta.setOrigin(origin);
		rowMeta.addValueMeta(fieldValueMeta);

		ValueMetaInterface keyFieldValueMeta = new ValueMeta(getKeyField(), ValueMetaInterface.TYPE_BINARY);
		keyFieldValueMeta.setOrigin(origin);
		rowMeta.addValueMeta(keyFieldValueMeta);
	}

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

}
