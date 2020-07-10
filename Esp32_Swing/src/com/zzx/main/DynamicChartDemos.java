package com.zzx.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

public class DynamicChartDemos extends JFrame {
	private TimeSeries series;
	private double Value = 100.0;
	private double Value1 = 100.0;
	public static final String HOST = "tcp://101.200.164.203:1883";
	public static final String TOPIC1 = "w";
	//public static final String TOPIC2 = "s";
	private static final String clientid = "w24471426";
	private static String userName = "zzx"; // �Ǳ���
	private static String passWord = "981216"; // �Ǳ���

	/**
	 * ����
	 */
	public DynamicChartDemos() {
		getContentPane().setBackground(Color.green);
	}

	/**
	 * ����Ӧ�ó������
	 */
	public void createUI() {
		this.series = new TimeSeries("Value", Millisecond.class);
		TimeSeriesCollection dataset = new TimeSeriesCollection(this.series);
		ChartPanel chartPanel = new ChartPanel(createChart(dataset));
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		add(chartPanel);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	/**
	 * ���ݽ��������JFreechart�������
	 * 
	 * @param dataset
	 * @return
	 */
	private JFreeChart createChart(XYDataset dataset) {
		JFreeChart result = ChartFactory.createTimeSeriesChart("SwingLine", "time", "Value", dataset, true, true,
				false);
		XYPlot plot = (XYPlot) result.getPlot();
		ValueAxis axis = plot.getDomainAxis();
		axis.setAutoRange(true);
		axis.setFixedAutoRange(60000.0);
		axis = plot.getRangeAxis();
		axis.setRange(0.0, 120.0);
		return result;
	}

	/**
	 * ��̬����
	 */
	public void dynamicRun() {
		while (true) {

			Millisecond now = new Millisecond();
			this.series.add(new Millisecond(), Value);
			try {
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void mqttstart() {
		try {
			// hostΪ��������clientid������MQTT�Ŀͻ���ID��һ����Ψһ��ʶ����ʾ��MemoryPersistence����clientid�ı�����ʽ��Ĭ��Ϊ���ڴ汣��
			MqttClient client = new MqttClient(HOST, clientid, new MemoryPersistence());
			// MQTT����������
			MqttConnectOptions options = new MqttConnectOptions();
			// �����Ƿ����session,�����������Ϊfalse��ʾ�������ᱣ���ͻ��˵����Ӽ�¼������Ϊtrue��ʾÿ�����ӵ������������µ��������
			options.setCleanSession(false);
			// �������ӵ��û���
			options.setUserName(userName);
			// �������ӵ�����
			options.setPassword(passWord.toCharArray());
			// ���ó�ʱʱ�� ��λΪ��
			options.setConnectionTimeout(10);
			// ���ûỰ����ʱ�� ��λΪ�� ��������ÿ��1.5*20���ʱ����ͻ��˷��͸���Ϣ�жϿͻ����Ƿ����ߣ������������û�������Ļ���
			options.setKeepAliveInterval(20);
			// ���ûص�
			client.setCallback(new PushCallback());
			// MqttTopic topic = client.getTopic(TOPIC1);
			// setWill�����������Ŀ����Ҫ֪���ͻ����Ƿ���߿��Ե��ø÷������������ն˿ڵ�֪ͨ��Ϣ
			// ���� options.setWill(topic, "close".getBytes(), 2, true);
			client.connect(options);
			// ������Ϣ
			int[] Qos = { 1 };
			String[] topic1 = { TOPIC1 };
			client.subscribe(topic1, Qos);

		

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class PushCallback implements MqttCallback {

		public void connectionLost(Throwable cause) {
			// ���Ӷ�ʧ��һ�����������������
			System.out.println("���ӶϿ���������");
		}

		public void deliveryComplete(IMqttDeliveryToken token) {
			System.out.println("deliveryComplete---------" + token.isComplete());
		}

		public void messageArrived(String topic, MqttMessage message) throws Exception {
			
				// subscribe��õ�����Ϣ��ִ�е�������
				System.out.println("������Ϣ���� : " + topic);
				System.out.println("������ϢQos : " + message.getQos());
				System.out.println("������Ϣ���� : " + new String(message.getPayload()));
				String strmessage = new String(message.getPayload());
				// chart
				Value = Double.parseDouble(strmessage);
			
		

		}

	}

	// ���������
	public static void main(String[] args) {
		DynamicChartDemos jsdChart = new DynamicChartDemos();
		jsdChart.setTitle("Swing��̬����ͼ");
		jsdChart.createUI();
		jsdChart.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jsdChart.setBounds(100, 100, 900, 600);
		jsdChart.setVisible(true);
		// mqtt
		jsdChart.mqttstart();
		jsdChart.dynamicRun();
	}
}
