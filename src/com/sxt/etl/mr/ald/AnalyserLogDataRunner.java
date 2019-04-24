package com.sxt.etl.mr.ald;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import com.sxt.common.EventLogConstants;
import com.sxt.common.GlobalConstants;
import com.sxt.util.TimeUtil;

/**
 * ��дmapreduce��runner��
 * 
 * @author root
 *
 */
public class AnalyserLogDataRunner implements Tool {
	private static final Logger logger = Logger
			.getLogger(AnalyserLogDataRunner.class);
	private Configuration conf = null;

	public static void main(String[] args) {
		try {
			ToolRunner.run(new Configuration(), new AnalyserLogDataRunner(), args);
		} catch (Exception e) {
			logger.error("ִ����־����job�쳣", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setConf(Configuration conf) {
		conf.set("fs.defaultFS", "hdfs://192.168.146.141:8020");
//		conf.set("yarn.resourcemanager.hostname", "node3");
		conf.set("hbase.zookeeper.quorum", "node4");
		this.conf = HBaseConfiguration.create(conf);
	}

	@Override
	public Configuration getConf() {
		return this.conf;
	}

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = this.getConf();
		this.processArgs(conf, args);

		Job job = Job.getInstance(conf, "analyser_logdata");

		// ���ñ����ύjob����Ⱥ���У���Ҫ����
		// File jarFile = EJob.createTempJar("target/classes");
		// ((JobConf) job.getConfiguration()).setJar(jarFile.toString());
		// ���ñ����ύjob����Ⱥ���У���Ҫ�������

		job.setJarByClass(AnalyserLogDataRunner.class);
		job.setMapperClass(AnalyserLogDataMapper.class);
		job.setMapOutputKeyClass(NullWritable.class);
		job.setMapOutputValueClass(Put.class);
		// ����reducer����
		// 1. ��Ⱥ�����У����jar����(Ҫ��addDependencyJars����Ϊtrue��Ĭ�Ͼ���true)
		// TableMapReduceUtil.initTableReducerJob(EventLogConstants.HBASE_NAME_EVENT_LOGS,
		// null, job);
		// 2. �������У�Ҫ�����addDependencyJarsΪfalse
		TableMapReduceUtil.initTableReducerJob(
				EventLogConstants.HBASE_NAME_EVENT_LOGS, null, job, null, null,
				null, null, false);
		job.setNumReduceTasks(0);

		// ��������·��
		this.setJobInputPaths(job);
		return job.waitForCompletion(true) ? 0 : -1;
	}

	/**
	 * �������
	 * 
	 * @param conf
	 * @param args
	 */
	private void processArgs(Configuration conf, String[] args) {
		String date = null;
		for (int i = 0; i < args.length; i++) {
			if ("-d".equals(args[i])) {
				if (i + 1 < args.length) {
					date = args[++i];
					break;
				}
			}
		}
		
		System.out.println("-----" + date);

		// Ҫ��date��ʽΪ: yyyy-MM-dd
		if (StringUtils.isBlank(date) || !TimeUtil.isValidateRunningDate(date)) {
			// date��һ����Чʱ������
			date = TimeUtil.getYesterday(); // Ĭ��ʱ��������
			System.out.println(date);
		}
		conf.set(GlobalConstants.RUNNING_DATE_PARAMES, date);
	}

	/**
	 * ����job������·��
	 * 
	 * @param job
	 */
	private void setJobInputPaths(Job job) {
		Configuration conf = job.getConfiguration();
		FileSystem fs = null;
		try {
			fs = FileSystem.get(conf);
			String date = conf.get(GlobalConstants.RUNNING_DATE_PARAMES);
			// Path inputPath = new Path("/flume/" +
			// TimeUtil.parseLong2String(TimeUtil.parseString2Long(date),
			// "MM/dd/"));
			Path inputPath = new Path("/log/"
					+ TimeUtil.parseLong2String(
							TimeUtil.parseString2Long(date), "yyyyMMdd")
					+ "/");
			
			if (fs.exists(inputPath)) {
				FileInputFormat.addInputPath(job, inputPath);
			} else {
				throw new RuntimeException("�ļ�������:" + inputPath);
			}
		} catch (IOException e) {
			throw new RuntimeException("����job��mapreduce����·�������쳣", e);
		} finally {
			if (fs != null) {
				try {
					fs.close();
				} catch (IOException e) {
					// nothing
				}
			}
		}
	}

}
