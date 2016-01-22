package com.bolaihui.weight.gui.form;

import com.bolaihui.weight.gui.context.WeightContext;
import com.bolaihui.weight.gui.custom.WaitOutListItem;
import com.bolaihui.weight.gui.custom.WeightListItem;
import com.bolaihui.weight.gui.po.OutData;
import com.bolaihui.weight.gui.po.Weight;
import com.bolaihui.weight.gui.service.WeightService;
import com.bolaihui.weight.gui.util.BaseUtil;
import com.bolaihui.weight.gui.util.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Timer;

/**
 * Created by fz on 2015/12/30.
 */
public class MainForm {

    private JPanel mainPanel;
    private JList weightList;
    private JButton downloadDataBtn;
    private JButton connectBtn;
    private JList downloadList;
    private JTextField emsNoText;
    private JTextField boxNoText;
    private JButton enableBoxNoBtn;
    private JButton disableBoxNoBtn;
    private JLabel connectStatus;
    private JLabel weightLabel;
    private JLabel orderStatus;
    private JTextField userName;
    private JButton loginBtn;
    private JPasswordField password;
    private JLabel loginStatus;
    private JTabbedPane tabbedPane;
    private JButton syncBtn;
    private JButton exportOutDataBtn;
    private JLabel refreshTime;
    private JLabel weightYLabel;
    private JLabel weightNLabel;
    private JList scanList;
    private JTextField scanEmsNo;
    private JLabel weightBlock;
    private JLabel scanBlock;
    private JLabel scanYLabel;
    private JLabel scanNLabel;

    private static final Logger logger = LoggerFactory.getLogger(MainForm.class);

    private WeightContext weightContext = WeightContext.getInstance();

    public MainForm(){

        initContext();
        initListeners();
        SwingUtilities.invokeLater(() -> {
            userName.requestFocus();
        });
        // 定时刷新放行数据（300s刷新一次）
        new Timer().schedule(new TimerTask() {
            public void run() {
                try {
                    weightContext.refreshDownloadOutData();
                } catch (Exception e) {
                    logger.error(BaseUtil.getExceptionStackTrace(e));
                    e.printStackTrace();
                }
            }
        }, 100, 300 * 1000);
    }

    private void initContext(){

        weightContext.putUiComponent("mainForm", mainPanel);
        weightContext.putUiComponent("weightList", weightList);
        weightContext.putUiComponent("downloadDataBtn", downloadDataBtn);
        weightContext.putUiComponent("connectBtn", connectBtn);
        weightContext.putUiComponent("downloadList", downloadList);

        weightContext.putUiComponent("emsNoText", emsNoText);
        weightContext.putUiComponent("boxNoText", boxNoText);
        weightContext.putUiComponent("enableBoxNoBtn", enableBoxNoBtn);
        weightContext.putUiComponent("disableBoxNoBtn", disableBoxNoBtn);
        weightContext.putUiComponent("connectStatus", connectStatus);

        weightContext.putUiComponent("weightLabel", weightLabel);
        weightContext.putUiComponent("orderStatus", orderStatus);

        weightContext.putUiComponent("userName", userName);
        weightContext.putUiComponent("loginBtn", loginBtn);
        weightContext.putUiComponent("password", password);
        weightContext.putUiComponent("loginStatus", loginStatus);

        weightContext.putUiComponent("syncBtn", syncBtn);
        weightContext.putUiComponent("exportOutDataBtn", exportOutDataBtn);
        weightContext.putUiComponent("weightYLabel", weightYLabel);
        weightContext.putUiComponent("weightNLabel", weightNLabel);

        weightContext.putUiComponent("scanList", scanList);
        weightContext.putUiComponent("scanEmsNo", scanEmsNo);
        weightContext.putUiComponent("weightBlock", weightBlock);
        weightContext.putUiComponent("scanBlock", scanBlock);

        weightContext.putUiComponent("scanYLabel", scanYLabel);
        weightContext.putUiComponent("scanNLabel", scanNLabel);

        weightContext.putUiComponent("refreshTime", refreshTime);

        weightList.setCellRenderer(new WeightListItem());
        downloadList.setCellRenderer(new WaitOutListItem());
        scanList.setCellRenderer(new WaitOutListItem());

        // done 读取本地今日称重数据
        Date today = new Date();
        File weightFile = new File("./data/" + BaseUtil.ymDateFormat(today) + "/" + BaseUtil.ymdDateFormat(today) + ".weight");
        try {
            if (weightFile.exists()) {
                List<String> weightList = FileUtils.readLines(weightFile, Charset.forName("utf-8"));
                for (String weightS : weightList) {
                    Weight weight = BaseUtil.parseJson(weightS, Weight.class);
                    weightContext.getListData().add(weight);
                }
            }
        } catch (IOException e) {
            logger.error(BaseUtil.getExceptionStackTrace(e));
            e.printStackTrace();
        }

        // 读取本地今日扫描数据
        File outDataFile = new File("./data/" + BaseUtil.ymDateFormat(today) + "/" + BaseUtil.ymdDateFormat(today) + ".outData");
        try {
            if (outDataFile.exists()) {
                List<String> outDataList = FileUtils.readLines(outDataFile, Charset.forName("utf-8"));
                for (String outDataS : outDataList) {
                    OutData outData = BaseUtil.parseJson(outDataS, OutData.class);
                    weightContext.getScanDataList().add(outData);
                }
            }
        } catch (IOException e) {
            logger.error(BaseUtil.getExceptionStackTrace(e));
            e.printStackTrace();
        }

        weightList.setListData(weightContext.getListData());

        scanList.setListData(weightContext.getScanDataList());

        downloadList.setListData(weightContext.getWaitOutListData());

        emsNoText.setEnabled(false);
        boxNoText.setEnabled(false);
        enableBoxNoBtn.setEnabled(false);
        disableBoxNoBtn.setEnabled(false);
    }

    private void initListeners(){

        /*
        // 上传重量数据
        uploadWeightBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                uploadWeightBtn.setText("正在上传称重数据...");
                uploadWeightBtn.setEnabled(false);
                SwingUtilities.invokeLater(() -> {
                    try {
                        // done 读取当天称重数据文件，序列化为json后上传
                        String json = BaseUtil.toJson(weightContext.getListData());
                        logger.debug(json);
                        Map<String, Object> result = weightContext.uploadWeightData(json);
                        if (StringUtils.equals(result.get("success").toString(), "true")) {
                            JOptionPane.showMessageDialog(null, "上传成功");
                        } else {
                            JOptionPane.showMessageDialog(null, result.get("message").toString());
                        }
                    } catch (Exception ex) {
                        logger.error(BaseUtil.getExceptionStackTrace(ex));
                        JOptionPane.showMessageDialog(null, ex.toString());
                    } finally {
                        uploadWeightBtn.setText("上传称重数据");
                        uploadWeightBtn.setEnabled(true);
                    }
                });
            }
        });
        */

        // 连接电子秤
        connectBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                connectBtn.setText("正在连接电子秤...");
                connectBtn.setEnabled(false);
                SwingUtilities.invokeLater(new WeightService());
            }
        });

        // 用户登录
        loginBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                weightContext.tryToLogin();
            }
        });

        // 帐号
        userName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER){

                    String userName = ((JTextField) weightContext.getUiComponent("userName")).getText();
                    if (StringUtils.isBlank(userName)) {
                        weightContext.getUiComponent("userName").requestFocus();
                    } else {
                        weightContext.getUiComponent("password").requestFocus();
                    }
                }
            }
        });

        // 密码
        password.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER){

                    char[] password = ((JPasswordField) weightContext.getUiComponent("password")).getPassword();
                    if (password != null) {
                        weightContext.tryToLogin();
                    } else {
                        weightContext.getUiComponent("password").requestFocus();
                    }
                }
            }
        });

        // 下载放行数据
        downloadDataBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                weightContext.refreshDownloadOutData();
            }
        });

        // 开启纸箱条码扫描
        enableBoxNoBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                Boolean b = true;
                weightContext.setEnableBoxNo(b.toString());
                enableBoxNoBtn.setEnabled(!b);
                disableBoxNoBtn.setEnabled(b);
                boxNoText.setEnabled(b);
                emsNoText.requestFocus();
            }
        });

        // 关闭纸箱条码扫描
        disableBoxNoBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                Boolean b = false;
                weightContext.setEnableBoxNo(b.toString());
                enableBoxNoBtn.setEnabled(!b);
                disableBoxNoBtn.setEnabled(b);
                boxNoText.setText("");
                boxNoText.setEnabled(b);
                emsNoText.requestFocus();
            }
        });

        // 监听运单号扫描回车事件
        emsNoText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER){

                    // 运单号没有填写
                    if (StringUtils.isBlank(emsNoText.getText())) {
                        emsNoText.requestFocus();
                        return;
                    }

                    // 置光标于下一待输入项
                    if (StringUtils.equals(weightContext.getEnableBoxNo(), "true")) {
                        if (StringUtils.isBlank(boxNoText.getText())) {
                            boxNoText.requestFocus();
                            return;
                        }
                    }

                    // 尝试称重
                    try {
                        weightContext.do4weight();
                    } catch (IOException ex) {
                        logger.error(BaseUtil.getExceptionStackTrace(ex));
                        ex.printStackTrace();
                    }
                }

            }
        });

        // 监听纸箱条码扫描回车事件
        boxNoText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                if(e.getKeyCode() == KeyEvent.VK_ENTER){

                    // 纸箱条码没有填写
                    if (StringUtils.isBlank(boxNoText.getText())) {
                        boxNoText.requestFocus();
                        return;
                    }

                    // 置光标于下一待输入项
                    if (StringUtils.isBlank(emsNoText.getText())) {
                        emsNoText.requestFocus();
                        return;
                    }

                    // 尝试称重
                    try {
                        weightContext.do4weight();
                    } catch (IOException ex) {
                        logger.error(BaseUtil.getExceptionStackTrace(ex));
                        ex.printStackTrace();
                    }
                }
            }
        });

        // 监听运单号扫描回车事件
        scanEmsNo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER){

                    // 运单号没有填写
                    if (StringUtils.isBlank(scanEmsNo.getText())) {
                        scanEmsNo.requestFocus();
                        return;
                    }

                    // 反馈扫描结果
                    try {
                        weightContext.scanEmsNoStatus();
                    } catch (IOException ex) {
                        logger.error(BaseUtil.getExceptionStackTrace(ex));
                        ex.printStackTrace();
                    }
                }

            }
        });

        syncBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                syncBtn.setText("正在同步");
                syncBtn.setEnabled(false);
                SwingUtilities.invokeLater(() -> {
                    Map<String, Object> result = new HashMap<>();
                    try {
                        // done 扫描只留放行并去重
                        Set<OutData> outDataSet = new HashSet<>();
                        Set<String> outDataEmsNos = new HashSet<>();
                        for (OutData outData : weightContext.getScanDataList()) {
                            if (StringUtils.equals(Constants.successStatus, outData.getStatus())) {
                                if (!outDataEmsNos.contains(outData.getEmsNo())) {
                                    outDataEmsNos.add(outData.getEmsNo());
                                    outDataSet.add(outData);
                                }
                            }
                        }
                        // done 称重记录取倒叙，以最后一次称重的重量为准
                        List<Weight> weightList = new ArrayList<>();
                        for (Weight weight : weightContext.getListData()) {
                            weight.setWeight(weight.getWeight().replace("kg", ""));
                            weightList.add(0, weight);
                        }
                        String weightJson = BaseUtil.toJson(weightList);
                        String outDataJson = BaseUtil.toJson(outDataSet);
                        weightContext.syncData(weightJson, outDataJson);
                    } catch (Exception ex) {
                        logger.error(BaseUtil.getExceptionStackTrace(ex));
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, ex.toString());
                    } finally {
                        syncBtn.setText("同步数据");
                        syncBtn.setEnabled(true);
                    }
                });
            }
        });

        exportOutDataBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                exportOutDataBtn.setText("正在下载");
                exportOutDataBtn.setEnabled(false);
                SwingUtilities.invokeLater(() -> {
                    weightContext.exportReleaseData();
                    exportOutDataBtn.setText("出区数据导出");
                    exportOutDataBtn.setEnabled(true);
                });
            }
        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
