package com.bolaihui.weight.gui.context;

import com.bolaihui.weight.gui.po.OutData;
import com.bolaihui.weight.gui.po.Weight;
import com.bolaihui.weight.gui.util.AES128Util;
import com.bolaihui.weight.gui.util.BaseUtil;
import com.bolaihui.weight.gui.util.Constants;
import com.bolaihui.weight.gui.util.HttpUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.audio.AudioPlayer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

/**
 * Created by fz on 2015/12/30.
 */
public class WeightContext {

    private static final Logger logger = LoggerFactory.getLogger(WeightContext.class);

    private WeightContext() {

        Properties properties = new Properties();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("system.properties");
        try {
            properties.load(inputStream);
            enableBoxNo = properties.get("enableBoxNo").toString();
            aesKey = properties.get("aesKey").toString();

            basePath = properties.get("basePath").toString();
            downloadOutDataUrl = basePath + properties.get("downloadOutDataUrl").toString();
            // uploadWeightDataUrl = basePath + properties.get("uploadWeightDataUrl").toString();
            loginCheckUrl = basePath + properties.getProperty("loginCheckUrl").toString();
            syncDataUrl = basePath + properties.getProperty("syncDataUrl").toString();
            exportReleaseDataUrl = basePath + properties.getProperty("exportReleaseDataUrl").toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static WeightContext weightContext = new WeightContext();

    private String enableBoxNo;

    private boolean isLogin;

    private String basePath;

    private String downloadOutDataUrl;

    private String aesKey;

    // private String uploadWeightDataUrl;

    private String loginCheckUrl;

    private String syncDataUrl;

    private String exportReleaseDataUrl;

    private String realName;

    private Map<String, Component> uiComponents = new HashMap<>();

    // 称重数据
    private Vector<Weight> listData = new Vector<>();

    // 放行数据
    private Vector<OutData> waitOutListData = new Vector<>();

    // 放行数据转map
    private Map<String, Object> waitOutDataMap = new HashMap<>();

    // 扫描过的待出区数据
    private Vector<OutData> scanDataList = new Vector<>();

    // 已称重已放行set
    private Set<String> weightYSet = new HashSet<>();

    // 已称重未放行set
    private Set<String> weightNSet = new HashSet<>();

    // 已扫描放行set
    private Set<String> scanYSet = new HashSet<>();

    // 已扫描未放行set
    private Set<String> scanNSet = new HashSet<>();

    public static WeightContext getInstance(){
        return weightContext;
    }

    public Component getUiComponent(String name) {
        return uiComponents.get(name);
    }

    public void putUiComponent(String name, Component component) {
        this.uiComponents.put(name, component);
    }

    public Vector<Weight> getListData() {
        return listData;
    }

    public String getEnableBoxNo() {
        return enableBoxNo;
    }

    public void setEnableBoxNo(String enableBoxNo) {
        this.enableBoxNo = enableBoxNo;
    }

    public Map<String, Object> getWaitOutDataMap() {
        return waitOutDataMap;
    }

    public Vector<OutData> getWaitOutListData() {
        return waitOutListData;
    }

    public Vector<OutData> getScanDataList() {
        return scanDataList;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public String getRealName() {
        return realName;
    }

    /* 判断当前是否可以称重，即需要输入的必要信息和重量都以具备 */
    public boolean ok4weight() {

        boolean ok = false;
        // done 需要注意纸箱条码的判断
        String weightValue = ((JLabel) weightContext.getUiComponent("weightLabel")).getText();
        String emsNoText = ((JTextField) weightContext.getUiComponent("emsNoText")).getText();
        String boxNoText = ((JTextField) weightContext.getUiComponent("boxNoText")).getText();
        if (StringUtils.isNotBlank(weightValue)
                && !StringUtils.equals(weightValue, Constants.weightValueDefault)
                && StringUtils.isNotBlank(emsNoText)
                && StringUtils.equals(enableBoxNo, "false")) {

            ok = true;
        } else if (StringUtils.isNotBlank(weightValue)
                && !StringUtils.equals(weightValue, Constants.weightValueDefault)
                && StringUtils.isNotBlank(emsNoText)
                && StringUtils.equals(enableBoxNo, "true")
                && StringUtils.isNotBlank(boxNoText)) {

            ok = true;
        }
        return ok;
    }

    /* 保存称重记录 */
    public void do4weight() throws IOException {

        logger.debug("尝试称重");

        // done 没有登录不可称重
        if (!isLogin) {
            JOptionPane.showMessageDialog(null, "称重前请先登录");
            weightContext.getUiComponent("userName").requestFocus();
            return;
        }

        if(ok4weight()){

            // done 以"_"进行数据拼接，依次为emsNo_weight_status_boxNo
            String weightValue = ((JLabel) weightContext.getUiComponent("weightLabel")).getText();
            String emsNoText = ((JTextField) weightContext.getUiComponent("emsNoText")).getText();
            String boxNoText = ((JTextField) weightContext.getUiComponent("boxNoText")).getText();

            // done 1.将称重数据保存
            String outStatus = getEmsNoStatus(emsNoText);
            Date today = new Date();

            // 称重数据处理
            Weight weight = new Weight(emsNoText, weightValue, (StringUtils.isNotBlank(boxNoText) ? boxNoText : "0"), realName, BaseUtil.ymdHmsDateFormat(today), outStatus);
            File weightFile = new File("./data/" + BaseUtil.ymDateFormat(today) + "/" + BaseUtil.ymdDateFormat(today) + ".weight");
            FileUtils.writeStringToFile(weightFile, BaseUtil.toJson(weight) + "\n", "utf-8", true);
            weightContext.getListData().add(0, weight);
            ((JList) weightContext.getUiComponent("weightList")).updateUI();

            // 放行数据处理
            OutData outData = new OutData(emsNoText, outStatus, realName, BaseUtil.ymdHmsDateFormat(today));
            File outDataFile = new File("./data/" + BaseUtil.ymDateFormat(today) + "/" + BaseUtil.ymdDateFormat(today) + ".outData");
            FileUtils.writeStringToFile(outDataFile, BaseUtil.toJson(outData) + "\n", "utf-8", true);
            weightContext.getScanDataList().add(0, outData);
            ((JList) weightContext.getUiComponent("scanList")).updateUI();

            // done 已称重已放行数量，已称重未放行数量更新
            if (StringUtils.equals(outStatus, Constants.successStatus)) {
                weightYSet.add(emsNoText + "_Y");
                weightNSet.remove(emsNoText + "_N");
                weightContext.getUiComponent("weightBlock").setForeground(Constants.okColor);
                // done 播放已放行音效
                successSound();
            } else {
                weightYSet.remove(emsNoText + "_Y");
                weightNSet.add(emsNoText + "_N");
                weightContext.getUiComponent("weightBlock").setForeground(Color.RED);
                // done 播放未放行音效
                failureSound();
            }
            JLabel weightYLabel = (JLabel) weightContext.getUiComponent("weightYLabel");
            weightYLabel.setText("已称重已放行" + weightYSet.size());
            JLabel weightNLabel = (JLabel) weightContext.getUiComponent("weightNLabel");
            weightNLabel.setText("已称重未放行" + weightNSet.size());

            // done 2.将页面重量数据，emsNo数据，boxNo数据清除，留下放行状态，以emsNo | weight | status格式并加以颜色指示保留
            ((JTextField) weightContext.getUiComponent("emsNoText")).setText("");
            ((JTextField) weightContext.getUiComponent("boxNoText")).setText("");

            ((JLabel) weightContext.getUiComponent("weightLabel")).setText(Constants.weightValueDefault);
            if (StringUtils.equals(outStatus, Constants.successStatus)) {
                weightContext.getUiComponent("orderStatus").setForeground(Constants.okColor);
            } else {
                weightContext.getUiComponent("orderStatus").setForeground(Color.RED);
            }
            ((JLabel) weightContext.getUiComponent("orderStatus")).setText(emsNoText + " | " + weightValue + " | " + outStatus);

            // done 3.将光标置于emsNo输入框
            weightContext.getUiComponent("emsNoText").requestFocus();
        }
    }

    public void scanEmsNoStatus() throws IOException {

        // done 没有登录不可扫描
        if (!isLogin) {
            JOptionPane.showMessageDialog(null, "扫描前请先登录");
            weightContext.getUiComponent("userName").requestFocus();
            return;
        }

        String emsNoText = ((JTextField) weightContext.getUiComponent("scanEmsNo")).getText();
        String outStatus = getEmsNoStatus(emsNoText);

        Date today = new Date();
        // 放行数据处理
        OutData outData = new OutData(emsNoText, outStatus, realName, BaseUtil.ymdHmsDateFormat(today));
        File outDataFile = new File("./data/" + BaseUtil.ymDateFormat(today) + "/" + BaseUtil.ymdDateFormat(today) + ".outData");
        FileUtils.writeStringToFile(outDataFile, BaseUtil.toJson(outData) + "\n", "utf-8", true);
        weightContext.getScanDataList().add(0, outData);
        ((JList) weightContext.getUiComponent("scanList")).updateUI();

        // done 已扫描放行数量，已扫描未放行数量更新
        if (StringUtils.equals(outStatus, Constants.successStatus)) {
            scanYSet.add(emsNoText + "_Y");
            scanNSet.remove(emsNoText + "_N");
            weightContext.getUiComponent("scanBlock").setForeground(Constants.okColor);
            // done 播放已放行音效
            successSound();
        } else {
            scanYSet.remove(emsNoText + "_Y");
            scanNSet.add(emsNoText + "_N");
            weightContext.getUiComponent("scanBlock").setForeground(Color.RED);
            // done 播放未放行音效
            failureSound();
        }
        JLabel scanYLabel = (JLabel) weightContext.getUiComponent("scanYLabel");
        scanYLabel.setText("已扫描已放行" + scanYSet.size());
        JLabel scanNLabel = (JLabel) weightContext.getUiComponent("scanNLabel");
        scanNLabel.setText("已扫描未放行" + scanNSet.size());

        // 将光标置于emsNo输入框
        ((JTextField) weightContext.getUiComponent("scanEmsNo")).setText("");
        weightContext.getUiComponent("scanEmsNo").requestFocus();
    }

    /* 获取运单的出区状态 */
    public String getEmsNoStatus(String emsNo) {

        String resultStatus = "无";
        if (!waitOutDataMap.isEmpty() && waitOutDataMap.get(emsNo) != null) {
            String value = waitOutDataMap.get(emsNo).toString();
            if (StringUtils.isNotBlank(value)) {
                resultStatus = value;
            }
        }
        return resultStatus;
    }

    /* 下载放行数据 */
    public void downloadOutData() throws IOException {

        JLabel refreshTime = (JLabel) weightContext.getUiComponent("refreshTime");
        Map<String, Object> params = new HashMap<>();
        String today = BaseUtil.ymdDateFormat(new Date());
        params.put("date", today);
        params.put("sign", DigestUtils.sha1Hex(today + Constants.simpleKey));
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        String resultStr = HttpUtil.httpPost(downloadOutDataUrl, headers, params);
        // key: emsNo, value: 放行状态
        if (StringUtils.isNotBlank(resultStr)) {
            synchronized (waitOutDataMap) {
                waitOutDataMap = BaseUtil.parseJson(resultStr, Map.class);
            }
        } else {
            refreshTime.setText("刷新出错");
            refreshTime.setForeground(Color.RED);
        }
    }

    /* 刷新放行数据 */
    public void refreshDownloadOutData() {
        JButton downloadDataBtn = (JButton) weightContext.getUiComponent("downloadDataBtn");
        JLabel refreshTime = (JLabel) weightContext.getUiComponent("refreshTime");
        downloadDataBtn.setText("正在刷新");
        downloadDataBtn.setEnabled(false);
        SwingUtilities.invokeLater(() -> {
            try {
                weightContext.downloadOutData();
                Map<String, Object> waitOutDataMap = weightContext.getWaitOutDataMap();
                if (!waitOutDataMap.isEmpty()) {
                    Vector<OutData> waitOutListData = weightContext.getWaitOutListData();
                    waitOutListData.clear();
                    for (Map.Entry entry : waitOutDataMap.entrySet()) {
                        OutData outData = new OutData();
                        outData.setEmsNo(entry.getKey().toString());
                        outData.setStatus(entry.getValue().toString());
                        waitOutListData.add(outData);
                    }
                    refreshTime.setForeground(Color.BLACK);
                    refreshTime.setText(BaseUtil.ymdHmsDateFormat(new Date()));
                    ((JList) weightContext.getUiComponent("downloadList")).updateUI();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error(BaseUtil.getExceptionStackTrace(ex));
                refreshTime.setText("刷新出错");
                refreshTime.setForeground(Color.RED);
            } finally {
                downloadDataBtn.setText("刷新数据");
                downloadDataBtn.setEnabled(true);
            }
        });
    }

    /* 上传称重数据 */
    /*
    public Map<String, Object> uploadWeightData(String json) throws IOException {

        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> params = new HashMap<>();
            String today = BaseUtil.ymdDateFormat(new Date());
            params.put("date", today);
            params.put("sign", DigestUtils.sha1Hex(today + Constants.simpleKey));
            // done 上传当日加密后的称重数据
            params.put("data", AES128Util.encrypt(aesKey, json));
            Map<String, Object> headers = new HashMap<>();
            headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            String resultStr = HttpUtil.httpPost(uploadWeightDataUrl, headers, params);
            result = BaseUtil.parseJson(resultStr, Map.class);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.toString());
            logger.error(BaseUtil.getExceptionStackTrace(e));
            e.printStackTrace();
        }
        return result;
    }
    */

    /* 同步数据 */
    public void syncData(String weightJson, String outDataJson) throws IOException {

        Map<String, Object> result;
        Date today = new Date();
        try {
            Map<String, Object> params = new HashMap<>();
            String curDate = BaseUtil.ymdDateFormat(new Date());
            params.put("date", curDate);
            params.put("sign", DigestUtils.sha1Hex(curDate + Constants.simpleKey));
            // done 上传当日加密后的称重数据
            params.put("weightJson", AES128Util.encrypt(aesKey, weightJson));
            params.put("outDataJson", AES128Util.encrypt(aesKey, outDataJson));
            Map<String, Object> headers = new HashMap<>();
            headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            String resultStr = HttpUtil.httpPost(syncDataUrl, headers, params);
            result = BaseUtil.parseJson(resultStr, Map.class);
            if (StringUtils.equals(result.get("success").toString(), "true")) {
                // 记录同步返回消息，暂不展示给用户
                String messageListJson = result.get("messageList").toString();
                List messageList = BaseUtil.parseJson(AES128Util.decrypt(aesKey, messageListJson), List.class);
                File syncMessageFile = new File("./data/" + BaseUtil.ymDateFormat(today) + "/" + BaseUtil.ymdDateFormat(today) + ".syncMessage");
                for (Object message : messageList) {
                    FileUtils.writeStringToFile(syncMessageFile, BaseUtil.toJson(message) + "\n", "utf-8", true);
                }
                // done 将数据移置历史完成数据文件中
                File historySyncWeightFile = new File("./data/" + BaseUtil.ymDateFormat(today) + "/" + BaseUtil.ymdDateFormat(today) + ".weightSyncHistory");
                File historySyncOutDataFile = new File("./data/" + BaseUtil.ymDateFormat(today) + "/" + BaseUtil.ymdDateFormat(today) + ".outDataSyncHistory");
                File weightFile = new File("./data/" + BaseUtil.ymDateFormat(today) + "/" + BaseUtil.ymdDateFormat(today) + ".weight");
                File outDataFile = new File("./data/" + BaseUtil.ymDateFormat(today) + "/" + BaseUtil.ymdDateFormat(today) + ".outData");
                for (Weight weight : weightContext.getListData()) {
                    FileUtils.writeStringToFile(historySyncWeightFile, BaseUtil.toJson(weight) + "\n", "utf-8", true);
                }
                weightContext.getListData().removeAllElements();
                ((JList) weightContext.getUiComponent("weightList")).updateUI();
                for (OutData outData : weightContext.getScanDataList()) {
                    FileUtils.writeStringToFile(historySyncOutDataFile, BaseUtil.toJson(outData) + "\n", "utf-8", true);
                }
                weightContext.getScanDataList().removeAllElements();
                ((JList) weightContext.getUiComponent("scanList")).updateUI();
                if (weightFile.exists()) {
                    weightFile.delete();
                }
                if (outDataFile.exists()) {
                    outDataFile.delete();
                }
                refreshDownloadOutData();
                JOptionPane.showMessageDialog(null, "同步成功");
            } else {
                JOptionPane.showMessageDialog(null, result.get("message").toString());
            }
        } catch (Exception e) {
            logger.error(BaseUtil.getExceptionStackTrace(e));
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.toString());
        }
    }

    public void exportReleaseData() {

        try {
            Map<String, Object> params = new HashMap<>();
            String curDate = BaseUtil.ymdDateFormat(new Date());
            params.put("date", curDate);
            params.put("sign", DigestUtils.sha1Hex(curDate + Constants.simpleKey));
            Map<String, Object> headers = new HashMap<>();
            headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            String resultStr = HttpUtil.httpPost(exportReleaseDataUrl, headers, params);
            Map<String, Object> result;
            result = BaseUtil.parseJson(resultStr, Map.class);
            if (StringUtils.equals(result.get("success").toString(), "true")) {
                String data = result.get("data").toString();
                String originalData = AES128Util.decrypt(aesKey, data);
                List dataList = BaseUtil.parseJson(originalData, List.class);
                buildExportFile(dataList);
            } else {
                JOptionPane.showMessageDialog(null, result.get("message").toString());
            }
        } catch (Exception e) {
            logger.error(BaseUtil.getExceptionStackTrace(e));
            JOptionPane.showMessageDialog(null, e.toString());
        }
    }

    public void buildExportFile(List dataList) {
        FileOutputStream fos = null;
        try {
            Date today = new Date();
            File exportReleaseDataFile = new File("./export/" + BaseUtil.ymDateFormat(today) + "/" + BaseUtil.ymdDateFormat(today) + ".xls");
            FileUtils.touch(exportReleaseDataFile);
            Workbook wb = new HSSFWorkbook();
            Sheet sheet = wb.createSheet("出区数据");

            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("运单号");

            for (int i=0; i<dataList.size(); i++) {
                Row r = sheet.createRow(i+1);
                Cell c = r.createCell(0);
                c.setCellValue(dataList.get(i).toString());
            }
            fos = new FileOutputStream(exportReleaseDataFile);
            wb.write(fos);
        } catch (Exception e) {
            logger.error(BaseUtil.getExceptionStackTrace(e));
            JOptionPane.showMessageDialog(null, e.toString());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void tryToLogin() {
        String loginStr = "登录";
        String loginBtnText = ((JButton) weightContext.getUiComponent("loginBtn")).getText();
        ((JButton) weightContext.getUiComponent("loginBtn")).setText("处理中...");
        weightContext.getUiComponent("loginBtn").setEnabled(false);
        SwingUtilities.invokeLater(() -> {
            // 尝试登录
            weightContext.loginLogOut(StringUtils.equals(loginStr, loginBtnText));
        });
    }

    /* 登录、退出 */
    public void loginLogOut(boolean isIn) {

        try {
            if (isIn) {
                Map<String, Object> result;
                // done 处理登录逻辑
                String userName = ((JTextField) weightContext.getUiComponent("userName")).getText();
                char[] password = ((JPasswordField) weightContext.getUiComponent("password")).getPassword();
                String passwordStr = new String(password);
                if (StringUtils.isBlank(userName) || StringUtils.isBlank(passwordStr)) {
                    JOptionPane.showMessageDialog(null, "帐号、密码不能为空");
                    weightContext.getUiComponent("userName").requestFocus();
                    weightContext.getUiComponent("loginBtn").setEnabled(true);
                    ((JButton) weightContext.getUiComponent("loginBtn")).setText("登录");
                    return;
                }
                /* http 加密请求过程 begin */
                Map<String, Object> params = new HashMap<>();
                String today = BaseUtil.ymdDateFormat(new Date());
                params.put("date", today);
                params.put("sign", DigestUtils.sha1Hex(today + Constants.simpleKey));
                params.put("userName", AES128Util.encrypt(aesKey, userName));
                params.put("password", AES128Util.encrypt(aesKey, passwordStr));
                Map<String, Object> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                String resultStr = HttpUtil.httpPost(loginCheckUrl, headers, params);
                result = BaseUtil.parseJson(resultStr, Map.class);
                /* http 加密请求过程 end */
                if (StringUtils.equals(result.get("success").toString(), "true")) {
                    // done 登录成功，改变登录状态为：已登录【张三】，帐号，密码输入框不可用，登录btn变为退出btn
                    isLogin = true;
                    realName = result.get("realName").toString();
                    weightContext.getUiComponent("userName").setEnabled(false);
                    weightContext.getUiComponent("password").setEnabled(false);
                    weightContext.getUiComponent("loginBtn").setEnabled(true);
                    ((JButton) weightContext.getUiComponent("loginBtn")).setText("退出");
                    weightContext.getUiComponent("loginStatus").setForeground(Constants.okColor);
                    ((JLabel) weightContext.getUiComponent("loginStatus")).setText("已登录【" + realName + "】");
                } else {
                    weightContext.getUiComponent("loginBtn").setEnabled(true);
                    ((JButton) weightContext.getUiComponent("loginBtn")).setText("登录");
                    JOptionPane.showMessageDialog(null, result.get("message").toString());
                }
            } else {
                // done 处理退出逻辑，清空登录信息，并且开启可输入状态，退出btn变为登录btn，登录状态变为未登录
                isLogin = false;
                realName = "";
                ((JTextField) weightContext.getUiComponent("userName")).setText("");
                ((JPasswordField) weightContext.getUiComponent("password")).setText("");
                weightContext.getUiComponent("userName").setEnabled(true);
                weightContext.getUiComponent("password").setEnabled(true);
                weightContext.getUiComponent("loginBtn").setEnabled(true);
                ((JButton) weightContext.getUiComponent("loginBtn")).setText("登录");
                weightContext.getUiComponent("loginStatus").setForeground(Color.RED);
                ((JLabel) weightContext.getUiComponent("loginStatus")).setText("未登录");
                weightContext.getUiComponent("userName").requestFocus();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.toString());
            logger.error(BaseUtil.getExceptionStackTrace(e));
            e.printStackTrace();
        }
    }

    public void successSound() {

        InputStream successSoundStream = this.getClass().getClassLoader().getResourceAsStream("success.wav");
        AudioPlayer.player.start(successSoundStream);
    }

    public void failureSound() {

        InputStream failureSoundStream = this.getClass().getClassLoader().getResourceAsStream("failure.wav");
        AudioPlayer.player.start(failureSoundStream);
    }
}
