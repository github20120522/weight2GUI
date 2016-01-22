package com.bolaihui.weight.gui.service;

import com.bolaihui.weight.gui.context.WeightContext;
import com.bolaihui.weight.gui.util.BaseUtil;
import gnu.io.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fz on 2016/1/4.
 */
public class WeightService implements SerialPortEventListener, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(WeightService.class);

    private BufferedInputStream inputStream;

    private CommPortIdentifier commPort;

    private SerialPort serialPort;

    Pattern p = Pattern.compile("\\d+\\.\\d+");

    private WeightContext weightContext = WeightContext.getInstance();

    @Override
    public void run() {
        try {
            connect();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(BaseUtil.getExceptionStackTrace(e));
            // done 结束线程，更新连接提示
            closeConnect();
        }
    }

    private void connect() throws PortInUseException, IOException, TooManyListenersException {

        // 打开称重端口
        CommPortIdentifier commPortIdentifier;
        Enumeration en = CommPortIdentifier.getPortIdentifiers();
        boolean isOpened = false;

        while (en.hasMoreElements()) {
            commPortIdentifier = (CommPortIdentifier) en.nextElement();
            if (commPortIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                commPort = commPortIdentifier;
                serialPort = (SerialPort) commPort.open("称重端口", 5000);
                isOpened = true;
                break;
            }
        }

        if (isOpened) {
            serialPort.addEventListener(this);
            inputStream = new BufferedInputStream(serialPort.getInputStream());
            serialPort.notifyOnDataAvailable(true);
            okConnect();
        } else {
            // done 端口打开失败
            logger.error("连接状态：连接失败，请重新连接");
            // done 结束线程，更新连接提示
            closeConnect();
        }

    }

    private void closeConnect() {

        weightContext.getUiComponent("connectStatus").setForeground(Color.RED);
        ((JLabel) weightContext.getUiComponent("connectStatus")).setText("连接状态：连接失败");
        weightContext.getUiComponent("connectBtn").setEnabled(true);
        ((JButton) weightContext.getUiComponent("connectBtn")).setText("连接电子秤");
        weightContext.getUiComponent("emsNoText").setEnabled(false);
        weightContext.getUiComponent("boxNoText").setEnabled(false);
        weightContext.getUiComponent("enableBoxNoBtn").setEnabled(false);
        weightContext.getUiComponent("disableBoxNoBtn").setEnabled(false);
        if (serialPort != null) {
            serialPort.close();
        }
    }

    private void okConnect() {

        Color okColor = new Color(0, 155, 55);
        weightContext.getUiComponent("connectStatus").setForeground(okColor);
        ((JLabel) weightContext.getUiComponent("connectStatus")).setText("连接状态：连接成功");
        weightContext.getUiComponent("connectBtn").setEnabled(false);
        ((JButton) weightContext.getUiComponent("connectBtn")).setText("连接电子秤成功");
        // done 开启可输入状态
        weightContext.getUiComponent("emsNoText").setEnabled(true);
        weightContext.getUiComponent("emsNoText").requestFocus();
        if (StringUtils.equals(weightContext.getEnableBoxNo(), "true")) {
            weightContext.getUiComponent("boxNoText").setEnabled(true);
            weightContext.getUiComponent("enableBoxNoBtn").setEnabled(false);
            weightContext.getUiComponent("disableBoxNoBtn").setEnabled(true);
        } else {
            weightContext.getUiComponent("boxNoText").setEnabled(false);
            weightContext.getUiComponent("enableBoxNoBtn").setEnabled(true);
            weightContext.getUiComponent("disableBoxNoBtn").setEnabled(false);
        }
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {

        switch (serialPortEvent.getEventType()) {
            case SerialPortEvent.BI:
                /* Break interrupt，通讯中断 */
                // done 结束线程，更新连接提示
                logger.error("电脑与电子计重秤通信中断，请立即检查连接状况");
                closeConnect();
                break;
            case SerialPortEvent.OE:
                /* Overrun error，溢位错误 */
            case SerialPortEvent.FE:
                /* Framing error，传帧错误 */
            case SerialPortEvent.PE:
                /* Parity error，校验错误 */
            case SerialPortEvent.CD:
                /* Carrier detect，载波检测 */
            case SerialPortEvent.CTS:
                /* Clear to send，清除发送 */
            case SerialPortEvent.DSR:
                /* Data set ready，数据设备就绪 */
            case SerialPortEvent.RI:
                /* Ring indicator，响铃指示 */
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                /* Output buffer is empty，输出缓冲区清空 */
                break;
            case SerialPortEvent.DATA_AVAILABLE:
                /* Data available at the serial port，端口有可用数据，读到缓冲数组，输出到终端 */
                try {
                    dataProcess();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error(BaseUtil.getExceptionStackTrace(e));
                    // done 关闭连接
                    closeConnect();
                }
        }
    }

    /* 处理串口数据 */
    private void dataProcess() throws Exception {

        // 等待完整的称重数据
        Thread.sleep(100);
        byte[] readBuffer = new byte[1024];
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        while (inputStream.available() > 0) {
            int len = inputStream.read(readBuffer);
            bytes.write(readBuffer, 0, len);
        }
        String readStr = new String(bytes.toByteArray());
        Matcher m = p.matcher(readStr);
        if (m.find()) {
            String weightValue = m.group();
            // done 显示称重重量
            showWeight(weightValue, readStr.contains("kg"));
        }
    }

    /* 显示称重信息 */
    private void showWeight(String weightValue, boolean isKg) throws IOException {
        // done 显示当前重量
        double weight = Double.parseDouble(weightValue.trim());
        if (!isKg) {
            weight = weight / 1000;
        }
        ((JLabel) weightContext.getUiComponent("weightLabel")).setText(weight + "kg");
        // done 尝试称重
        weightContext.do4weight();
    }

}
