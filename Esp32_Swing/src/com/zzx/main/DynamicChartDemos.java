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
	private static String userName = "zzx"; // 非必须
	private static String passWord = "981216"; // 非必须

	/**
	 * 构造
	 */
	public DynamicChartDemos() {
		getContentPane().setBackground(Color.green);
	}

	/**
	 * 创建应用程序界面
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
	 * 根据结果集构造JFreechart报表对象
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
	 * 动态运行
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
			// host为主机名，clientid即连接MQTT的客户端ID，一般以唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
			MqttClient client = new MqttClient(HOST, clientid, new MemoryPersistence());
			// MQTT的连接设置
			MqttConnectOptions options = new MqttConnectOptions();
			// 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，设置为true表示每次连接到服务器都以新的身份连接
			options.setCleanSession(false);
			// 设置连接的用户名
			options.setUserName(userName);
			// 设置连接的密码
			options.setPassword(passWord.toCharArray());
			// 设置超时时间 单位为秒
			options.setConnectionTimeout(10);
			// 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
			options.setKeepAliveInterval(20);
			// 设置回调
			client.setCallback(new PushCallback());
			// MqttTopic topic = client.getTopic(TOPIC1);
			// setWill方法，如果项目中需要知道客户端是否掉线可以调用该方法。设置最终端口的通知消息
			// 遗嘱 options.setWill(topic, "close".getBytes(), 2, true);
			client.connect(options);
			// 订阅消息
			int[] Qos = { 1 };
			String[] topic1 = { TOPIC1 };
			client.subscribe(topic1, Qos);

		

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class PushCallback implements MqttCallback {

		public void connectionLost(Throwable cause) {
			// 连接丢失后，一般在这里面进行重连
			System.out.println("连接断开，请重连");
		}

		public void deliveryComplete(IMqttDeliveryToken token) {
			System.out.println("deliveryComplete---------" + token.isComplete());
		}

		public void messageArrived(String topic, MqttMessage message) throws Exception {
			
				// subscribe后得到的消息会执行到这里面
				System.out.println("接收消息主题 : " + topic);
				System.out.println("接收消息Qos : " + message.getQos());
				System.out.println("接收消息内容 : " + new String(message.getPayload()));
				String strmessage = new String(message.getPayload());
				// chart
				Value = Double.parseDouble(strmessage);
			
		

		}

	}

	// 主函数入口
	public static void main(String[] args) {
		DynamicChartDemos jsdChart = new DynamicChartDemos();
		jsdChart.setTitle("Swing动态折线图");
		jsdChart.createUI();
		jsdChart.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jsdChart.setBounds(100, 100, 900, 600);
		jsdChart.setVisible(true);
		// mqtt
		jsdChart.mqttstart();
		jsdChart.dynamicRun();
	}
}
